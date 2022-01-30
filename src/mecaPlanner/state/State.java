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

    public State(Set<String> agents, Set<Worlds> worlds, designated) {
        super(agents, worlds, new HashSet<World>(Arrays.asList(designatedWorld)));
    }

    //public State(EpistemicState toCopy) {
    //    this(toCopy.getKripke(), toCopy.getDesignated());
    //}

    public World getDesignated() {
        assert(getDesignated().size() == 1);
        return getDesignated().iterator().next();
    }

    public Set<World> getBelievedWorlds(String agent) {
        //TODO
        return kripkeStructure.getBelievedWorlds(agent, getDesignatedWorld());
    }

    public Set<World> getSafeBelievedWorlds(String agent) {
        //TODO
        return kripkeStructure.getBelievedWorlds(agent, getDesignatedWorld());
    }

    public Set<World> getKnownWorlds(String agent) {
        //TODO
        return kripkeStructure.getKnownWorlds(agent, getDesignatedWorld());
    }

    //public boolean perspectiveEquivalent(EpistemicState other, String agent) {
    //    return getBeliefPerspective(agent).equivalent(other.getBeliefPerspective(agent));
    //}

    //public NDState getBeliefPerspective(String agent) {
    //    return new NDState(kripkeStructure, getBelievedWorlds(agent));
    //    //return new NDState(new KirpkeStructure(kripkeStructure), getBelievedWorlds(agent));
    //}




        //TODO
    public String toStringCompact() {
        return getDesignatedWorld().toString();
    }


        //TODO
    @Override
    public String toString() {
        assert(kripkeStructure.containsWorlds(getDesignatedWorlds()));
        StringBuilder str = new StringBuilder();
        str.append(kripkeStructure.toString(getDesignatedWorlds()));
        return str.toString();
    }


}

