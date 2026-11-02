// Aggregated data republished by BranchAggregator: how many transactions a branch saw and their net effect
public record SummaryEvent(String branchId, long txCount, long netCents) implements Event {
    @Override
    public EventType type() {
        return EventType.BRANCH_SUMMARY;
    }
}
