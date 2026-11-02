import java.util.List;
import java.util.StringJoiner;

// Hand-written JSON export (compact). Every visit returns the JSON of its own subtree. Stateless.
public class StatementExporter implements Visitor<String> {
    @Override
    public String visitBank(Bank bank) {
        return "{\"type\":\"bank\",\"name\":" + quote(bank.name()) + ",\"branches\":" + array(bank.branches()) + "}";
    }

    @Override
    public String visitBranch(Branch branch) {
        return "{\"type\":\"branch\",\"id\":" + quote(branch.id()) + ",\"city\":" + quote(branch.city())
                + ",\"clients\":" + array(branch.clients()) + "}";
    }

    @Override
    public String visitClient(Client client) {
        return "{\"type\":\"client\",\"id\":" + quote(client.id()) + ",\"name\":" + quote(client.name())
                + ",\"holdings\":" + array(client.holdings()) + "}";
    }

    @Override
    public String visitCurrent(CurrentAccount a) {
        return "{\"type\":\"current\",\"id\":" + quote(a.id()) + ",\"balance\":" + Money.format(a.balanceCents())
                + ",\"dailyLimit\":" + Money.format(a.dailyLimitCents()) + "}";
    }

    @Override
    public String visitSavings(SavingsAccount a) {
        return "{\"type\":\"savings\",\"id\":" + quote(a.id()) + ",\"balance\":" + Money.format(a.balanceCents())
                + ",\"ratePercent\":" + Money.percent(a.rateBp()) + "}";
    }

    @Override
    public String visitCard(Card c) {
        return "{\"type\":\"card\",\"id\":" + quote(c.id()) + ",\"creditLimit\":" + Money.format(c.creditLimitCents())
                + ",\"debt\":" + Money.format(c.debtCents()) + "}";
    }

    @Override
    public String visitLoan(Loan l) {
        return "{\"type\":\"loan\",\"id\":" + quote(l.id()) + ",\"principal\":" + Money.format(l.principalCents())
                + ",\"ratePercent\":" + Money.percent(l.rateBp()) + ",\"termMonths\":" + l.termMonths() + "}";
    }

    @Override
    public String visitDeposit(Deposit d) {
        return "{\"type\":\"deposit\",\"id\":" + quote(d.id()) + ",\"balance\":" + Money.format(d.balanceCents())
                + ",\"ratePercent\":" + Money.percent(d.rateBp()) + ",\"termMonths\":" + d.termMonths() + "}";
    }

    private String array(List<? extends Element> items) {
        StringJoiner json = new StringJoiner(",", "[", "]");
        for (Element item : items) {
            json.add(item.accept(this));
        }
        return json.toString();
    }

    private static String quote(String text) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
