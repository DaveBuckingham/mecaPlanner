package mecaPlanner.state;

import mecaPlanner.formulae.FluentAtom;
import mecaPlanner.formulae.FluentLiteral;

import java.util.List;
import java.util.ArrayList;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.BitSet;
import java.util.Comparator;



public class World implements java.io.Serializable {

    private static int idCounter = 0;

    private final int id;

    private final Set<FluentAtom> atoms;
    private final Map<Fluent, Boolean> booleanFluents;
    private final Map<Fluent, Integer> integerFluents;
    private final Map<Fluent, String> objectFluents;

    private String name;

    public World(String name,
                 Map<Fluent, Boolean> booleanFluents,
                 Map<Fluent, Integer> integerFluents,
                 Map<Fluent, String> objectFluents
                ) {
        this.id = World.idCounter++;
        this.name = name;
        this.booleanFluents = booleanFluents;
        this.integerFluents = integerFluents;
        this.objectFluents = objectFluents;
    }

    public World(World toCopy) {
        this(null, toCopy.getAtoms());
        booleanFluents = new HashMap<Fluent, Boolean>();
        integerFluents = new HashMap<Fluent, Integer>();
        objectFluents = new HashMap<Fluent, String>();
        booleanFluents.addAll(toCopy.getBooleanFluents());
        integerFluents.addAll(toCopy.getIntegerFluents());
        objectFluents.addAll(toCopy.getObjectFluents());
    }

    public Map<Fluent, Boolean> getBooleanFluents() {
        return booleanFluents;
    }

    public Map<Fluent, Integer> getIntegerFluents() {
        return integerFluents;
    }

    public Map<Fluent, String> getObjectFluents() {
        return objectFluents;
    }

    public int getId() {
        return this.id;
    }

    public Boolean resolveBooleanFluent(Fluent f) {
        if (!booleanFluents.containsKey(f)) {
            throw new RuntimException("unknown boolean fluent: " + f);
        }
        return booleanFluents.get(f);
    }

    public Integer resolveIntegerFluent(Fluent f) {
        if (!integer.containsKey(f)) {
            throw new RuntimException("unknown integer fluent: " + f);
        }
        return integerFluents.get(f);
    }

    public String resolveObjectFluent(Fluent f) {
        if (!objectFluents.containsKey(f)) {
            throw new RuntimException("unknown object fluent: " + f);
        }
        return objectFluents.get(f);
    }


    public boolean equivalent(World otherWorld) {
    }

    public String getName() {
        return name == null ? Integer.toString(id) : name;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(name == null ? id : name);
        str.append("{");
        if (atoms.size() > 0) {
            for (FluentAtom fluent : atoms) {
                str.append(fluent);
                str.append(",");
            }
            str.deleteCharAt(str.length() - 1);
        }
        str.append("}");
        return str.toString();
    }


}

