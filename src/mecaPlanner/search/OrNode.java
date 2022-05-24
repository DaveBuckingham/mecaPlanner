package mecaPlanner.search;

import mecaPlanner.state.State;
import mecaPlanner.state.Action;
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

    public OrNode(State estate,
                 Formula goal,
                 List<TimeConstraint> timeConstraints,
                 int time,
                 GNode parent,
                 Domain domain
                ) {

        super(estate, goal, timeConstraints, time, parent, domain);
    }

    // BOTTOM OUT A RECURSIVE DESCENT THROUGH AND NODES
    public GroundSuccessors descend() {
        Set<OrNode> s = new HashSet<OrNode>();
        if (isGoal()) {
            //Log.trace("goal\n");
            return new GroundSuccessors(time, s);
        }
        if (isCycle()) {
            //Log.trace("cycle\n");
            return null;
        }
        s.add(this);
        return new GroundSuccessors(Integer.MAX_VALUE, s);
    }

}


