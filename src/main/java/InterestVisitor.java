/**
 * Numeric visitor: the bank's yearly net interest margin in cents = interest the bank earns on loans
 * minus interest it pays on savings. Integer division truncates (rounds down to the cent). Stateless.
 */
public class InterestVisitor implements Visitor<Long> {
    private static final long BASIS_POINTS = 10_000;

    @Override
    public Long visitBank(Bank bank) {
        return bank.branches().stream().mapToLong(b -> b.accept(this)).sum();
    }

    @Override
    public Long visitBranch(Branch branch) {
        return branch.clients().stream().mapToLong(c -> c.accept(this)).sum();
    }

    @Override
    public Long visitClient(Client client) {
        return client.holdings().stream().mapToLong(h -> h.accept(this)).sum();
    }

    @Override
    public Long visitCurrent(CurrentAccount account) {
        return 0L;
    }

    @Override
    public Long visitSavings(SavingsAccount account) {
        return -(account.balanceCents() * account.rateBp() / BASIS_POINTS);
    }

    @Override
    public Long visitCard(Card card) {
        return 0L;
    }

    @Override
    public Long visitLoan(Loan loan) {
        return loan.principalCents() * loan.rateBp() / BASIS_POINTS;
    }

    @Override
    public Long visitDeposit(Deposit deposit) {
        return -(deposit.balanceCents() * deposit.rateBp() / BASIS_POINTS);
    }
}
