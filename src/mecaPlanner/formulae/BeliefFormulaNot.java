package mecaPlanner.formulae;

import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.World;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import java.util.Set;

public class BeliefFormulaNot extends BeliefFormula{

    private BeliefFormula formula;

    public BeliefFormulaNot(BeliefFormula formula) {
        this.formula = formula;
    }

    public BeliefFormula getFormula() {
        return formula;
    }

    public Boolean holdsAtWorld(KripkeStructure kripke, World world) {
        return (!formula.holdsAtWorld(kripke, world));
    }

    public Set<FluentAtom> getAllAtoms() {
        return formula.getAllAtoms();
    }

    @Override
    public BeliefFormula negate() {
        return formula;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("not(");
        str.append(this.formula);
        str.append(")");
        return str.toString();
    }

}
