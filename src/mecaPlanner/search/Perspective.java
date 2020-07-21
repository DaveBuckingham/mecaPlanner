package mecaPlanner.search;

import mecaPlanner.state.NDState;
import mecaPlanner.state.EpistemicState;



public class Perspective implements java.io.Serializable {
    private NDState agentView;

    public Perspective(EpistemicState eState, String agent) {
        agentView = eState.getBeliefPerspective(agent);
    }

    public NDState getAgentView() {
        return agentView;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        return agentView.equals(((Perspective) obj).getAgentView());
    }

    public String toString() {
        return agentView.toString();
    }
}

