package mecaPlanner.search;

import mecaPlanner.search.GNode;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.actions.Action;
import mecaPlanner.models.Model;
import mecaPlanner.Log;
import mecaPlanner.formulae.GeneralFormula;
import mecaPlanner.Domain;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public class AndNode extends GNode {

    public AndNode(EpistemicState estate,
                 GeneralFormula goal,
                 int time,
                 GNode parent,
                 Map<String, Model> models,
                 int systemAgentIndex,
                 Domain domain
                ) {

        super(estate, goal, time, parent, models, systemAgentIndex, domain);
    }

    protected Set<Action> getPossibleActions() {
        Set<Action> possibleActions = new HashSet<Action>();
        Set<Action> prediction = models.get(agent).getPrediction(estate.getBeliefPerspective(agent));

        if (prediction == null) {
            throw new RuntimeException("Model returned null, indicating model failure.");
        }

        Log.debug(agent + " prediction:");
        for (Action action : prediction) {
            Log.debug("  " + action.getSignature());
            if (action.executable(estate)) {
                possibleActions.add(action);
            }
        }

        if (possibleActions.isEmpty()) {
            Log.warning("Model for " + agent + "predicted no necessarily executable action.");
        }

        return possibleActions;
    }

    // descending through layers of and-nodes, stops when we reach an or-node layer
    public Set<OrNode> descend() {
        Set<OrNode> allOrSuccessors = new HashSet<>();
        if (isGoal()) {
            return allOrSuccessors;
        }
        if (isCycle()) {
            return null;
        }
        for (Action action : getPossibleActions()) {
            GNode successor = transition(action);
            Set<OrNode> orSuccessors = successor.descend();
            if (orSuccessors == null) {
                return null;
            }
            allOrSuccessors.addAll(orSuccessors);
        }
        return allOrSuccessors;
    }
}


