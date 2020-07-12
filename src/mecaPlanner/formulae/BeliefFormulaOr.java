package mecaPlanner.formulae;

import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.World;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import java.util.Set;
import java.util.HashSet;

public class BeliefFormulaOr extends BeliefFormula{

    private List<BeliefFormula> formulae;

    public BeliefFormulaOr(List<BeliefFormula> formulae) {
        this.formulae = formulae;
    }

    public BeliefFormulaOr(BeliefFormula ...formula) {
        this(new ArrayList<BeliefFormula>(Arrays.asList(formula)));
    }

    public List<BeliefFormula> getFormulae() {
        return formulae;
    }

    public Boolean holdsAtWorld(KripkeStructure kripke, World world) {
        for (BeliefFormula formula : formulae) {
            if (formula.holdsAtWorld(kripke, world)) {
                return true;
            }
        }
        return false;
    }

    public Set<FluentAtom> getAllAtoms() {
        Set<FluentAtom> allAtoms = new HashSet<>();
        for (BeliefFormula formula : formulae) {
            allAtoms.addAll(formula.getAllAtoms());
        }
        return allAtoms;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("or(");
        if (formulae.size() > 0) {
            for (BeliefFormula formula : formulae) {
                str.append(formula);
                str.append(",");
            }
            str.deleteCharAt(str.length() - 1);
        }
        str.append(")");
        return str.toString();
    }

}
