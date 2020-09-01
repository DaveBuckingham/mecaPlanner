
package mecaPlanner.state;

import mecaPlanner.formulae.Formula;


public class Assignment {
    private Fluent reference;
    private Formula value;
    public Assignment(Fluent reference, Formula value) {
        this.references = references;
        this.value = value;
    }
    public Fluent getReference() {
        return references;
    }
    public Formula getValue() {
        return value;
    }
}
