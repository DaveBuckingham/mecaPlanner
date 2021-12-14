package mecaPlanner.formulae;

import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.World;


import java.util.Set;
import java.util.HashSet;



public class CommonFormula extends Formula {

    private Set<String> commonAgents;
    private Formula formula;

    public CommonFormula(Set<String> commonAgents, Formula formula) {
        this.commonAgents = commonAgents;
        this.formula = formula;
    }

    // COMMON AGENTS NOT SPECIFIED, WE'LL ASSUME ITS ALL AGENTS
    // WHICH WE WILL PULL DYNAMICALLY FROM THE STATE AT EVALUATION,
    // SEE evaluate() BELOW.
    public CommonFormula(Formula formula) {
        this.commonAgents = new HashSet<String>();
        this.formula = formula;
    }

    public Integer getHeight() {
        return formula.getHeight() + 1;
    }

    public Set<String> getAgents() {
        return this.commonAgents;
    }

    public Formula getFormula() {
        return this.formula;
    }

    public Boolean isFalse() {
        return false;
    }
    public Boolean isTrue() {
        return false;
    }

    // THIS IS WRONG, THIS IS THE SAME AS E()
    public Boolean evaluate(KripkeStructure kripke, World world) {
        //System.out.println("WARNING: incorrectly computing 'evaluate' for Common belief");
        if (kripke == null) {
            throw new RuntimeException("Can't evaluate modal formula without a model");
        }
        for (String agent : commonAgents) {
            BelievesFormula believes = new BelievesFormula(agent, formula);
            if (!believes.evaluate(kripke, world)) {
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
        CommonFormula otherFormula = (CommonFormula) obj;
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
