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


public class EpistemicState extends NDState {

    private World designatedWorld;

    public EpistemicState(KripkeStructure kripkeStructure, World designatedWorld) {
        super(kripkeStructure, new HashSet<World>(Arrays.asList(designatedWorld)));
        this.designatedWorld = designatedWorld;
    }

    public EpistemicState(EpistemicState toCopy) {
        this(toCopy.getKripke(), toCopy.getDesignatedWorld());
    }

    public World getDesignatedWorld() {
        return this.designatedWorld;
    }

    public Set<World> getBelievedWorlds(String agent) {
        return kripkeStructure.getBelievedWorlds(agent, designatedWorld);
    }

    public boolean perspectiveEquivalent(EpistemicState other, String agent) {
        return getBeliefPerspective(agent).equivalent(other.getBeliefPerspective(agent));
    }

    public NDState getBeliefPerspective(String agent) {
        return new NDState(kripkeStructure, getBelievedWorlds(agent));
        //return new NDState(new KirpkeStructure(kripkeStructure), getBelievedWorlds(agent));
    }

    public Set<World> getKnownWorlds(String agent) {
        return kripkeStructure.getKnownWorlds(agent, designatedWorld);
    }



    public String toStringCompact() {
        return designatedWorld.toString();
    }

//    public Boolean resolveBoolean(Fluent f) {
//        return designatedWorld.resolveBoolean(f);
//    }
//
//    public Integer resolveInteger(Fluent f) {
//        return designatedWorld.resolveInteger(f);
//    }
//
//    public String resolveObject(Fluent f) {
//        return designatedWorld.resolveObject(f);
//    }





    @Override
    public String toString() {
        assert(kripkeStructure.containsWorlds(getDesignatedWorlds()));
        StringBuilder str = new StringBuilder();
        str.append(kripkeStructure.toString(getDesignatedWorlds()));
        return str.toString();
    }


}

