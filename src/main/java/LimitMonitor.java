import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Listener 2: sums outgoing money per account and raises LIMIT_BREACH exactly once when the daily limit is passed. */
public class LimitMonitor implements Observer<TransactionEvent> {
    private final Map<String, Long> dailyLimits;
    private final EventChannel<AlertEvent> alerts;
    private final Map<String, Long> spent = new ConcurrentHashMap<>();
    private final Set<String> breached = ConcurrentHashMap.newKeySet();

    public LimitMonitor(Map<String, Long> dailyLimits, EventChannel<AlertEvent> alerts) {
        this.dailyLimits = Map.copyOf(dailyLimits);
        this.alerts = alerts;
    }

    @Override
    public void onEvent(TransactionEvent tx) {
        Long limit = dailyLimits.get(tx.accountId());
        if (tx.type() == EventType.DEPOSIT || limit == null) {
            return;
        }
        long total = spent.merge(tx.accountId(), tx.amountCents(), Long::sum);
        if (total > limit && breached.add(tx.accountId())) {   // add() is atomic: only one thread wins
            alerts.publish(new AlertEvent(EventType.LIMIT_BREACH, tx.accountId(), tx.id(),
                    "spent " + Money.format(total) + " > daily limit " + Money.format(limit)));
        }
    }
}
