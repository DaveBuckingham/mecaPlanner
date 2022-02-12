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
import mecaPlanner.formulae.Formula;
import mecaPlanner.formulae.Literal;

import org.javatuples.Triplet;


public class EventModel implements Transformer {

    private String name;
    protected Set<String> agents;
    protected Set<Event> designated;
    protected Set<Event> events;

    protected Map<Triplet<String, Event, Event>, Formula> edges;

    public EventModel(String name, Set<String> agents, Set<Event> events, Set<Event> designated) {
        this.name = name;
        this.agents = agents;
        this.events = events;
        this.designated = designated;
        assert (events.containsAll(designated));

        for (String agent : agents) {
            for (Event from : events) {
                for (Event to : events) {
                    if (from == to) {
                        addEdge(agent, from, to, new Literal(true));
                    }
                    else {
                        addEdge(agent, from, to, new Literal(false));
                    }
                }
            }
        }

    }

    public void addEdge(String agent, Event from, Event to, Formula f) {
        edges.put(new Triplet(agent, from, to), f);
    }

    private Formula getEdge(String agent, Event from, Event to) {
        return edges.get(new Triplet(agent,from,to));
    }

    public boolean isConnected(String agent, Event from, Event to) {
        return !edges.get(new Triplet(agent,from,to)).isFalse();
    }

    public boolean isConnectedStrict(String agent, Event from, Event to) {
        return  (isConnected(agent,from,to) && !isConnected(agent,to,from));
     }

    public State transition(State beforeState) {
        assert(agents.equals(beforeState.getAgents()));
        Set<World> newWorlds = new HashSet<>();
        Set<World> newDesignated = new HashSet<>();
        Map<World, World> toParentWorld = new HashMap<>();
        Map<World, Event> toParentEvent = new HashMap<>();
        for (Event event : getEvents()) {
            for (World world : beforeState.getEvents()) {
                if (event.getPrecondition().evaluate(world)) {
                    World newWorld = world.update(event.getEffects());
                    newWorlds.add(newWorld);
                    toParentWorld.put(newWorld, world);
                    toParentEvent.put(newWorld, event);
                    if (world == beforeState.getDesignatedPoint() && event == getDesignatedPoint()){
                         newDesignated.add(newWorld);
                    }
                }
            }
        }
        if (newDesignated.isEmpty()) {
            throw new RuntimeException("event model has no designated event");
        }
        if (newDesignated.size() != 1) {
            throw new RuntimeException("event model has multiple designated events: " + newDesignated);
        }
        State newState = new State(agents, newWorlds, newDesignated.iterator().next());

        for (String agent : agents) {
            for (World newFromWorld : newWorlds) {
                World oldFromWorld = toParentWorld.get(newFromWorld);
                Event fromEvent = toParentEvent.get(newFromWorld);
                for (World newToWorld : newWorlds) {
                    World oldToWorld = toParentWorld.get(newToWorld);
                    Event toEvent = toParentEvent.get(newToWorld);
                    Formula edgeCondition = getEdge(agent, fromEvent, toEvent);
                    if (edgeCondition.evaluate(beforeState,oldFromWorld) && edgeCondition.evaluate(beforeState, oldToWorld)) {
                        if (beforeState.isConnected(agent, oldFromWorld, oldToWorld) ||
                            isConnectedStrict(agent, fromEvent, toEvent)) {
                            newState.addMorePlausible(agent, newFromWorld, newToWorld);
                        }
                    }
                }
            }
        }

        return newState;
    }

    public boolean checkRelations() {
        return isTransitive() && isReflexive() && isConnected();
    }

    private boolean isTransitive() {
        for (String a : agents) {
            for (Event u : points) {
                for (Event v : points) {
                    for (Event w : points) {
                        if (isConnected(a,u,v) && isConnected(a,v,w) && !isConnected(a,u,w)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean isReflexive() {
        for (String a : agents) {
            for (Point u : points) {
                if (!isConnected(a,u,u)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isWell() {
        for (String a : agents) {
            for (Point v : events) {
                for (Point w : events) {
                    if (!isConnected(a,v,w) && !isConnected(a,w,v)) {
                        for (Point u : events) {
                            if (isConnected(a,u,v) &&
                                isConnected(a,u,w) &&
                               !isConnected(a,v,w) && 
                               !isConnected(a,w,v)) {
                                return false;
                            }
                            if (isConnected(a,v,u) &&
                                isConnected(a,w,u) &&
                               !isConnected(a,v,w) && 
                               !isConnected(a,w,v)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
    }



    public String getSignature() {
        return name;
    }
}

