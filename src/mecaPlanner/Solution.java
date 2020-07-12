
package mecaPlanner;

import mecaPlanner.state.NDState;
import mecaPlanner.actions.Action;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class Solution implements java.io.Serializable{

    private static int idCounter = 0;

    private Perspective perspective;
    private Action action;
    private Set<Solution> children;
    private int id;
    private int time;

    public Solution(Perspective perspective, Action action, int time) {
        this.perspective = perspective;
        this.action = action;
        this.time = time;
        this.children = new HashSet<Solution>();
        this.id = Solution.idCounter;
        Solution.idCounter += 1;
    }

    public void addChild(Solution s) {
        this.children.add(s);
    }

    public String toString() {
        return listPerspectives() + printPlan(0);
    }

    public Perspective getPerspective() {
        return perspective;
    }

    public Action getAction() {
        return action;
    }

    public Set<Solution> getChildren() {
        return children;
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
