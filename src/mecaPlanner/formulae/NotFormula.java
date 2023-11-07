package mecaPlanner.formulae;


import mecaPlanner.state.AbstractState;
import mecaPlanner.state.World;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import java.util.Set;

public class NotFormula extends Formula{

    private Formula formula;


    private NotFormula(Formula formula) {
        this.formula = formula;
    }

    public static Formula make(Formula inputFormula) {
        if (inputFormula.isTrue()) {
            return new Literal(false);
        }
        if (inputFormula.isFalse()) {
            return new Literal(true);
        }
        if (inputFormula instanceof NotFormula) {
            return ((NotFormula) inputFormula).getFormula();
        }
        return new NotFormula(inputFormula);
    }

    public Formula getFormula() {
        return formula;
    }

    public Set<Fluent> getAllFluents() {
        return formula.getAllFluents();
    }

    public Boolean evaluate(World world) {
        return (!formula.evaluate(world));
    }

    public Boolean evaluate(AbstractState model, World world) {
        return (!formula.evaluate(model, world));
    }

    public Integer getHeight() {
        return formula.getHeight();
    }

    @Override
    public Formula negate() {
        return formula;
    }

    public Boolean isFalse() {
        return false;
    }
    public Boolean isTrue() {
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
        NotFormula other = (NotFormula) obj;
        return formula.equals(other.getFormula());
    }


    @Override
    public int hashCode() {
        return formula.hashCode() * 7;
    }




    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("~(");
        str.append(this.formula);
        str.append(")");
        return str.toString();
    }

}
