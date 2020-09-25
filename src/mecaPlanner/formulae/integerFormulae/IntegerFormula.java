package mecaPlanner.formulae.integerFormulae;

import mecaPlanner.formulae.Formula;

import mecaPlanner.state.World;
import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.EpistemicState;

import mecaPlanner.*;



public abstract class IntegerFormula implements LocalFormula{

//    public enum Operation {
//        ADD,
//        SUBTRACT,
//        MULTIPLY,
//        DIVIDE,
//        MODULO
//    }

    protected IntegerFormula lhs;
    protected IntegerFormula rhs;

    public IntegerFormula getLhs() {
        return lhs;
    }

    public IntegerFormula getRhs() {
        return rhs;
    }

    public Integer evaluate(EpistemicState state) {
        return evaluate(state.getDesignatedWorld());
    }
 
    public Integer evaluate(KripkeStructure kripke, World world) {
        return evaluate(world);
    }

    public abstract Integer evaluate(World world);

    public Integer getLiteral() {
        return null;
    }

    public Boolean isLiteral() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        IntegerFormula other = (IntegerFormula) obj;
        return (lhs.equals(other.getLhs()) && rhs.equals(other.getRhs()));
    }

    @Override
    public int hashCode() {
        return (lhs.hashCode() * rhs.hashCode());
    }




}
