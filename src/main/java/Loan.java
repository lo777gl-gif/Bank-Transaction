public record Loan(String id, long principalCents, int rateBp, int termMonths) implements Holding {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitLoan(this);
    }
}
