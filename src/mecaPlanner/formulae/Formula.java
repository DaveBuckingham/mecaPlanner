package mecaPlanner.formulae;

import mecaPlanner.state.World;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.state.KripkeStructure;

import mecaPlanner.*;


import java.util.Objects;

import java.util.Set;


public abstract class Formula{

    public abstract Object evaluate(EpistemicState state);

    public abstract Object evaluate(KripkeStructure kripke, World world);


}
