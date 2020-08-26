package mecaPlanner.state;

public class ObjectValue extends Value {

    String val;

    public ObjectValue(String val) {
        this.val = val;
    }

    public String get() {
        return val;
    }

    public String toString() {
        return val.toString();
    }


}
