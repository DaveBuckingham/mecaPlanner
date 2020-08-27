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


public class FluentCompareBoolean extends FluentFormula{


    private FluentCompareBoolean(Inequality op, Atom lhs, Atom rhs) {
        this.op = op;
        this.lhs = lhs;
        this.rhs = rhs;
    }


    public static FluentFormula make(Inequality op, Atom lhs, Atom rhs) {
        lhsFluent = lhs instnaceof Fluent;
        rhsFluent = rhs instnaceof Fluent;
        assert (op == Inequalty.EQ || op == Inequality.NE);
        assert (lhsFluent || lhs instanceof BooleanValue);
        assert (rhsFluent || rhs instanceof BooleanValue);
        if (lhsFluent || rhsFluent) {
            return new FluentCompareBoolean(op, lhs, rhs);
        }
        return new BooleanValue(evaluate(op, (BooleanValue) lhs, (BooleanValue rhs)));
    }

    private static Boolean evaluate(Inequality.op, BooleanValue a, BooleanValue b) {
        if (op == Inequality.EQ) {
            return a.equals(b);
        }
        return !a.equals(b);
    }


    public Boolean holds(World world) {
        ObjectValue lhsGround = lhsFluent ? world.resolve((Fluent) lhs) : (ObjectValue) lhs;
        ObjectValue rhsGround = rhsFluent ? world.resolve((Fluent) rhs) : (ObjectValue) rhs;
        return FluentCompareObject.evaluate(op, lhsGround, rhsGround);
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
        return (op.equals(other.getOp()) && lhs.equals(other.getLhs()) && rhs.equals(other.getRhs()));
    }



    @Override
    public int hashCode() {
        return (op * lhs.hashCode() * rhs.hashCode());
    }




}
