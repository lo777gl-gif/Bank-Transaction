/** Numeric visitor: what the visited part of the bank owns minus what it owes (cents). Stateless. */
public class NetPositionVisitor implements Visitor<Long> {
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
        return account.balanceCents();
    }

    @Override
    public Long visitSavings(SavingsAccount account) {
        return account.balanceCents();
    }

    @Override
    public Long visitCard(Card card) {
        return -card.debtCents();
    }

    @Override
    public Long visitLoan(Loan loan) {
        return -loan.principalCents();
    }
}
