
// One method per concrete element type. Each visitor decides how to traverse the containers
// (sum, collect, build a string...), so the tree classes stay data-only.
public interface Visitor<R> {
    R visitBank(Bank bank);

    R visitBranch(Branch branch);

    R visitClient(Client client);

    R visitCurrent(CurrentAccount account);

    R visitSavings(SavingsAccount account);

    R visitCard(Card card);

    R visitLoan(Loan loan);

    R visitDeposit(Deposit deposit);
}
