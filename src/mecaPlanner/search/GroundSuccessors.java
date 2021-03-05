package mecaPlanner.search;

import java.util.Set;
import java.util.HashSet;

public class GroundSuccessors {

    Integer bestCaseDepth;
    Set<OrNode> orLayer;

    public GroundSuccessors(Integer bestCaseDepth, Set<OrNode> orLayer) {
        this.bestCaseDepth = bestCaseDepth;
        this.orLayer = orLayer;
    }

    Integer getBestCaseDepth() {
        return bestCaseDepth;
    }

    Set<OrNode> getOrLayer() {
        return orLayer;
    }

}


