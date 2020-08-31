package mecaPlanner.formulae.integerFormulae;

import mecaPlanner.state.World;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;


public class IntegerAdd extends IntegerFormula{

    public IntegerAdd(IntegerFormula lhs, IntegerFormula, rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Integer evaluate(World world) {
        return (lhs.evaluate(world) + rhs.evaluate(world));
    }

    @Override
    public String toString() {
        return ("(" + lhs.toString() + "+" + rhs.toString() + ")");
    }

}
