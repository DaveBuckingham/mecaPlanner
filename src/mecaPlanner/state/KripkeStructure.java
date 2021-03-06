package mecaPlanner.state;

import mecaPlanner.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Collections;


// THE BISIMULATION QUOTIENTING ALGORITHM IS FROM "PRINCIPLES OF MODEL CHECKING" BY BAIER AND KATOEN.


public class KripkeStructure implements java.io.Serializable {

    private Map<String, Relation> beliefRelations;

    private Map<String, Relation> knowledgeRelations;

    private Set<String> agents;

    private Set<World> worlds;


    public KripkeStructure(Set<World> worlds, Map<String, Relation> belief, Map<String, Relation> knowledge) {

        assert(!worlds.isEmpty());
        assert(belief.keySet().equals(knowledge.keySet()));

        this.worlds = worlds;

        this.beliefRelations = belief;

        this.knowledgeRelations = knowledge;

        this.agents = new HashSet<>(belief.keySet());

        for (World w : worlds) {
            assert (!getChildren(w).isEmpty());
        }

    }

    public KripkeStructure(KripkeStructure toCopy) {
        this.worlds = new HashSet<World>(toCopy.getWorlds());
        this.beliefRelations = new HashMap<String, Relation>(toCopy.getBeliefRelations());
        this.knowledgeRelations = new HashMap<String, Relation>(toCopy.getKnowledgeRelations());
        this.agents = new HashSet<String>(toCopy.getAgents());
    }

    public KripkeStructure(Set<World> worlds, Set<String> agents) {
        assert(!worlds.isEmpty());
        this.worlds = worlds;

        this.beliefRelations = new HashMap<String, Relation>();
        this.knowledgeRelations = new HashMap<String, Relation>();
        this.agents = agents;

        for (String agent : agents) {
            beliefRelations.put(agent, new Relation());
            knowledgeRelations.put(agent, new Relation());
        }

    }



    public Set<World> getWorlds() {
        return worlds;
    }

    public boolean containsWorld(World member) {
        return worlds.contains(member);
    }

    public boolean containsWorlds(Set<World> subset) {
        return this.worlds.containsAll(subset);
    }

    public void connectBelief(String agent, World from, World to) {
        beliefRelations.get(agent).connect(from,to);
    }

    public void connectKnowledge(String agent, World from, World to) {
        knowledgeRelations.get(agent).connect(from,to);
    }

    public void connectBelief(String agent, World from, Set<World> to) {
        beliefRelations.get(agent).connect(from,to);
    }

