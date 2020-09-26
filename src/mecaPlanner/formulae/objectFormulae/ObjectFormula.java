package mecaPlanner.formulae.objectFormulae;

import mecaPlanner.formulae.LocalFormula;

import mecaPlanner.state.World;


public abstract class ObjectFormula implements LocalFormula{

    public abstract String evaluate(World world);

}
