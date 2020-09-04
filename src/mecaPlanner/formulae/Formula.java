package mecaPlanner.formulae;

import mecaPlanner.state.World;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.state.KripkeStructure;

import mecaPlanner.*;


import java.util.Objects;

import java.util.Set;


public abstract class Formula{

    //public abstract Object evaluate(EpistemicState state);

    //public abstract Object evaluate(KripkeStructure kripke, World world);

    public Integer getIntegerValue() {
        throw new RuntimeException("attempted to get integer value of non-integer-atomic formula");
    }

    public Boolean getBooleanValue() {
        throw new RuntimeException("attempted to get boolean value of non-boolean-atomic formula");
    }

    public String getObjectValue() {
        throw new RuntimeException("attempted to get object value of non-object-atomic formula");
    }


}
