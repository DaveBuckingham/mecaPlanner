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


public class Model<T> implements java.io.Serializable {

    private Set<String> agents;
    private Set<T> designated;

    private Set<T> points;

    private Map<String, Map<T, Set<T>>> lessToMorePlausible;
    private Map<String, Map<T, Set<T>>> moreToLessPlausible;

    public Model(Set<String> agents, Set<T> points, Set<T> designated) {
        assert(!points.isEmpty());
        assert(points.containsAll(designated));
        assert(!agents.isEmpty());
        for (World w : designatedWorlds) {
            assert (w != null);
        }
        this.points = points;
        this.agents = agents;
        this.morePlausible = new HashMap<>();
        this.lessPlausible = new HashMap<>();
        for (String agent : agents) {
            morePlausible.put(agent, new HashMap<>());
            lessPlausible.put(agent, new HashMap<>());
            for (T t : points) {
                morePlausible.get(agent).put(t, new HashSet<>());
                lessPlausible.get(agent).put(t, new HashSet<>());
            }
        }
    }

    //public Model(Model toCopy) {
    //    this(new HashSet<String>(toCopy.getAgents(), new HashSet<T>(toCopy.getPoints());
    //    this.morePlausible = new HashMap<String, Relation>(toCopy.getMorePlausible());
    //    this.lessPlausible = new HashMap<String, Relation>(toCopy.lessPlausible());
    //}

    public Set<T> getPoints() {
        return points;
    }
    public Set<T> getPointsCopy() {
        return points;
    }

    public Set<World> getDesignated() {
        return this.designated;
    }

    public void setMorePlausible(String agent, T morePlausible, T lessPlausible) {
        lessToMorePlausible.get(agent).get(lessPlausible).add(morePlausible);
        moreToLessPlausible.get(agent).get(morePlausible).add(lessPlausible);
    }

    public boolean isMorePlausible(String agent, T morePlausible, T lessPlausible) {
        return  lessToMorePlausible.get(agent).get(lessPlausible).contains(morePlausible);
    }

    public Set<T> getMorePlausible(String agent, T lessPlausible) {
        return lessToMorePlausible.get(agent).get(lessPlausible);
    }

    public Set<T> getMostPlausible(String agent, T lessPlausible) {
        Set<T> morePlausible = getMorePlausible(agent, lessPlausible);
        Set<T> mostPlausible = new HashSet<>();
        for (T t : morePlausible) {
            if (lessToMorePlausible.get(agent).get(t).isEmpty()) {
                mostPlausible.add(t);
            }
        }
        return mostPlausible;
    }

    public Set<T> getPossible(String agent, T root) {
        Set<T> accessible = lessToMorePlausible.get(agent).get(root);
        accessible.add(moreToLessPlausible(get(agent).get(root)));
        return accessible;
    }

    public Set<String> getAgents() {
        return agents;
    }


    // MAYBE THE BINARY TREE ALG IN THE TEXTBOOK (P. 478) WOULD BE FASTER?
    private Set<Set<T>> getInitialPartition() {
        Set<Set<T>> partition = new HashSet<>();
        for (T w : points) {
            boolean foundPart = false;
            for (Set<T> part : partition) {
                assert(!part.isEmpty());
                T sample = part.iterator().next();
                if (sample.equivalent(w)) {
                    part.add(w);
                    foundPart = true;
                    break;
                }
            }
            if (!foundPart) {
                Set<T> newPart = new HashSet<>();
                newPart.add(w);
                partition.add(newPart);
            }
        }
        // ITS IMPORTANT NOT TO RETURN partition BECAUSE WE 
        // MODIFIED IT'S ELEMENTS AFTER WE INSERTED THEM
        // AND THOSE MODIFICATIONS CHANGED THEIR HASHCODES
        // SO WE MAKE A NEW HASHSET, WHICH RECOMPUTES THE HASH CODES
        return new HashSet<Set<T>>(partition);
    }


