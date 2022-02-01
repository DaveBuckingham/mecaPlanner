package mecaPlanner.state;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

import mecaPlanner.formulae.Fluent;


public class World implements java.io.Serializable {

    private static int idCounter = 0;

    private final int id;

    private final Set<Fluent> fluents;

    private String name;

    //private World child;

    public World(String name, Set<Fluent> fluents) {
        this.id = World.idCounter++;
        this.name = name;
        this.fluents = fluents;
    }

    public World(String name, Fluent ...fluents) {
        this(name, new HashSet(Arrays.asList(fluents)));
    }


    public World(String name) {
        this(name, new HashSet<Fluent>());
    }

    public World(Set<Fluent> fluents) {
        this(null, fluents);
    }

    public World(Fluent ...fluents) {
        this(new HashSet(Arrays.asList(fluents)));
    }

    public World(World toCopy) {
        id = World.idCounter++;
        name = toCopy.getName();
        fluents = new HashSet<Fluent>(toCopy.getFluents());
    }

//    private World setChild(World child) {
//        this.child = child;
//    }

//    private World getChild(){
//        return this.child;
//    }

    protected Set<Fluent> getFluents() {
        return fluents;
    }

    public World update(Set<Assignment> assignments) {
        World world = new World(this);
        Set<Fluent> newFluents = new HashSet<Fluent>(fluents);
        for (Assignment assignment : assignments) {
            if (assignment.getValue()) {
                newFluents.add(assignment.getFluent());
            }
            else {
                newFluents.remove(assignment.getFluent());
            }
        }
        return new World(null, newFluents);
    }

    public Boolean alteredByAssignment(Assignment assignment) {
        return (fluents.contains(assignment.getFluent()) ^ assignment.getValue());
    }

    public int getId() {
        return this.id;
    }

    public Boolean ground(Fluent f) {
        return fluents.contains(f);
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
        for (Fluent f : fluents) {
            str.append(f);
            str.append(", ");
        }
        if (!fluents.isEmpty()) {
            str.delete(str.length()-2, str.length());
        }
        str.append("}");
        return str.toString();
    }


}

