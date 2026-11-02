/** Monthly fee per product kind, in cents. */
public record FeeSchedule(long currentCents, long savingsCents, long cardCents, long loanCents) {}
