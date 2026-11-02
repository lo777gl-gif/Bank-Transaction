/** Listener 1: a transaction at or above the threshold raises a FRAUD_ALERT. Stateless, so thread-safe. */
public class FraudDetector implements Observer<TransactionEvent> {
    private final long thresholdCents;
    private final EventChannel<AlertEvent> alerts;

    public FraudDetector(long thresholdCents, EventChannel<AlertEvent> alerts) {
        this.thresholdCents = thresholdCents;
        this.alerts = alerts;
    }

    @Override
    public void onEvent(TransactionEvent tx) {
        if (tx.amountCents() >= thresholdCents) {
            alerts.publish(new AlertEvent(EventType.FRAUD_ALERT, tx.accountId(), tx.id(),
                    "amount " + Money.format(tx.amountCents()) + " >= threshold " + Money.format(thresholdCents)));
        }
    }
}
