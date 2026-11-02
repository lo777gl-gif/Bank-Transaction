import java.util.Map;

/**
 * Integration link: an alert (Observer side) triggers visitors (Visitor side) on the affected
 * client's subtree only, and the result goes back into the system as a CLIENT_REVIEW event.
 * Both visitors are stateless, so one instance can serve all threads.
 */
public class ReviewService implements Observer<AlertEvent> {
    private final Map<String, Client> clientOfAccount;
    private final Visitor<Long> netPosition;
    private final Visitor<String> statement;
    private final EventChannel<ReviewEvent> reviews;

    public ReviewService(Map<String, Client> clientOfAccount, Visitor<Long> netPosition,
                         Visitor<String> statement, EventChannel<ReviewEvent> reviews) {
        this.clientOfAccount = Map.copyOf(clientOfAccount);
        this.netPosition = netPosition;
        this.statement = statement;
        this.reviews = reviews;
    }

    @Override
    public void onEvent(AlertEvent alert) {
        Client client = clientOfAccount.get(alert.accountId());
        if (client == null) {
            return;
        }
        reviews.publish(new ReviewEvent(client.id(), alert.type() + ": " + alert.reason(),
                client.accept(netPosition), client.accept(statement)));
    }
}
