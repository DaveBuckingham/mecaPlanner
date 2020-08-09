package mecaPlanner.formulae;

import mecaPlanner.state.World;
import mecaPlanner.state.KripkeStructure;

import mecaPlanner.*;



public abstract class FluentFormula extends BeliefFormula {

    public abstract Boolean holds(World world);

    public abstract Boolean alwaysHolds();

    public final Boolean holdsAtWorld(KripkeStructure kripke, World world) {
        return this.holds(world);
    }

    public FluentFormula negate() {
        return new FluentFormulaNot(this);
    }


}
