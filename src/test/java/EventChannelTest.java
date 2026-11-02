import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class EventChannelTest {
    private final EventChannel<TransactionEvent> channel = new EventChannel<>("test");

    private static TransactionEvent tx(long id, EventType type) {
        return new TransactionEvent(id, "A", type, 100, "test");
    }

    @Test
    void deliversOnlyToObserversOfThatEventType() {
        List<Long> deposits = new ArrayList<>();
        List<Long> withdrawals = new ArrayList<>();
        channel.subscribe(EventType.DEPOSIT, e -> deposits.add(e.id()));
        channel.subscribe(EventType.WITHDRAWAL, e -> withdrawals.add(e.id()));

        channel.publish(tx(1, EventType.DEPOSIT));
        channel.publish(tx(2, EventType.WITHDRAWAL));
        channel.publish(tx(3, EventType.PAYMENT));

        assertEquals(List.of(1L), deposits);
        assertEquals(List.of(2L), withdrawals);
    }

    @Test
    void unsubscribedObserverGetsNothingMore() {
        AtomicInteger calls = new AtomicInteger();
        Observer<TransactionEvent> observer = e -> calls.incrementAndGet();
        channel.subscribe(EventType.DEPOSIT, observer);
        channel.publish(tx(1, EventType.DEPOSIT));
        channel.unsubscribe(EventType.DEPOSIT, observer);
        channel.publish(tx(2, EventType.DEPOSIT));
        assertEquals(1, calls.get());
    }

    @Test
    void observerCanUnsubscribeItselfInsideItsHandler() {
        class OneShot implements Observer<TransactionEvent> {
            int calls;

            @Override
            public void onEvent(TransactionEvent event) {
                calls++;
                channel.unsubscribe(EventType.DEPOSIT, this);
            }
        }
        OneShot oneShot = new OneShot();
        AtomicInteger other = new AtomicInteger();
        channel.subscribe(EventType.DEPOSIT, oneShot);
        channel.subscribe(EventType.DEPOSIT, e -> other.incrementAndGet());

        for (int i = 0; i < 5; i++) {
            channel.publish(tx(i, EventType.DEPOSIT));
        }

        assertEquals(1, oneShot.calls);
        assertEquals(5, other.get());   // the other observer was not skipped by the removal
    }

    @Test
    void observerSubscribedInsideHandlerSeesOnlyLaterEvents() {
        List<Long> late = new ArrayList<>();
        Observer<TransactionEvent> newcomer = e -> late.add(e.id());
        channel.subscribe(EventType.DEPOSIT, e -> channel.subscribe(EventType.DEPOSIT, newcomer));

        channel.publish(tx(1, EventType.DEPOSIT));
        channel.publish(tx(2, EventType.DEPOSIT));

        assertEquals(List.of(2L), late);
    }

    @Test
    void sameObserverSubscribedTwiceIsCalledOnce() {
        AtomicInteger calls = new AtomicInteger();
        Observer<TransactionEvent> observer = e -> calls.incrementAndGet();
        channel.subscribe(EventType.DEPOSIT, observer);
        channel.subscribe(EventType.DEPOSIT, observer);
        channel.publish(tx(1, EventType.DEPOSIT));
        assertEquals(1, calls.get());
    }

    @Test
    void eventsRejectInvalidData() {
        assertThrows(IllegalArgumentException.class,
                () -> new TransactionEvent(1, "A", EventType.FRAUD_ALERT, 100, "x"));
        assertThrows(IllegalArgumentException.class,
                () -> new TransactionEvent(1, "A", EventType.DEPOSIT, 0, "x"));
        assertThrows(IllegalArgumentException.class,
                () -> new AlertEvent(EventType.DEPOSIT, "A", 1, "x"));
    }

    @Test
    void moneyIsFormattedAsDecimal() {
        assertEquals("1250.75", Money.format(125_075));
        assertEquals("0.05", Money.format(5));
        assertEquals("-5.50", Money.format(-550));
        assertEquals("5.00", Money.percent(500));
    }
}
