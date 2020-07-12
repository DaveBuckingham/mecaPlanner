package mecaPlanner.formulae;

import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.World;
import mecaPlanner.agents.Agent;


import java.util.Set;
import java.util.HashSet;



public class BeliefFormulaCommon extends BeliefFormula {

    private Set<Agent> commonAgents;
    private BeliefFormula formula;

    public BeliefFormulaCommon(Set<Agent> commonAgents, BeliefFormula formula) {
        this.commonAgents = commonAgents;
        this.formula = formula;
    }

    // COMMON AGENTS NOT SPECIFIED, WE'LL ASSUME ITS ALL AGENTS
    // WHICH WE WILL PULL DYNAMICALLY FROM THE STATE AT EVALUATION,
    // SEE holdsAtWorld() BELOW.
    public BeliefFormulaCommon(BeliefFormula formula) {
        this.commonAgents = new HashSet<Agent>();
        this.formula = formula;
    }

    public Set<Agent> getAgents() {
        return this.commonAgents;
    }

    public BeliefFormula getFormula() {
        return this.formula;
    }

    // THIS IS WRONG, THIS IS THE SAME AS E()
    public Boolean holdsAtWorld(KripkeStructure kripke, World world) {
        //System.out.println("WARNING: incorrectly computing 'holds' for Common belief");
        for (Agent agent : commonAgents) {
            BeliefFormulaBelieves believes = new BeliefFormulaBelieves(agent, formula);
            if (!believes.holdsAtWorld(kripke, world)) {
                return false;
            }
        }
        return true;
    }

    public Set<FluentAtom> getAllAtoms() {
        return formula.getAllAtoms();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("C[");
        if (commonAgents.size() > 0) {
            for (Agent agent : commonAgents) {
                str.append(agent.getName());
                str.append(",");
            }
            str.deleteCharAt(str.length() - 1);
        }
        str.append("](");
        str.append(this.formula);
        str.append(")");
        return str.toString();
    }


}
