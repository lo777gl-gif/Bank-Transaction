import java.util.List;

public record Bank(String name, List<Branch> branches) implements Element {
    public Bank {
        branches = List.copyOf(branches);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitBank(this);
    }
}
