package mecaPlanner.formulae.localFormulae;

import mecaPlanner.state.World;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import java.util.Set;

public class LocalNotFormula extends LocalFormula{

    private LocalFormula formula;

    private LocalNotFormula(LocalFormula formula) {
        this.formula = formula;
    }

    public static LocalFormula make(LocalFormula inputFormula) {
        if (inputFormula.isTrue()) {
            return new Literal(false);
        }
        if (inputFormula.isFalse()) {
            return new Literal(true);
        }
        if (inputFormula instanceof LocalNotFormula) {
            return ((LocalNotFormula) inputFormula).getFormula();
        }
        return new LocalNotFormula(inputFormula);
    }

    public LocalFormula getFormula() {
        return formula;
    }

    public Boolean evaluate(World world) {
        return !formula.evaluate(world);
    }

    public LocalFormula negate() {
        return formula;
    }

    //public Set<FluentAtom> getAllAtoms() {
    //    return formula.getAllAtoms();
    //}



    @Override
    public String toString() {
        return ("!(" + formula.toString() + ")");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        LocalNotFormula other = (LocalNotFormula) obj;
        return formula.equals(other.getFormula());
    }


     @Override
     public int hashCode() {
         return formula.hashCode() * 11;
     }




}
