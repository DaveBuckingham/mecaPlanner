
package mecaPlanner.state;

import mecaPlanner.formulae.Formula;


public class Assignment {
    private Fluent reference;
    private LocalFormula value;
    public Assignment(Fluent reference, LocalFormula value) {
        this.reference = reference;
        this.value = value;
    }
    public Fluent getReference() {
        return reference;
    }
    public Formula getValue() {
        return value;
    }

    public String toString()  {
        return (reference.toString() + "<-" + value.toString());
    }
}
