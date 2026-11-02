import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** All expected values are calculated by hand from SampleBank. */
class VisitorTest {
    private final Bank bank = SampleBank.create();
    private final Client nurlan = bank.branches().get(0).clients().get(1);

    @Test
    void netPositionOfTheWholeBank() {
        // C1: 1000.00 + 5000.00 - 200.00 = 5800.00   C2: 250.00 - 10000.00 = -9750.00   C3: 2000.00 + 1000.00 = 3000.00
        assertEquals(-95_000L, bank.accept(new NetPositionVisitor()));
    }

    @Test
    void netPositionOfOneClientVisitsOnlyThatSubtree() {
        assertEquals(-975_000L, nurlan.accept(new NetPositionVisitor()));
    }

    @Test
    void feeVisitorAccumulatesFeesOverTheWholeTree() {
        FeeVisitor fees = new FeeVisitor(new FeeSchedule(300, 0, 150, 200));
        bank.accept(fees);
        // 3 current x 3.00, 2 savings x 0, 1 card x 1.50, 1 loan x 2.00
        assertEquals(1_250, fees.totalCents());
        assertEquals(Map.of("current", 900L, "savings", 0L, "card", 150L, "loan", 200L), fees.byProduct());
    }

    @Test
    void largeHoldingsFilterReturnsMatchingLeavesInTreeOrder() {
        List<Holding> found = bank.accept(new LargeHoldingsFilter(200_000));
        assertEquals(List.of("SAV-1", "LOAN-1", "ACC-3"), found.stream().map(Holding::id).toList());
    }

    @Test
    void statementExporterBuildsCorrectlyNestedJson() {
        Client client = new Client("C9", "Ali \"Al\" Baur", List.of(
                new CurrentAccount("ACC-9", 125_075, 50_000),
                new Loan("LOAN-9", 1_000_000, 1250, 12)));
        String expected = "{\"type\":\"client\",\"id\":\"C9\",\"name\":\"Ali \\\"Al\\\" Baur\",\"holdings\":["
                + "{\"type\":\"current\",\"id\":\"ACC-9\",\"balance\":1250.75,\"dailyLimit\":500.00},"
                + "{\"type\":\"loan\",\"id\":\"LOAN-9\",\"principal\":10000.00,\"ratePercent\":12.50,\"termMonths\":12}]}";
        assertEquals(expected, client.accept(new StatementExporter()));
    }

    @Test
    void wholeBankExportHasBalancedBracketsAndAllThreeLevels() {
        String json = bank.accept(new StatementExporter());
        assertEquals(count(json, '{'), count(json, '}'));
        assertEquals(count(json, '['), count(json, ']'));
        assertTrue(json.startsWith("{\"type\":\"bank\""));
        assertEquals(3, countText(json, "\"type\":\"client\""));
        assertEquals(2, countText(json, "\"type\":\"branch\""));
    }

    @Test
    void accountIndexerMapsAccountsToBranchClientLimitAndBalance() {
        AccountIndexer index = new AccountIndexer();
        bank.accept(index);
        assertEquals(Map.of("ACC-1", "B1", "ACC-2", "B1", "ACC-3", "B2"), index.branchOfAccount());
        assertEquals("C2", index.clientOfAccount().get("ACC-2").id());
        assertEquals(150_000L, index.dailyLimits().get("ACC-3"));
        assertEquals(100_000L, index.openingBalances().get("ACC-1"));
    }

    @Test
    void doubleDispatchPicksTheVisitMethodOfTheRuntimeType() {
        Visitor<String> names = new Visitor<>() {
            public String visitBank(Bank b) { return "bank"; }
            public String visitBranch(Branch b) { return "branch"; }
            public String visitClient(Client c) { return "client"; }
            public String visitCurrent(CurrentAccount a) { return "current"; }
            public String visitSavings(SavingsAccount a) { return "savings"; }
            public String visitCard(Card c) { return "card"; }
            public String visitLoan(Loan l) { return "loan"; }
        };
        // The static type is Holding / Element; the element itself calls back the matching method.
        List<Holding> holdings = nurlan.holdings();
        assertEquals("current", holdings.get(0).accept(names));
        assertEquals("loan", holdings.get(1).accept(names));
        Element element = nurlan;
        assertEquals("client", element.accept(names));
    }

    private static long count(String text, char c) {
        return text.chars().filter(x -> x == c).count();
    }

    private static long countText(String text, String part) {
        return text.split(java.util.regex.Pattern.quote(part), -1).length - 1;
    }
}
