package mecaPlanner.formulae;

import mecaPlanner.state.EpistemicState;

import java.util.Set;
import java.util.HashSet;



public class TimeFormula extends GeneralFormula {

    public enum Inequality {
        EQ,            // equals
        NE,            // not equal
        LT,            // less than 
        LTE,           // less than or equal
        GT,            // greater than
        GTE            // greater than or equal
    }

    private Inequality inequality;
    private Integer value;

    public TimeFormula(Inequality inequality, Integer value) {
        this.inequality = inequality;
        this.value = value;
    }

    public Inequality getInequality() {
        return inequality;
    }

    public Integer getValue() {
        return value;
    }

    public Boolean holds(EpistemicState state, Integer timestep) {
        // System.out.print("EVALUATING: ");
        // System.out.print(timestep);
        // System.out.print(toString());
        // System.out.print(" -> ");
        // System.out.print(holds(timestep));
        // System.out.print("\n");
        return holds(timestep);
    }

    public Boolean holds(Integer timestep) {
        switch (this.inequality) {
            case EQ:
                return timestep == this.value;
            case NE:
                return timestep != this.value;
            case LT:
                return timestep < this.value;
            case LTE:
                return timestep <= this.value;
            case GT:
                return timestep > this.value;
            case GTE:
                return timestep >= this.value;
            default:
                throw new RuntimeException("invalide inequality: " + this.inequality);
        }
    }

    public Set<FluentAtom> getAllAtoms() {
        return new HashSet<FluentAtom>();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        switch (this.inequality) {
            case EQ:
                str.append("==");
                break;
            case NE:
                str.append("!=");
                break;
            case LT:
                str.append("<");
                break;
            case LTE:
                str.append("<=");
                break;
            case GT:
                str.append(">");
                break;
            case GTE:
                str.append(">=");
                break;
            default:
                throw new RuntimeException("invalide inequality: " + this.inequality);
        }
        str.append(value.toString());
        return str.toString();
    }



}
