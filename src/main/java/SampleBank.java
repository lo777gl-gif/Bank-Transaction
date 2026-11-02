import java.util.List;

/** Sample data. Amounts are in cents, rates in basis points (500 = 5 %). */
public final class SampleBank {
    private SampleBank() {}

    public static Bank create() {
        Client aigerim = new Client("C1", "Aigerim", List.of(
                new CurrentAccount("ACC-1", 100_000, 50_000),      // 1000.00, daily limit 500.00
                new SavingsAccount("SAV-1", 500_000, 500),         // 5000.00 at 5 %
                new Card("CARD-1", 300_000, 20_000)));             // limit 3000.00, debt 200.00
        Client nurlan = new Client("C2", "Nurlan", List.of(
                new CurrentAccount("ACC-2", 25_000, 100_000),      // 250.00, daily limit 1000.00
                new Loan("LOAN-1", 1_000_000, 1200, 24)));         // 10000.00 at 12 %, 24 months
        Client dana = new Client("C3", "Dana", List.of(
                new CurrentAccount("ACC-3", 200_000, 150_000),     // 2000.00, daily limit 1500.00
                new SavingsAccount("SAV-3", 100_000, 400)));       // 1000.00 at 4 %
        return new Bank("Astana Bank", List.of(
                new Branch("B1", "Astana", List.of(aigerim, nurlan)),
                new Branch("B2", "Almaty", List.of(dana))));
    }
}
