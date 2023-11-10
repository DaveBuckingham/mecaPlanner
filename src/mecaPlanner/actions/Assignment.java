
package mecaPlanner.actions;

import mecaPlanner.formulae.Fluent;
import mecaPlanner.formulae.Formula;
import mecaPlanner.formulae.Literal;

// REPRESENTS THE ASSIGNMENT OF A VALUE TO A FLUENT BY AN ACTION'S ONTIC EFFECT

public class Assignment {
    private Formula condition;
    private Fluent fluent;
    public Assignment(Fluent fluent, Formula condition) {
        this.condition = condition;
        this.fluent = fluent;
    }
    public Assignment(Fluent fluent, Boolean val) {
        this(fluent, new Literal(val));
    }

    public Formula getCondition() {
        return condition;
    }
    public Fluent getFluent() {
        return fluent;
    }

    public String toString()  {
        return (fluent.toString() + "<-" + condition.toString());
    }
}
