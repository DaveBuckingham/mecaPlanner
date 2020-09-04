package mecaPlanner.formulae.beliefFormulae;

import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.World;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import java.util.Set;
import java.util.HashSet;

public class BeliefAndFormula extends BeliefFormula{

    private List<BeliefFormula> formulae;

    public BeliefAndFormula(List<BeliefFormula> formulae) {
        this.formulae = formulae;
    }

    public BeliefAndFormula(BeliefFormula ...formula) {
        this(new ArrayList<BeliefFormula>(Arrays.asList(formula)));
    }

    public BeliefAndFormula(Set<BeliefFormula> formulae) {
        this(new ArrayList<BeliefFormula>(formulae));
    }


    public static BeliefFormula make(List<BeliefFormula> inputFormulae) {
        List<BeliefFormula> formulae = new ArrayLiset<>();
        for (BeliefFormula ff : inputFormulae) {
            if (ff instanceof BooleanValue) {
                if (!((BooleanValue) simplified).get()) {
                    return ff;
                }
            }
            else {
                formulae.add(ff);
            }
        }
        if (formulae.isEmpty()) {
            return new BooleanValue(true);
        }
        return new BeliefAndFormula(formulae);
    }

    public static BeliefFormula make(Set<BeliefFormula> inputFormulae) {
        return BeliefAndFormula.make(Arrays.asList(inputFormulae));
    }

    public static BeliefFormula make(BeliefFormula ...inputFormulae) {
        return BeliefAndFormula.make(Arrays.asList(inputFormulae));
    }









    public List<BeliefFormula> getFormulae() {
        return formulae;
    }

    public Boolean evaluate(KripkeStructure kripke, World world) {
        for (BeliefFormula formula : formulae) {
            if (!formula.evaluate(kripke, world)) {
                return false;
            }
        }
        return true;
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
        BeliefAndFormula other = (BeliefAndFormula) obj;
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
