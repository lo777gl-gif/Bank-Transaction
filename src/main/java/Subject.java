public interface Subject<E extends Event> {
    void subscribe(EventType type, Observer<? super E> observer);

    void unsubscribe(EventType type, Observer<? super E> observer);
}
