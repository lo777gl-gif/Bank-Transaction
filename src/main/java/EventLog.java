import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Keeps every event it receives. Used as the audit log, the alert log and the review log. */
public class EventLog<E extends Event> implements Observer<E> {
    private final Queue<E> entries = new ConcurrentLinkedQueue<>();

    @Override
    public void onEvent(E event) {
        entries.add(event);
    }

    public int size() {
        return entries.size();
    }

    public List<E> snapshot() {
        return List.copyOf(entries);
    }
}
