import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class DepositTest {
    private final Client client = new Client("C9", "Test", List.of(new Deposit("DEP-1", 400_000, 600, 12)));

    @Test
    void depositCountsInNetPositionAndInterest() {
        assertEquals(400_000L, client.accept(new NetPositionVisitor()));
        assertEquals(-24_000L, client.accept(new InterestVisitor()));   // 4000.00 x 6 % = 240.00 paid
    }

    @Test
    void depositIsExportedAsJson() {
        assertEquals("{\"type\":\"client\",\"id\":\"C9\",\"name\":\"Test\",\"holdings\":["
                + "{\"type\":\"deposit\",\"id\":\"DEP-1\",\"balance\":4000.00,\"ratePercent\":6.00,\"termMonths\":12}]}",
                client.accept(new StatementExporter()));
    }
}