    // USE A SPLITTER AND A RELATION TO REDUCE ALL THE BLOCKS IN THE PARTITION
    // THIS IS LIKE THE "REFINE" OPERATOR IN DEFINITION 7.35, EXCEPT THAT
    // 1. WE APPLY THE REDUCE OPERATION TO EVERY BLOCK IN THE PARTITION,
    // I.E. THIS INCLUDES THE "FOR" LOOP FROM ALOGIRHTM 31 ON PAGE 487.
    // 2. WE NEED TO SPECIFY A RELATION, SINCE OUR SYSTEMS HAVE MANY RELATIONS.
    private Void splitBlocks(Set<Set<T>> partition, Set<T> splitter, Relation relation) {
        Set<Set<T>> oldBlocks = new HashSet<Set<T>>(partition);

        for (Set<T> block : oldBlocks) {
            Set<T> inPre = new HashSet<>();
            Set<T> notInPre = new HashSet<>();
            for (T w : block) {
                if (Collections.disjoint(relation.get(w), splitter)) {
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
    public Set<Set<T>> refineSystem() {
        Set<Set<T>> partition = getInitialPartition();
        Set<Set<T>> oldBlocks;
        do {
            oldBlocks = new HashSet<Set<T>>(partition);
            for (Set<T> splitter : oldBlocks) {
                for (String agent : agents) {
                    splitBlocks(partition, splitter, lessToMorePlausible.get(agent));
                }
            }
        } while (partition.size() != oldBlocks.size());
        return partition;
    }


    // THIS IS THE TOP-LEVEL FOR REDUCING THE KRIPKE STRUCTURE.
    // WE START BY CALLING THE QUOTIENTING ALGORITHM,
    // WHICH GIVES US A PARTITIONING OF THE WORLDS THAT IS
    // MAXIMALLY GRANULAR WHILE MAINTAINING BISIMILARITY.
    // WE USE THE PARTITIONING TO DO THREE THINGS:
    // 1. MAKE OUR NEW WORLDS, ONE FOR EACH PARTITION.
    // 2. BUILD THE RELATIONS OVER OUR NEW WORLDS
    // 3. MAKE AND RETURN A MAP FROM OUR OLD WORLDS TO THE NEW ONES,
    //    THE NDSTATE WILL USE THIS MAP TO FIND THE NEW DESIGNATED WORLD
    // 3. INSTEAD, SET OUR NEW DESIGNATED WORLDS ACCORDING TO THE OLD ONES
    public Model reduce() {
        Set<Set<T>> partition = refineSystem();

        Map <T, T> oldToNew = new HashMap<>();

        Set<T> newPoints = new HashSet<>();

        for (Set<T> block : partition) {
            T newPoint = block.iterator().next();
            newPoints.add(newPoint);
            for (T oldPoint : block) {
                oldToNew.put(oldPoint, newPoint);
            }
        }

        Set<T> newDesignated = new HashSet<>();

        for (T d : designated) {
            newDesignated.add(oldToNew.get(d));
        }

        Model reduced = new Model(agents, newPoints, newDesignated);

        for (Map.Entry<T, T> entry : oldToNew.entrySet()) {
            T oldSource = entry.getKey();
            T newSource = entry.getValue();
            for (String agent : agents) {
                for (T oldMorePlausible : lessToMorePlausible.get(agent).get(oldSource)) {
                    T newMorePlausible = oldToNew.get(oldMorePlausible);
                    reduced.setMorePlausible(agent,newMorePlausible,newSource); 
                }
            }
        }

        return reduced;
    }



    //public KripkeStructure union(KripkeStructure other) {
    //    assert (this != other);

    //    Set<T> unionTs = new HashSet<T>(points);
    //    unionTs.addAll(other.getPoints());

    //    Map<String, Relation> unionBelief = new HashMap<>();
    //    Map<String, Relation> unionKnowledge = new HashMap<>();
    //    for (String agent : agents) {
    //        unionBelief.put(agent, beliefRelations.get(agent).union(other.getBeliefRelations().get(agent)));
    //        unionKnowledge.put(agent, knowledgeRelations.get(agent).union(other.getKnowledgeRelations().get(agent)));
    //    }

    //    return new KripkeStructure(unionTs, unionBelief, unionKnowledge);
    //}



    public void forceCheck() {
        if (!checkRelations()) {
            System.out.println("Failed Model:");
            System.out.println(toString());
            System.exit(1);
        }
    }

    public boolean checkRelations() {
        return true;
    }


    public String toString(Set<T> designated) {

        assert(points.containsAll(designated));
        StringBuilder str = new StringBuilder();

        //List<T> worldsSorted = getPoints().stream().collect(Collectors.toList());
        //worldsSorted.sort(Comparator.comparingInt(T::getId));

        for (T t : points) {
            if (designated.contains(t)) {
                str.append("*");
            }
            str.append(t);
            str.append("\n");
            for (String agent : agents) {
                str.append(agent + "=(");
                    for (T root : points) {
                        str.append(root + "->{");
                        for (T morePlausible : lessToMorePlausible.get(agent).get(t)) {
                            str.append("o,");
                        }
                        str.deleteCharAt(str.length() - 1);
                        str.append("} ");
                    }
                str.append(")");
            }
 
        }
        return str.toString();
    }

    public String toString() {
        return this.toString(new HashSet<T>());
    }

}

