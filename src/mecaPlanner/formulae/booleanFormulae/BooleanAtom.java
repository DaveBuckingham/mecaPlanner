package mecaPlanner.formulae.booleanFormulae;

import mecaPlanner.state.World;
import mecaPlanner.state.Fluent;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class BooleanAtom extends BooleanFormula{

    private Fluent fluent;
    private Boolean value;
    private Boolean isFluent;

    public BooleanAtom(Fluent fluent) {
        this.fluent = fluent;
        this.value = null;
        this.isFluent = true;
    }

    public BooleanAtom(Boolean value) {
        this.fluent = null;
        this.value = value;
        this.isFluent = false;
    }

    public BooleanAtom(String name, String ...strParams) {
        if (name.equalsIgnoreCase("true")) {
            this.fluent = null;
            this.value = true;
            this.isFluent = false;
        }
        else if (name.equalsIgnoreCase("false")) {
            this.fluent = null;
            this.value = false;
            this.isFluent = false;
        }
        else {
            this.fluent = new Fluent(name, strParams);
            this.value = null;
            this.isFluent = true;
        }
    }

    public Boolean isFluent() {
        return isFluent;
    }
    public Boolean isValue() {
        return !isFluent;
    }

    protected Fluent getFluent() {
        return fluent;
    }

    public Boolean getBooleanValue() {
        if (value == null) {
            throw new RuntimeException("can't get value of fluent atom: " + fluent.toString());
        }
        return value;
    }

    public Boolean evaluate(World world) {
        if (isFluent) {
            return world.resolveBoolean(fluent);
        }
        return value;
    }

    public BooleanFormula negate() {
        return isFluent ? BooleanNotFormula.make(this) : new BooleanAtom(!value);
    }

    public Boolean isFalse() {
        return (!isFluent && !value);
    }

    public Boolean isTrue() {
        return (!isFluent && value);
    }

    //public Boolean isFluent() {
    //    return isFluent;
    //}

    //public Set<FluentAtom> getAllAtoms() {
    //    return formula.getAllAtoms();
    //}



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
        BooleanAtom other = (BooleanAtom) obj;
        return (value == other.getBooleanValue() && fluent == other.getFluent());
    }


     @Override
     public int hashCode() {
         return 1;
     }




}
