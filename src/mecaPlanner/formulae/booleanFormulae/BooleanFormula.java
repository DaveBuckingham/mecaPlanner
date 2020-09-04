package mecaPlanner.formulae.booleanFormulae;

import mecaPlanner.formulae.beliefFormulae.BeliefFormula;

import mecaPlanner.state.World;
import mecaPlanner.state.KripkeStructure;

import mecaPlanner.*;



public abstract class BooleanFormula extends BeliefFormula {

    public Boolean evaluate(KripkeStructure kripke, World world) {
        return this.evaluate(world);
    }

    public abstract Boolean evaluate(World world);

    public BooleanFormula negate() {
        return BooleanNotFormula.make(this);
    }


}
