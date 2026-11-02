import java.util.List;

public record Branch(String id, String city, List<Client> clients) implements Element {
    public Branch {
        clients = List.copyOf(clients);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitBranch(this);
    }
}
