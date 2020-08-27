package mecaPlanner.formulae.belief;

import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.World;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import java.util.Set;

public class BeliefFormulaNot extends BeliefFormula{

    private BeliefFormula formula;


    private BeliefFormulaNot(BeliefFormula formula) {
        this.formula = formula;
    }

    publics static BeliefFormula make(BeliefFormula inputFormula) {
        if (inputFormula instanceof BooleanValue) {
            return ((BooleanValue) simplified).negate();
        }
        if (inputFormula instanceof BeliefFormulaNot) {
            return ((BeliefFormulaNot) inputFormula).getFormula();
        }
        return new BeliefFormulaNot(newFormula);
    }





    public Boolean holdsAtWorld(KripkeStructure kripke, World world) {
        return (!formula.holdsAtWorld(kripke, world));
    }

    //public Set<FluentAtom> getAllAtoms() {
    //    return formula.getAllAtoms();
    //}

    @Override
    public BeliefFormula negate() {
        return formula;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        BeliefFormulaNot other = (BeliefFormulaNot) obj;
        return formula.equals(other.getFormula());
    }


    @Override
    public int hashCode() {
        return formula.hashCode() * 7;
    }




    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("not(");
        str.append(this.formula);
        str.append(")");
        return str.toString();
    }

}
