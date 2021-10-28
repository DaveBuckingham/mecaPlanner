package mecaPlanner.search;

import mecaPlanner.Action;
import mecaPlanner.Domain;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import java.util.concurrent.RecursiveTask;


public class PNode extends RecursiveTask<Integer> {
    private Action successfulAction;
    private Perspective perspective;
    private Set<OrNode> grounds;
    private Set<PNode> successors;
    private Integer time;       // DEPTH OF ALL NODES, I.E. ALL ACTIONS
    private int depth;          // OF P-NODES, I.E. NUM SYSTEM AGENT ACTIONS
    private int maxDepth;
    private Domain domain;



    public PNode(Perspective perspective, Set<OrNode> grounds, int time, int depth, int maxDepth, Domain domain) {
        successfulAction = null;
        this.perspective = perspective;
        this.grounds = grounds;
        this.time = time;
        this.depth = depth;
        this.maxDepth = maxDepth;
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

    public PerspectiveSuccessors evaluate(Action action) {
        PerspectiveSuccessors successorsWithScore = pTransition(action);
        if (successorsWithScore == null) {
            return null;
        }
        Set<PNode> potentialSuccessors = successorsWithScore.getPLayer();

        Integer best = successorsWithScore.getBestCaseDepth();



        // NO MULTITHREADING
        for (PNode successor : potentialSuccessors) {
            Integer successorsBest = successor.expand();
            if (successorsBest == Integer.MAX_VALUE) {
                return null;
            }
            best = Integer.min(best, successorsBest);
        }



        // WITH MULTITHREADING
//        for (PNode successor : potentialSuccessors) {
//            successor.fork();
//        }
//        boolean failed = false;
//        for (PNode successor : potentialSuccessors) {
//            Integer successorsBest = successor.join();
//            if (successorsBest == Integer.MAX_VALUE) {
//                failed = true;
//            }
//            best = Integer.min(best, successorsBest);
//        }
//        if (failed) {
//            return null;
//        }


        
        return new PerspectiveSuccessors(best, potentialSuccessors);
    }

    public Integer compute() {
        return expand();
    }

    public Integer expand() {
        if (depth > maxDepth) {
            return Integer.MAX_VALUE;
        }
        Integer bestBestCaseDepth = Integer.MAX_VALUE;
        for (Action action : getPossibleActions()) {
            PerspectiveSuccessors successorsWithScore = evaluate(action);
            if (successorsWithScore == null) {
                continue;
            }

            Integer actionBestScore = successorsWithScore.getBestCaseDepth();

            if (actionBestScore < bestBestCaseDepth) {
                successfulAction = action;
                bestBestCaseDepth = actionBestScore;
                successors = successorsWithScore.getPLayer();
            }
        }
        return (bestBestCaseDepth);
    }

    // RETURNS NULL IF TRANSITION FAILS DUE TO CYCLES OR DEPTH LIMIT
    // RETURNS EMPTY SET IF FOUND GOAL
    private PerspectiveSuccessors pTransition(Action action) {
        Map<Perspective, Set<OrNode>> successorPerspectives = new HashMap<>();
        Integer bestCaseDepth = Integer.MAX_VALUE;
        for (OrNode ground : grounds ){

            //assert(ground.getState().getKripke().checkRelations());

            GroundSuccessors successors = ground.transition(action).descend();

            if (successors == null) {
                return null;
            }

            Set<OrNode> gSuccessors = successors.getOrLayer();

            bestCaseDepth = Integer.min(bestCaseDepth, successors.getBestCaseDepth());

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
            successorNodes.add(new PNode(entry.getKey(), entry.getValue(), newTime, depth+1, maxDepth, domain));
        }
        return new PerspectiveSuccessors(bestCaseDepth, successorNodes);
    }
}


