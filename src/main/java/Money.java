import java.math.BigDecimal;

/** Money is stored as long cents everywhere; this class only formats it as a decimal. */
public final class Money {
    private Money() {}

    public static String format(long cents) {
        return BigDecimal.valueOf(cents, 2).toPlainString();      // 125075 -> "1250.75"
    }

    public static String percent(int basisPoints) {
        return BigDecimal.valueOf(basisPoints, 2).toPlainString(); // 500 bp -> "5.00"
    }
}
