package mecaPlanner.formulae.localFormulae;

import mecaPlanner.state.World;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Iterator;

import java.util.Set;
import java.util.HashSet;

public class LocalAndFormula extends LocalFormula{

    // SHOULD THIS BE A SET? THE ORDER SHOULDN'T MATTER WHEN CHECKING EQUALITY...
    private List<LocalFormula> formulae;


    private LocalAndFormula(List<LocalFormula> formulae) {
        this.formulae = formulae;
    }

    public static LocalFormula make(List<LocalFormula> inputFormulae) {
        List<LocalFormula> formulae = new ArrayList<>();
        for (LocalFormula f : inputFormulae) {
            if (f.isFalse()) {
                return f;
            }
            else if (!f.isTrue()) {
                formulae.add(f);
            }
        }
        if (formulae.isEmpty()) {
            return new Literal(true);
        }
        return new LocalAndFormula(formulae);
    }

    public static LocalFormula make(Set<LocalFormula> inputFormulae) {
        return LocalAndFormula.make(new ArrayList(inputFormulae));
    }

    public static LocalFormula make(LocalFormula ...inputFormulae) {
        return LocalAndFormula.make(Arrays.asList(inputFormulae));
    }



    public List<LocalFormula> getFormulae() {
        return formulae;
    }

    public Boolean evaluate(World world) {
        for (LocalFormula formula : formulae) {
            if (!formula.evaluate(world)) {
                return false;
            }
        }
        return true;
    }


    //public Set<Atom> getAllAtoms() {
    //    Set<Atom> allAtoms = new HashSet<>();
    //    for (LocalFormula formula : formulae) {
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
        LocalAndFormula other = (LocalAndFormula) obj;
        Set<LocalFormula> asSet = new HashSet<>(formulae);
        return asSet.equals(new HashSet<LocalFormula>(other.getFormulae()));
    }



    @Override
    public int hashCode() {
        int result = 7;
        for (LocalFormula f : formulae) {
            result = (31 * result) + f.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("(");
        if (formulae.size() > 0) {
            for (LocalFormula formula : formulae) {
                str.append(formula);
                str.append("&");
            }
            str.deleteCharAt(str.length() - 1);
        }
        str.append(")");
        return str.toString();
    }

}