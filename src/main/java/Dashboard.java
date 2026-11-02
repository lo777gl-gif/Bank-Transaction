import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Listener 5: second link of the chain. Keeps the newest summary per branch. */
public class Dashboard implements Observer<SummaryEvent> {
    private final Map<String, SummaryEvent> latest = new ConcurrentHashMap<>();

    @Override
    public void onEvent(SummaryEvent summary) {
        // Threads may deliver summaries out of order; the one with the larger count is the newer one.
        latest.merge(summary.branchId(), summary, (old, added) -> added.txCount() > old.txCount() ? added : old);
    }

    public SummaryEvent latest(String branchId) {
        return latest.get(branchId);
    }
}
