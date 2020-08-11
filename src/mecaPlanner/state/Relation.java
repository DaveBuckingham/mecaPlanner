package mecaPlanner.state;

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

    public void connect(World from, World to) {
        if (!edges.containsKey(from)) {
            edges.put(from, new HashSet<World>());
        }
        edges.get(from).add(to);
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
            return new HashSet<World>();
        }
        return edges.get(from);
    }

    public boolean deadEnd(World from) {
        return (edges.containsKey(from) && !edges.get(from).isEmpty());
    }

    protected Map<World, Set<World>> getEdges() {
        assert (edges != null);
        return edges;
    }

    public Relation union(Relation other) {
        assert (other != null);
        Map<World, Set<World>> unionEdges = new HashMap<>(edges);
        unionEdges.putAll(other.getEdges());
        return new Relation(unionEdges);
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
