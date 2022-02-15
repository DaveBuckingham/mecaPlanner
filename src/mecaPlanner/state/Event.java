package mecaPlanner.state;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

import mecaPlanner.formulae.*;


public class Event {

    private static int idCounter = 0;
    private final int id;
    private String name;

    private Formula precondition;
    private Set<Assignment> effects;

    public Event(String name, Formula precondition, Set<Assignment> effects) {
        this.name = name;
        this.precondition = precondition;
        this.effects = effects;
        this.id = Event.idCounter++;
    }

    public Event(Formula precondition, Set<Assignment> effects) {
        this(null, precondition, effects);
    }

    public Event(String name, Formula precondition) {
        this(name, precondition, new HashSet<Assignment>());
    }

    public Event(Formula precondition) {
        this(null, precondition, new HashSet<Assignment>());
    }

    public String getName() {
        return name == null ? Integer.toString(id) : name;
    }

    public int getId() {
        return this.id;
    }

    protected Formula getPrecondition() {
        return precondition;
    }

    public Set<Assignment> getEffects() {
        return this.effects;
    }

}

