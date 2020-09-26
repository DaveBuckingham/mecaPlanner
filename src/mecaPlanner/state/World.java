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

    private final Map<Fluent, Object> fluents;

    private String name;

    public World(String name, Map<Fluent, Object> fluents) {
        this.id = World.idCounter++;
        this.name = name;
        this.fluents = fluents;
    }

    public World(World toCopy) {
        id = World.idCounter++;
        name = toCopy.getName();
        fluents = new HashMap<Fluent, Object>(toCopy.getFluents());
    }

    protected Map<Fluent, Object> getFluents() {
        return fluents;
    }

    public World update(Set<Assignment> assignments) {
        World world = new World(this);
        Map<Fluent, Object> newFluents = new HashMap<>(fluents);
        for (Assignment assignment : assignments) {
            Fluent reference = assignment.getReference();
            if (!newFluents.containsKey(assignment.getReference())) {
                throw new RuntimeException("invalid assignment ref: " + reference);
            }
            newFluents.put(reference, assignment.getValue().evaluate(this));
        }
        return new World(null, newFluents);
    }

    public Boolean alteredByAssignment(Assignment assignment) {
        Fluent reference = assignment.getReference();
        if (!fluents.containsKey(assignment.getReference())) {
            throw new RuntimeException("invalid assignment ref: " + reference);
        }
        return fluents.get(reference).equals(assignment.getValue().evaluate(this));
    }

    public int getId() {
        return this.id;
    }

    public Object ground(Fluent f) {
        if (!fluents.containsKey(f)) {
            throw new RuntimeException("unknown fluent: " + f);
        }
        return fluents.get(f);
    }


    public boolean equivalent(World otherWorld) {
        return fluents.equals(otherWorld.getFluents());
    }

    public String getName() {
        return name == null ? Integer.toString(id) : name;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(getName());
        str.append("{");
        for (Map.Entry<Fluent, Object> entry : fluents.entrySet()) {
            str.append(entry.getKey().toString() + "==" + entry.getValue().toString());
            str.append(", ");
        }
        str.delete(str.length()-2, str.length());
        str.append("}");
        return str.toString();
    }


}

