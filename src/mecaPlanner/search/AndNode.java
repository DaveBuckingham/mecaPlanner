package mecaPlanner.search;

import mecaPlanner.search.GNode;
import mecaPlanner.state.State;
import mecaPlanner.state.Action;
import mecaPlanner.agents.Agent;
import mecaPlanner.Log;
import mecaPlanner.formulae.Formula;
import mecaPlanner.formulae.TimeConstraint;
import mecaPlanner.Domain;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public class AndNode extends GNode {

    public AndNode(State estate,
                 Formula goal,
                 List<TimeConstraint> timeConstraints,
                 int time,
                 GNode parent,
                 Domain domain
                ) {

        super(estate, goal, timeConstraints, time, parent, domain);
        // MAKE SURE ITS NOT THE SYSTEM AGENT'S TURN
        assert(!domain.isSystemAgentIndex(time));
    }

    protected Set<Action> getPossibleActions() {
        Set<Action> possibleActions = new HashSet<Action>();
        Set<Action> prediction = domain.getEnvironmentAgents().get(agent).getPrediction(estate);

        if (prediction == null) {
            throw new RuntimeException("Model returned null, indicating model failure.");
        }

        for (Action a : prediction) {
            if (!a.getActor().equals(agent)) {
                throw new RuntimeException("model returned an action for the wrong agent");
            }
        }

        Log.debug(agent + " prediction:");
        for (Action action : prediction) {
            Log.debug("  " + action.getSignature());
            if (action.executable(estate)) {
                possibleActions.add(action);
            }
        }

        if (possibleActions.isEmpty()) {
            Log.warning("Model for " + agent + " predicted no necessarily executable action.");
        }

        return possibleActions;
    }

    // starts at the first AndNode after an OrNode,
    // descends through layers of and-nodes,
    // stops when we reach an or-node layer
    public GroundSuccessors descend() {
        Set<OrNode> allOrSuccessors = new HashSet<>();
        if (isGoal()) {
            return new GroundSuccessors(time, allOrSuccessors);
        }
        if (isCycle()) {
            return null;
        }
        Integer bestDistanceToGoal = Integer.MAX_VALUE;
        for (Action action : getPossibleActions()) {
            GNode successor = transition(action);

            GroundSuccessors successors = successor.descend();

            if (successors == null) {
                return null;
            }

            Set<OrNode> orSuccessors = successors.getOrLayer();

            bestDistanceToGoal = Integer.min(bestDistanceToGoal, successors.getBestDistanceToGoal());

            allOrSuccessors.addAll(orSuccessors);
        }
        return new GroundSuccessors(bestDistanceToGoal, allOrSuccessors);
    }
}


