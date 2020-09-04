package mecaPlanner.formulae.beliefFormulae;

import mecaPlanner.formulae.Formula;

import mecaPlanner.state.World;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.state.KripkeStructure;

import mecaPlanner.*;


import java.util.Objects;

import java.util.Set;


public abstract class BeliefFormula extends Formula{

    public Boolean evaluate(EpistemicState state) {
        return evaluate(state.getKripke(), state.getDesignatedWorld());
    }

    public abstract Boolean evaluate(KripkeStructure kripke, World world);

    public BeliefFormula negate() {
        return BeliefNotFormula.make(this);
    }

    // THESE ARE OVERRIDEN IN BooleanAtom
    public Boolean isTrue() {
        return false;
    }
    public Boolean isFalse() {
        return false;
    }



}
