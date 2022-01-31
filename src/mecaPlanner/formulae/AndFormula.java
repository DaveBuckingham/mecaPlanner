package mecaPlanner.formulae;


import mecaPlanner.state.Model;
import mecaPlanner.state.World;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import java.util.Set;
import java.util.HashSet;

public class AndFormula extends Formula{

    private List<Formula> formulae;

    private AndFormula(List<Formula> formulae) {
        this.formulae = formulae;
    }

    public static Formula make(List<Formula> inputFormulae) {
        List<Formula> formulae = new ArrayList<>();
        for (Formula f : inputFormulae) {
            if (f.isFalse()) {
                return new Literal(false);
            }
            else if (f instanceof AndFormula) {
                formulae.addAll(((AndFormula) f).getFormulae());
            }
            else if (!f.isTrue()) {
                formulae.add(f);
            }
        }
        if (formulae.isEmpty()) {
            return new Literal(true);
        }
        if (formulae.size() == 1) {
            return formulae.get(0);
        }
        return new AndFormula(formulae);
    }

    public static Formula make(Set<Formula> inputFormulae) {
        return AndFormula.make(new ArrayList(inputFormulae));
    }

    public static Formula make(Formula ...inputFormulae) {
        return AndFormula.make(Arrays.asList(inputFormulae));
    }

    public Set<Fluent> getAllFluents() {
        Set<Fluent> allFluents = new HashSet<>();
        for (Formula f : formulae) {
            allFluents.addAll(f.getAllFluents());
        }
        return allFluents;
    }

    public Integer getHeight() {
        Integer height = 0;
        for (Formula f : formulae) {
            Integer h = f.getHeight();
            if (h > height) {
                height = h;
            }
        }
        return height;
    }

    public Boolean isFalse() {
        return false;
    }
    public Boolean isTrue() {
        return false;
    }


//    public boolean isBinary() {
//        return formulae.size() == 2;
//    }
//
//    public AndFormula binarize() {
//        List<Formula> allFormulae = new ArrayList<>(formulae);
//        Formula lastFormula = allFormulae.get(allFormulae.size() - 1);
//        allFormulae.remove(allFormulae.size() - 1);
//        Formula theRest = AndFormula.make(allFormulae);
//        List<Formula> twoFormulae = new ArrayList<>();
//        twoFormulae.add(theRest);
//        twoFormulae.add(lastFormula);
//        return new AndFormula(twoFormulae);
//    }



    public List<Formula> getFormulae() {
        return formulae;
    }

    public Boolean evaluate(Model<World> model, World world) {
        for (Formula formula : formulae) {
            if (!formula.evaluate(model, world)) {
                return false;
            }
        }
        return true;
    }

    public Formula negate() {
        return NotFormula.make(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        AndFormula other = (AndFormula) obj;
        Set<Formula> asSet = new HashSet<>(formulae);
        return asSet.equals(new HashSet<Formula>(other.getFormulae()));
    }


    @Override
    public int hashCode() {
        int hash = 7;
        for (Formula f : this.formulae) {
            hash = (31 * hash) + f.hashCode();
        }
        return hash;
    }



    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("(");
        if (formulae.size() > 0) {
            for (Formula formula : formulae) {
                str.append(formula);
                str.append(" & ");
            }
            str.deleteCharAt(str.length() - 1);
            str.deleteCharAt(str.length() - 1);
            str.deleteCharAt(str.length() - 1);
        }
        str.append(")");
        return str.toString();
    }

}
