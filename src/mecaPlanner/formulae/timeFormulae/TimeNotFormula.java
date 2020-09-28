package mecaPlanner.formulae.timeFormulae;

import mecaPlanner.state.*;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import java.util.Set;

public class TimeNotFormula extends TimeFormula{

    private TimeFormula formula;

    public TimeNotFormula(TimeFormula formula) {
        this.formula = formula;
    }

    public TimeFormula getFormula() {
        return formula;
    }

    public Boolean evaluate(KripkeStructure kripke, World world, Integer timestep) {
        return !formula.evaluate(kripke, world, timestep);
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
        TimeNotFormula other = (TimeNotFormula) obj;
        return formula.equals(other.getFormula());
    }


     @Override
     public int hashCode() {
         return formula.hashCode() * 31;
     }


}
