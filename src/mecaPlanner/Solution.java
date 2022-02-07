
package mecaPlanner;

import mecaPlanner.search.Perspective;
import mecaPlanner.state.Action;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class Solution implements java.io.Serializable{


    private Map<Perspective, Action> actions;
    private Map<Perspective, Solution> children;
    private Problem problem;

    public Solution(Problem problem) {
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

    public Solution getChild(Perspective p) {
        return children.get(p);
    }

    public boolean hasPerspective(Perspective p) {
        return actions.containsKey(p);
    }

    public Problem getProblem() {
        return problem;
    }


    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("SOLUTION PERSPECTIVE STATES:\n");
        List<Perspective> perspectives = listPerspectives();
        Map<Perspective, Integer> perspectiveNames = new HashMap<>();
        for (Integer i = 0; i < perspectives.size(); i++) {
            perspectiveNames.put(perspectives.get(i), i);
            str.append("---");
            str.append(i);
            str.append("---\n");
            str.append(perspectives.get(i));
        }
        str.append("SOLUTION ACTIONS:\n");
        str.append(printPlan(perspectiveNames, 0));
        return str.toString();
    }


    public List<Perspective> listPerspectives() {
        List<Perspective> list = new ArrayList<>();
        for (Perspective p : actions.keySet()) {
            list.add(p);
        }
        for (Solution s : children.values()) {
            list.addAll(s.listPerspectives());
        }
        return list;
    }


    private String printPlan(Map<Perspective, Integer> perspectiveNames, int d) {
        StringBuilder str = new StringBuilder();
        for (Perspective p : actions.keySet()) {
            for (int i = 0; i<d; i++) {
                str.append("  ");
            }
            str.append("[");
            str.append(perspectiveNames.get(p));
            str.append("] ");
            str.append(actions.get(p).getSignatureWithActor());
            str.append("\n");
            str.append(children.get(p).printPlan(perspectiveNames, d+1));
        }
        return str.toString();
    }





}
