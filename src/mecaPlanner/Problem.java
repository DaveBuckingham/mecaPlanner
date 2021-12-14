package mecaPlanner;

import mecaPlanner.models.Model;
import mecaPlanner.formulae.*;
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
    private Set<Formula> goals;
    private Set<TimeConstraint> timeConstraints;


    public Problem(Domain domain,
                   int systemAgentIndex,
                   Set<EpistemicState> startStates,
                   Map<String,Model> startingModels,
                   Set<Formula> goals,
                   Set<TimeConstraint> timeConstraints
                  ) {
        this.domain = domain;
        this.systemAgentIndex = systemAgentIndex;
        this.startStates = startStates;
        this.startingModels = startingModels;
        this.goals = goals;
        this.timeConstraints = timeConstraints;
    }


    public Set<EpistemicState> getStartStates() {
        return startStates;
    }

    public EpistemicState getStartState() {
        if (startStates.size() != 1) {
            throw new RuntimeException("problem contains " + startStates.size() + " start states.");
        }
        return startStates.iterator().next();
    }

    public Integer getSystemAgentIndex() {
        return systemAgentIndex;
    }

    public Set<Formula> getGoals() {
        return goals;
    }

    public Set<TimeConstraint> getTimeConstraints() {
        return timeConstraints;
    }

    public Formula getGoal() {
        if (goals.size() == 1) {
            return goals.iterator().next();
        }
        return AndFormula.make(goals);
    }

    public Map<String, Model> getStartingModels() {
        return startingModels;
    }

    public Domain getDomain() {
        return domain;
    }


    public String toString() {
        StringBuilder str = new StringBuilder();

        //str.append("DOMAIN:\n");
        //str.append(domain);

        str.append("INITIALLY:\n");
        for (EpistemicState s : startStates) {
            str.append(s.toString());
        }
        str.append("\n");

        str.append("GOALS:\n");
        for (Formula g : goals) {
            str.append(g.toString());
            str.append("\n");
        }
        for (TimeConstraint t : timeConstraints) {
            str.append(t.toString());
            str.append("\n");
        }
        str.append("\n");



        return str.toString();
    }



}

