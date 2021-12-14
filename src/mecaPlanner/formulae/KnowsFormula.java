package mecaPlanner.formulae;

import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.World;


import java.util.Objects;

import java.util.Set;


public class KnowsFormula extends Formula {

    private String agent;
    private Formula formula;

    public KnowsFormula(String agent, Formula formula) {
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

    public Set<Fluent> getAllFluents() {
        return formula.getAllFluents();
    }

    public Boolean evaluate(KripkeStructure kripke, World world) {
        if (kripke == null) {
            throw new RuntimeException("Can't evaluate modal formula without a model");
        }
        for (World w : kripke.getKnownWorlds(agent, world)) {
            if (!formula.evaluate(kripke, w)){
                return false;
            }
        }
        return true;
    }

    public Formula negate() {
        return NotFormula.make(this);
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
        KnowsFormula otherFormula = (KnowsFormula) obj;
        return (this.agent.equals(otherFormula.getAgent()) && this.formula.equals(otherFormula.getFormula()));
    }

     @Override
     public int hashCode() {
         int result = 7;
         result = (31 * result) + agent.hashCode();
         result = (31 * result) + formula.hashCode();
         return result;
     }


    @Override
    public String toString() {
        return ("K[" + this.agent + "](" + this.formula + ")");
    }


}
