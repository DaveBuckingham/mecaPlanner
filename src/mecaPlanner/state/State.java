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


// A POINTED KRIPKE MODEL


public class State extends NDState {

    public State(Set<String> agents, Set<World> worlds, World designated) {
        super(agents, worlds, new HashSet<World>(Arrays.asList(designated)));
    }


    //public State(EpistemicState toCopy) {
    //    this(toCopy.getKripke(), toCopy.getDesignated());
    //}

    public World getDesignatedWorld() {
        return getDesignatedPoint();
    }

//    public Set<World> getBelievedWorlds(String agent) {
//        //TODO
//        return kripkeStructure.getBelievedWorlds(agent, getDesignatedWorld());
//    }
//
//    public Set<World> getSafeBelievedWorlds(String agent) {
//        //TODO
//        return kripkeStructure.getBelievedWorlds(agent, getDesignatedWorld());
//    }
//
//    public Set<World> getKnownWorlds(String agent) {
//        //TODO
//        return kripkeStructure.getKnownWorlds(agent, getDesignatedWorld());
//    }

    //public boolean perspectiveEquivalent(EpistemicState other, String agent) {
    //    return getBeliefPerspective(agent).equivalent(other.getBeliefPerspective(agent));
    //}

    public NDState getBeliefPerspective(String agent) {
        return new NDState(new HashSet<String>(agents),
                           new HashSet<World>(points),
                           getMostPlausible(agent, getDesignatedWorld()));
    }







}

