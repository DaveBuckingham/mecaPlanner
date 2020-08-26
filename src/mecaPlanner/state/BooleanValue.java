package mecaPlanner.state;

public class BooleanValue extends Value {

    Boolean val;

    public BooleanValue(Boolean val) {
        this.val = val;
    }

    public Boolean get() {
        return val;
    }

    public String toString() {
        return val.toString();
    }

}
