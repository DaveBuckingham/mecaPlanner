package mecaPlanner.formulae.belief;

import mecaPlanner.state.World;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.state.KripkeStructure;

import mecaPlanner.*;


import java.util.Objects;

import java.util.Set;


public abstract class BeliefFormula{

    public final Boolean evaluate(EpistemicState state) {
        return holdsAtWorld(state.getKripke(), state.getDesignatedWorld());
    }

    public abstract Boolean evaluate(KripkeStructure kripke, World world);


}
