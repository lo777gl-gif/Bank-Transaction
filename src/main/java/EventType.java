// Observers subscribe per event type
public enum EventType {
    DEPOSIT, WITHDRAWAL, PAYMENT,          // transaction stream
    FRAUD_ALERT, LIMIT_BREACH,             // alerts
    BRANCH_SUMMARY,                        // aggregated data (chain of observers)
    CLIENT_REVIEW                          // result of the visitor run (integration)
}
