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

    private Formula precondition;
    private Set<Assignment> effects;

    public Event(Formula precondition, Set<Fluent> deletes, Set<Fluent> adds) {
        this.precondition = precondition;
        effects = new HashSet<>();
        for (Fluent f : deletes) {
            effects.add(new Assignment(new Literal(true), f, false));
        }
        for (Fluent f : adds) {
            effects.add(new Assignment(new Literal(true), f, true));
        }
    }

    protected Formula getPrecondition() {
        return precondition;
    }

    public Set<Assignment> getEffects() {
        return this.effects;
    }

}

