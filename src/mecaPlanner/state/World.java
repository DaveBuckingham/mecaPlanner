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

    private Set<Fluent> allFluents;

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
        setAllFluents();
    }

    public World(World toCopy) {
        this.name = toCopy.getName() + "'";
        this.booleanFluents = new HashMap<Fluent, Boolean>();
        this.integerFluents = new HashMap<Fluent, Integer>();
        this.objectFluents = new HashMap<Fluent, String>();
        this.booleanFluents.putAll(toCopy.getBooleanFluents());
        this.integerFluents.putAll(toCopy.getIntegerFluents());
        this.objectFluents.putAll(toCopy.getObjectFluents());
        setAllFluents();
    }

    private void setAllFluents() {
        this.allFluents = new HashSet<>();
        allFluents.addAll(booleanFluents.keySet());
        allFluents.addAll(integerFluents.keySet());
        allFluents.addAll(objectFluents.keySet());
    }
    
    public Set<Fluent> getAllFluents() {
        return allFluents;
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
            throw new RuntimeException("unknown boolean fluent: " + f);
        }
        return booleanFluents.get(f);
    }

    public Integer resolveIntegerFluent(Fluent f) {
        if (!integerFluents.containsKey(f)) {
            throw new RuntimeException("unknown integer fluent: " + f);
        }
        return integerFluents.get(f);
    }

    public String resolveObjectFluent(Fluent f) {
        if (!objectFluents.containsKey(f)) {
            throw new RuntimeException("unknown object fluent: " + f);
        }
        return objectFluents.get(f);
    }

    public Object resolveFluent(Fluent f) {
        if (booleanFluents.containsKey(f)) {
            return booleanFluents.get(f);
        }
        if (integerFluents.containsKey(f)) {
            return integerFluents.get(f);
        }
        if (objectFluents.containsKey(f)) {
            return objectFluents.get(f);
        }
        throw new RuntimeException("unknown untyped fluent: " + f);
    }


    public boolean equivalent(World otherWorld) {
        assert(booleanFluents.keySet() == otherWorld.getBooleanFluents.keySet());
        assert(integerFluents.keySet() == otherWorld.getIntegerFluents.keySet());
        assert(objectFluents.keySet() == otherWorld.getObjectFluents.keySet());
        return (booleanFluents.equals(otherWorld.getBooleanFluents()) &&
                integerFluents.equals(otherWorld.getIntegerFluents()) &&
                objectFluents.equals(otherWorld.getObjectFluents())
               );

        //for (Fluent booleanFluent : booleanFluents.keySet()) {
        //    if (!booleanFluents.get(booleanFluent).equals(otherWorld.resolveBooleanFluent(booleanFluent))) {
        //        return false;
        //    }
        //}
        //for (Fluent integerFluent : integerFluents.keySet()) {
        //    if (!integerFluents.get(integerFluent).equals(otherWorld.resolveBooleanFluent(integerFluent))) {
        //        return false;
        //    }
        //}
        //for (Fluent objectFluent : objectFluents.keySet()) {
        //    if (!objectFluents.get(objectFluent).equals(otherWorld.resolveBooleanFluent(objectFluent))) {
        //        return false;
        //    }
        //}
        //return true;
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

