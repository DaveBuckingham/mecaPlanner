package mecaPlanner.formulae.localFormulae;

import mecaPlanner.state.World;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class Literal extends LocalFormula{

    Boolean value;

    public Literal(Boolean value) {
        this.value = value;
    }

    public Boolean getValue() {
        return value;
    }

    public Boolean evaluate(World world) {
        return value;
    }

    public LocalFormula negate() {
        return new Literal(!value);
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
        Literal other = (Literal) obj;
        return (value.equals(other.getValue()));
    }


     @Override
     public int hashCode() {
         return value.hashCode();
     }


}
