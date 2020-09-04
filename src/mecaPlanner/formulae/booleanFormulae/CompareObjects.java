package mecaPlanner.formulae.booleanFormulae;

import mecaPlanner.state.Fluent;
import mecaPlanner.state.World;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;


public class CompareObjects extends BooleanFormula{

    private ObjectAtom lhs;
    private ObjectAtom rhs;


    private CompareObjects(ObjectAtom lhs, ObjectAtom rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }


    public static BooleanFormula make(ObjectAtom lhs, ObjectAtom rhs) {
        if (lhs.isFluent() || rhs.isFluent()) {
            return new CompareObjects(lhs, rhs);
        }
        String lhsString = lhs.getObjectValue();
        String rhsString = rhs.getObjectValue();
        return new BooleanAtom(lhsString.equals(rhsString));
    }

    public ObjectAtom getLhs() {
        return lhs;
    }

    public ObjectAtom getRhs() {
        return rhs;
    }

    public Boolean evaluate(World world) {
        return (lhs.evaluate(world) == rhs.evaluate(world));
    }


    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("(");
        str.append(lhs.toString());
        str.append("==");
        str.append(rhs.toString());
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
        CompareObjects other = (CompareObjects) obj;
        return (lhs.equals(other.getLhs()) && rhs.equals(other.getRhs()));
    }



    @Override
    public int hashCode() {
        return (lhs.hashCode() * rhs.hashCode());
    }




}
