package mecaPlanner.search;

import mecaPlanner.state.AbstractState;
import mecaPlanner.actions.Action;
import mecaPlanner.agents.Agent;
import mecaPlanner.formulae.Formula;
import mecaPlanner.formulae.TimeConstraint;
import mecaPlanner.Domain;
import mecaPlanner.Log;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public class OrNode extends GNode {

    public OrNode(AbstractState estate,
                 Formula goal,
                 List<TimeConstraint> timeConstraints,
                 int time,
                 GNode parent,
                 Domain domain,
                 int maxDepth
                ) {

        super(estate, goal, timeConstraints, time, parent, domain,maxDepth);
    }

    // BOTTOM OUT A RECURSIVE DESCENT THROUGH AND NODES
    public OrLayer descend() {
        Set<OrNode> s = new HashSet<OrNode>();
        if (isGoal()) {
            Log.debug("goal\n");
            //return new OrLayer(time, s);
            return new OrLayer(time, maxDepth,domain);
        }
        if (isCycle()) {
            //Log.debug("cycle");
            return null;
        }
        return new OrLayer(time, this, maxDepth,domain);
    }

}


