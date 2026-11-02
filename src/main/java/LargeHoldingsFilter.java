import java.util.List;

/**
 * Filter visitor: returns the holdings whose size (balance, debt or principal) is at least minCents,
 * in tree order. Stateless.
 */
public class LargeHoldingsFilter implements Visitor<List<Holding>> {
    private final long minCents;

    public LargeHoldingsFilter(long minCents) {
        this.minCents = minCents;
    }

    @Override
    public List<Holding> visitBank(Bank bank) {
        return bank.branches().stream().flatMap(b -> b.accept(this).stream()).toList();
    }

    @Override
    public List<Holding> visitBranch(Branch branch) {
        return branch.clients().stream().flatMap(c -> c.accept(this).stream()).toList();
    }

    @Override
    public List<Holding> visitClient(Client client) {
        return client.holdings().stream().flatMap(h -> h.accept(this).stream()).toList();
    }

    @Override
    public List<Holding> visitCurrent(CurrentAccount account) {
        return account.balanceCents() >= minCents ? List.of(account) : List.of();
    }

    @Override
    public List<Holding> visitSavings(SavingsAccount account) {
        return account.balanceCents() >= minCents ? List.of(account) : List.of();
    }

    @Override
    public List<Holding> visitCard(Card card) {
        return card.debtCents() >= minCents ? List.of(card) : List.of();
    }

    @Override
    public List<Holding> visitLoan(Loan loan) {
        return loan.principalCents() >= minCents ? List.of(loan) : List.of();
    }

    @Override
    public List<Holding> visitDeposit(Deposit deposit) {
        return deposit.balanceCents() >= minCents ? List.of(deposit) : List.of();
    }
}
