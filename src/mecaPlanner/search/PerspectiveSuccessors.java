package mecaPlanner.search;

import java.util.Set;
import java.util.HashSet;

public class PerspectiveSuccessors {

    Integer bestCaseDepth;
    Set<PNode> pLayer;

    public PerspectiveSuccessors(Integer bestCaseDepth, Set<PNode> pLayer) {
        this.bestCaseDepth = bestCaseDepth;
        this.pLayer = pLayer;
        assert(pLayer != null);
    }

    Integer getBestCaseDepth() {
        return bestCaseDepth;
    }

    Set<PNode> getPLayer() {
        return pLayer;
    }

}


