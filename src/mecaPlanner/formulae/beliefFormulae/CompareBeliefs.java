package mecaPlanner.formulae.beliefFormulae;

import mecaPlanner.state.Fluent;
import mecaPlanner.formulae.booleanFormulae.BooleanValue;
import mecaPlanner.state.World;
import mecaPlanner.state.KripkeStructure;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;


public class CompareBeliefs extends BeliefFormula{

    private BeliefFormula lhs;
    private BeliefFormula rhs;


    private CompareBeliefs(BeliefFormula lhs, BeliefFormula rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }


    public static BeliefFormula make(BeliefFormula lhs, BeliefFormula rhs) {
        if (lhs.isTrue()) {
            if (rhs.isTrue()) {
                return new BooleanValue(true);
            }
            if (rhs.isFalse()) {
                return new BooleanValue(false);
            }
        }
        else if (lhs.isFalse()) {
            if (rhs.isTrue()) {
                return new BooleanValue(false);
            }
            if (rhs.isFalse()) {
                return new BooleanValue(true);
            }
        }
        return new CompareBeliefs(lhs, rhs);
    }

    public BeliefFormula getLhs() {
        return lhs;
    }

    public BeliefFormula getRhs() {
        return rhs;
    }

    public Boolean evaluate(KripkeStructure kripke, World world) {
        return (lhs.evaluate(kripke, world) == rhs.evaluate(kripke, world));
    }


    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("(");
        str.append(lhs);
        str.append("==");
        str.append(rhs);
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
        CompareBeliefs other = (CompareBeliefs) obj;
        return (lhs.equals(other.getLhs()) && rhs.equals(other.getRhs()));
    }



    @Override
    public int hashCode() {
        return (lhs.hashCode() * rhs.hashCode());
    }




}
