package mecaPlanner.state;


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

    public World(String name,
                 Map<Fluent, Boolean> booleanFluents,
                 Map<Fluent, Integer> integerFluents,
                 Map<Fluent, String> objectFluents) {
        this.id = World.idCounter++;
        this.name = name;
        this.booleanFluents = booleanFluents;
        this.integerFluents = integerFluents;
        this.objectFluents = objectFluents;
    }

    public World(World toCopy) {
        id = World.idCounter++;
        name = toCopy.getName() + "'";
        booleanFluents = new HashMap<Fluent, Boolean>(toCopy.getBooleanFluents());
        integerFluents = new HashMap<Fluent, Integer>(toCopy.getIntegerFluents());
        objectFluents = new HashMap<Fluent, String>(toCopy.getObjectFluents());
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

    public World update(Set<Assignment> assignments) {
        World world = new World(this);
        Map<Fluent, Boolean> newBooleanFluents = new HashMap<>(booleanFluents);;
        Map<Fluent, Integer> newIntegerFluents = new HashMap<>(integerFluents);;
        Map<Fluent, String> newObjectFluents = new HashMap<>(objectFluents);;
        for (Assignment assignment : assignments) {
            Fluent reference = assignment.getReference();
            if (newBooleanFluents.containsKey(assignment.getReference())) {
                newBooleanFluents.put(reference, assignment.getValue().getBooleanValue());
            }
            else if (newIntegerFluents.containsKey(assignment.getReference())) {
                newIntegerFluents.put(reference, assignment.getValue().getIntegerValue());
            }
            else if (newObjectFluents.containsKey(assignment.getReference())) {
                newObjectFluents.put(reference, assignment.getValue().getObjectValue());
            }
            else {
                throw new RuntimeException("invalid assignment ref: " + reference);
            }
            
        }
        return new World(this.name, newBooleanFluents, newIntegerFluents, newObjectFluents);
    }

    public Boolean alteredByAssignment(Assignment assignment) {
            Fluent reference = assignment.getReference();
            if (booleanFluents.containsKey(assignment.getReference())) {
                return booleanFluents.get(reference).equals(assignment.getValue());
            }
            else if (integerFluents.containsKey(assignment.getReference())) {
                return integerFluents.get(reference).equals(assignment.getValue());
            }
            else if (objectFluents.containsKey(assignment.getReference())) {
                return objectFluents.get(reference).equals(assignment.getValue());
            }
            else {
                throw new RuntimeException("invalid assignment ref: " + reference);
            }
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
        return (booleanFluents.equals(otherWorld.getBooleanFluents()) &&
                integerFluents.equals(otherWorld.getIntegerFluents()) &&
                objectFluents.equals(otherWorld.getObjectFluents()) );
    }

    public String getFullName() {
        StringBuilder str = new StringBuilder();
        if (name != null) {
            str.append(name);
            str.append(":");
        }
        str.append(id);
        return str.toString();
    }

    public String getName() {
        if (name == null) {
            throw new RuntimeException("can't get name of anonymous world: " + id);
        }
        return name;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(getFullName());
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

