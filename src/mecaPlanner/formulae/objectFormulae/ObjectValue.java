package mecaPlanner.formulae.objectFormulae;

import mecaPlanner.state.World;

public class ObjectValue extends ObjectFormula{

    private String value;

    public ObjectValue(String value) {
        this.value = value;
    }

    public String resolve() {
        return value;
    }

    public String evaluate(World world) {
        return value;
    }



    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        ObjectValue other = (ObjectValue) obj;
        return (value.equals(other.resolve()));
    }


     @Override
     public int hashCode() {
         return value.hashCode();
     }




}
