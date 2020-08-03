package mecaPlanner;

import mecaPlanner.models.Model;
import mecaPlanner.formulae.FluentAtom;
import mecaPlanner.formulae.FluentAtom;
import mecaPlanner.formulae.BeliefFormula;
import mecaPlanner.formulae.GeneralFormula;
import mecaPlanner.formulae.GeneralFormulaAnd;
import mecaPlanner.actions.Action;
import mecaPlanner.state.EpistemicState;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class Problem implements java.io.Serializable {

    private Domain domain;
    private int systemAgentIndex;
    private Set<EpistemicState> startStates;
    private Map<String, Model> startingModels;
    private Set<GeneralFormula> goals;


    public Problem(Domain domain,
                   int systemAgentIndex,
                   Set<EpistemicState> startStates,
                   Map<String,Model> startingModels,
                   Set<GeneralFormula> goals
                  ) {
        this.domain = domain;
        this.systemAgentIndex = systemAgentIndex;
        this.startStates = startStates;
        this.startingModels = startingModels;
        this.goals = goals;
    }



    public Set<EpistemicState> getStartStates() {
        return startStates;
    }

    public int getSystemAgentIndex() {
        return systemAgentIndex;
    }

    public Set<GeneralFormula> getGoals() {
        return goals;
    }

    public GeneralFormula getGoal() {
        if (goals.size() == 1) {
            return goals.iterator().next();
        }
        return new GeneralFormulaAnd(goals);
    }

    public Map<String, Model> getStartingModels() {
        return startingModels;
    }

    public Domain getDomain() {
        return domain;
    }


    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("DOMAIN:\n");
        str.append(domain);

        str.append("INITIALLY:\n");
        //str.append(startState.toString());
        str.append("\n");

        str.append("GOALS:\n");
        for (GeneralFormula g : goals) {
            str.append(g.toString());
            str.append("\n");
        }
        str.append("\n");



        return str.toString();
    }



}

