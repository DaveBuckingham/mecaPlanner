
package mecaPlanner.state;

import mecaPlanner.formulae.Fluent;

// REPRESENTS THE ASSIGNMENT OF A VALUE TO A FLUENT BY AN ACTION'S ONTIC EFFECT

public class Assignment {
    private Formula condition;
    private Fluent fluent;
    private Boolean value;
    public Assignment(Formula condition, Fluent fluent, Boolean value) {
        this.condition = condition;
        this.fluent = fluent;
        this.value = value;
    }
    public Formula getCondition() {
        return condition;
    }
    public Fluent getFluent() {
        return fluent;
    }
    public Boolean getValue() {
        return value;
    }

    public String toString()  {
        return (fluent.toString() + "<-" + value.toString() + " if " + condition.toString());
    }
}
