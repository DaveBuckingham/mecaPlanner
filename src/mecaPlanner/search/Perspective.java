package mecaPlanner.search;

import mecaPlanner.state.PlausibilityState;
import mecaPlanner.state.PointedPlausibilityState;



public class Perspective implements java.io.Serializable {
    private PlausibilityState agentView;

    public Perspective(PointedPlausibilityState eState, String agent) {
        agentView = eState.getBeliefPerspective(agent);
    }

    public PlausibilityState getAgentView() {
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

    public boolean equivalent(Perspective other) {
        return agentView.bisimilar(other.getAgentView());
    }

    @Override
    public int hashCode() {
        return agentView.hashCode();
    }

    public String toString() {
        return agentView.toString();
    }
}

