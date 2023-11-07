package mecaPlanner;

import mecaPlanner.formulae.*;
import mecaPlanner.state.AbstractState;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class Problem implements java.io.Serializable {

    private Domain domain;
    private Set<AbstractState> startStates;
    private List<Formula> initially;
    private List<Formula> goals;
    private List<TimeConstraint> timeConstraints;


    public Problem(Domain domain,
                   Set<AbstractState> startStates,
                   List<Formula> goals,
                   List<TimeConstraint> timeConstraints
                  ) {
        this.domain = domain;
        this.startStates = startStates;
        this.goals = goals;
        this.timeConstraints = timeConstraints;
    }


    public Set<AbstractState> getStartStates() {
        return startStates;
    }

    public AbstractState getStartState() {
        if (startStates.size() != 1) {
            throw new RuntimeException("problem contains " + startStates.size() + " start states.");
        }
        return startStates.iterator().next();
    }



    public List<Formula> getGoals() {
        return goals;
    }

    public List<TimeConstraint> getTimeConstraints() {
        return timeConstraints;
    }

    public Formula getGoal() {
        if (goals.size() == 1) {
            return goals.iterator().next();
        }
        return AndFormula.make(goals);
    }

    public Domain getDomain() {
        return domain;
    }


    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("DOMAIN:\n");
        str.append(domain);

        str.append("INITIALLY:\n");
        for (AbstractState s : startStates) {
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

