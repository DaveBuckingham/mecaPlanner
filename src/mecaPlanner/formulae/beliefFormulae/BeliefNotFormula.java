package mecaPlanner.formulae.beliefFormulae;


import mecaPlanner.formulae.localFormulae.Literal;
import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.World;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import java.util.Set;

public class BeliefNotFormula extends BeliefFormula{

    private BeliefFormula formula;


    private BeliefNotFormula(BeliefFormula formula) {
        this.formula = formula;
    }

    public static BeliefFormula make(BeliefFormula inputFormula) {
        if (inputFormula.isTrue()) {
            return new Literal(false);
        }
        if (inputFormula.isFalse()) {
            return new Literal(true);
        }
        if (inputFormula instanceof BeliefNotFormula) {
            return ((BeliefNotFormula) inputFormula).getFormula();
        }
        return new BeliefNotFormula(inputFormula);
    }

    public BeliefFormula getFormula() {
        return formula;
    }


    public Boolean evaluate(KripkeStructure kripke, World world) {
        return (!formula.evaluate(kripke, world));
    }

    //public Set<FluentAtom> getAllAtoms() {
    //    return formula.getAllAtoms();
    //}

    @Override
    public BeliefFormula negate() {
        return formula;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        BeliefNotFormula other = (BeliefNotFormula) obj;
        return formula.equals(other.getFormula());
    }


    @Override
    public int hashCode() {
        return formula.hashCode() * 7;
    }




    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("not(");
        str.append(this.formula);
        str.append(")");
        return str.toString();
    }

}
