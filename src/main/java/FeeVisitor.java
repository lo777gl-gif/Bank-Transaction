import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/** Accumulating visitor: walks the whole tree and collects the monthly fees. Use one instance per run. */
public class FeeVisitor implements Visitor<Void> {
    private final FeeSchedule fees;
    private final Map<String, Long> byProduct = new TreeMap<>();
    private long totalCents;

    public FeeVisitor(FeeSchedule fees) {
        this.fees = fees;
    }

    public long totalCents() {
        return totalCents;
    }

    public Map<String, Long> byProduct() {
        return Collections.unmodifiableMap(byProduct);
    }

    private void charge(String product, long cents) {
        byProduct.merge(product, cents, Long::sum);
        totalCents += cents;
    }

    @Override
    public Void visitBank(Bank bank) {
        bank.branches().forEach(b -> b.accept(this));
        return null;
    }

    @Override
    public Void visitBranch(Branch branch) {
        branch.clients().forEach(c -> c.accept(this));
        return null;
    }

    @Override
    public Void visitClient(Client client) {
        client.holdings().forEach(h -> h.accept(this));
        return null;
    }

    @Override
    public Void visitCurrent(CurrentAccount account) {
        charge("current", fees.currentCents());
        return null;
    }

    @Override
    public Void visitSavings(SavingsAccount account) {
        charge("savings", fees.savingsCents());
        return null;
    }

    @Override
    public Void visitCard(Card card) {
        charge("card", fees.cardCents());
        return null;
    }

    @Override
    public Void visitLoan(Loan loan) {
        charge("loan", fees.loanCents());
        return null;
    }
}
