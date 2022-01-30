package mecaPlanner.formulae;

import java.util.List;
import java.util.ArrayList;

import mecaPlanner.state.World;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.state.KripkeStructure;

import mecaPlanner.*;


import java.util.Objects;

import java.util.Set;


public abstract class Formula {

    public abstract Integer getHeight();
    //public abstract Integer getOrder();
    public abstract Formula negate();
    public abstract Boolean isTrue();
    public abstract Boolean isFalse();
    public abstract Set<Fluent> getAllFluents();

    public abstract Boolean evaluate(NDState kripke, World world);

    public Boolean evaluate(World world) {
        return this.evaluate(null, world);
    }

    public Boolean evaluate(EpistemicState state) {
        return evaluate(state, state.getDesignated());
    }

    public Boolean possibly(NDState n) {
        for (World w : n.getDesignated()) {
            if (evaluate(n, w)) {
                return true;
            }
        }
        return false;
    }

    public Boolean necessarily(NDState n) {
        for (World w : n.getDesignated()) {
            if (!evaluate(n, w)) {
                return false;
            }
        }
        return true;
    }

    public static Formula makeDisjunction(List<Formula> disjuncts) {
        List<Formula> negated = new ArrayList<>();
        for (Formula f : disjuncts) {
            negated.add(f.negate());
        }
        return AndFormula.make(negated).negate();
    }

    public static Formula makeDisjunction(Set<Formula> disjuncts) {
        return AndFormula.make(new ArrayList(disjuncts));
    }


}
