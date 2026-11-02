import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** Listener 3: keeps live balances and notifies the client after every transaction. The sink must be thread-safe. */
public class BalanceNotifier implements Observer<TransactionEvent> {
    private final Map<String, Long> balances;
    private final Consumer<String> sink;

    public BalanceNotifier(Map<String, Long> openingBalances, Consumer<String> sink) {
        this.balances = new ConcurrentHashMap<>(openingBalances);
        this.sink = sink;
    }

    @Override
    public void onEvent(TransactionEvent tx) {
        long balance = balances.merge(tx.accountId(), tx.signedAmountCents(), Long::sum);
        sink.accept(tx.accountId() + " balance: " + Money.format(balance));
    }

    public long balanceOf(String accountId) {
        return balances.getOrDefault(accountId, 0L);
    }
}
