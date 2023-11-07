package mecaPlanner.search;

import mecaPlanner.state.AbstractState;
import mecaPlanner.state.PointedAbstractState;



public class Perspective implements java.io.Serializable {
    private AbstractState agentView;

    public Perspective(PointedAbstractState eState, String agent) {
        agentView = eState.getBeliefPerspective(agent);
    }

    public AbstractState getAgentView() {
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

