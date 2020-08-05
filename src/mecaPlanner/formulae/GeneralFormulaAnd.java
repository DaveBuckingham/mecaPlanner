package mecaPlanner.formulae;

import mecaPlanner.state.EpistemicState;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Iterator;

import java.util.Set;
import java.util.HashSet;

public class GeneralFormulaAnd extends GeneralFormula{

    private List<GeneralFormula> formulae;

    public GeneralFormulaAnd(GeneralFormula ...formula) {
        this.formulae = new ArrayList<GeneralFormula>(Arrays.asList(formula));
    }

    public GeneralFormulaAnd(Set<GeneralFormula> formulae) {
        this.formulae = new ArrayList<GeneralFormula>(formulae);
    }

    public GeneralFormulaAnd(List<GeneralFormula> formulae) {
        this.formulae = formulae;
    }

    public List<GeneralFormula> getFormulae() {
        return formulae;
    }

    public Boolean holds(EpistemicState state, Integer timestep) {
        for (GeneralFormula formula : formulae) {
            if (!formula.holds(state, timestep)) {
                return false;
            }
        }
        return true;
    }

    public Set<FluentAtom> getAllAtoms() {
        Set<FluentAtom> allAtoms = new HashSet<>();
        for (GeneralFormula formula : formulae) {
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
        GeneralFormulaAnd other = (GeneralFormulaAnd) obj;
        Set<GeneralFormula> asSet = new HashSet<>(formulae);
        return asSet.equals(new HashSet<GeneralFormula>(other.getFormulae()));
    }


    @Override
    public int hashCode() {
        int hash = 7;
        for (GeneralFormula f : this.formulae) {
            hash = (31 * hash) + f.hashCode();
        }
        return hash;
    }



    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("and(");
        if (formulae.size() > 0) {
            for (GeneralFormula formula : formulae) {
                str.append(formula);
                str.append(",");
            }
            str.deleteCharAt(str.length() - 1);
        }
        str.append(")");
        return str.toString();
    }



}
