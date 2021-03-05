package mecaPlanner.search;

import java.util.Set;
import java.util.HashSet;

public class PerspectiveSuccessors {

    Integer bestCaseDepth;
    Set<PNode> pLayer;

    public PerspectiveSuccessors(Integer bestCaseDepth, Set<PNode> orLayer) {
        this.bestCaseDepth = bestCaseDepth;
        this.pLayer = pLayer;
    }

    Integer getBestCaseDepth() {
        return bestCaseDepth;
    }

    Set<PNode> getPLayer() {
        return pLayer;
    }

}


