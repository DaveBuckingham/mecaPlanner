

package mecaPlanner.agents;



public class PassiveAgent extends Agent{

    private String name;


    public PassiveAgent(String name) {
        //super(name);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return ("p-agent:" + getName());
    }

}
