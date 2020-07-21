package mecaPlanner.search;

import mecaPlanner.search.GNode;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.actions.Action;
import mecaPlanner.models.Model;
import mecaPlanner.formulae.GeneralFormula;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public class OrNode extends GNode {

    public OrNode(EpistemicState estate, GeneralFormula goal, int time, GNode parent, Map<String, Model> models, int maxDepth) {
        super(estate, goal, time, parent, models, maxDepth);
    }

    // bottom out a recursive descent through and nodes
    public Set<OrNode> descend() {
        if (isCycle() || time == maxDepth) {
            return null;
        }
        Set<OrNode> s = new HashSet<OrNode>();
        if (!isGoal()) {
            s.add(this);
        }
        return s;
    }

    // follow the action, recursively descending through any and nodes, and return the resulting or node set
    //public Set<OrNode> orTransition(Action action) {
    //    GNode successor = transition(action);
    //    Set<OrNode> nextOrLayer = successor.descend();
    //    if (nextOrLayer == null) {
    //        return null;
    //    }
    //    return nextOrLayer;
    //}
}


