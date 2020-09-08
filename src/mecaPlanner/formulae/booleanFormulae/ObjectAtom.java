package mecaPlanner.formulae.booleanFormulae;

import mecaPlanner.state.World;
import mecaPlanner.state.Fluent;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.state.KripkeStructure;
import mecaPlanner.formulae.Formula;


public class ObjectAtom extends Formula{

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

    public String evaluate(EpistemicState state) {
        return evaluate(state.getDesignatedWorld());
    }

    public String evaluate(KripkeStructure kripke, World world) {
        return evaluate(world);
    }

    public String evaluate(World world) {
        if (isFluent) {
            return world.resolveObject(fluent);
        }
        return value;
    }


    public Boolean isFluent() {
        return isFluent;
    }
    public Boolean isValue() {
        return !isFluent;
    }

    public Fluent getFluent() {
        return fluent;
    }

    public String getValue() {
        return value;
    }

    public String getObjectValue() {
        if (value == null) {
            throw new RuntimeException("can't get value of fluent atom: " + fluent.toString());
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
        if (isFluent) {
            return (other.isFluent() && fluent.equals(other.getFluent()));
        }
        return ((!other.isFluent()) && value.equals(other.getValue()));
    }


     @Override
     public int hashCode() {
         return 1;
     }


}
