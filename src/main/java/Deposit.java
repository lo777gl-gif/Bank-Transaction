public record Deposit(String id, long balanceCents, int rateBp, int termMonths) implements Holding {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitDeposit(this);
    }
}
