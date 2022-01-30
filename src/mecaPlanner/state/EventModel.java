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


public class EventModel {

    Set<Event> events;
    Event designated;
    private Map<String, Map<Event, Map<Event, Formula>>> edges;

    public EventModel(Set<Event> events) {
        this.events = events;
    }

    public EventModel() {
        this(new HashSet<Event>());
    }

    public Set<Event> getEvents() {
        return events;
    }

    public Boolean addEvent(Event e) {
        if (events.contains(e)) {
            return false;
        }
        events.add(e);
        return true;
    }

    public void setDesignated(Event e) {
        assert(events.contains(e));
        designated = e;
    }

    public Event getDesignated() {
        return designated;
    }

    public void connect(String agent, Event from, Event to, Formula condition) {
        assert (from != null);
        assert (events.contains(from));
        assert (to != null);
        assert (events.contains(to));
        if (!edges.containsKey(agent)) {
            edges.put(agent, new HashMap<Event, Map<Event,Formula>>());
        }
        if (!edges.get(agent).containsKey(from)) {
            edges.get(agent).put(from, new HashMap<Event,Formula>());
        }
        edges.get(agent).get(from).put(to, condition);
    }

    public Map<Event, Formula> getConnectedEvents(String agent, Event origin) {
        return edges.get(agent).get(origin);
    }

    public EpistemicState apply(EpistemicState startState) {
        Map<World, Event> newWorldsToEvents = new HashMap();
        Map<World, World> newWorldsToOld = new HashMap();
        Set<World> newWorlds = new HashSet<>();
        World newDesignated = null;
        Map<String, Relation> newRelations = new HashMap<>();
        for (Event event : events) {
            for (World oldWorld : startState.getWorlds()) {
                if (event.getPrecondition().evaluate(oldWorld)) {
                    Set<Fluent> fluents = oldWorld.getFluents();
                    fluents.removeAll(event.getNegativeEffects());
                    fluents.addAll(event.getPositiveEffects());
                    World newWorld = new World(fluents);
                    newWorlds.add(newWorld);
                    newWorldsToEvents.put(newWorld, event);
                    newWorldsToOld.put(newWorld, oldWorld);
                    if (event == designated && oldWorld == startState.getDesignatedWorld()) {
                        newDesignated = newWorld;
                    }
                }
            }
        }
        assert(newDesignated != null);
        for (String agent : startState.getKripke().getAgents()) {
            Relation relation = new Relation();
            for (World fromWorld : newWorlds) {
                Event fromEvent = newWorldsToEvents.get(fromWorld);
                World oldFromWorld = newWorldsToOld.get(fromWorld);
                for (World toWorld : newWorlds) {
                    Event toEvent = newWorldsToEvents.get(toWorld);
                    World oldToWorld = newWorldsToOld.get(toWorld);
                    if (startState.getKripke().isConnectedBelief(agent, oldFromWorld, oldToWorld)) {
                        if (edges.get(agent).get(fromEvent).containsKey(toEvent)) {
                            Formula edgeCondition = edges.get(agent).get(fromEvent).get(toEvent);
                            if (edgeCondition.evaluate(startState.getKripke(), newWorldsToOld.get(fromWorld))) {
                                relation.connect(fromWorld, toWorld);
                            }
                        }
                    }
                }
            }
            newRelations.put(agent, relation);
        }
        KripkeStructure newKripke = new KripkeStructure(newWorlds, newRelations, newRelations);
        return new EpistemicState(newKripke, newDesignated);
    }
}

