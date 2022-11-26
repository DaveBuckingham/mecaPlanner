package mecaPlanner.state;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;

import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Collections;



public class PointedPlausibilityState extends PlausibilityState {

    public State(List<String> agents, Set<World> worlds, World designated) {
        super(agents, worlds, new HashSet<World>(Arrays.asList(designated)));
    }


    public World getDesignatedWorld() {
        assert(getDesignated().size() == 1);
        return getDesignated().iterator().next();
    }

    public PlausibilityState getBeliefPerspective(String agent) {
        PlausibilityState perspective =  new PlausibilityState(agents, new HashSet<World>(worlds), getBelieved(agent, getDesignatedWorld()));
        perspective.trim();
        return perspective;
    }


}

