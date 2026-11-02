import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ListenerTest {
    private static TransactionEvent tx(long id, EventType type, long amount) {
        return new TransactionEvent(id, "A", type, amount, "test");
    }

    @Test
    void fraudDetectorAlertsAtOrAboveThresholdOnly() {
        EventChannel<AlertEvent> alerts = new EventChannel<>("alerts");
        EventLog<AlertEvent> log = new EventLog<>();
        alerts.subscribe(EventType.FRAUD_ALERT, log);
        FraudDetector detector = new FraudDetector(1_000, alerts);

        detector.onEvent(tx(1, EventType.WITHDRAWAL, 999));
        assertEquals(0, log.size());
        detector.onEvent(tx(2, EventType.WITHDRAWAL, 1_000));

        assertEquals(1, log.size());
        assertEquals(2, log.snapshot().get(0).transactionId());
    }

    @Test
    void limitMonitorAlertsOnceWhenOutgoingMoneyPassesTheLimit() {
        EventChannel<AlertEvent> alerts = new EventChannel<>("alerts");
        EventLog<AlertEvent> log = new EventLog<>();
        alerts.subscribe(EventType.LIMIT_BREACH, log);
        LimitMonitor monitor = new LimitMonitor(Map.of("A", 1_000L), alerts);

        monitor.onEvent(tx(1, EventType.DEPOSIT, 5_000));      // deposits do not count
        monitor.onEvent(tx(2, EventType.WITHDRAWAL, 600));
        assertEquals(0, log.size());
        monitor.onEvent(tx(3, EventType.PAYMENT, 500));         // 1100 > 1000
        monitor.onEvent(tx(4, EventType.WITHDRAWAL, 100));      // already reported

        assertEquals(1, log.size());
        assertEquals(3, log.snapshot().get(0).transactionId());
    }

    @Test
    void balanceNotifierTracksBalanceAndNotifies() {
        List<String> messages = new ArrayList<>();
        BalanceNotifier notifier = new BalanceNotifier(Map.of("A", 100_000L), messages::add);

        notifier.onEvent(tx(1, EventType.WITHDRAWAL, 2_575));
        notifier.onEvent(tx(2, EventType.DEPOSIT, 100));

        assertEquals(List.of("A balance: 974.25", "A balance: 975.25"), messages);
        assertEquals(97_525, notifier.balanceOf("A"));
    }

    @Test
    void aggregatorRepublishesSummariesToItsOwnObservers() {
        BranchAggregator aggregator = new BranchAggregator(Map.of("A", "B1"), new EventChannel<>("summaries"));
        Dashboard dashboard = new Dashboard();
        aggregator.subscribe(EventType.BRANCH_SUMMARY, dashboard);

        aggregator.onEvent(tx(1, EventType.DEPOSIT, 1_000));
        aggregator.onEvent(tx(2, EventType.WITHDRAWAL, 300));
        aggregator.onEvent(new TransactionEvent(3, "UNKNOWN", EventType.DEPOSIT, 50, "test"));   // no branch: ignored

        assertEquals(new SummaryEvent("B1", 2, 700), dashboard.latest("B1"));
    }

    @Test
    void dashboardKeepsTheNewestSummaryWhenDeliveredOutOfOrder() {
        Dashboard dashboard = new Dashboard();
        dashboard.onEvent(new SummaryEvent("B1", 5, 500));
        dashboard.onEvent(new SummaryEvent("B1", 4, 400));      // late arrival
        assertEquals(5, dashboard.latest("B1").txCount());
    }
}
