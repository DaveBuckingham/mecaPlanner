package mecaPlanner.formulae.integerFormulae;

import mecaPlanner.state.World;
import mecaPlanner.state.Fluent;
import mecaPlanner.formulae.integerFormulae.IntegerFormula;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import java.util.Set;

public class IntegerValue extends IntegerFormula{

    private Integer value;

    public IntegerValue(Integer value) {
        this.value = value;
    }

    public Integer resolve() {
        return value;
    }

    public Integer evaluate(World world) {
        return value;
    }



    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        IntegerValue other = (IntegerValue) obj;
        return (value.equals(other.resolve()));
    }


     @Override
     public int hashCode() {
         return value.hashCode();
     }




}
