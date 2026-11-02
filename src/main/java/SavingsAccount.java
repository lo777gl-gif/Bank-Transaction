public record SavingsAccount(String id, long balanceCents, int rateBp) implements Holding {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitSavings(this);
    }
}
