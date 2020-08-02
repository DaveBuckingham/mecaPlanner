package mecaPlanner.search;

import mecaPlanner.search.GNode;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.actions.Action;
import mecaPlanner.models.Model;
import mecaPlanner.formulae.GeneralFormula;
import mecaPlanner.Domain;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public class OrNode extends GNode {

    public OrNode(EpistemicState estate,
                 GeneralFormula goal,
                 int time,
                 GNode parent,
                 Map<String, Model> models,
                 int systemAgentIndex,
                 int numAgents
                ) {

        super(estate, goal, time, parent, models, systemAgentIndex, numAgents);
    }

    // bottom out a recursive descent through and nodes
    public Set<OrNode> descend() {
        Set<OrNode> s = new HashSet<OrNode>();
        if (isGoal()) {
            return s;
        }
        if (isCycle()) {
            return null;
        }
        s.add(this);
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


