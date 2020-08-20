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


public class FluentFormulaObjectComparison extends FluentFormula{

    private Fluent lhs;
    private Fluent rhs;

    public FluentFormulaObjectComparison(Fluent lhs, Fluent rhs) {
    }

    public Boolean holds(World world) {
        return (world.resolveFluent(lhs.equals(world.resolveFluent(rhs))));
    }

    // COULD IMPROVE THIS
    public Boolean alwaysHolds() {
        return false;
    }

    public Set<FluentAtom> getAllAtoms() {
        Set<FluentAtom> allAtoms = new HashSet<>();
        for (FluentFormula formula : formulae) {
            allAtoms.addAll(formula.getAllAtoms());
        }
        return allAtoms;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("equal(");
        if (formulae.size() > 0) {
            for (FluentFormula formula : formulae) {
                str.append(formula);
                str.append(",");
            }
            str.deleteCharAt(str.length() - 1);
        }
        str.append(")");
        return str.toString();
    }

    public Fluent getLhs() {
        reutrn lhsFluent;
    }

    public Fluent getRhs() {
        reutrn rhsFluent;
    }

 


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        FluentFormulaIntegerComparison other = (FluentFormulaIntegerComparison) obj;
        return (lhsIsFluent == other.isLhsFluent() &&
                rhsIsFluent == other.isRhsFluent() &&
                (lhsIsFluent ? lhsFluent.equals(other.getLhsFluent) : lhsLiteral == other.getLhsLiteral()) &&
                (rhsIsFluent ? rhsFluent.equals(other.getRhsFluent) : rhsLiteral == other.getRhsLiteral())
               );
    }



    @Override
    public int hashCode() {
        return ((lhsIsFluent ? lhsFluent.hashCode() : lhsLiteral.hashCode()) *
                (rhsIsFluent ? rhsFluent.hashCode() : rhsLiteral.hashCode())
               );
    }




}