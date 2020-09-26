package mecaPlanner.formulae.booleanFormulae;

import mecaPlanner.state.Fluent;
import mecaPlanner.state.World;

import mecaPlanner.formulae.objectFormulae.*;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;


public class CompareObjects extends BooleanFormula{

    private ObjectFormula lhs;
    private ObjectFormula rhs;


    private CompareObjects(ObjectFormula lhs, ObjectFormula rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }


    public static BooleanFormula make(ObjectFormula lhs, ObjectFormula rhs) {
        if (lhs instanceof ObjectValue && rhs instanceof ObjectValue) {
            return new BooleanValue(lhs.equals(rhs));
        }
        return new CompareObjects(lhs, rhs);
    }

    public ObjectFormula getLhs() {
        return lhs;
    }

    public ObjectFormula getRhs() {
        return rhs;
    }

    public Boolean evaluate(World world) {
        return (lhs.evaluate(world).equals(rhs.evaluate(world)));
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
