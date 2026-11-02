import java.util.Objects;

public record AlertEvent(EventType type, String accountId, long transactionId, String reason) implements Event {

    public AlertEvent {
        if (type != EventType.FRAUD_ALERT && type != EventType.LIMIT_BREACH) {
            throw new IllegalArgumentException("not an alert type: " + type);
        }
        Objects.requireNonNull(accountId);
        Objects.requireNonNull(reason);
    }
}
