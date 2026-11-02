import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The Subject implementation. The registry is a ConcurrentHashMap of CopyOnWriteArrayLists:
 * publish() iterates a snapshot, so observers may join or leave at any time (even from inside
 * their own handler, or from another thread) without a lock around the whole subject.
 * Delivery is synchronous: the publishing thread runs the handlers.
 */
public class EventChannel<E extends Event> implements Subject<E> {
    private final String name;
    private final Map<EventType, CopyOnWriteArrayList<Observer<? super E>>> registry = new ConcurrentHashMap<>();

    public EventChannel(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public void subscribe(EventType type, Observer<? super E> observer) {
        registry.computeIfAbsent(type, t -> new CopyOnWriteArrayList<>()).addIfAbsent(observer);
    }

    @Override
    public void unsubscribe(EventType type, Observer<? super E> observer) {
        List<Observer<? super E>> observers = registry.get(type);
        if (observers != null) {
            observers.remove(observer);
        }
    }

    public void publish(E event) {
        List<Observer<? super E>> observers = registry.get(event.type());
        if (observers == null) {
            return;
        }
        for (Observer<? super E> observer : observers) {   // snapshot iteration
            observer.onEvent(event);
        }
    }
}
