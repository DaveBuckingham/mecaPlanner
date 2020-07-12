package mecaPlanner.formulae;

import mecaPlanner.state.World;


import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;



public class FluentLiteral extends FluentFormula{

    private Boolean value;
    private FluentAtom atom;

    public FluentLiteral(Boolean value, FluentAtom atom) {
        this.value = value;
        this.atom = atom;
    }

    public FluentLiteral(Boolean value, String name, List<String> parameters) {
        this(value, new FluentAtom(name, parameters));
    }

    public FluentLiteral(Boolean value, String name, String ...parameters) {
        this(value, name, Arrays.asList(parameters));
    }

    public FluentLiteral(String name, String ...parameters) {
        this(true, name, Arrays.asList(parameters));
    }

    public FluentLiteral(String name, List<String> parameters) {
        this(true, name, parameters);
    }


    public FluentAtom getAtom() {
        return this.atom;
    }

    public Boolean getValue() {
        return this.value;
    }

    public FluentLiteral negated() {
        return new FluentLiteral(!value, atom);
    }

    public Boolean isNegated() {
        return !getValue();
    }

    public Boolean isNotNegated() {
        return getValue();
    }

    public Boolean holds(World world) {
        if (this.getValue()) {
            return (this.atom.holds(world));
        }
        return (!this.atom.holds(world));
    }

    public Set<FluentAtom> getAllAtoms() {
        Set<FluentAtom> allAtoms = new HashSet<>();
        allAtoms.add(atom);
        return allAtoms;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        FluentLiteral otherLiteral = (FluentLiteral) obj;
        return (this.value.equals(otherLiteral.getValue()) && this.atom.equals(otherLiteral.getAtom()));
    }



    @Override
    public String toString() {
        if (value) {
            return atom.toString();
        }
        else {
            return "not(" + atom.toString() + ")";
        }
    }



}
