package mecaPlanner.state;

public interface Transformer {
    public State transition(State s);
    public String getSignature();
    public boolean executable(State state);
}

