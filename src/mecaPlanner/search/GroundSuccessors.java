package mecaPlanner.search;

import java.util.Set;
import java.util.HashSet;

public class GroundSuccessors {

    Integer time;
    Integer bestDistanceToGoal; // FROM START STATE GO GOAL, THORUGH THIS PATH
    Set<OrNode> orLayer;

    public GroundSuccessors(Integer bestDistanceToGoal, Set<OrNode> orLayer) {
        //this.time = time;
        this.bestDistanceToGoal = bestDistanceToGoal;
        this.orLayer = orLayer;
    }

//    Integer getTime() {
//        return time;
//    }

    Integer getBestDistanceToGoal() {
        return bestDistanceToGoal;
    }

    Set<OrNode> getOrLayer() {
        return orLayer;
    }

}


