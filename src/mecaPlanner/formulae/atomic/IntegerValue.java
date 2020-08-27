package mecaPlanner.formulae.atomic;

public class IntegerValue extends Value {

    Integer val;

    public IntegerValue(Integer val) {
        this.val = val;
    }

    public Integer get() {
        return val;
    }

    public String toString() {
        return val.toString();
    }


}
