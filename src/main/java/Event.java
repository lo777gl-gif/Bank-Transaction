// Immutable message that travels from a Subject to its Observers.
public interface Event {
    EventType type();
}
