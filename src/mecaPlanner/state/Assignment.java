
package mecaPlanner.state;

import mecaPlanner.formulae.LocalFormula;


public class Assignment {
    private Fluent fluent;
    private Boolean value;
    public Assignment(Fluent fluent, Boolean value) {
        this.fluent = fluent;
        this.value = value;
    }
    public Fluent getFluent() {
        return fluent;
    }
    public Boolean getValue() {
        return value;
    }

    public String toString()  {
        return (fluent.toString() + "<-" + value.toString());
    }
}