    public void connectKnowledge(String agent, World from, Set<World> to) {
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

    public Map<String, Relation> getBelieRelations() {
        return beliefRelations;
    }

    public Set<World> getKnownWorlds(String agent, World from) {
        return knowledgeRelations.get(agent).getToWorlds(from);
    }

    public Map<String, Relation> getKnowledgeRelations() {
        return knowledgeRelations;
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

    public Set<String> getAgents() {
        return agents;
    }


    // MAYBE THE BINARY TREE ALG IN THE TEXTBOOK (P. 478) WOULD BE FASTER?
    private Set<Set<World>> getInitialPartition() {
        Set<Set<World>> partition = new HashSet<>();
        for (World w : worlds) {
            boolean foundPart = false;
            for (Set<World> part : partition) {
                assert(!part.isEmpty());
                World sample = part.iterator().next();
                if (sample.equivalent(w)) {
                    part.add(w);
                    foundPart = true;
                    break;
                }
            }
            if (!foundPart) {
                Set<World> newPart = new HashSet<>();
                newPart.add(w);
                partition.add(newPart);
            }
        }
        // ITS IMPORTANT NOT TO RETURN partition BECAUSE WE 
        // MODIFIED IT'S ELEMENTS AFTER WE INSERTED THEM
        // AND THOSE MODIFICATIONS CHANGED THEIR HASHCODES
        // SO WE MAKE A NEW HASHSET, WHICH RECOMPUTES THE HASH CODES
        return new HashSet<Set<World>>(partition);
    }


    // USE A SPLITTER AND A RELATION TO REDUCE ALL THE BLOCKS IN THE PARTITION
    // THIS IS LIKE THE "REFINE" OPERATOR IN DEFINITION 7.35, EXCEPT THAT
    // 1. WE APPLY THE REDUCE OPERATION TO EVERY BLOCK IN THE PARTITION,
    // I.E. THIS INCLUDES THE "FOR" LOOP FROM ALOGIRHTM 31 ON PAGE 487.
    // 2. WE NEED TO SPECIFY A RELATION, SINCE OUR SYSTEMS HAVE MANY RELATIONS.
    private Void splitBlocks(Set<Set<World>> partition, Set<World> splitter, Relation relation) {
        Set<Set<World>> oldBlocks = new HashSet<Set<World>>(partition);

        for (Set<World> block : oldBlocks) {
            Set<World> inPre = new HashSet<>();
            Set<World> notInPre = new HashSet<>();
            for (World w : block) {
                if (Collections.disjoint(relation.getToWorlds(w), splitter)) {
                    notInPre.add(w);
                }
                else {
                    inPre.add(w);
                }
            }
            if ((!notInPre.isEmpty()) && (!inPre.isEmpty()))  {
                if (!partition.remove(block)) {
                    throw new RuntimeException("failed to remove a split block while computing bisimulation");
                }
                partition.add(notInPre);
                partition.add(inPre);
            }
        }
        return null;
    }

    // HERE'S THE MAIN ALGORITHM, BASICALLY ALGORITHM 31 IN THE TEXTBOOK.
    // UNLIKE THE TEXTBOOK WHERE THE TRANSITION SYSTEM HAS A SINGLE RELATION
    // WE HAVE TWO SYSTEMS PER AGENT, AND POSSIBLY MANY AGENTS,
    // SO WHEN WE REFINE THE SYSTEM WE NEED TO DO SO USING EACH RELATION.
    public Set<Set<World>> refineSystem() {
        Set<Set<World>> partition = getInitialPartition();
        Set<Set<World>> oldBlocks;
        do {
            oldBlocks = new HashSet<Set<World>>(partition);
            for (Set<World> splitter : oldBlocks) {
                for (String agent : agents) {
                    splitBlocks(partition, splitter, beliefRelations.get(agent));
                    splitBlocks(partition, splitter, knowledgeRelations.get(agent));
                }
            }
        } while (partition.size() != oldBlocks.size());
        return partition;
    }


    // THIS IS THE TOP-LEVEL FOR REDUCING THE KRIPKE STRUCTURE.
    // WE START BY CALLING THE QUOTIENTING ALGORITHM FROM,
    // WHICH GIVES US A PARTITIONING OF THE WORLDS THAT IS
    // MAXIMALLY GRANULAR WHILE MAINTAINING BISIMILARITY.
    // WE USE THE PARTITIONING TO DO THREE THINGS:
    // 1. MAKE OUR NEW WORLDS, ONE FOR EACH PARTITION.
    // 2. BUILD THE RELATIONS OVER OUR NEW WORLDS
    // 3. MAKE AND RETURN A MAP FROM OUR OLD WORLDS TO THE NEW ONES,
    //    THE NDSTATE WILL USE THIS MAP TO FIND THE NEW DESIGNATED WORLD
    public Map<World,World> reduce() {
        Set<Set<World>> partition = refineSystem();

        Map <World, World> oldWorldsToNew = new HashMap<>();

        for (Set<World> block : partition) {
            World newWorld = block.iterator().next();
            for (World oldWorld : block) {
                oldWorldsToNew.put(oldWorld, newWorld);
            }
        }

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

        this.worlds = new HashSet<World>(oldWorldsToNew.values());
        this.beliefRelations = newBeliefRelations;
        this.knowledgeRelations = newKnowledgeRelations;
        return oldWorldsToNew;
    }



    public KripkeStructure union(KripkeStructure other) {
        assert (this != other);

        Set<World> unionWorlds = new HashSet<World>(worlds);
        unionWorlds.addAll(other.getWorlds());

        Map<String, Relation> unionBelief = new HashMap<>();
        Map<String, Relation> unionKnowledge = new HashMap<>();
        for (String agent : agents) {
            unionBelief.put(agent, beliefRelations.get(agent).union(other.getBeliefRelations().get(agent)));
            unionKnowledge.put(agent, knowledgeRelations.get(agent).union(other.getKnowledgeRelations().get(agent)));
        }

        return new KripkeStructure(unionWorlds, unionBelief, unionKnowledge);
    }



    public void forceCheck() {
        if (!checkRelations()) {
            System.out.println("Failed Kripke:");
            System.out.println(toString());
            System.exit(1);
        }
    }

    public boolean checkRelations() {
        for (String agent : agents) {
            for (World f : beliefRelations.get(agent).getEdges().keySet()) {
                if (!worlds.contains(f)) {
                    Log.severe("failed check: unknown from world in b-relation: " + f.toString());
                    return false;
                }
                for (World t : beliefRelations.get(agent).getEdges().get(f)) {
                    if (!worlds.contains(t)) {
                        Log.severe("failed check: unknown to world in b-relation: " + t.toString());
                        return false;
                    }
                }
            }
            for (World f : knowledgeRelations.get(agent).getEdges().keySet()) {
                if (!worlds.contains(f)) {
                    Log.severe("failed check: unknown from world in k-relation: " + f.toString());
                    return false;
                }
                for (World t : knowledgeRelations.get(agent).getEdges().get(f)) {
                    if (!worlds.contains(t)) {
                        Log.severe("failed check: unknown to world in k-relation: " + t.toString());
                        return false;
                    }
                }
            }


            if (!beliefRelations.get(agent).checkSerial(worlds)) {
                Log.severe("failed check: serial belief for agent " + agent);
                //Log.debug(toString());
                return false;
            }
            if (!beliefRelations.get(agent).checkTransitive(worlds)) {
                Log.severe("failed check: transitive belief for agent " + agent);
                //Log.debug(toString());
                return false;
            }
            if (!beliefRelations.get(agent).checkEuclidean(worlds)) {
                Log.severe("failed check: euclidean belief for agent " + agent);
                //Log.debug(toString());
                return false;
            }

            if (!knowledgeRelations.get(agent).checkReflexive(worlds)) {
                Log.severe("failed check: reflexive knowledge for agent " + agent);
                //Log.debug(toString());
                return false;
            }
            if (!knowledgeRelations.get(agent).checkTransitive(worlds)) {
                Log.severe("failed check: transitive knowledge for agent " + agent);
                //Log.debug(toString());
                return false;
            }
            if (!knowledgeRelations.get(agent).checkSymmetric(worlds)) {
                Log.severe("failed check: symmetric knowledge for agent " + agent);
                //Log.debug(toString());
                return false;
            }
            if (!checkKb1(knowledgeRelations.get(agent), beliefRelations.get(agent), worlds)) {
                Log.severe("failed check: KB1 for agent " + agent);
                return false;
            }
            if (!checkKb2(knowledgeRelations.get(agent), beliefRelations.get(agent), worlds)) {
                Log.severe("failed check: KB2 for agent " + agent);
                return false;
            }
        }
        return true;
    }

    private boolean checkKb1(Relation knowledge, Relation belief, Set<World> worlds) {
        for (World fromWorld : worlds) {
            for (World toWorld : belief.getToWorlds(fromWorld)) {
                if (!knowledge.isConnected(fromWorld,toWorld)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkKb2(Relation knowledge, Relation belief, Set<World> worlds) {
        for (World u : worlds) {
            for (World v : knowledge.getToWorlds(u)) {
                for (World w : belief.getToWorlds(v)) {
                    if (!belief.isConnected(u,w)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }



    public String toString(Set<World> designated) {

        assert(worlds.containsAll(designated));
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

