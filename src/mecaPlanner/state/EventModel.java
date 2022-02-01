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


public class EventModel implements Transformer extends Model<Event> {


    public EventModel(Set<String> agents, Set<Event> events, Set<Event> designated) {
        super(agents, events, designated);
    }

    public State transition(State beforeState) {
        assert(agents.equals(beforeState.getAgents()));
        Set<World> newWorlds = new HashSet<>();
        Set<World> newDesignated = new HashSet<>();
        for (Event event : getPoints()) {
            for (World world : beforeState.getPoints()) {
                if (event.getPrecondition().evaluate(world)) {
                    World newWorld = world.update(event.getEffects());
                    newWorlds.add(newWorld);
                    if (world == beforeState.getDesignatedWorld()) {
                         newDesignated.add(newWorld);
                    }
                }
            }
        }
        State newState = new State(agents, newWorlds, newDesignated);

        HERE: NEW EDGES...

        return newState;
    }
}

