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
                 Domain domain,
                 int maxDepth
                ) {

        super(estate, goal, timeConstraints, time, parent, domain, maxDepth);
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
    public OrLayer descend() {
        if (isGoal()) {
            return new OrLayer(time,maxDepth,domain);
        }
        if (isCycle()) {
            Log.debug("cycle");
            return null;
        }
        OrLayer allSuccessors = new OrLayer(maxDepth,domain);
        for (Action action : getPossibleActions()) {
            GNode successor = transition(action);

            OrLayer successors = successor.descend();

            if (successors == null) {
                return null;
            }

            allSuccessors.merge(successors);

        }
        return allSuccessors;
    }
}


