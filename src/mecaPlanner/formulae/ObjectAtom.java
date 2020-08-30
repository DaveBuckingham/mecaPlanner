package mecaPlanner.formulae;

import mecaPlanner.state.World;
import mecaPlanner.state.Fluent;


public class ObjectAtom {

    private Fluent fluent;
    private String value;
    private Boolean isFluent;

    public ObjectAtom(Fluent fluent) {
        this.fluent = fluent;
        this.value = null;
        this.isFluent = true;
    }

    public ObjectAtom(String value) {
        this.fluent = null;
        this.value = value;
        this.isFluent = false;
    }

    protected Fluent getFluent() {
        return fluent;
    }

    protected String getValue() {
        return value;
    }

    public Boolean evaluate(World world) {
        if (isFluent) {
            return world.resolveObject(fluent);
        }
        return value;
    }


    @Override
    public String toString() {
        return isFluent ? fluent.toString() : value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        ObjectAtom other = (ObjectAtom) obj;
        return (value == other.getValue() && fluent == other.getFluent());
    }


     @Override
     public int hashCode() {
         return 1;
     }


}
