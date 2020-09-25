package mecaPlanner.formulae.objectFormulae;

import mecaPlanner.state.World;
import mecaPlanner.state.Fluent;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class ObjectFluent extends ObjectFormula{

    private Fluent fluent;

    public ObjectFluent(Fluent fluent) {
        this.fluent = fluent;
    }

    public Fluent resolve() {
        return fluent;
    }

    public String evaluate(World world) {
        return (String) world.ground(fluent);
    }

    @Override
    public String toString() {
        return fluent.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        ObjectFluent other = (ObjectFluent) obj;
        return (value.equals(other.resolve()));
    }


     @Override
     public int hashCode() {
         return fluent.hashCode();
     }




}
