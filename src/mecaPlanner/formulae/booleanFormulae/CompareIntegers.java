package mecaPlanner.formulae.booleanFormulae;

import mecaPlanner.state.World;
import mecaPlanner.state.Fluent;
import mecaPlanner.formulae.integerFormulae.IntegerFormula;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;


public class CompareIntegers extends BooleanFormula{

    public enum Inequality {
        EQ,            // equals
        NE,            // not equal
        LT,            // less than 
        LTE,           // less than or equal
        GT,            // greater than
        GTE            // greater than or equal
    }

    private BooleanFormula lhs;
    private BooleanFormula rhs;
    private Inequality operator;

    private CompareIntegers(Inequality op, IntegerFormula lhs, IntegerFormula rhs) {
        this.operator = op;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public static BooleanFormula make(String op, IntegerFormula lhs, IntegerFormula rhs) {
        if (op.matches("[eE][qQ]|=|==")) {
            return make(Inequality.EQ, lhs, rhs);
        }
        if (op.matches("[nN][eE]|!=|~=")) {
            return make(Inequality.NE, lhs, rhs);
        }
        if (op.matches("[lL][tT]|<")) {
            return make(Inequality.LT, lhs, rhs);
        }
        if (op.matches("[lL][tT][eE]|<=")) {
            return make(Inequality.LTE, lhs, rhs);
        }
        if (op.matches("[gG][tT]|>")) {
            return make(Inequality.GT, lhs, rhs);
        }
        if (op.matches("[gG][tT][eE]|>=")) {
            return make(Inequality.GTE, lhs, rhs);
        }
        throw new RuntimeException("invalid integer inequality: " + op);
    }

    public static BooleanFormula make(Inequality op, IntegerFormula lhs, IntegerFormula rhs) {
        Integer lhsLiteral = lhs.getLiteral();
        if (lhsLiteral != null) {
            Integer rhsLiteral = rhs.getLiteral();
            if (rhsLiteral != null) {
                return new BooleanAtom(compare(op, lhsLiteral, rhsLiteral));
            }
        }
        return new CompareIntegers(op, lhs, rhs);
    }

    public IntegerFormula getLhs() {
        return lhs;
    }
    public IntegerFormula getRhs() {
        return Lhs;
    }
    public Inequality getOperator() {
        return operator;
    }


    private static Boolean compare(Inequality op, Integer a, Integer b) {
        switch (op) {
            case Inequality.EQ:  return (a == b);
            case Inequality.NE:  return (a != b);
            case Inequality.LT:  return (a <  b);
            case Inequality.LTE: return (a <= b);
            case Inequality.GT:  return (a >  b);
            case Inequality.GTE: return (a >= b);
        }
    }


    public Boolean evaluate(World world) {
        return compare(compare(this.operator, lhs.evaluate(world), rhs.evaluate(world)));
    }


    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("(");
        str.append(lhs.toString);
        switch (operator) {
            case Inequality.EQ:  str.append("==");
            case Inequality.NE:  str.append("!=");
            case Inequality.LT:  str.append("< ");
            case Inequality.LTE: str.append("<=");
            case Inequality.GT:  str.append("> ");
            case Inequality.GTE: str.append(">=");
        }
        str.append(rhs.toString);
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
        CompareIntegers other = (CompareIntegers) obj;
        return (lhs.equals(other.getLhs()) &&
                rhs.equals(other.getRhs()) &&
                operator.equals(other.getOperator())
               );
    }



    @Override
    public int hashCode() {
        return (operator.hashCode() * lhs.hashCode() * rhs.hashCode());
    }




}
