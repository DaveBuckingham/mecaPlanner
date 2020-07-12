package mecaPlanner.formulae;

import mecaPlanner.state.EpistemicState;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import java.util.Set;

public class GeneralFormulaNot extends GeneralFormula{

    private GeneralFormula formula;

    public GeneralFormulaNot(GeneralFormula formula) {
        this.formula = formula;
    }

    public GeneralFormula getFormula() {
        return formula;
    }

    public Boolean holds(EpistemicState state, Integer timestep) {
        return !formula.holds(state, timestep);
    }

    public Set<FluentAtom> getAllAtoms() {
        return formula.getAllAtoms();
    }

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
        GeneralFormulaNot other = (GeneralFormulaNot) obj;
        return formula.equals(other.getFormula());
    }


     @Override
     public int hashCode() {
         return formula.hashCode() * 31;
     }


}
