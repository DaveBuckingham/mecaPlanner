package mecaPlanner.formulae.integerFormulae;

import mecaPlanner.state.World;
import mecaPlanner.state.Fluent;
import mecaPlanner.formulae.integerFormulae.IntegerFormula;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import java.util.Set;

public class IntegerAtom extends IntegerFormula{

    private Fluent fluent;
    private Integer value;
    private Integer isFluent;

    public IntegerAtom(Fluent fluent) {
        this.fluent = fluent;
        this.value = null;
        this.isFluent = true;
    }

    public IntegerAtom(Integer value) {
        this.fluent = null;
        this.value = value;
        this.isFluent = false;
    }

    protected Fluent getFluent() {
        return fluent;
    }

    public Integer getValue() {
        if (value == null) {
            throw new RuntimeException("can't get value of fluent atom: " + fluent.toString());
        }
        return value;
    }

    public Boolean isFluent() {
        return isFluent;
    }
    public Boolean isValue() {
        return !isFluent;
    }

    public Integer getLiteral() {
        return value;
    }

    public Integer evaluate(World world) {
        if (isFluent) {
            return world.resolveInteger(fluent);
        }
        return value;
    }

    public Boolean isFalse() {
        return (!isFluent && !value);
    }

    public Boolean isTrue() {
        return (!isFluent && value);
    }

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
        FluentFormulaAtom other = (FluentFormulaAtom) obj;
        return (value == other.getValue() && fluent == other.getFluent);
    }


     @Override
     public int hashCode() {
         return 1;
     }




}