package mecaPlanner.formulae;

import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.World;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import java.util.Set;
import java.util.HashSet;

public class BeliefFormulaAnd extends BeliefFormula{

    private List<BeliefFormula> formulae;

    public BeliefFormulaAnd(List<BeliefFormula> formulae) {
        this.formulae = formulae;
    }

    public BeliefFormulaAnd(BeliefFormula ...formula) {
        this(new ArrayList<BeliefFormula>(Arrays.asList(formula)));
    }

    public List<BeliefFormula> getFormulae() {
        return formulae;
    }

    public Boolean holdsAtWorld(KripkeStructure kripke, World world) {
        for (BeliefFormula formula : formulae) {
            if (!formula.holdsAtWorld(kripke, world)) {
                return false;
            }
        }
        return true;
    }

    public Set<FluentAtom> getAllAtoms() {
        Set<FluentAtom> allAtoms = new HashSet<>();
        for (BeliefFormula formula : formulae) {
            allAtoms.addAll(formula.getAllAtoms());
        }
        return allAtoms;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        BeliefFormulaAnd other = (BeliefFormulaAnd) obj;
        Set<BeliefFormula> asSet = new HashSet<>(formulae);
        return asSet.equals(new HashSet<BeliefFormula>(other.getFormulae()));
    }


    @Override
    public int hashCode() {
        int hash = 7;
        for (BeliefFormula f : this.formulae) {
            hash = (31 * hash) + f.hashCode();
        }
        return hash;
    }



    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("and(");
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
