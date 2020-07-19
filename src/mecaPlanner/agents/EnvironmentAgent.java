
package mecaPlanner.agents;

import mecaPlanner.models.Model;
import mecaPlanner.state.NDState;
import java.util.Set;
import java.util.HashSet;
import mecaPlanner.actions.Action;


public class EnvironmentAgent extends Agent implements java.io.Serializable{


    public EnvironmentAgent(String name) {
        this.name = name;
    }


    public String toString() {
        return ("e-agent:" + getName());
    }

}
