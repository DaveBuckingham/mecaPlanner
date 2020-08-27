package mecaPlanner.formulae.atomic;

public class ObjectValue extends Value {

    String val;

    public ObjectValue(String val) {
        this.val = val;
    }

    public String get() {
        return val;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        return val.equals(((ObjectValue) obj).get())
    }

    public String toString() {
        return val.toString();
    }


}
