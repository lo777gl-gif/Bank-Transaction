import java.util.List;

public record Client(String id, String name, List<Holding> holdings) implements Element {
    public Client {
        holdings = List.copyOf(holdings);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitClient(this);
    }
}
