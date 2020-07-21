


package mecaPlanner.agents;



public class SystemAgent extends Agent implements java.io.Serializable {


    public SystemAgent(String name) {
        super(name);
    }


    public String toString() {
        return ("s-agent:" + getName());
    }

}
