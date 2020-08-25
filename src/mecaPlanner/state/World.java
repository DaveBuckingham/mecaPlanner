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

    private final Map<Fluent, Value> fluents;

    private String name;

    public World(String name,
                 Map<Fluent, Value> fluents,
                ) {
        this.id = World.idCounter++;
        this.name = name;
        this.fluentsluents = fluents;
    }

    public World(World toCopy) {
        this.name = toCopy.getName() + "'";
        this.fluents = new HashMap<Fluent, Value>(toCopy.getFluents());
    }

    public Set<Fluent> getFluents() {
        return fluents;
    }

    public int getId() {
        return this.id;
    }

    public Value resolveFluent(Fluent f) {
        if (!fluents.containsKey(f)) {
            throw new RuntimeException("unknown fluent: " + f);
        }
        return fluents.get(f);
    }



    public boolean equivalent(World otherWorld) {
        assert(fluents.keySet() == otherWorld.getFluents.keySet());
        return (fluents.equals(otherWorld.getFluents());
    }

    public String getName() {
        return name == null ? Integer.toString(id) : name;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(name == null ? id : name);
        str.append("{");
        for (Map.Entry<Fluent, Value> entry : fluents.entrySet()) {
            str.append(entry.getKey().toString() + "==" + entry.getValue().toString());
            str.append(", ");
        }
        str.deleteCharAt(str.length() - 2);
        str.append("}");
        return str.toString();
    }


}

