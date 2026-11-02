public record CurrentAccount(String id, long balanceCents, long dailyLimitCents) implements Holding {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitCurrent(this);
    }
}
