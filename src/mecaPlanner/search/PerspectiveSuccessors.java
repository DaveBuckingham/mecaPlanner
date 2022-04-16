package mecaPlanner.search;

import java.util.Set;
import java.util.HashSet;

public class PerspectiveSuccessors {

    Integer bestDistanceToGoal; // FROM START STATE GO GOAL, THORUGH THIS PATH
    Set<PNode> pLayer;

    public PerspectiveSuccessors(Integer bestDistanceToGoal, Set<PNode> pLayer) {
        this.bestDistanceToGoal = bestDistanceToGoal;
        this.pLayer = pLayer;
        assert(pLayer != null);
    }

    Integer getBestDistanceToGoal() {
        return bestDistanceToGoal;
    }

    Set<PNode> getPLayer() {
        return pLayer;
    }

}


