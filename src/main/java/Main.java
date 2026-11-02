import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 4;
        LongAdder notifications = new LongAdder();
        BankSystem system = new BankSystem(SampleBank.create(), 100_000, line -> notifications.increment());

        AtomicLong ids = new AtomicLong();
        List<TransactionProducer> producers = List.of(
                new TransactionProducer(system.atm(), List.of("ACC-1", "ACC-2"), 500, 1_000, ids),
                new TransactionProducer(system.online(), List.of("ACC-3"), 500, 2_000, ids),
                new TransactionProducer(system.online(), List.of("ACC-1"), 10, 150_000, ids));   // large -> fraud
        new Simulation(threads, 30).run(producers);

        System.out.println("threads: " + threads);
        System.out.println("transactions in audit log: " + system.audit().size());
        System.out.println("balance notifications sent: " + notifications.sum());
        System.out.println("alerts: " + system.alertLog().size() + ", client reviews: " + system.reviewLog().size());
        System.out.println("dashboard B1: " + system.dashboard().latest("B1"));
        System.out.println("dashboard B2: " + system.dashboard().latest("B2"));
        for (String account : List.of("ACC-1", "ACC-2", "ACC-3")) {
            System.out.println(account + " live balance: " + Money.format(system.balances().balanceOf(account)));
        }

        Bank bank = system.bank();
        FeeVisitor fees = new FeeVisitor(new FeeSchedule(300, 0, 150, 200));
        bank.accept(fees);
        System.out.println("net position of the bank: " + Money.format(bank.accept(new NetPositionVisitor())));
        System.out.println("monthly fees: " + Money.format(fees.totalCents()) + " " + fees.byProduct());
        System.out.println("holdings >= 2000.00: " + bank.accept(new LargeHoldingsFilter(200_000)));
        System.out.println(bank.accept(new StatementExporter()));
    }
}
