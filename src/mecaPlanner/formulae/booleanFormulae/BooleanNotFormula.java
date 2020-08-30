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

    publics static BooleanFormula make(BooleanFormula inputFormula) {
        if (inputFormula instanceof BooleanAtom) {
            return ((BooleanValue) simplified).negate();
        }
        if (inputFormula instanceof BooleanNotFormula) {
            return ((BooleanNotFormula) inputFormula).getFormula();
        }
        return new FluentFormulaNot(newFormula);
    }

    public FluentFormula getFormula() {
        return formula;
    }

    public Boolean holds(World world) {
        return !formula.holds(world);
    }

    //public Set<FluentAtom> getAllAtoms() {
    //    return formula.getAllAtoms();
    //}



    @Override
    public String toString() {
        return ("not(" + formula.toString() + ")");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        FluentFormulaNot other = (FluentFormulaNot) obj;
        return formula.equals(other.getFormula());
    }


     @Override
     public int hashCode() {
         return formula.hashCode() * 11;
     }




}
