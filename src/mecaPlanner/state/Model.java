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

    protected Set<String> agents;
    protected Set<T> designated;

    protected Set<T> points;

    protected Map<String, Map<T, Set<T>>> lessToMorePlausible;
    protected Map<String, Map<T, Set<T>>> moreToLessPlausible;

    public Model(Set<String> agents, Set<T> points, Set<T> designated) {
        assert(!points.isEmpty());
        assert(points.containsAll(designated));
        assert(!agents.isEmpty());
        for (T p : designated) {
            assert (p != null);
        }
        this.agents = agents;
        this.points = points;
        this.designated = designated;
        this.lessToMorePlausible = new HashMap<>();
        this.moreToLessPlausible = new HashMap<>();
        for (String agent : agents) {
            lessToMorePlausible.put(agent, new HashMap<>());
            moreToLessPlausible.put(agent, new HashMap<>());
            for (T t : points) {
                lessToMorePlausible.get(agent).put(t, new HashSet<>());
                lessToMorePlausible.get(agent).get(t).add(t);
                moreToLessPlausible.get(agent).put(t, new HashSet<>());
                moreToLessPlausible.get(agent).get(t).add(t);
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

    public Set<T> getDesignated() {
        return this.designated;
    }

    public T getDesignatedPoint() {
        assert(getDesignated().size() == 1);
        return getDesignated().iterator().next();
    }


    public void addMorePlausible(String agent, T lessPlausible, T morePlausible) {
        lessToMorePlausible.get(agent).get(lessPlausible).add(morePlausible);
        moreToLessPlausible.get(agent).get(morePlausible).add(lessPlausible);
    }

    public void addMorePlausible(String agent, T lessPlausible, Set<T> morePlausible) {
        for (T t : morePlausible) {
            addMorePlausible(agent, lessPlausible, t);
        }
    }

    public void addMorePlausibleTransitive(String agent, T lessPlausible, T morePlausible) {
        addMorePlausible(agent, lessPlausible, lessToMorePlausible.get(agent).get(morePlausible));
    }

    public boolean isConnected(String agent, T lessPlausible, T morePlausible) {
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
        accessible.addAll(moreToLessPlausible.get(agent).get(root));
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
                if (sample.equals(w)) {
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
    private Void splitBlocks(Set<Set<T>> partition, Set<T> splitter, String agent) {
        Set<Set<T>> oldBlocks = new HashSet<Set<T>>(partition);

        for (Set<T> block : oldBlocks) {
            Set<T> inPre = new HashSet<>();
            Set<T> notInPre = new HashSet<>();
            for (T w : block) {
                if (Collections.disjoint(lessToMorePlausible.get(agent).get(w), splitter)) {
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
                    splitBlocks(partition, splitter, agent);
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
                    reduced.addMorePlausible(agent,newSource,newMorePlausible); 
                }
            }
        }

        return reduced;
    }



//    public Model union(Model other) {
//        assert (this != other);
//
//        Set<T> unionPoints = new HashSet<T>(points);
//        unionPoints.addAll(other.getPoints());
//
//        Set<T> unionDesignated = new HashSet<T>(designated);
//        unionDesignated.addAll(other.getDesignated());
//
//        Model unionModel = new Model(agents, unionPoints, unionDesignated);
//
//        for (String agent : agents) {
//            for (T p : unionPoints) {
//            }
//            HERE
//            unionKnowledge.put(agent, knowledgeRelations.get(agent).union(other.getKnowledgeRelations().get(agent)));
//        }
//
//        return new KripkeStructure(unionTs, unionBelief, unionKnowledge);
//    }



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


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Model other = (Model) obj;

        //return equivalent(other);
        return false;
    }

    //private Boolean equivalent(Model other) {
    //    Model unioned = union(other);

    //    Set<T> otherInitials = other.getDesignated();

    //    for (Set<T> block : union.refineSystem()) {
    //        if (Collections.disjoint(block, designated) != Collections.disjoint(block, otherInitials)) {
    //            return false;
    //        }
    //    }
    //    return true;
    //}


    @Override
    public int hashCode() {
        // THERE MAY BE SOME ROOM FOR IMPROVEMENT HERE...
        return 1;
    }



    public String toString() {
        assert(points.containsAll(designated));
        StringBuilder str = new StringBuilder();

        //List<T> worldsSorted = getPoints().stream().collect(Collectors.toList());
        //worldsSorted.sort(Comparator.comparingInt(T::getId));

        for (T t : points) {
            if (designated.contains(t)) {
                str.append("*");
            }
            str.append(t);
            for (String agent : agents) {
                str.append(" " + agent + "{");
                for (T morePlausible : lessToMorePlausible.get(agent).get(t)) {
                    str.append(morePlausible + ",");
                }
                str.deleteCharAt(str.length() - 1);
                str.append("}");
            }
            str.append("\n");
 
        }
        return str.toString();
    }


}

