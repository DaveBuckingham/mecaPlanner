package mecaPlanner.formulae;

import mecaPlanner.state.EpistemicState;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;


public class GeneralFormulaOr extends GeneralFormula{

    // SHOULD THIS BE A SET? THE ORDER SHOULDN'T MATTER WHEN CHECKING EQUALITY...
    private List<GeneralFormula> formulae;

    public GeneralFormulaOr(GeneralFormula ...formula) {
        this.formulae = new ArrayList<GeneralFormula>(Arrays.asList(formula));
    }

    public GeneralFormulaOr(List<GeneralFormula> formulae) {
        this.formulae = formulae;
    }

    public List<GeneralFormula> getFormulae() {
        return formulae;
    }

    public Boolean holds(EpistemicState state, Integer timestep) {
        for (GeneralFormula formula : formulae) {
            if (formula.holds(state, timestep)) {
                return true;
            }
        }
        return false;
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
        GeneralFormulaOr other = (GeneralFormulaOr) obj;
        Set<GeneralFormula> asSet = new HashSet<>(formulae);
        return asSet.equals(new HashSet<GeneralFormula>(other.getFormulae()));
    }


    @Override
    public int hashCode() {
        int hash = 11;
        for (GeneralFormula f : this.formulae) {
            hash = (31 * hash) + f.hashCode();
        }
        return hash;
    }







    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("or(");
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

//    @Override
//    public boolean equals(Object obj) {
//        if (obj == this) {
//            return true;
//        }
//        if (obj == null || obj.getClass() != this.getClass()) {
//            return false;
//        }
//        GeneralFormulaOr other = (GeneralFormulaOr) obj;
//        List<GeneralFormula> otherFormulae = other.getFormulae();
//
//        if (formulae.size() != otherFormulae.size()) {
//            return false;
//        }
//
//        return formulae.containsAll(otherFormulae);
//
//    }




}
