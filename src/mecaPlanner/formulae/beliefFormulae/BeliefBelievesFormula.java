package mecaPlanner.formulae.beliefFormulae;

import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.World;


import java.util.Objects;

import java.util.Set;


public class BeliefBelievesFormula extends BeliefFormula {

    private String agent;
    private BeliefFormula formula;

    public BeliefBelievesFormula(String agent, BeliefFormula formula) {
        this.agent = agent;
        this.formula = formula;
    }

    public String getAgent() {
        return this.agent;
    }

    public BeliefFormula getFormula() {
        return this.formula;
    }

    public Boolean evaluate(KripkeStructure kripke, World world) {
        assert(kripke.containsWorld(world));
        for (World w : kripke.getBelievedWorlds(agent, world)) {
            if (!formula.evaluate(kripke, w)){
                return false;
            }
        }
        return true;
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
        BeliefBelievesFormula otherFormula = (BeliefBelievesFormula) obj;
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
