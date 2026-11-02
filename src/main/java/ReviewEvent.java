// Result of running visitors over one client's subtree after an alert.
public record ReviewEvent(String clientId, String trigger, long netPositionCents, String statementJson) implements Event {
    @Override
    public EventType type() {
        return EventType.CLIENT_REVIEW;
    }
}
