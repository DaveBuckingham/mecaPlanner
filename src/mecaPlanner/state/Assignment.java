
package mecaPlanner.state;

import mecaPlanner.formulae.Formula;


public class Assignment {
    private Fluent reference;
    private Formula value;
    public Assignment(Fluent reference, Formula value) {
        this.reference = reference;
        this.value = value;
    }
    public Fluent getReference() {
        return reference;
    }
    public Formula getValue() {
        return value;
    }
}
