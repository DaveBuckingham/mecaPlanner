package mecaPlanner.formulae;

import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.World;


import java.util.Set;
import java.util.HashSet;



public class BeliefFormulaCommon extends BeliefFormula {

    private Set<String> commonAgents;
    private BeliefFormula formula;

    public BeliefFormulaCommon(Set<String> commonAgents, BeliefFormula formula) {
        this.commonAgents = commonAgents;
        this.formula = formula;
    }

    // COMMON AGENTS NOT SPECIFIED, WE'LL ASSUME ITS ALL AGENTS
    // WHICH WE WILL PULL DYNAMICALLY FROM THE STATE AT EVALUATION,
    // SEE holdsAtWorld() BELOW.
    public BeliefFormulaCommon(BeliefFormula formula) {
        this.commonAgents = new HashSet<String>();
        this.formula = formula;
    }

    public Set<String> getAgents() {
        return this.commonAgents;
    }

    public BeliefFormula getFormula() {
        return this.formula;
    }

    // THIS IS WRONG, THIS IS THE SAME AS E()
    public Boolean holdsAtWorld(KripkeStructure kripke, World world) {
        //System.out.println("WARNING: incorrectly computing 'holds' for Common belief");
        for (String agent : commonAgents) {
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
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        BeliefFormulaCommon otherFormula = (BeliefFormulaCommon) obj;
        return (this.commonAgents.equals(otherFormula.getAgents()) && this.formula.equals(otherFormula.getFormula()));
    }

     @Override
     public int hashCode() {
         int result = 7;
         result = (31 * result) + commonAgents.hashCode();
         result = (31 * result) + formula.hashCode();
         return result;
     }




    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("C[");
        if (commonAgents.size() > 0) {
            for (String agent : commonAgents) {
                str.append(agent);
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
