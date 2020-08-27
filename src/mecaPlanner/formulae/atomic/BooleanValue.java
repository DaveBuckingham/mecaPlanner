package mecaPlanner.formulae.atomic;

public class BooleanValue extends Value {

    Boolean val;

    public BooleanValue(Boolean val) {
        this.val = val;
    }

    public Boolean get() {
        return val;
    }

    public BooleanValue negate() {
        return new BooleanValue(!val);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        return ((BooleanValue)obj).get() == val;
    }



    public String toString() {
        return val.toString();
    }

}
