package mecaPlanner.formulae.booleanFormulae;

import mecaPlanner.state.World;
import mecaPlanner.state.Fluent;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class BooleanFluent extends BooleanFormula{

    private Fluent fluent;

    public BooleanFluent(Fluent fluent) {
        this.fluent = fluent;
    }

    public BooleanFluent(String name, List<String> parameters) {
        this(new Fluent(name, parameters));
    }

    public BooleanFluent(String ...strParams) {
        this(new Fluent(strParams));
    }

    public BooleanFluent(String name) {
        this(new Fluent(name));
    }



    public Fluent resolve() {
        return fluent;
    }

    public Boolean evaluate(World world) {
        return (Boolean) world.ground(fluent);
    }

    public BooleanFormula negate() {
        return BooleanNotFormula.make(new BooleanFluent(fluent));
    }

    public Boolean isFalse() {
        return false;
    }

    public Boolean isTrue() {
        return false;
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
        BooleanFluent other = (BooleanFluent) obj;
        return (value.equals(other.resolve()));
    }


     @Override
     public int hashCode() {
         return fluent.hashCode();
     }




}
