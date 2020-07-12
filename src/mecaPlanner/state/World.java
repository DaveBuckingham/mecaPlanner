package mecaPlanner.state;

import mecaPlanner.formulae.FluentAtom;
import mecaPlanner.formulae.FluentLiteral;

import java.util.List;
import java.util.ArrayList;

import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.util.BitSet;
import java.util.Comparator;



public class World implements java.io.Serializable {

    private static int idCounter = 0;

    private final int id;

    private final Set<FluentAtom> fluents;

    private String name;

    public World(String name, Set<FluentAtom> fluents) {
        this.id = World.idCounter++;
        this.name = name;
        this.fluents = fluents;
    }

    public World(String name, String ...atomNames) {
        this.id = World.idCounter++;
        this.name = name;
        fluents = new HashSet<FluentAtom>();
        for (String atomName : atomNames) {
            fluents.add(new FluentAtom(atomName));
        }
    }

    public World(Set<FluentAtom> fluents) {
        this(null, fluents);
    }

    public World(World toCopy) {
        this(null, toCopy.getFluents());
    }

    public World update(Set<FluentLiteral> literals) {
        Set<FluentAtom> newFluents = new HashSet<FluentAtom>(fluents);
        for (FluentLiteral literal : literals) {
            if (literal.getValue()){
                newFluents.add(literal.getAtom());
            }
            else {
                newFluents.remove(literal.getAtom());
            }
        }
        return (new World(newFluents));
    }

    public Set<FluentAtom> getFluents() {
        return fluents;
    }

    public int getId() {
        return this.id;
    }

    public Boolean containsAtom(FluentAtom a) {
        return fluents.contains(a);
    }

    public boolean equivalent(World otherWorld) {
        return this.fluents.equals(otherWorld.getFluents());
    }

    public String getName() {
        return name == null ? Integer.toString(id) : name;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(name == null ? id : name);
        str.append("{");
        if (fluents.size() > 0) {
            for (FluentAtom fluent : fluents) {
                str.append(fluent);
                str.append(",");
            }
            str.deleteCharAt(str.length() - 1);
        }
        str.append("}");
        return str.toString();
    }


}

