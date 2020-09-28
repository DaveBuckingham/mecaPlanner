package mecaPlanner.formulae.beliefFormulae;


import mecaPlanner.state.World;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.state.KripkeStructure;
import mecaPlanner.formulae.timeFormulae.TimeFormula;

import mecaPlanner.*;


import java.util.Objects;

import java.util.Set;


public abstract class BeliefFormula extends TimeFormula{

    public abstract Boolean evaluate(KripkeStructure kripke, World world);

    public Boolean evaluate(KripkeStructure kripke, World world, Integer timestep) {
        return this.evaluate(kripke, world);
    }

    public Boolean evaluate(EpistemicState state) {
        return evaluate(state.getKripke(), state.getDesignatedWorld());
    }


    public BeliefFormula negate() {
        return BeliefNotFormula.make(this);
    }



}
