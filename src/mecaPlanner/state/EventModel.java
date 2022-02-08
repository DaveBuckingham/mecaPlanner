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


public class EventModel extends Model<Event> implements Transformer {

    private String name;

    public EventModel(String name, Set<String> agents, Set<Event> events, Set<Event> designated) {
        super(agents, events, designated);
        this.name = name;
    }

    public State transition(State beforeState) {
        assert(agents.equals(beforeState.getAgents()));
        Set<World> newWorlds = new HashSet<>();
        Set<World> newDesignated = new HashSet<>();
        Map<World, World> toParentWorld = new HashMap<>();
        Map<World, Event> toParentEvent = new HashMap<>();
        for (Event event : getPoints()) {
            for (World world : beforeState.getPoints()) {
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
            for (World from : newWorlds) {
                for (World to : newWorlds) {
                    if (beforeState.isConnected(agent, toParentWorld.get(from), toParentWorld.get(to))
                    && this.isConnected(agent, toParentEvent.get(from), toParentEvent.get(to))) {
                        newState.addMorePlausible(agent, from, to);
                    }
                    else if (this.isConnected(agent, toParentEvent.get(from), toParentEvent.get(to))
                    && toParentEvent.get(from) != toParentEvent.get(to)) {
                        newState.addMorePlausible(agent, from, to);
                    }
                }
            }
        }

        return newState;
    }

    public String getSignature() {
        return name;
    }
}

