package mecaPlanner.formulae;

import mecaPlanner.state.World;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;


public class FluentFormulaFalse extends FluentFormula{


    public FluentFormulaFalse() {
    }

    public String getName() {
        return "False";
    }

    public Boolean holds(World world) {
        return false;
    }

    public Set<FluentAtom> getAllAtoms() {
        return new HashSet<>();
    }

    @Override
    public String toString() {
        return getName();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 1;
    }


}
