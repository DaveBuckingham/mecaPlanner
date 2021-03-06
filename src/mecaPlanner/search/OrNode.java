package mecaPlanner.search;

import mecaPlanner.state.EpistemicState;
import mecaPlanner.Action;
import mecaPlanner.models.Model;
import mecaPlanner.formulae.Formula;
import mecaPlanner.formulae.TimeConstraint;
import mecaPlanner.Domain;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public class OrNode extends GNode {

    public OrNode(EpistemicState estate,
                 Formula goal,
                 Set<TimeConstraint> timeConstraints,
                 int time,
                 GNode parent,
                 Map<String, Model> models,
                 int systemAgentIndex,
                 Domain domain
                ) {

        super(estate, goal, timeConstraints, time, parent, models, systemAgentIndex, domain);
    }

    // BOTTOM OUT A RECURSIVE DESCENT THROUGH AND NODES
    public GroundSuccessors descend() {
        Set<OrNode> s = new HashSet<OrNode>();
        if (isGoal()) {
            return new GroundSuccessors(time, s);
        }
        if (isCycle()) {
            return null;
        }
        s.add(this);
        return new GroundSuccessors(Integer.MAX_VALUE, s);
    }

}


