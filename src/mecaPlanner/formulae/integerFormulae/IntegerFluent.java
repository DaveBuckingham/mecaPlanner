package mecaPlanner.formulae.integerFormulae;

import mecaPlanner.state.World;
import mecaPlanner.state.Fluent;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class IntegerFluent extends IntegerFormula{

    private Fluent fluent;

    public IntegerFluent(Fluent fluent) {
        this.fluent = fluent;
    }

    public Fluent resolve() {
        return fluent;
    }

    public Integer evaluate(World world) {
        return (Integer) world.ground(fluent);
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
        IntegerFluent other = (IntegerFluent) obj;
        return (fluent.equals(other.resolve()));
    }


     @Override
     public int hashCode() {
         return fluent.hashCode();
     }




}
