
package mecaPlanner;

import mecaPlanner.state.NDState;
import mecaPlanner.actions.Action;
import mecaPlanner.search.Perspective;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

public class Solution implements java.io.Serializable{

    private Map<Perspective, Action> actions;
    private Map<Perspective, Solution> children;
    private Problem problem;

    public Solution(Problem problem);
        this.problem = problem;
        this.actions = new HashMap<>();
        this.children = new HashMap<>();
    }

    public void addAction(Perspective p, Action a, Solution s) {
        this.actions.put(p,a);
        this.children.put(p,s);
    }

    public Action getAction(Perspective p) {
        return actions.get(p);
    }

    public Action getChild(Perspective p) {
        return children.get(p);
    }


    public String toString() {
        return listPerspectives() + printPlan(0);
    }

    public Problem getProblem() {
        return problem;
    }

    private String printPlan(int d) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i<d; i++) {
            str.append("  ");
        }
        for (Perspective p : actions.keySet()) {
            str.append(p + ": " + actions.get(p));
            children.get(p).printPlan(d+1));
        }
        return str.toString();
    }
}
