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

    public State apply(State startState) {
        return startState;
    }
}

