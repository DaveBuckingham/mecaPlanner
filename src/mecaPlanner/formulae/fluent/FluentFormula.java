package mecaPlanner.formulae.fluent;

import mecaPlanner.formulae.belief.BeliefFormula;

import mecaPlanner.state.World;
import mecaPlanner.state.KripkeStructure;

import mecaPlanner.*;



public abstract class FluentFormula extends BeliefFormula {

    public abstract Boolean holds(World world);

    public final Boolean holdsAtWorld(KripkeStructure kripke, World world) {
        return this.holds(world);
    }

    //public static buildConjunction(Set<FluentFormula> parts) {
    //    Set<FluentFormula> conjuncts = new HashSet<>();
    //    for (FluentF
    //}

    //public static Set<FluentFormula> breakConjunction(FluentFormula conjunction) {
    //    if (conjunction instanceof FluentFormu

    //}


}
