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


public class FluentFormulaIntegerComparison extends FluentFormula{

    public enum Inequality {
        EQ,            // equals
        NE,            // not equal
        LT,            // less than 
        LTE,           // less than or equal
        GT,            // greater than
        GTE            // greater than or equal
    }

    private Inequality inequality;
    
    boolean lhsIsFluent;
    boolean rhsIsFluent;

    private Integer lhsLiteral;
    private Fluent lhsFluent;

    private Integer rhsLiteral;
    private Fluent rhsFluent;

    public FluentFormulaIntegerComparison(Inequality inequality, Integer lhs, Integer rhs) {
        this.inequality = inequality;

        lhsLiteral = lhs;
        lhsFluent = null;
        lhsIsFluent = false;

        rhsLiteral = rhs;
        rhsFluent = null;
        rhsIsFluent = false;
    }

    public FluentFormulaIntegerComparison(Inequality inequality, Fluent lhs, Integer rhs) {
        this.inequality = inequality;

        rhsLiteral = null;
        lhsFluent = lhs;
        lhsIsFluent = true;

        rhsLiteral = rhs;
        rhsFluent = null;
        rhsIsFluent = false;
    }

    public FluentFormulaIntegerComparison(Inequality inequality, Integer lhs, Fluent rhs) {
        this.inequality = inequality;

        lhsLiteral = lhs;
        lhsFluent = null;
        lhsIsFluent = false;

        rhsLiteral = null;
        rhsFluent = rhs;
        rhsIsFluent = true;
    }

    public FluentFormulaIntegerComparison(Inequality inequality, Fluent lhs, Fluent rhs) {
        this.inequality = inequality;

        lhsLiteral = null;
        lhsFluent = lhs;
        lhsIsFluent = true;

        rhsLiteral = null;
        rhsFluent = rhs;
        rhsIsFluent = true;
    }

    public FluentFormulaIntegerComparison(String strInequality, Integer lhs, Integer rhs) {
        if (strInequality.equalsIgnoreCase("eq")) {
            this(Inequality.EQ, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("ne")) {
            this(Inequality.NE, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("lt")) {
            this(Inequality.LT, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("lte")) {
            this(Inequality.LTE, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("gt")) {
            this(Inequality.GT, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("gte")) {
            this(Inequality.GTE, lhs, rhs);
        }
        else {
            throw new RuntimeException("invalid inequality string: " + strInequality);
        }
    }

    public FluentFormulaIntegerComparison(String inequality, Fluent lhs, Integer rhs) {
        if (strInequality.equalsIgnoreCase("eq")) {
            this(Inequality.EQ, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("ne")) {
            this(Inequality.NE, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("lt")) {
            this(Inequality.LT, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("lte")) {
            this(Inequality.LTE, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("gt")) {
            this(Inequality.GT, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("gte")) {
            this(Inequality.GTE, lhs, rhs);
        }
        else {
            throw new RuntimeException("invalid inequality string: " + strInequality);
        }

    }

    public FluentFormulaIntegerComparison(String inequality, Integer lhs, Fluent rhs) {
        if (strInequality.equalsIgnoreCase("eq")) {
            this(Inequality.EQ, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("ne")) {
            this(Inequality.NE, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("lt")) {
            this(Inequality.LT, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("lte")) {
            this(Inequality.LTE, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("gt")) {
            this(Inequality.GT, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("gte")) {
            this(Inequality.GTE, lhs, rhs);
        }
        else {
            throw new RuntimeException("invalid inequality string: " + strInequality);
        }

    }

    public FluentFormulaIntegerComparison(String inequality, Fluent lhs, Fluent rhs) {
         if (strInequality.equalsIgnoreCase("eq")) {
            this(Inequality.EQ, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("ne")) {
            this(Inequality.NE, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("lt")) {
            this(Inequality.LT, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("lte")) {
            this(Inequality.LTE, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("gt")) {
            this(Inequality.GT, lhs, rhs);
        }
        else if (strInequality.equalsIgnoreCase("gte")) {
            this(Inequality.GTE, lhs, rhs);
        }
        else {
            throw new RuntimeException("invalid inequality string: " + strInequality);
        }
    }

    public Boolean holds(World world) {
        Integer lhsValue = lhsIsFluent ? world.resolveFluent(lhsFluent) : lhsLiteral;
        Integer rhsValue = rhsIsFluent ? world.resolveFluent(rhsFluent) : rhsLiteral;
        switch (this.inequality) {
            case EQ:
                return lhsValue == rhsValue;
            case NE:
                return lhsValue != rhsValue;
            case LT:
                return lhsValue <  rhsValue;
            case LTE:
                return lhsValue <= rhsValue;
            case GT:
                return lhsValue >  rhsValue;
            case GTE:
                return lhsValue >= rhsValue;
            default:
                throw new RuntimeException("invalide inequality: " + this.inequality);
        }
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

    public boolean isLhsFluent() {
        return lhsIsFluent;
    }

    public boolean isRhsFluent() {
        return rhsIsFluent;
    }

    public Integer getLhsLiteral() {
        return lhsLiteral;
    }

    public Integer getRhsLiteral() {
        return rhsLiteral;
    }

    public Fluent getLhsFluent() {
        reutrn lhsFluent;
    }

    public Fluent getRhsFluent() {
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
