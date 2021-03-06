package mecaPlanner.state;

import mecaPlanner.Log;

import java.util.List;
import java.util.ArrayList;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

import java.util.stream.Collectors;
import java.util.Comparator;


public class Relation implements java.io.Serializable {


    private Map<World, Set<World>> edges;


    public Relation() {
        this.edges = new HashMap<World, Set<World>>();
    }

    public Relation(Map<World, Set<World>> edgesParam) {
        this.edges = new HashMap<World, Set<World>>(edgesParam);
    }

    public Relation(Relation toCopy) {
        edges = new HashMap<World, Set<World>>();
        for (World fromWorld : toCopy.getEdges().keySet()) {
            edges.put(fromWorld, new HashSet<World>(toCopy.getEdges().get(fromWorld)));
        }
    }

    public void connect(World from, World to) {
        assert (from != null);
        assert (to != null);
        if (!edges.containsKey(from)) {
            edges.put(from, new HashSet<World>());
        }
        edges.get(from).add(to);
    }

    public void connect(World from, Set<World> to) {
        assert (from != null);
        assert (to != null);
        if (!edges.containsKey(from)) {
            edges.put(from, new HashSet<World>());
        }
        edges.get(from).addAll(to);
    }

    public void connectBack(World from, World to) {
        connect(from, to);
        connect(to, from);
    }

    public boolean isConnected(World from, World to) {
        return (edges.containsKey(from) && edges.get(from).contains(to));
    }

    public Set<World> getToWorlds(World from) {
        if(!edges.containsKey(from)) {
            //throw new RuntimeException("no to worlds from " + from.toString());
            return new HashSet<World>();
        }
        return edges.get(from);
    }

    // OBVIOUSLY NOT THAT EFFICIENT
    public Set<World> getFromWorlds(World to) {
        Set<World> fromWorlds = new HashSet<>();
        for (Map.Entry<World,Set<World>> entry : edges.entrySet()) {
            if (entry.getValue().contains(to)) {
                fromWorlds.add(entry.getKey());
            }
        }
        return fromWorlds;
    }

    // THERE ARE TWO WAYS TO HAVE A DEAD END AT WORLD U: U NOT IN KEYS, OR U MAPS TO EMPTY SET,
    // MAYBE WE SHOULD CHANGE THIS AND REQUIRE EVERY WORLD IN KEYS...
    public boolean deadEnd(World from) {
        return (!edges.containsKey(from) || edges.get(from).isEmpty());
    }

    protected Map<World, Set<World>> getEdges() {
        assert (edges != null);
        return edges;
    }

    public void add(Relation other) {
        assert (other != null);
        edges.putAll(other.getEdges());
    }

    public Relation union(Relation other) {
        assert (other != null);
        Map<World, Set<World>> unionEdges = new HashMap<>(edges);
        unionEdges.putAll(other.getEdges());
        return new Relation(unionEdges);
    }


    // u implies some u->v
    public boolean checkSerial(Set<World> worlds) {
        for (World u : worlds) {
            if (getToWorlds(u).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // u->v and v->z implies u->z
    public boolean checkTransitive(Set<World> worlds) {
        for (World u : worlds) {
            for (World v : worlds) {
                for (World z : worlds) {
                    if (isConnected(u, v) && isConnected(v, z)) {
                        if (!isConnected(u, z)) {
                            Log.severe("not transitive: " + u.getName() + ", " + v.getName() + ", " + z.getName());
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    // u->v and u->z implies v->z
    public boolean checkEuclidean(Set<World> worlds) {
        for (World u : worlds) {
            for (World v : worlds) {
                for (World z : worlds) {
                    if (isConnected(u, v) && isConnected(u, z)) {
                        if (!isConnected(v, z)) {
                            Log.severe("not euclidean: " + u.getName() + ", " + v.getName() + ", " + z.getName());
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean checkReflexive(Set<World> worlds) {
        for (World u : worlds) {
            if (!isConnected(u, u)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkSymmetric(Set<World> worlds) {
        for (World u : worlds) {
            for (World v : worlds) {
                if (isConnected(u, v)) {
                    if (!isConnected(v, u)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }



    public String toString() {
        StringBuilder str = new StringBuilder();
        for (World fromWorld : edges.keySet()) {
            str.append(fromWorld.getId());
            str.append(" -> ");
            for (World toWorld : edges.get(fromWorld)) {
                str.append(toWorld.getId());
                str.append(", ");
            }
            str.append("\n");
        }
        return str.toString();
    }

}
