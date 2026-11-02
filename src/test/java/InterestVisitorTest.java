import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class InterestVisitorTest {
    @Test
    void netInterestMarginOfTheSampleBank() {
        // loan: 10000.00 x 12 % = 1200.00 earned; savings: 5000.00 x 5 % = 250.00 and 1000.00 x 4 % = 40.00 paid
        // 1200.00 - 250.00 - 40.00 = 910.00
        assertEquals(91_000L, SampleBank.create().accept(new InterestVisitor()));
    }
}
