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

    private final Map<Fluent, Boolean> booleanFluents;
    private final Map<Fluent, Integer> integerFluents;
    private final Map<Fluent, String> objectFluents;

    private String name;

    public World(String name, Map<Fluent, Boolean> booleanFluents,
                              Map<Fluent, Integer> integerFluents,
                              Map<Fluent, String> objectFluents) {
        this.id = World.idCounter++;
        this.name = name;
        this.booleanFluents = booleanFluents;
        this.integerFluents = integerFluents;
        this.objectFluents = objectFluents;
    }

    public World(World toCopy) {
        this.name = toCopy.getName() + "'";
        this.booleanFluents = new HashMap<Fluent, Boolean>(toCopy.getBooleanFluents());
        this.integerFluents = new HashMap<Fluent, Integer>(toCopy.getIntegerFluents());
        this.objectFluents = new HashMap<Fluent, String>(toCopy.getObjectFluents());
    }

    protected Map<Fluent,Boolean> getBooleanFluents() {
        return booleanFluents;
    }

    protected Map<Fluent,Integer> getIntegerFluents() {
        return integerFluents;
    }

    protected Map<Fluent,String> getObjectFluents() {
        return objectFluents;
    }

    public int getId() {
        return this.id;
    }

    public Boolean resolveBoolean(Fluent f) {
        if (!booleanFluents.containsKey(f)) {
            throw new RuntimeException("unknown fluent: " + f);
        }
        return booleanFluents.get(f);
    }

    public Integer resolveInteger(Fluent f) {
        if (!integerFluents.containsKey(f)) {
            throw new RuntimeException("unknown fluent: " + f);
        }
        return integerFluents.get(f);
    }

    public String resolveObject(Fluent f) {
        if (!objectFluents.containsKey(f)) {
            throw new RuntimeException("unknown fluent: " + f);
        }
        return objectFluents.get(f);
    }


    public boolean equivalent(World otherWorld) {
        return booleanFluents.equals(otherWorld.getBooleanFluents());
        return integerFluents.equals(otherWorld.getIntegerFluents());
        return objectFluents.equals(otherWorld.getobjectFluents());
    }

    public String getName() {
        return name == null ? Integer.toString(id) : name;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(name == null ? id : name);
        str.append("{");
        for (Map.Entry<Fluent, Boolean> entry : booleanFluents.entrySet()) {
            str.append(entry.getKey().toString() + "==" + entry.getValue().toString());
            str.append(", ");
        }
        for (Map.Entry<Fluent, Integer> entry : integerFluents.entrySet()) {
            str.append(entry.getKey().toString() + "==" + entry.getValue().toString());
            str.append(", ");
        }
        for (Map.Entry<Fluent, String> entry : objectFluents.entrySet()) {
            str.append(entry.getKey().toString() + "==" + entry.getValue().toString());
            str.append(", ");
        }
        str.deleteCharAt(str.length() - 2);
        str.append("}");
        return str.toString();
    }


}

