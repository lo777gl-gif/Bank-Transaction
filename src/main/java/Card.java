public record Card(String id, long creditLimitCents, long debtCents) implements Holding {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitCard(this);
    }
}
