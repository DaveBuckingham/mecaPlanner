package mecaPlanner.search;

import mecaPlanner.actions.Action;
import mecaPlanner.Domain;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;


public class PNode  {
    private Action successfulAction;
    private Perspective perspective;
    private Set<OrNode> grounds;
    private Set<PNode> successors;
    private int time;
    private int depth;
    private Domain domain;



    public PNode(Perspective perspective, Set<OrNode> grounds, int time, int depth, Domain domain) {
        successfulAction = null;
        this.perspective = perspective;
        this.grounds = grounds;
        this.time = time;
        this.depth = depth;
        this.domain = domain;
    }

    public Perspective getPerspective() {
        return perspective;
    }

    public Action getAction() {
        return successfulAction;
    }

    public Set<PNode> getSuccessors() {
        return successors;
    }

    public int getTime() {
        return time;
    }

    private int nextSystemAgentTime() {
        int i = time + 1;
        while (domain.isEnvironmentAgent(i)) {
            i += 1;
        }
        return i;
    }

    private Set<Action> getPossibleActions() {
        Set<Action> possibleActions = new HashSet<>();
        for (Action action : domain.getAgentActions(time)) {
            boolean safe = true;
            for (OrNode ground : grounds) {
                if (!action.executable(ground.getState())) {
                    safe = false;
                    break;
                }
            }
            if (safe) {
                possibleActions.add(action);
            }
        }
        return possibleActions;
    }

    public boolean evaluate(int maxDepth) {
        if (depth > maxDepth) {
            return false;
        }
        for (Action action : getPossibleActions()) {
            successors = pTransition(action);
            if (successors == null) {
                continue;
            }
            boolean failedSuccessor = false;
            for (PNode successor : successors) {
                if (!successor.evaluate(maxDepth)) {
                    failedSuccessor = true;
                    break;
                }
            }
            if (!failedSuccessor) {
                successfulAction = action;
                return true;
            }
        }
        return false;
    }
    // returns null if transition fails due to cycles or depth limit
    // returns empty set if found goal
    private Set<PNode> pTransition(Action action) {
        Map<Perspective, Set<OrNode>> successorPerspectives = new HashMap<>();
        for (OrNode ground : grounds ){

            Set<OrNode> gSuccessors = ground.transition(action, domain).descend();

            if (gSuccessors == null) {
                return null;
            }
            for (OrNode gSuccessor : gSuccessors) {
                Perspective successorPerspective = new Perspective(gSuccessor.getState(), gSuccessor.getAgent());
                if (!successorPerspectives.containsKey(successorPerspective)) {
                    successorPerspectives.put(successorPerspective, new HashSet<OrNode>());
                }
                successorPerspectives.get(successorPerspective).add(gSuccessor);
            }
        }
        
        Set<PNode> successorNodes = new HashSet<>();
        for (Map.Entry<Perspective, Set<OrNode>> entry : successorPerspectives.entrySet()) {
            successorNodes.add(new PNode(entry.getKey(), entry.getValue(), nextSystemAgentTime(), depth+1, domain));
        }
        return successorNodes;
    }
}


