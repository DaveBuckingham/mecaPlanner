package mecaPlanner.formulae.integerFormulae;

import mecaPlanner.formulae.LocalFormula;

import mecaPlanner.state.World;



public abstract class IntegerFormula implements LocalFormula{

    public abstract Integer evaluate(World world);

}
