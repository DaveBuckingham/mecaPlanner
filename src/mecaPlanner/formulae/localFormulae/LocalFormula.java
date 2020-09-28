package mecaPlanner.formulae.localFormulae;

import mecaPlanner.formulae.beliefFormulae.BeliefFormula;

import mecaPlanner.state.World;
import mecaPlanner.state.KripkeStructure;

import mecaPlanner.*;



public abstract class LocalFormula extends BeliefFormula {

    public Boolean evaluate(KripkeStructure kripke, World world) {
        return this.evaluate(world);
    }

    public abstract Boolean evaluate(World world);

    public LocalFormula negate() {
        return LocalNotFormula.make(this);
    }


}
