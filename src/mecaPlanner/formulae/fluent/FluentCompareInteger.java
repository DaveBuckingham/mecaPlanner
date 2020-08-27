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

    private FluentCompareInteger(Inequality op, Atom lhs, Atom rhs) {
    }

    public static FluentFormula make(Inequality op, Atom lhs, Atom rhs) {
        lhsFluent = lhs instnaceof Fluent;
        rhsFluent = rhs instnaceof Fluent;
        assert (lhsFluent || lhs instanceof IntegerValue);
        assert (rhsFluent || rhs instanceof IntegerValue);
        if (lhsFluent || rhsFluent) {
            return new FluentCompareInteger(op, lhs, rhs);
        }
        return new BooleanValue(evaluate(op, (IntegerValue) lhs, (IntegerValue rhs)));
    }

    private static boolean evaluate(Inequality.op, IntegerValue a, IntegerValue b) {
        switch (op) {
            case Inequality.EQ:  return (a.get() == b.get());
            case Inequality.NE:  return (a.get() != b.get());
            case Inequality.LT:  return (a.get() <  b.get());
            case Inequality.LTE: return (a.get() <= b.get());
            case Inequality.GT:  return (a.get() >  b.get());
            case Inequality.GTE: return (a.get() >= b.get());
        }
    }


    public Boolean holds(World world) {
        IntegerValue lhsGround = lhsFluent ? world.resolve((Fluent) lhs) : (IntegerValue) lhs;
        IntegerValue rhsGround = rhsFluent ? world.resolve((Fluent) rhs) : (IntegerValue) rhs;
        return FluentCompareInteger.evaluate(op, lhsGround, rhsGround);
    }

    //public Set<FluentAtom> getAllAtoms() {
    //    Set<FluentAtom> allAtoms = new HashSet<>();
    //    for (FluentFormula formula : formulae) {
    //        allAtoms.addAll(formula.getAllAtoms());
    //    }
    //    return allAtoms;
    //}

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
