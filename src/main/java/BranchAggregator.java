import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener 4, and also a Subject: it observes transactions and republishes per-branch summaries,
 * which makes the chain  source -> BranchAggregator -> Dashboard.
 */
public class BranchAggregator implements Observer<TransactionEvent>, Subject<SummaryEvent> {
    private record Totals(long count, long netCents) {}

    private final Map<String, String> branchOfAccount;
    private final EventChannel<SummaryEvent> summaries;
    private final Map<String, Totals> totals = new ConcurrentHashMap<>();

    public BranchAggregator(Map<String, String> branchOfAccount, EventChannel<SummaryEvent> summaries) {
        this.branchOfAccount = Map.copyOf(branchOfAccount);
        this.summaries = summaries;
    }

    @Override
    public void onEvent(TransactionEvent tx) {
        String branch = branchOfAccount.get(tx.accountId());
        if (branch == null) {
            return;
        }
        Totals now = totals.merge(branch, new Totals(1, tx.signedAmountCents()),
                (old, add) -> new Totals(old.count() + add.count(), old.netCents() + add.netCents()));
        summaries.publish(new SummaryEvent(branch, now.count(), now.netCents()));
    }

    @Override
    public void subscribe(EventType type, Observer<? super SummaryEvent> observer) {
        summaries.subscribe(type, observer);
    }

    @Override
    public void unsubscribe(EventType type, Observer<? super SummaryEvent> observer) {
        summaries.unsubscribe(type, observer);
    }
}
