package mecaPlanner.state;

import mecaPlanner.formulae.FluentFormula;
import mecaPlanner.formulae.FluentAtom;
import mecaPlanner.Log;

import java.util.List;
import java.util.ArrayList;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Collections;


public class KripkeStructure implements java.io.Serializable {

    private Map<String, Relation> beliefRelations;

    private Map<String, Relation> knowledgeRelations;

    private Set<String> agents;

    private Set<World> worlds;


    // THERE'S PROBABLY A BETTER WAY TO DO THIS WITH A COPY CONSTRUCTOR,
    // BUT I COULDNT' FIGURE OUT HOW TO MAKE THE NEW SET OF WORLDS ALL IN ONE LINE.
    //public KripkeStructure copy() {
    //   Set<World> newWorlds = new HashSet<>();
    //   for (World w : worlds) {
    //       newWorlds.add(new World(w));
    //   }
    //   return new KripkeStructure(newWorlds, beliefRelations, knowledgeRelations);
    //}




    public KripkeStructure(Set<World> worlds, Map<String, Relation> belief, Map<String, Relation> knowledge) {

        assert(!worlds.isEmpty());
        assert(belief.keySet().equals(knowledge.keySet()));

        this.worlds = worlds;

        this.beliefRelations = belief;

        this.knowledgeRelations = knowledge;

        this.agents = new HashSet<>(belief.keySet());

        for (World w : worlds) {
            if (getChildren(w).isEmpty()) {
                System.out.println(toString());
            }
            assert (!getChildren(w).isEmpty());
        }

    }

    public Set<World> getWorlds() {
        return worlds;
    }

    public boolean containsWorld(World world) {
        return worlds.contains(world);
    }

    public boolean containsWorlds(Set<World> worlds) {
        return worlds.containsAll(worlds);
    }

    public void connectBelief(String agent, World from, World to) {
        beliefRelations.get(agent).connect(from,to);
    }

    public void connectKnowledge(String agent, World from, World to) {
        knowledgeRelations.get(agent).connect(from,to);
    }

    public Boolean isConnectedBelief(String agent, World from, World to) {
        return beliefRelations.get(agent).isConnected(from,to);
    }

    public Boolean isConnectedKnowledge(String agent, World from, World to) {
        return knowledgeRelations.get(agent).isConnected(from,to);
    }

    public Set<World> getBelievedWorlds(String agent, World from) {
        return beliefRelations.get(agent).getToWorlds(from);
    }

    public Set<World> getKnownWorlds(String agent, World from) {
        return knowledgeRelations.get(agent).getToWorlds(from);
    }

    public Set<World> getChildren(World world) {
        Set<World> children = new HashSet<>();
        for (String agent : agents) {
            children.addAll(getBelievedWorlds(agent, world));
            children.addAll(getKnownWorlds(agent, world));
        }
        return children;
    }

    public Map<String, Relation> getBeliefRelations() {
        return this.beliefRelations;
    }

    public Map<String, Relation> getKnowledgeRelations() {
        return this.knowledgeRelations;
    }

    // MAYBE THE BINARY TREE ALG IN THE TEXTBOOK WOULD BE FASTER?
    private Set<Set<World>> getInitialPartition() {
        Map<Set<FluentAtom>, Set<World>> mapValuation = new HashMap<>();
        for (World w : worlds) {
            if (!mapValuation.containsKey(w.getAtoms())) {
                mapValuation.put(w.getAtoms(), new HashSet<World>());
            }
            mapValuation.get(w.getAtoms()).add(w);
        }
        return new HashSet<Set<World>>(mapValuation.values());
    }

    private Set<Set<World>> refinePartition(Set<Set<World>> partition, Set<World> splitter) {
        Set<Set<World>> refined= new HashSet<>();
        for (Set<World> block : partition) {
            Set<World> inPre = new HashSet<>();
            Set<World> notInPre = new HashSet<>();
            for (World w : block) {
                if (Collections.disjoint(getChildren(w), splitter)) {
                    inPre.add(w);
                }
                else {
                    notInPre.add(w);
                }
            }
            if (!inPre.isEmpty()) {
                refined.add(inPre);
            }
            if (!notInPre.isEmpty()) {
                refined.add(notInPre);
            }
        }
        return refined;
    }

    // Baier and Katoen Alg.32 p.489
    public Set<Set<World>> refineSystem() {

        Set<Set<World>> partition = getInitialPartition();

        Set<Set<World>> oldPartition;


        do {
            oldPartition = new HashSet<Set<World>>(partition);
            for (Set<World> block : oldPartition) {
                partition = refinePartition(partition, block);
            }

        } while (partition.size() != oldPartition.size());

        return partition;
    }

