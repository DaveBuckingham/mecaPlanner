package mecaPlanner.search;

import mecaPlanner.state.*;
import mecaPlanner.Domain;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;


public class OrLayer {
    Set<OrNode> nodes;
    private Integer time;
    Integer bestDistanceToGoal;
    private int maxDepth;
    private Domain domain;


    // BUILT FROM OR-NODE
    public OrLayer(Integer best, OrNode n, int max, Domain d) {
        this.time = n.getTime();
        this.bestDistanceToGoal = best;
        this.nodes = new HashSet<OrNode>();
        nodes.add(n);
        maxDepth = max;
        domain = d;
    }

    // A GOAL NODE
    public OrLayer(Integer time, int max, Domain d) {
        this.time = time;
        this.bestDistanceToGoal = time;
        this.nodes = new HashSet<OrNode>();
        maxDepth = max;
        domain = d;
    }

    // FOR INITIALZING PROBLEM
    public OrLayer(Integer time, Set<OrNode> n, int max, Domain d) {
        this.time = time;
        this.bestDistanceToGoal = time;
        this.nodes = n;
        maxDepth = max;
        domain = d;
    }

    // AN EMPTY LAYER, FOR MERGING
    public OrLayer(int max, Domain d) {
        this.time = null;
        this.bestDistanceToGoal = Integer.MAX_VALUE;
        this.nodes = new HashSet<OrNode>();
        maxDepth = max;
        domain = d;
    }


    public void merge(OrLayer other) {
        if (time == null) {
            time = other.getTime();
        }
        if (time != other.getTime()) {
            throw new RuntimeException("can't merge or-layers with different timesteps");
        }
        assert(maxDepth == other.getMaxDepth());
        nodes.addAll(other.getNodes());
        bestDistanceToGoal = Integer.min(bestDistanceToGoal, other.getBest());
    }

    public Integer getTime() {
        return time;
    }

    public Integer getBest() {
        return bestDistanceToGoal;
    }

    public Set<OrNode> getNodes() {
        return nodes;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public PerspectiveSuccessors lift() {

        Map<Perspective, Set<OrNode>> successorPerspectives = new HashMap<>();

        for (OrNode gSuccessor : nodes) {
            Perspective successorPerspective = new Perspective(gSuccessor.getState(), gSuccessor.getAgent());

            boolean found = false;
            for (Perspective p : successorPerspectives.keySet()) {
                if (p.equivalent(successorPerspective)) {
                    successorPerspectives.get(p).add(gSuccessor);
                    found = true;
                    break;
                }
            }
            if (!found) {
                Set<OrNode> singleton = new HashSet<>();
                singleton.add(gSuccessor);
                successorPerspectives.put(successorPerspective, singleton);
            }
        }
    
        Set<PNode> successorNodes = new HashSet<>();
        for (Map.Entry<Perspective, Set<OrNode>> entry : successorPerspectives.entrySet()) {
            successorNodes.add(new PNode(entry.getKey(), entry.getValue(), time, maxDepth, domain));
        }
        return new PerspectiveSuccessors(bestDistanceToGoal, successorNodes);

    }

}

