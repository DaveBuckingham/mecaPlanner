package mecaPlanner.agents;



public abstract class Agent {

    protected String name;

    public Agent(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public int hashCode() {
        //if (getName() == null) {
        //    System.out.println("YIKES!");
        //    System.out.println(this.getClass().getName());
        //    System.exit(1);
        //}
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        return this.getName().equals(((Agent) obj).getName());
    }



}
