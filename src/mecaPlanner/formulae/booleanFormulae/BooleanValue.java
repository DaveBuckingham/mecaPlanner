package mecaPlanner.formulae.booleanFormulae;

import mecaPlanner.state.World;
import mecaPlanner.state.Fluent;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class BooleanValue extends BooleanFormula{

    private Boolean value;

    public BooleanValue(Boolean value) {
        this.value = value;
    }

    public Boolean resolve() {
        return value;
    }

    public Boolean evaluate(World world) {
        return value;
    }

    public BooleanFormula negate() {
        return BooleanValue(!value);
    }

    public Boolean isFalse() {
        return (!value);
    }

    public Boolean isTrue() {
        return (value);
    }


    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        BooleanValue other = (BooleanValue) obj;
        return (value.equals(other.resolve()));
    }


     @Override
     public int hashCode() {
         return value.hashCode();
     }




}
