
package mecaPlanner;

import mecaPlanner.state.NDState;
import mecaPlanner.actions.Action;
import mecaPlanner.search.Perspective;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class Solution implements java.io.Serializable{

    private static int idCounter = 0;

    private Map<Perspective, Action> actions;
    private Map<Perspective, Solution> children;
    private Problem problem;
    private int id;

    public Solution(Problem problem) {
        this.problem = problem;
        this.actions = new HashMap<>();
        this.children = new HashMap<>();
        this.id = Solution.idCounter;
        Solution.idCounter += 1;
    }

    public void addAction(Perspective p, Action a, Solution s) {
        this.actions.put(p,a);
        this.children.put(p,s);
    }

    public Action getAction(Perspective p) {
        return actions.get(p);
    }

    public Solution getChild(Perspective p) {
        return children.get(p);
    }


    public String toString() {
        return listPerspectives() + printPlan(0);
    }

    public Problem getProblem() {
        return problem;
    }

    private String listPerspectives() {
        StringBuilder str = new StringBuilder();
        str.append("[");
        str.append(id);
        str.append("] ");
        str.append("t=");
        str.append(time);
        str.append(" \n");
        str.append(perspective);
        str.append("\n");
        for (Solution s : children) {
            str.append(s.listPerspectives());
        }
        return str.toString();
    }



    private String printPlan(int d) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i<d; i++) {
            str.append("  ");
        }
        for (Perspective p : actions.keySet()) {
            str.append(p + ": " + actions.get(p).getSignatureWithActor());
            str.append("\n");
            str.append(children.get(p).printPlan(d+1));
        }
        return str.toString();
    }


    private String printPlan(int d) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i<d; i++) {
            str.append("  ");
        }
        str.append("[");
        str.append(id);
        str.append("] ");
        str.append(action.getSignatureWithActor());
        str.append("\n");
        for (Solution s : children) {
            str.append(s.printPlan(d+1));
        }
        return str.toString();
    }







}
