package mecaPlanner.formulae;

import mecaPlanner.formulae.FluentFormulaNot;
import mecaPlanner.state.World;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import java.util.Set;

public class FluentFormulaNot extends FluentFormula{

    private FluentFormula formula;

    private FluentFormulaNot(FluentFormula formula) {
        this.formula = formula;
    }

    publics static FluentFormula make(FluentFormula inputFormula) {
        if (inputFormula instanceof BooleanValue) {
            return ((BooleanValue) simplified).negate();
        }
        if (inputFormula instanceof FluentFormulaNot) {
            return ((FluentFormulaNot) inputFormula).getFormula();
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
