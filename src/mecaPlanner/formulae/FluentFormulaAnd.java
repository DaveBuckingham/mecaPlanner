package mecaPlanner.formulae;

import mecaPlanner.state.World;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Iterator;

import java.util.Set;
import java.util.HashSet;

public class FluentFormulaAnd extends FluentFormula{

    // SHOULD THIS BE A SET? THE ORDER SHOULDN'T MATTER WHEN CHECKING EQUALITY...
    private List<FluentFormula> formulae;

    public FluentFormulaAnd(FluentFormula ...formula) {
        this.formulae = new ArrayList<FluentFormula>(Arrays.asList(formula));
    }

    public FluentFormulaAnd(List<FluentFormula> formulae) {
        this.formulae = formulae;
    }

    public List<FluentFormula> getFormulae() {
        return formulae;
    }

    public Boolean holds(World world) {
        for (FluentFormula formula : formulae) {
            if (!formula.holds(world)) {
                return false;
            }
        }
        return true;
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
        str.append("and(");
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

    //@Override
    //public boolean equals(Object obj) {
    //    if (obj == this) {
    //        return true;
    //    }
    //    if (obj == null || obj.getClass() != this.getClass()) {
    //        return false;
    //    }
    //    FluentFormulaAnd other = (FluentFormulaAnd) obj;
    //    List<FluentFormula> otherFormulae = other.getFormulae();

    //    if (formulae.size() != otherFormulae.size()) {
    //        return false;
    //    }

    //    return formulae.containsAll(otherFormulae);

    //    // Iterator thisIt = formulae.iterator();
    //    // Iterator otherIt = otherFormulae.iterator();
    //    // while(thisIt.hasNext() || otherIt.hasNext()) {
    //    //     if (!(thisIt.hasNext() && otherIt.hasNext())) {
    //    //         return false;
    //    //     }
    //    //     if (!(thisIt.next().equals(otherIt.next()))) {
    //    //         return false;
    //    //     }
    //    // }
    //    // return true;
    //}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 7;
        for (FluentFormula f : formulae) {
            result = (prime * result) + f.hashCode();
        }
        return result;
    }


}
