package mecaPlanner.formulae;

import mecaPlanner.state.World;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;


public class FluentFormulaOr extends FluentFormula{

    // SHOULD THIS BE A SET? THE ORDER SHOULDN'T MATTER WHEN CHECKING EQUALITY...
    private List<FluentFormula> formulae;

    public FluentFormulaOr(FluentFormula ...formula) {
        this.formulae = new ArrayList<FluentFormula>(Arrays.asList(formula));
    }

    public FluentFormulaOr(List<FluentFormula> formulae) {
        this.formulae = formulae;
    }

    public List<FluentFormula> getFormulae() {
        return formulae;
    }

    public Boolean holds(World world) {
        for (FluentFormula formula : formulae) {
            if (formula.holds(world)) {
                return true;
            }
        }
        return false;
    }

    public Set<FluentAtom> getAllAtoms() {
        Set<FluentAtom> allAtoms = new HashSet<>();
        for (FluentFormula formula : formulae) {
            allAtoms.addAll(formula.getAllAtoms());
        }
        return allAtoms;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("or(");
        if (formulae.size() > 0) {
            for (FluentFormula formula : formulae) {
                str.append(formula);
                str.append(",");
            }
            str.deleteCharAt(str.length() - 1);
        }
        str.append(")");
        return str.toString();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        FluentFormulaAnd other = (FluentFormulaAnd) obj;
        return formulae.containsAll(other.getFormulae()) && other.getFormulae().containsAll(formulae);
    }

    @Override
    public int hashCode() {
        int result = 7;
        for (FluentFormula f : formulae) {
            result = (31 * result) + f.hashCode();
        }
        return result;
    }




}
