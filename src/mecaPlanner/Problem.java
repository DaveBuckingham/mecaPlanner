package mecaPlanner;

import mecaPlanner.agents.Agent;
import mecaPlanner.formulae.*;
import mecaPlanner.state.State;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class Problem implements java.io.Serializable {

    private Domain domain;
    private int systemAgentIndex;
    private Set<State> startStates;
    private Map<String, Agent> startingModels;
    private Set<Formula> initially;
    private Set<Formula> goals;
    private Set<TimeConstraint> timeConstraints;


    public Problem(Domain domain,
                   int systemAgentIndex,
                   Set<State> startStates,
                   Map<String,Agent> startingModels,
                   Set<Formula> initially,
                   Set<Formula> goals,
                   Set<TimeConstraint> timeConstraints
                  ) {
        this.domain = domain;
        this.systemAgentIndex = systemAgentIndex;
        this.startStates = startStates;
        this.startingModels = startingModels;
        this.initially = initially;
        this.goals = goals;
        this.timeConstraints = timeConstraints;
    }


    public Set<State> getStartStates() {
        return startStates;
    }

    public State getStartState() {
        if (startStates.size() != 1) {
            throw new RuntimeException("problem contains " + startStates.size() + " start states.");
        }
        return startStates.iterator().next();
    }

    public Integer getSystemAgentIndex() {
        return systemAgentIndex;
    }

    public Set<Formula> getInitially() {
        return initially;
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

    public Map<String, Agent> getStartingModels() {
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
        for (State s : startStates) {
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

