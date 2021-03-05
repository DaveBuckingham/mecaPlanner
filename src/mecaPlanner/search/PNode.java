package mecaPlanner.search;

import mecaPlanner.Action;
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
    private Integer time;       // DEPTH OF ALL NODES, I.E. ALL ACTIONS
    private int depth;      // OF P-NODES, I.E. NUM SYSTEM AGENT ACTIONS
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

    public Integer getTime() {
        return time;
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
        Integer bestBestCaseDepth = Integer.MAX_VALUE;
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
                if (bestCaseDepthForAction < bestBestCaseDepth) {
                    successfulAction = action;
                    bestBestCaseDepth = bestCaseDepthForAction;
                }
            }
        }
        return (successfulAction != null);
    }
    // returns null if transition fails due to cycles or depth limit
    // returns empty set if found goal
    private PerspectiveSuccessors pTransition(Action action) {
        Map<Perspective, Set<OrNode>> successorPerspectives = new HashMap<>();
        Integer bestCaseDepth = Integer.MAX_VALUE;
        for (OrNode ground : grounds ){

            //Set<OrNode> gSuccessors = ground.transition(action).descend();

            GroundSuccessors successors = ground.transition(action).descend();
            Set<OrNode> gSuccessors = sucessors.getOrLayer();

            Integer.min bestCaseDepth = successors.getBestCaseDepth();
            bestCaseDepth = Integer.min(bestCaseDepth, successors.getBestCaseDepth());

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
            int newTime = time + domain.getNonPassiveAgents().size();
            successorNodes.add(new PNode(entry.getKey(), entry.getValue(), newTime, depth+1, domain));
        }
        return new PerspectiveSuccessors(bestCaseDepth, successorNodes);
    }
}


