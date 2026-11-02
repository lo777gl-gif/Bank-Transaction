import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Deterministic data source thread: event i goes to account i % n and has kind DEPOSIT, WITHDRAWAL, PAYMENT
 * in turn, always with the same amount. That makes the expected totals of any run easy to calculate by hand.
 */
public class TransactionProducer implements Runnable {
    private static final EventType[] CYCLE = {EventType.DEPOSIT, EventType.WITHDRAWAL, EventType.PAYMENT};

    private final EventChannel<TransactionEvent> target;
    private final List<String> accountIds;
    private final int count;
    private final long amountCents;
    private final AtomicLong idGenerator;

    public TransactionProducer(EventChannel<TransactionEvent> target, List<String> accountIds, int count,
                               long amountCents, AtomicLong idGenerator) {
        this.target = target;
        this.accountIds = List.copyOf(accountIds);
        this.count = count;
        this.amountCents = amountCents;
        this.idGenerator = idGenerator;
    }

    @Override
    public void run() {
        for (int i = 0; i < count; i++) {
            target.publish(new TransactionEvent(idGenerator.incrementAndGet(), accountIds.get(i % accountIds.size()),
                    CYCLE[i % CYCLE.length], amountCents, target.name()));
        }
    }
}
