package mecaPlanner.formulae.beliefFormulae;

import mecaPlanner.state.Fluent;
import mecaPlanner.state.World;


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
                return new BooleanAtom(true);
            }
            if (rhs.isFalse()) {
                return new BooleanAtom(false);
            }
        }
        else if (lhs.isFalse()) {
            if (rhs.isTrue()) {
                return new BooleanAtom(false);
            }
            if (rhs.isFalse()) {
                return new BooleanAtom(true);
            }
        }
        return new CompareBeliefs(lhs, rhs);
    }

    public BeliefFormula getLhs() {
        return lhs;
    }

    public BeliefFormula getlhs() {
        return rhs;
    }

    public Boolean evaluate(World world) {
        return (lhs.evaluate(world) == rhs.evaluate(world));
    }


    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("(");
        if (formulae.size() > 0) {
            for (BeliefFormula formula : formulae) {
                str.append(formula);
                str.append("=");
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
        CompareBeliefs other = (CompareBelief) obj;
        return (lhs.equals(other.getLhs()) && rhs.equals(other.getRhs()));
    }



    @Override
    public int hashCode() {
        return (lhs.hashCode() * rhs.hashCode());
    }




}
