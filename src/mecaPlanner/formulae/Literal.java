package mecaPlanner.formulae;

import mecaPlanner.state.AbstractState;
import mecaPlanner.state.World;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

public class Literal extends Fluent{

    Boolean value;

    public Literal(Boolean value) {
        super(value ? "TRUE" : "FALSE");
        this.value = value;
    }

    public Boolean getValue() {
        return value;
    }

    public Boolean evaluate(World world) {
        return value;
    }

    public Boolean evaluate(AbstractState model, World world) {
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

    public Set<Fluent> getAllFluents() {
        return new HashSet<Fluent>();
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
