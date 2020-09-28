package mecaPlanner.formulae.timeFormulae;

import mecaPlanner.state.*;

import mecaPlanner.*;


import java.util.Objects;

import java.util.Set;


public abstract class TimeFormula implements java.io.Serializable {

    public abstract Boolean evaluate(KripkeStructure kripke, World world, Integer timestep);

    public Boolean evaluate(EpistemicState state, Integer timestep) {
        return evaluate(state.getKripke(), state.getDesignatedWorld(), timestep);
    }

    // THESE ARE OVERRIDEN IN localValue.Literal
    public Boolean isTrue() {
        return false;
    }
    public Boolean isFalse() {
        return false;
    }



}
