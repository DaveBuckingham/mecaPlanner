package mecaPlanner.formulae.integer;

import mecaPlanner.formulae.belief.BeliefFormula;

import mecaPlanner.state.World;
import mecaPlanner.state.KripkeStructure;

import mecaPlanner.*;



public abstract class IntegerFormula {

    public enum Operation {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE,
        MODULO
    }

    protected IntegerFormula lhs;
    protected IntegerFormula rhs;
    protected Operation operation;

    public abstract Integer evaluate(World world);





}