    public KripkeStructure union(KripkeStructure other) {
        // IF THIS ASSERT FAILS, SEE COMMENT  IN NDState.equals()
        assert(this != other);
        Set<World> unionWorlds = new HashSet<World>(worlds);
        unionWorlds.addAll(other.getWorlds());
        assert(unionWorlds.size() == (worlds.size() + other.getWorlds().size()));

        Map<String, Relation> unionBelief = new HashMap<>();
        Map<String, Relation> unionKnowledge = new HashMap<>();

        for (String agent : agents) {
            unionBelief.put(agent, beliefRelations.get(agent).union(other.getBeliefRelations().get(agent)));
            unionKnowledge.put(agent, knowledgeRelations.get(agent).union(other.getKnowledgeRelations().get(agent)));
        }

        return new KripkeStructure(unionWorlds, unionBelief, unionKnowledge);
    }

    public boolean reduce() {
        Set<Set<World>> partition = refineSystem();
        if (partition.size() == worlds.size()) {
            return false;
        }


        Map <World, World> oldWorldsToNew = new HashMap<>();

        for (Set<World> block : partition) {
            World newWorld = new World(block.iterator().next());
            for (World oldWorld : block) {
                oldWorldsToNew.put(oldWorld, newWorld);
            }
        }

        this.worlds = new HashSet<World>(oldWorldsToNew.values());

        Map<String, Relation> newBeliefRelations = new HashMap<>();
        Map<String, Relation> newKnowledgeRelations = new HashMap<>();

        for (String agent : agents) {
            newBeliefRelations.put(agent, new Relation());
            newKnowledgeRelations.put(agent, new Relation());
        }

        for (Map.Entry<World, World> entry : oldWorldsToNew.entrySet()) {
            World oldSource = entry.getKey();
            World newSource = entry.getValue();
            for (String agent : agents) {
                for (World oldDestination : beliefRelations.get(agent).getToWorlds(oldSource)) {
                    World newDestination = oldWorldsToNew.get(oldDestination);
                    newBeliefRelations.get(agent).connect(newSource, newDestination);
                }
                for (World oldDestination : knowledgeRelations.get(agent).getToWorlds(oldSource)) {
                    World newDestination = oldWorldsToNew.get(oldDestination);
                    newKnowledgeRelations.get(agent).connect(newSource, newDestination);
                }
            }
        }

        this.beliefRelations = newBeliefRelations;
        this.knowledgeRelations = newKnowledgeRelations;
        return true;
    }


    public boolean checkRelations() {
        for (String agent : agents) {
            if (!beliefRelations.get(agent).checkSerial(worlds)) {
                Log.severe("failed check: serial belief for agent " + agent);
                Log.debug(toString());
                return false;
            }
            if (!beliefRelations.get(agent).checkTransitive(worlds)) {
                Log.severe("failed check: transitive belief for agent " + agent);
                Log.debug(toString());
                return false;
            }
            if (!beliefRelations.get(agent).checkEuclidean(worlds)) {
                Log.severe("failed check: euclidean belief for agent " + agent);
                Log.debug(toString());
                return false;
            }

            if (!knowledgeRelations.get(agent).checkReflexive(worlds)) {
                Log.severe("failed check: reflexive knowledge for agent " + agent);
                Log.debug(toString());
                return false;
            }
            if (!knowledgeRelations.get(agent).checkTransitive(worlds)) {
                Log.severe("failed check: transitive knowledge for agent " + agent);
                Log.debug(toString());
                return false;
            }
            if (!knowledgeRelations.get(agent).checkSymmetric(worlds)) {
                Log.severe("failed check: symmetric knowledge for agent " + agent);
                Log.debug(toString());
                return false;
            }
        }
        return true;
    }






    public String toString(Set<World> designated) {
        StringBuilder str = new StringBuilder();

        List<World> worldsSorted = getWorlds().stream().collect(Collectors.toList());
        worldsSorted.sort(Comparator.comparingInt(World::getId));

        for (World w : worldsSorted) {
            if (designated.contains(w)) {
                str.append("*");
            }
            str.append(w);
            str.append("\n");
            for (String agent : agents) {
                str.append("  B(" + agent + ") = ");
                List<World> believed = getBelievedWorlds(agent, w).stream().collect(Collectors.toList());
                believed.sort(Comparator.comparingInt(World::getId));
                for (World to : believed) {
                    str.append(to.getName());
                        str.append(", ");
                }
                str.deleteCharAt(str.length() - 1);
                str.append("\n");
                str.append("  K(" + agent + ") = ");
                List<World> known = getKnownWorlds(agent, w).stream().collect(Collectors.toList());
                known.sort(Comparator.comparingInt(World::getId));
                for (World to : known) {
                    str.append(to.getName());
                        str.append(", ");
                }
                str.deleteCharAt(str.length() - 1);
                str.append("\n");
            }
 
        }
        return str.toString();
    }

    public String toString() {
        return this.toString(new HashSet<World>());
    }


}

