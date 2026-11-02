import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class SystemTest {
    private static final long NEVER_FRAUD = Long.MAX_VALUE;

    private static TransactionEvent tx(String account, EventType type, long amount) {
        return new TransactionEvent(1, account, type, amount, "test");
    }

    // ---------- integration: Observer event -> visitor on one subtree -> new event ----------

    @Test
    void fraudAlertTriggersReviewOfThatClientOnly() {
        BankSystem system = new BankSystem(SampleBank.create(), 100_000, line -> {});

        system.atm().publish(tx("ACC-3", EventType.DEPOSIT, 120_000));   // deposit: no limit check, only fraud

        assertEquals(1, system.reviewLog().size());
        ReviewEvent review = system.reviewLog().snapshot().get(0);
        assertEquals("C3", review.clientId());
        assertEquals(300_000, review.netPositionCents());                // 2000.00 + 1000.00, not the whole bank
        assertTrue(review.statementJson().contains("\"id\":\"C3\""));
        assertFalse(review.statementJson().contains("\"id\":\"C1\""));
        assertFalse(review.statementJson().contains("\"id\":\"C2\""));
        assertTrue(review.trigger().startsWith("FRAUD_ALERT"));
    }

    @Test
    void limitBreachTriggersReviewOnlyOnce() {
        BankSystem system = new BankSystem(SampleBank.create(), NEVER_FRAUD, line -> {});

        system.online().publish(tx("ACC-1", EventType.WITHDRAWAL, 60_000));   // limit is 500.00
        system.online().publish(tx("ACC-1", EventType.WITHDRAWAL, 10_000));

        assertEquals(1, system.reviewLog().size());
        ReviewEvent review = system.reviewLog().snapshot().get(0);
        assertEquals("C1", review.clientId());
        assertEquals(580_000, review.netPositionCents());
        assertTrue(review.trigger().startsWith("LIMIT_BREACH"));
    }

    @Test
    void sourcesAreIndependentSubjectsWithTheSameListeners() {
        BankSystem system = new BankSystem(SampleBank.create(), NEVER_FRAUD, line -> {});
        system.atm().publish(tx("ACC-1", EventType.DEPOSIT, 100));
        system.online().publish(tx("ACC-1", EventType.DEPOSIT, 100));
        assertEquals(2, system.audit().size());
        assertEquals(new SummaryEvent("B1", 2, 200), system.dashboard().latest("B1"));
    }

    // ---------- concurrency ----------

    @Test
    void stressFourThreadsThousandEventsEach() throws InterruptedException {
        List<String> notifications = java.util.Collections.synchronizedList(new ArrayList<>());
        BankSystem system = new BankSystem(SampleBank.create(), NEVER_FRAUD, notifications::add);
        AtomicLong ids = new AtomicLong();
        List<TransactionProducer> producers = new ArrayList<>();
        for (int p = 0; p < 4; p++) {
            EventChannel<TransactionEvent> source = p % 2 == 0 ? system.atm() : system.online();
            producers.add(new TransactionProducer(source, List.of("ACC-1"), 1_000, 100, ids));
        }

        new Simulation(4, 30).run(producers);

        // Per producer: 334 deposits (+100) and 333 withdrawals + 333 payments (-100): net -33200. Four producers: -132800.
        assertEquals(4_000, system.audit().size());
        assertEquals(4_000, system.audit().snapshot().stream().map(TransactionEvent::id).distinct().count());
        assertEquals(4_000, notifications.size());
        assertEquals(100_000 - 132_800, system.balances().balanceOf("ACC-1"));
        assertEquals(new SummaryEvent("B1", 4_000, -132_800), system.dashboard().latest("B1"));
        // Outgoing money 266400 passes the 500.00 limit: exactly one alert and one review despite 4 racing threads.
        assertEquals(1, system.alertLog().size());
        assertEquals(1, system.reviewLog().size());
    }

    @Test
    void observersMayJoinAndLeaveWhilePublishersAreRunning() throws InterruptedException {
        EventChannel<TransactionEvent> channel = new EventChannel<>("load");
        AtomicInteger stable = new AtomicInteger();
        AtomicInteger transientCalls = new AtomicInteger();
        channel.subscribe(EventType.DEPOSIT, e -> stable.incrementAndGet());
        Observer<TransactionEvent> visitor = e -> transientCalls.incrementAndGet();

        AtomicLong ids = new AtomicLong();
        List<Runnable> tasks = new ArrayList<>();
        for (int p = 0; p < 3; p++) {
            tasks.add(new TransactionProducer(channel, List.of("A"), 3_000, 100, ids));   // 1000 deposits each
        }
        tasks.add(() -> {
            for (int i = 0; i < 5_000; i++) {
                channel.subscribe(EventType.DEPOSIT, visitor);
                channel.unsubscribe(EventType.DEPOSIT, visitor);
            }
        });

        new Simulation(4, 30).run(tasks);   // would throw if any thread failed

        assertEquals(3_000, stable.get());   // the permanent listener never missed an event
        assertTrue(transientCalls.get() <= 3_000);
    }

    @Test
    void failingProducerIsReportedNotSwallowed() {
        Runnable broken = () -> { throw new IllegalStateException("boom"); };
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new Simulation(2, 5).run(List.of(broken)));
        assertEquals("boom", e.getCause().getMessage());
    }

    @Test
    void producersThatNeverFinishAreStoppedAfterTheTimeout() {
        Runnable sleeper = () -> {
            try {
                Thread.sleep(60_000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        };
        long start = System.nanoTime();
        assertThrows(IllegalStateException.class, () -> new Simulation(1, 1).run(List.of(sleeper)));
        assertTrue((System.nanoTime() - start) / 1_000_000_000 < 10);   // shutdownNow interrupted the thread
    }

    @Test
    void simulationNeedsAtLeastOneThread() {
        assertThrows(IllegalArgumentException.class, () -> new Simulation(0, 1));
    }
}
