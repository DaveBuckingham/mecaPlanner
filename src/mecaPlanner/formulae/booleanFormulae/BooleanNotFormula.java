package mecaPlanner.formulae.booleanFormulae;

import mecaPlanner.state.World;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import java.util.Set;

public class BooleanNotFormula extends BooleanFormula{

    private BooleanFormula formula;

    private BooleanNotFormula(BooleanFormula formula) {
        this.formula = formula;
    }

    public static BooleanFormula make(BooleanFormula inputFormula) {
        if (inputFormula.isTrue()) {
            return new BooleanAtom(false);
        }
        if (inputFormula.isFalse()) {
            return new BooleanAtom(true);
        }
        if (inputFormula instanceof BooleanNotFormula) {
            return ((BooleanNotFormula) inputFormula).getFormula();
        }
        return new BooleanNotFormula(inputFormula);
    }

    public BooleanFormula getFormula() {
        return formula;
    }

    public Boolean evaluate(World world) {
        return !formula.evaluate(world);
    }

    public BooleanFormula negate() {
        return formula;
    }

    //public Set<FluentAtom> getAllAtoms() {
    //    return formula.getAllAtoms();
    //}



    @Override
    public String toString() {
        return ("~(" + formula.toString() + ")");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        BooleanNotFormula other = (BooleanNotFormula) obj;
        return formula.equals(other.getFormula());
    }


     @Override
     public int hashCode() {
         return formula.hashCode() * 11;
     }




}
