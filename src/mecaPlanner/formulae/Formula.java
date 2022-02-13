package mecaPlanner.formulae;

import java.util.List;
import java.util.ArrayList;

import mecaPlanner.state.World;
import mecaPlanner.state.NDState;
import mecaPlanner.state.State;

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

    public abstract Boolean evaluate(NDState model, World world);
    public abstract Boolean evaluate(World world);

    public Boolean evaluate(State state) {
        return evaluate(state, state.getDesignatedWorld());
    }

    public Boolean possibly(NDState state) {
        for (World w : state.getDesignated()) {
            if (evaluate(state, w)) {
                return true;
            }
        }
        return false;
    }

    public Boolean necessarily(NDState state) {
        for (World w : state.getDesignated()) {
            if (!evaluate(state, w)) {
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
