package mecaPlanner.formulae;

import mecaPlanner.state.World;
import mecaPlanner.state.Fluent;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;


public abstract FluentCompare extends FluentFormula{

    public enum Inequality {
        EQ,            // equals
        NE,            // not equal
        LT,            // less than 
        LTE,           // less than or equal
        GT,            // greater than
        GTE            // greater than or equal
    }

    protected Atom lhs;
    protected Atom rhs;
    protected Inequality op;

    protected lhsFluent;
    protected rhsFluent;

    public Atom getLhs() {
        reutrn lhs;
    }

    public Atom getRhs() {
        reutrn rhs;
    }

    public Inequality getOp() {
        return op;
    }


    //public Set<FluentAtom> getAllAtoms() {
    //    Set<FluentAtom> allAtoms = new HashSet<>();
    //    for (FluentFormula formula : formulae) {
    //        allAtoms.addAll(formula.getAllAtoms());
    //    }
    //    return allAtoms;
    //}




}
