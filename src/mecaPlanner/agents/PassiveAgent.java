

package mecaPlanner.agents;



public class PassiveAgent extends Agent{


    public PassiveAgent(String name) {
        this.name = name;
    }

    public String toString() {
        return ("p-agent:" + getName());
    }

}
