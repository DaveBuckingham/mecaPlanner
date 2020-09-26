package mecaPlanner.formulae.booleanFormulae;

import mecaPlanner.state.World;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Iterator;

import java.util.Set;
import java.util.HashSet;

public class BooleanAndFormula extends BooleanFormula{

    // SHOULD THIS BE A SET? THE ORDER SHOULDN'T MATTER WHEN CHECKING EQUALITY...
    private List<BooleanFormula> formulae;


    private BooleanAndFormula(List<BooleanFormula> formulae) {
        this.formulae = formulae;
    }

    public static BooleanFormula make(List<BooleanFormula> inputFormulae) {
        List<BooleanFormula> formulae = new ArrayList<>();
        for (BooleanFormula bf : inputFormulae) {
            if (bf.isFalse()) {
                return bf;
            }
            else {
                formulae.add(bf);
            }
        }
        if (formulae.isEmpty()) {
            return new BooleanValue(true);
        }
        return new BooleanAndFormula(formulae);
    }

    public static BooleanFormula make(Set<BooleanFormula> inputFormulae) {
        return BooleanAndFormula.make(new ArrayList(inputFormulae));
    }

    public static BooleanFormula make(BooleanFormula ...inputFormulae) {
        return BooleanAndFormula.make(Arrays.asList(inputFormulae));
    }



    public List<BooleanFormula> getFormulae() {
        return formulae;
    }

    public Boolean evaluate(World world) {
        for (BooleanFormula formula : formulae) {
            if (!formula.evaluate(world)) {
                return false;
            }
        }
        return true;
    }


    //public Set<Atom> getAllAtoms() {
    //    Set<Atom> allAtoms = new HashSet<>();
    //    for (BooleanFormula formula : formulae) {
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
        BooleanAndFormula other = (BooleanAndFormula) obj;
        Set<BooleanFormula> asSet = new HashSet<>(formulae);
        return asSet.equals(new HashSet<BooleanFormula>(other.getFormulae()));
    }



    @Override
    public int hashCode() {
        int result = 7;
        for (BooleanFormula f : formulae) {
            result = (31 * result) + f.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("(");
        if (formulae.size() > 0) {
            for (BooleanFormula formula : formulae) {
                str.append(formula);
                str.append("&");
            }
            str.deleteCharAt(str.length() - 1);
        }
        str.append(")");
        return str.toString();
    }

}
