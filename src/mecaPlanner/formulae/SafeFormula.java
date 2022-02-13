package mecaPlanner.formulae;

import mecaPlanner.state.NDState;
import mecaPlanner.state.World;


import java.util.Objects;

import java.util.Set;


public class SafeFormula extends Formula {

    private String agent;
    private Formula formula;

    public SafeFormula(String agent, Formula formula) {
        this.agent = agent;
        this.formula = formula;
    }

    public String getAgent() {
        return this.agent;
    }

    public Formula getFormula() {
        return this.formula;
    }

    public Integer getHeight() {
        return formula.getHeight() + 1;
    }

    public Boolean isFalse() {
        return false;
    }
    public Boolean isTrue() {
        return false;
    }

    public Set<Fluent> getAllFluents() {
        return formula.getAllFluents();
    }

    public Boolean evaluate(World world) {
        throw new RuntimeException("Can't evaluate modal formula without a model");
    }

    public Boolean evaluate(NDState model, World world) {
        if (model == null) {
            throw new RuntimeException("Can't evaluate modal formula without a model");
        }
        assert(model.getWorlds().contains(world));
        for (World w : model.getMorePlausible(agent, world)) {
            if (!formula.evaluate(model, w)){
                return false;
            }
        }
        return true;
    }


    public Formula negate() {
        return NotFormula.make(this);
    }


    //public Set<FluentAtom> getAllAtoms() {
    //    return formula.getAllAtoms();
    //}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        SafeFormula otherFormula = (SafeFormula) obj;
        return (this.agent.equals(otherFormula.getAgent()) && this.formula.equals(otherFormula.getFormula()));
    }

     @Override
     public int hashCode() {
         int result = 11;
         result = (31 * result) + agent.hashCode();
         result = (31 * result) + formula.hashCode();
         return result;
     }


    @Override
    public String toString() {
        return ("B[" + this.agent + "](" + this.formula + ")");
    }


}
