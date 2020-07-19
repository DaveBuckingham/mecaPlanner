
package mecaPlanner.agents;

import mecaPlanner.models.Model;
import mecaPlanner.state.NDState;
import java.util.Set;
import java.util.HashSet;
import mecaPlanner.actions.Action;


public class EnvironmentAgent extends Agent implements java.io.Serializable{

    Model model;


    public EnvironmentAgent(String name, Model model) {
        this.name = name;
        this.model = model;
    }

    public Model getModel() {
        return this.model;
    }


    public String toString() {
        return ("e-agent:" + getName());
    }

}
