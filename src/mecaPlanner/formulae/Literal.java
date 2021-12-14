package mecaPlanner.formulae;

import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.World;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class Literal extends Formula{

    Boolean value;

    public Literal(Boolean value) {
        this.value = value;
    }

    public Boolean getValue() {
        return value;
    }

    public Boolean evaluate(KripkeStructure kripke, World world) {
        return value;
    }

    public Formula negate() {
        return new Literal(!value);
    }

    public Boolean isFalse() {
        return (!value);
    }

    public Boolean isTrue() {
        return (value);
    }


    public Integer getHeight() {
        return 0;
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
