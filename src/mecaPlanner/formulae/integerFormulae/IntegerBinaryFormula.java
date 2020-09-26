package mecaPlanner.formulae.integerFormulae;

import mecaPlanner.state.World;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;


public abstract class IntegerBinaryFormula extends IntegerFormula{
  
    IntegerFormula lhs;
    IntegerFormula rhs;


    IntegerFormula getLhs() {
        return lhs;
    }

    IntegerFormula getRhs() {
        return rhs;
    }

}
