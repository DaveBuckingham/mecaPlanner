package mecaPlanner.formulae.belief;

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


    private BeleifFormulaOr(List<BeleifFormula> formulae) {
        this.formulae = formulae;
    }

    public static BeleifFormula make(List<BeleifFormula> inputFormulae) {
        List<BeleifFormula> formulae = new ArrayLiset<>();
        for (BeleifFormula ff : inputFormulae) {
            if (ff instanceof BooleanValue) {
                if (((BooleanValue) simplified).get()) {
                    return ff;
                }
            }
            else {
                formulae.add(ff);
            }
        }
        if (formulae.isEmpty()) {
            return new BooleanValue(false);
        }
        return new BeleifFormulaOr(formulae);
    }

    public static BeleifFormula make(Set<BeleifFormula> inputFormulae) {
        return BeleifFormulaOr.make(Arrays.asList(inputFormulae));
    }

    public static BeleifFormula make(BeleifFormula ...inputFormulae) {
        return BeleifFormulaOr.make(Arrays.asList(inputFormulae));
    }









    public List<BeliefFormula> getFormulae() {
        return formulae;
    }

    public Boolean evaluate(KripkeStructure kripke, World world) {
        for (BeliefFormula formula : formulae) {
            if (formula.evaluate(kripke, world)) {
                return true;
            }
        }
        return false;
    }

    //public Set<FluentAtom> getAllAtoms() {
    //    Set<FluentAtom> allAtoms = new HashSet<>();
    //    for (BeliefFormula formula : formulae) {
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
        BeliefFormulaOr other = (BeliefFormulaOr) obj;
        Set<BeliefFormula> asSet = new HashSet<>(formulae);
        return asSet.equals(new HashSet<BeliefFormula>(other.getFormulae()));
    }


    @Override
    public int hashCode() {
        int hash = 11;
        for (BeliefFormula f : this.formulae) {
            hash = (31 * hash) + f.hashCode();
        }
        return hash;
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
