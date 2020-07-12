package mecaPlanner.formulae;

import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.World;
import mecaPlanner.agents.Agent;


import java.util.Objects;

import java.util.Set;


public class BeliefFormulaBelieves extends BeliefFormula {

    private Agent agent;
    private BeliefFormula formula;

    public BeliefFormulaBelieves(Agent agent, BeliefFormula formula) {
        this.agent = agent;
        this.formula = formula;
    }

    public Agent getAgent() {
        return this.agent;
    }

    public BeliefFormula getFormula() {
        return this.formula;
    }

    public Boolean holdsAtWorld(KripkeStructure kripke, World world) {
        assert(kripke.containsWorld(world));
        for (World w : kripke.getBelievedWorlds(agent, world)) {
            if (!formula.holdsAtWorld(kripke, w)){
                return false;
            }
        }
        return true;
    }



    public Set<FluentAtom> getAllAtoms() {
        return formula.getAllAtoms();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        BeliefFormulaBelieves otherFormula = (BeliefFormulaBelieves) obj;
        return (this.agent.equals(otherFormula.getAgent()) && this.formula.equals(otherFormula.getFormula()));
    }

     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 7;
         result = (prime * result) + ((this.agent == null) ? 0 : this.agent.hashCode());
         result = (prime * result) + ((this.formula == null) ? 0 : this.formula.hashCode());
         return result;
     }


    @Override
    public String toString() {
        return ("B[" + this.agent.getName() + "](" + this.formula + ")");
    }


}
