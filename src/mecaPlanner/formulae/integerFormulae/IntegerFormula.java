package mecaPlanner.formulae.integerFormulae;

import mecaPlanner.formulae.Formula;

import mecaPlanner.state.World;
import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.EpistemicState;

import mecaPlanner.*;



public abstract class IntegerFormula extends Formula{

//    public enum Operation {
//        ADD,
//        SUBTRACT,
//        MULTIPLY,
//        DIVIDE,
//        MODULO
//    }

    private IntegerFormula lhs;
    private IntegerFormula rhs;

    public Integer getLhs() {
        return lhs;
    }

    public Integer getRhs() {
        return rhs;
    }

    public String evaluate(EpistemicState state) {
        return evaluate(state.getDesignatedWorld());
    }

    public String evaluate(KripkeStructure kripke, World world) {
        return evaluate(world);
    }

    public abstract Integer evaluate(World world);

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
