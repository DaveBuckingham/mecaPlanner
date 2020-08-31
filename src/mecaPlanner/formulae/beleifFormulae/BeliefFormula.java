package mecaPlanner.formulae;
package mecaPlanner.formulae.belief;

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


}
