package mecaPlanner.state;

import mecaPlanner.agents.Agent;

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

    public Set<World> getBelievedWorlds(Agent agent) {
        return kripkeStructure.getBelievedWorlds(agent, designatedWorld);
    }

    public boolean perspectiveEquivalent(EpistemicState other, Agent agent) {
        return getBeliefPerspective(agent).equivalent(other.getBeliefPerspective(agent));
    }

    public NDState getBeliefPerspective(Agent agent) {
        return new NDState(kripkeStructure, getBelievedWorlds(agent));
    }

    public Set<World> getKnownWorlds(Agent agent) {
        return kripkeStructure.getKnownWorlds(agent, designatedWorld);
    }



    public String toStringCompact() {
        return designatedWorld.toString();
    }


//    public String toStringCompact() {
//        StringBuilder str = new StringBuilder();
//        List<World> worldsSorted = kripkeStructure.getWorlds().stream().collect(Collectors.toList());
//        worldsSorted.sort(Comparator.comparingInt(World::getId));
//        str.append("t=");
//        str.append(time.toString());
//        str.append("\n");
//        for (World world : worldsSorted) {
//            if (world.equals(designatedWorld)) {
//                str.append("*");
//            }
//            str.append(world);
//            str.append(" --> ");
//            for (Agent agent : domain.getAllAgents()) {
//                str.append(agent.getName());
//                str.append("(");
//                Set<World> accessibleWorlds = kripkeStructure.getBelievedWorlds(agent, world);
//                if (!accessibleWorlds.isEmpty()) {
//                    for (World to : accessibleWorlds) {
//                        str.append(to.getId());
//                        str.append(",");
//                    }
//                    str.deleteCharAt(str.length() - 1);
//                }
//                str.append(")");
//            }
//            str.append("\n");
//        }
//        return str.toString();
//    }
   

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(kripkeStructure.toString(getDesignatedWorlds()));
        return str.toString();
    }


}

