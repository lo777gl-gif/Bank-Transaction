import java.util.Objects;

public record TransactionEvent(long id, String accountId, EventType type, long amountCents, String source)
        implements Event {

    public TransactionEvent {
        if (type != EventType.DEPOSIT && type != EventType.WITHDRAWAL && type != EventType.PAYMENT) {
            throw new IllegalArgumentException("not a transaction type: " + type);
        }
        if (amountCents <= 0) {
            throw new IllegalArgumentException("amount must be positive: " + amountCents);
        }
        Objects.requireNonNull(accountId);
        Objects.requireNonNull(source);
    }

    // Effect on the account balance: deposits add, withdrawals and payments subtract
    public long signedAmountCents() {
        return type == EventType.DEPOSIT ? amountCents : -amountCents;
    }
}
