package mecaPlanner.state;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

import mecaPlanner.Log;
import mecaPlanner.formulae.Fluent;
import mecaPlanner.formulae.Formula;
import mecaPlanner.formulae.Literal;

import org.javatuples.Triplet;


public class EventModel implements Transformer {

    private String name;
    protected List<String> agents;
    protected Set<Event> designated;
    protected Set<Event> events;

    protected Map<Triplet<String, Event, Event>, Formula> edges;

    public EventModel(String name, List<String> agents, Set<Event> events, Set<Event> designated) {
        this.name = name;
        this.agents = agents;
        this.events = events;
        this.designated = designated;
        assert (events.containsAll(designated));

        this.edges = new HashMap<>();

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
                        assert(from != null);
                        assert(to != null);
                        assert(agent != null);
                        assert(f != null);
        edges.put(new Triplet<String,Event,Event>(agent, from, to), f);
    }

    public void addEdge(String agent, Event from, Event to) {
        addEdge(agent, from, to, new Literal(true));
    }

    private Set<Event> getEvents() {
        return events;
    }

    private Formula getEdge(String agent, Event from, Event to) {
        return edges.get(new Triplet(agent,from,to));
    }

    //public boolean isConnected(String agent, Event from, Event to) {
    //    return !edges.get(new Triplet(agent,from,to)).isFalse();
    //}

    //public boolean isConnectedStrict(String agent, Event from, Event to) {
    //    return  (isConnected(agent,from,to) && !isConnected(agent,to,from));
    // }

    public State transition(State beforeState) {
        //Log.info("applying action: " + toString());
        assert(agents.equals(beforeState.getAgents()));
        Set<World> newWorlds = new HashSet<>();
        Set<World> newDesignated = new HashSet<>();
        Map<World, World> toParentWorld = new HashMap<>();
        Map<World, Event> toParentEvent = new HashMap<>();
        for (Event event : getEvents()) {
            for (World world : beforeState.getWorlds()) {
                if (event.getPrecondition().evaluate(beforeState, world)) {
                    String newName = world.getName() + event.getName();
                    //World newWorld = new World(newName, world.update(event.getEffects()));
                    World newWorld = new World(world.update(event.getEffects()));
                    newWorlds.add(newWorld);
                    toParentWorld.put(newWorld, world);
                    toParentEvent.put(newWorld, event);
                    if (world == beforeState.getDesignatedWorld() && designated.contains(event)) {
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
                    Formula backCondition = getEdge(agent, toEvent, fromEvent);
                    if (edgeCondition.evaluate(oldFromWorld) && edgeCondition.evaluate(oldToWorld)) {
                        if (beforeState.isConnected(agent, oldFromWorld, oldToWorld) ||
                            (beforeState.isConnected(agent, oldToWorld, oldFromWorld)  &
                            (!backCondition.evaluate(oldFromWorld) || !backCondition.evaluate(oldToWorld)))) {
                            newState.addMorePlausible(agent, newFromWorld, newToWorld);
                        }
                    }
                            
                            
                    //if (edgeCondition.evaluate(beforeState,oldFromWorld) && edgeCondition.evaluate(beforeState, oldToWorld)) {
                    //    if (beforeState.isConnected(agent, oldFromWorld, oldToWorld) ||
                    //        isConnectedStrict(agent, fromEvent, toEvent)) {
                    //        newState.addMorePlausible(agent, newFromWorld, newToWorld);
                    //    }
                    //}
                }
            }
        }

        newState.trim();
        String old = newState.toString();
        if (newState.normalize()) {
            Log.debug("transitioned state not normal");
            System.exit(1);
        }
        newState.reduce();
        return newState;
    }

//    public boolean checkRelations() {
//        return isTransitive() && isReflexive() && isWell();
//    }
//
//    private boolean isTransitive() {
//        for (String a : agents) {
//            for (Event u : events) {
//                for (Event v : events) {
//                    for (Event w : events) {
//                        if (isConnected(a,u,v) && isConnected(a,v,w) && !isConnected(a,u,w)) {
//                            return false;
//                        }
//                    }
//                }
//            }
//        }
//        return true;
//    }
//
//    private boolean isReflexive() {
//        for (String a : agents) {
//            for (Event u : events) {
//                if (!isConnected(a,u,u)) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
//
//    private boolean isWell() {
//        for (String a : agents) {
//            for (Event v : events) {
//                for (Event w : events) {
//                    if (!isConnected(a,v,w) && !isConnected(a,w,v)) {
//                        for (Event u : events) {
//                            if (isConnected(a,u,v) &&
//                                isConnected(a,u,w) &&
//                               !isConnected(a,v,w) && 
//                               !isConnected(a,w,v)) {
//                                return false;
//                            }
//                            if (isConnected(a,v,u) &&
//                                isConnected(a,w,u) &&
//                               !isConnected(a,v,w) && 
//                               !isConnected(a,w,v)) {
//                                return false;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return true;
//    }



    public String getSignature() {
        return name;
    }



}

