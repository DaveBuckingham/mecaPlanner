


package mecaPlanner.agents;



public class SystemAgent extends Agent implements java.io.Serializable {

    private String name;


    public SystemAgent(String name) {
        //super(name);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return ("s-agent:" + getName());
    }

}
