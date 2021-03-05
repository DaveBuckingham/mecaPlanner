package mecaPlanner.search;

import java.util.Set;
import java.util.HashSet;

public class PerspectiveSuccessors {

    Integer bestCaseDepth;
    Set<PNode> pLayer;

    public PerspectiveSuccessors(Integer bestCaseDepth, Set<PNode> orLayer) {
        this.bestCaseDepth = bestCaseDepth;
        this.orLayer = orLayer;
    }

    Integer getBestCaseDepth() {
        return bestCaseDepth;
    }

    Set<PNode> getPLayer() {
        return pLayer;
    }

}


