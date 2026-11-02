import java.util.List;
import java.util.function.Consumer;

/**
 * The single composition root: the only place that creates listeners and channels and connects them.
 * Everything else receives its dependencies through constructors.
 *
 *   atm ----\                 /-> FraudDetector ----\
 *            +-> (tx stream) +--> LimitMonitor ------+-> alerts -> ReviewService -> reviews -> reviewLog
 *   online -/                +--> BalanceNotifier   (alertLog also listens to alerts)
 *                            +--> audit (EventLog)
 *                            \-> BranchAggregator -> Dashboard
 */
public class BankSystem {
    private static final List<EventType> TX_TYPES = List.of(EventType.DEPOSIT, EventType.WITHDRAWAL, EventType.PAYMENT);

    private final Bank bank;
    private final EventChannel<TransactionEvent> atm = new EventChannel<>("ATM");
    private final EventChannel<TransactionEvent> online = new EventChannel<>("Online");
    private final EventChannel<AlertEvent> alerts = new EventChannel<>("alerts");
    private final EventChannel<ReviewEvent> reviews = new EventChannel<>("reviews");
    private final EventLog<TransactionEvent> audit = new EventLog<>();
    private final EventLog<AlertEvent> alertLog = new EventLog<>();
    private final EventLog<ReviewEvent> reviewLog = new EventLog<>();
    private final Dashboard dashboard = new Dashboard();
    private final BalanceNotifier balances;

    public BankSystem(Bank bank, long fraudThresholdCents, Consumer<String> notifications) {
        this.bank = bank;
        AccountIndexer index = new AccountIndexer();
        bank.accept(index);

        FraudDetector fraud = new FraudDetector(fraudThresholdCents, alerts);
        LimitMonitor limits = new LimitMonitor(index.dailyLimits(), alerts);
        BranchAggregator aggregator = new BranchAggregator(index.branchOfAccount(), new EventChannel<>("summaries"));
        ReviewService review = new ReviewService(index.clientOfAccount(), new NetPositionVisitor(),
                new StatementExporter(), reviews);
        this.balances = new BalanceNotifier(index.openingBalances(), notifications);

        aggregator.subscribe(EventType.BRANCH_SUMMARY, dashboard);
        alerts.subscribe(EventType.FRAUD_ALERT, review);
        alerts.subscribe(EventType.LIMIT_BREACH, review);
        alerts.subscribe(EventType.FRAUD_ALERT, alertLog);
        alerts.subscribe(EventType.LIMIT_BREACH, alertLog);
        reviews.subscribe(EventType.CLIENT_REVIEW, reviewLog);

        for (EventChannel<TransactionEvent> source : List.of(atm, online)) {
            for (EventType type : TX_TYPES) {
                source.subscribe(type, fraud);
                source.subscribe(type, balances);
                source.subscribe(type, audit);
                source.subscribe(type, aggregator);
            }
            source.subscribe(EventType.WITHDRAWAL, limits);
            source.subscribe(EventType.PAYMENT, limits);
        }
    }

    public Bank bank() {
        return bank;
    }

    public EventChannel<TransactionEvent> atm() {
        return atm;
    }

    public EventChannel<TransactionEvent> online() {
        return online;
    }

    public EventLog<TransactionEvent> audit() {
        return audit;
    }

    public EventLog<AlertEvent> alertLog() {
        return alertLog;
    }

    public EventLog<ReviewEvent> reviewLog() {
        return reviewLog;
    }

    public Dashboard dashboard() {
        return dashboard;
    }

    public BalanceNotifier balances() {
        return balances;
    }
}
