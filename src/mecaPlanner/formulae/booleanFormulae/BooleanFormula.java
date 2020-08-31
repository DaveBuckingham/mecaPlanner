package mecaPlanner.formulae;
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


    // THESE ARE OVERRIDEN IN BooleanAtom
    public Boolean isTrue() {
        return False;
    }
    public Boolean isFalse() {
        return False;
    }


}
