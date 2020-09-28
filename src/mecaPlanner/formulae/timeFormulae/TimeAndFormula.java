package mecaPlanner.formulae.timeFormulae;

import mecaPlanner.state.*;
import mecaPlanner.formulae.localFormulae.*;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Iterator;

import java.util.Set;
import java.util.HashSet;

public class TimeAndFormula extends TimeFormula{

    private List<TimeFormula> formulae;


    private TimeAndFormula(List<TimeFormula> formulae) {
        this.formulae = formulae;
    }

    public static TimeFormula make(List<TimeFormula> inputFormulae) {
        List<TimeFormula> formulae = new ArrayList<>();
        for (TimeFormula f : inputFormulae) {
            if (f.isFalse()) {
                return new Literal(false);
            }
            else if (!f.isTrue()) {
                formulae.add(f);
            }
        }
        if (formulae.isEmpty()) {
            return new Literal(true);
        }
        return new TimeAndFormula(formulae);
    }

    public static TimeFormula make(Set<TimeFormula> inputFormulae) {
        return TimeAndFormula.make(new ArrayList(inputFormulae));
    }



    public List<TimeFormula> getFormulae() {
        return formulae;
    }

    public Boolean evaluate(KripkeStructure kripke, World world, Integer timestep) {
        for (TimeFormula formula : formulae) {
            if (!formula.evaluate(kripke, world, timestep)) {
                return false;
            }
        }
        return true;
    }

    //public Set<FluentAtom> getAllAtoms() {
    //    Set<FluentAtom> allAtoms = new HashSet<>();
    //    for (TimeFormula formula : formulae) {
    //        allAtoms.addAll(formula.getAllAtoms());
    //    }
    //    return allAtoms;
    //}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        TimeAndFormula other = (TimeAndFormula) obj;
        Set<TimeFormula> asSet = new HashSet<>(formulae);
        return asSet.equals(new HashSet<TimeFormula>(other.getFormulae()));
    }


    @Override
    public int hashCode() {
        int hash = 7;
        for (TimeFormula f : this.formulae) {
            hash = (31 * hash) + f.hashCode();
        }
        return hash;
    }



    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("and(");
        if (formulae.size() > 0) {
            for (TimeFormula formula : formulae) {
                str.append(formula);
                str.append(",");
            }
            str.deleteCharAt(str.length() - 1);
        }
        str.append(")");
        return str.toString();
    }



}
