package mecaPlanner.formulae;

import mecaPlanner.state.World;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.state.KripkeStructure;

import mecaPlanner.*;


import java.util.Objects;

import java.util.Set;


public abstract class BeliefFormula extends GeneralFormula{

    public Boolean holds(EpistemicState state, Integer timestep) {
        return holdsAtWorld(state.getKripke(), state.getDesignatedWorld());
    }

    public final Boolean holds(EpistemicState state) {
        return holdsAtWorld(state.getKripke(), state.getDesignatedWorld());
    }

    public abstract Boolean holdsAtWorld(KripkeStructure kripke, World world);


}
