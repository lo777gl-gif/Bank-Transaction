import java.util.HashMap;
import java.util.Map;

/**
 * Accumulating visitor used once at start-up: remembers which branch and client every current account
 * belongs to, plus its limit and opening balance. The composition root hands these maps to the listeners.
 */
public class AccountIndexer implements Visitor<Void> {
    private final Map<String, String> branchOfAccount = new HashMap<>();
    private final Map<String, Client> clientOfAccount = new HashMap<>();
    private final Map<String, Long> dailyLimits = new HashMap<>();
    private final Map<String, Long> openingBalances = new HashMap<>();
    private String currentBranchId;
    private Client currentClient;

    public Map<String, String> branchOfAccount() {
        return Map.copyOf(branchOfAccount);
    }

    public Map<String, Client> clientOfAccount() {
        return Map.copyOf(clientOfAccount);
    }

    public Map<String, Long> dailyLimits() {
        return Map.copyOf(dailyLimits);
    }

    public Map<String, Long> openingBalances() {
        return Map.copyOf(openingBalances);
    }

    @Override
    public Void visitBank(Bank bank) {
        bank.branches().forEach(b -> b.accept(this));
        return null;
    }

    @Override
    public Void visitBranch(Branch branch) {
        currentBranchId = branch.id();
        branch.clients().forEach(c -> c.accept(this));
        return null;
    }

    @Override
    public Void visitClient(Client client) {
        currentClient = client;
        client.holdings().forEach(h -> h.accept(this));
        return null;
    }

    @Override
    public Void visitCurrent(CurrentAccount account) {
        branchOfAccount.put(account.id(), currentBranchId);
        clientOfAccount.put(account.id(), currentClient);
        dailyLimits.put(account.id(), account.dailyLimitCents());
        openingBalances.put(account.id(), account.balanceCents());
        return null;
    }

    @Override
    public Void visitSavings(SavingsAccount account) {
        return null;
    }

    @Override
    public Void visitCard(Card card) {
        return null;
    }

    @Override
    public Void visitLoan(Loan loan) {
        return null;
    }
}
