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


public class Event {

    private Formula precondition;
    private Set<Assignment> effects;

    public Event() {
    }

    protected Formula getPrecondition() {
        return precondition;
    }

    public Set<Assignment> getEffects() {
        return this.effects;
    }

}

