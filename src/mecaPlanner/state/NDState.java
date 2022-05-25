package mecaPlanner.state;


import mecaPlanner.Log;
import mecaPlanner.formulae.Formula;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Stack;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Collections;
import java.util.Objects;

import mecaPlanner.formulae.Fluent;

//import org.javatuples.Triplet;





public class NDState implements java.io.Serializable {



    protected List<String> agents;
    protected Set<World> designated;

    protected Set<World> worlds;

    protected Map<String, Map<World, Set<World>>> lessToMorePlausible;
    protected Map<String, Map<World, Set<World>>> moreToLessPlausible;

    //protected Map<String, Set<Set<World>>> epistemicClasses;

    protected Map<Set<Fluent>, Set<World>> valuationClasses;


    public NDState(List<String> agents, Set<World> worlds, Set<World> designated) {
        assert(!worlds.isEmpty());
        assert(worlds.containsAll(designated));
        assert(!agents.isEmpty());
        for (World p : designated) {
            assert (p != null);
        }
        this.agents = agents;
        this.worlds = worlds;
        this.designated = designated;
        this.lessToMorePlausible = new HashMap<>();
        this.moreToLessPlausible = new HashMap<>();
        for (String agent : agents) {
            lessToMorePlausible.put(agent, new HashMap<>());
            moreToLessPlausible.put(agent, new HashMap<>());
            for (World t : worlds) {
                lessToMorePlausible.get(agent).put(t, new HashSet<>());
                lessToMorePlausible.get(agent).get(t).add(t);
                moreToLessPlausible.get(agent).put(t, new HashSet<>());
                moreToLessPlausible.get(agent).get(t).add(t);
            }
        }

        //this.epistemicClasses = new HashMap<>();
        //for (String agent : agents) {
        //    epistemicClasses.put(agent, new HashSet<>());
        //    for (World w : worlds) {
        //        Set<World> selfSet = new HashSet<>();
        //        selfSet.add(w);
        //        epistemiClasses.get(agent).add(selfSet);
        //    }
        //}

        this.valuationClasses = new HashMap<>();
        for (World w : worlds) {
            Set<Fluent> valuation = w.getFluents();
            if (!valuationClasses.containsKey(valuation)) {
                valuationClasses.put(valuation, new HashSet<>());
            }
            valuationClasses.get(valuation).add(w);
        }
        
    }

    //public NDState(NDState toCopy) {
    //    model = new NDState(toCopy.getNDState());
    //    designated = new HashSet<World>();
    //    for (World d : toCopy.getDesignatedWorlds()) {
    //        designated.add(d.getChild());
    //    }
    //}


    //public NDState(NDState toCopy) {
    //    this(new HashSet<String>(toCopy.getAgents(), new HashSet<World>(toCopy.getWorlds());
    //    this.morePlausible = new HashMap<String, Relation>(toCopy.getMorePlausible());
    //    this.lessPlausible = new HashMap<String, Relation>(toCopy.lessPlausible());
    //}

    public Set<World> getWorlds() {
        return worlds;
    }
    public Set<World> getWorldsCopy() {
        return worlds;
    }

    public Set<World> getDesignated() {
        return this.designated;
    }


    private void setMorePlausible(String agent, World lessPlausible, Set<World> morePlausible) {
        assert (worlds.contains(lessPlausible));
        assert (worlds.containsAll(morePlausible));
        lessToMorePlausible.get(agent).put(lessPlausible, morePlausible);
    }

    private void setLessPlausible(String agent, World morePlausible, Set<World> lessPlausible) {
        assert (worlds.contains(morePlausible));
        assert (worlds.containsAll(lessPlausible));
        moreToLessPlausible.get(agent).put(morePlausible, lessPlausible);
    }

    public void addMorePlausible(String agent, World lessPlausible, World morePlausible) {
        assert (worlds.contains(lessPlausible));
        assert (worlds.contains(morePlausible));
        lessToMorePlausible.get(agent).get(lessPlausible).add(morePlausible);
        moreToLessPlausible.get(agent).get(morePlausible).add(lessPlausible);
    }

    public void addMorePlausible(String agent, World lessPlausible, Set<World> morePlausible) {
        for (World t : morePlausible) {
            addMorePlausible(agent, lessPlausible, t);
        }
    }

    public void addMorePlausibleTransitive(String agent, World lessPlausible, World morePlausible) {
        for (World w : getLessPlausible(agent, lessPlausible)) {
            addMorePlausible(agent, w, lessToMorePlausible.get(agent).get(morePlausible));
        }
    }





    public boolean isConnected(String agent, World lessPlausible, World morePlausible) {
        return  lessToMorePlausible.get(agent).get(lessPlausible).contains(morePlausible);
    }

    public boolean isConnectedStrict(String agent, World lessPlausible, World morePlausible) {
        return  (lessToMorePlausible.get(agent).get(lessPlausible).contains(morePlausible) && 
                 !lessToMorePlausible.get(agent).get(morePlausible).contains(lessPlausible));
    }

    public Set<World> getMorePlausible(String agent, World lessPlausible) {
        return lessToMorePlausible.get(agent).get(lessPlausible);
    }

    public Set<World> getLessPlausible(String agent, World morePlausible) {
        return moreToLessPlausible.get(agent).get(morePlausible);
    }

    public Set<World> getMostPlausible(String agent, World lessPlausible) {
        return getMinimum(agent, getMorePlausible(agent, lessPlausible));
    }

    //private boolean isMorePlausible(String agent, Set<World> more, Set<World> less) {
    //    for (World l : less) {
    //        if (!lessToMorePlausible.get(agent).get(l).containsAll(more)) {
    //            return false;
    //        }
    //    }
    //    return true;
    //}

    private Set<World> getMinimum(String agent, Set<World> worlds) {
        Set<World> min = new HashSet<>();
        for (World u : worlds) {
            boolean isMinimal = true;
            for (World v : worlds) {
                if (!isConnected(agent, v, u)) {
                    isMinimal = false;
                    break;
                }
            }
            if (isMinimal) {
                min.add(u);
            }
        }
        assert(!min.isEmpty());
        return min;
    }

    public Set<World> getPossible(String agent, World root) {
        assert(worlds.contains(root));
        Set<World> accessible = new HashSet<World>(lessToMorePlausible.get(agent).get(root));
        accessible.addAll(moreToLessPlausible.get(agent).get(root));
        return accessible;
    }

    public List<String> getAgents() {
        return agents;
    }

    public Set<State> getStates() {
        Set<State> states = new HashSet<State>();
        for (World w : designated) {
            State subState = new State(agents, worlds, w);
            for (String agent : agents) {
                for (World f : getWorlds()) {
                    for (World t : getMorePlausible(agent, f)) {
                        subState.addMorePlausible(agent, f, t);
                    }
                }
            }
            states.add(subState);
        }
        return states;
    }






//     // MAYBE THE BINARY TREE ALG IN THE TEXTBOOK (P. 478) WOULD BE FASTER?
//     private Set<Set<World>> getInitialPartition() {
//         Set<Set<World>> partition = new HashSet<>();
//         for (World w : worlds) {
//             boolean foundPart = false;
//             for (Set<World> part : partition) {
//                 assert(!part.isEmpty());
//                 World sample = part.iterator().next();
//                 if (sample.equals(w) && epistemicallyConnected(w,sample)) {
//                     part.add(w);
//                     foundPart = true;
//                     break;
//                 }
//             }
//             if (!foundPart) {
//                 Set<World> newPart = new HashSet<>();
//                 newPart.add(w);
//                 partition.add(newPart);
//             }
//         }
//         // ITS IMPORTANT NOT TO RETURN partition BECAUSE WE 
//         // MODIFIED IT'S ELEMENTS AFTER WE INSERTED THEM
//         // AND THOSE MODIFICATIONS CHANGED THEIR HASHCODES
//         // SO WE MAKE A NEW HASHSET, WHICH RECOMPUTES THE HASH CODES
//         return new HashSet<Set<World>>(partition);
//     }

//     private Set<T> getContainingSet(Set<Set<T>> sets, T item) {
//         for (Set<T> set : sets) {
//             if (set.contains(item)) {
//                 return set;
//             }
//         }
//         return new HashSet<T>;
//    }

    private boolean equivalentForAllAgents(Set<World> block, World w) {
        for (String a : agents) {
            if (!getPossible(a, w).containsAll(block)) {
                return false;
            }
        }
        return true;
    }

    private boolean allMorePlausible(String agent, Set<World> less, Set<World> more) {
        for (World l : less) {
            for (World m : more) {
                if (!isConnected(agent, l, m)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean anyMorePlausible(String agent, Set<World> less, Set<World> more) {
        for (World l : less) {
            for (World m : more) {
                if (isConnected(agent, l, m)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean normalize() {
        Map<String, Map<World, Set<World>>> newLessToMorePlausible = new HashMap<>();
        Map<String, Map<World, Set<World>>> newMoreToLessPlausible = new HashMap<>();

        Set<Set<World>> allAgentEpistemicClasses = new HashSet<>();

        for (Set<World> equiValuated : valuationClasses.values()) {
            Set<Set<World>> blocks = new HashSet<>();
            for (World w : equiValuated) {
                boolean foundBlock = false;
                for (Set<World> block : blocks) {
                    if (equivalentForAllAgents(block, w)) {
                        foundBlock = true;
                        block.add(w);
                        break;
                    }
                }
                if (!foundBlock) {
                    Set<World> newBlock = new HashSet<>();
                    newBlock.add(w);
                    blocks.add(newBlock);
                }
            }

            allAgentEpistemicClasses.addAll(blocks);
        }


        for (String agent : agents) {
            newLessToMorePlausible.put(agent, new HashMap<>());
            newMoreToLessPlausible.put(agent, new HashMap<>());
            for (World t : worlds) {
                newLessToMorePlausible.get(agent).put(t, new HashSet<>());
                newMoreToLessPlausible.get(agent).put(t, new HashSet<>());
            }
        }

        for (String agent : agents) {
            for (Set<World> block : allAgentEpistemicClasses) {
                for (Set<World> blockPrime : allAgentEpistemicClasses) {
                    Set<World> blockMinimum = getMinimum(agent, block);
                    Set<World> blockMinimumPrime = getMinimum(agent, blockPrime);
                    assert(allMorePlausible(agent, blockMinimum, blockMinimumPrime) == anyMorePlausible(agent, blockMinimum, blockMinimumPrime));
                    if (anyMorePlausible(agent, blockMinimum, blockMinimumPrime)) {
                         for (World w : block) {
                             for (World wPrime : blockPrime) {
                                 newLessToMorePlausible.get(agent).get(w).add(wPrime);
                                 newMoreToLessPlausible.get(agent).get(wPrime).add(w);
                             }
                         }
                     }
                }
            }
        }

        boolean unchanged = lessToMorePlausible.equals(newLessToMorePlausible) && moreToLessPlausible.equals(newMoreToLessPlausible);

        if (!unchanged) {
            System.out.println("=======");
            System.out.println(this);
            System.out.println("=======");
            System.out.println(lessToMorePlausible);
            System.out.println("=======");
            System.out.println(newLessToMorePlausible);
            System.out.println("=======");
            System.out.println(moreToLessPlausible);
            System.out.println("=======");
            System.out.println(newMoreToLessPlausible);
            System.exit(0);
        }

        lessToMorePlausible = newLessToMorePlausible;
        moreToLessPlausible = newMoreToLessPlausible;
        
        return !unchanged;
    }


    // USE A SPLITTER AND A RELATION TO REDUCE ALL THE BLOCKS IN THE PARTITION
    // THIS IS LIKE THE "REFINE" OPERATOR IN DEFINITION 7.35, EXCEPT THAT
    // 1. WE APPLY THE REDUCE OPERATION TO EVERY BLOCK IN THE PARTITION,
    // I.E. THIS INCLUDES THE "FOR" LOOP FROM ALOGIRHTM 31 ON PAGE 487.
    // 2. WE NEED TO SPECIFY A RELATION, SINCE OUR SYSTEMS HAVE MANY RELATIONS.
     private Void splitBlocks(Set<Set<World>> partition, Set<World> splitter, String agent) {
         Set<Set<World>> oldBlocks = new HashSet<Set<World>>(partition);
 
         for (Set<World> block : oldBlocks) {
             Set<World> inPre = new HashSet<>();
             Set<World> notInPre = new HashSet<>();
             for (World w : block) {
 //                if (Collections.disjoint(lessToMorePlausible.get(agent).get(w), splitter)) {
 
                 boolean normalSplit = false;
                 if (normalSplit) {
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
        //Set<Set<World>> partition = getInitialPartition();
        Set<Set<World>> partition = new HashSet<>(valuationClasses.values());
        Set<Set<World>> oldBlocks;
        do {
            oldBlocks = new HashSet<Set<World>>(partition);
            for (Set<World> splitter : oldBlocks) {
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
    public NDState reduce() {
        //normalize();
        Set<Set<World>> partition = refineSystem();

        Map <World, World> oldToNew = new HashMap<>();

        Set<World> newWorlds = new HashSet<>();

        for (Set<World> block : partition) {
            World newWorld = block.iterator().next();
            newWorlds.add(newWorld);
            for (World oldWorld : block) {
                oldToNew.put(oldWorld, newWorld);
            }
        }

        Set<World> newDesignated = new HashSet<>();

        for (World d : designated) {
            newDesignated.add(oldToNew.get(d));
        }

        NDState reduced = new NDState(agents, newWorlds, newDesignated);

        for (Map.Entry<World, World> entry : oldToNew.entrySet()) {
            World oldSource = entry.getKey();
            World newSource = entry.getValue();
            for (String agent : agents) {
                for (World oldMorePlausible : lessToMorePlausible.get(agent).get(oldSource)) {
                    World newMorePlausible = oldToNew.get(oldMorePlausible);
                    reduced.addMorePlausible(agent,newSource,newMorePlausible); 
                }
            }
        }

        return reduced;
    }



    private NDState union(NDState other) {
        assert (this != other);

        assert (Collections.disjoint(worlds, other.getWorlds()));

        Set<World> unionWorlds = new HashSet<World>(worlds);
        unionWorlds.addAll(other.getWorlds());

        Set<World> unionDesignated = new HashSet<World>(designated);
        unionDesignated.addAll(other.getDesignated());

        NDState unionNDState = new NDState(agents, unionWorlds, unionDesignated);

        for (String agent : agents) {
            for (World w : worlds) {
                unionNDState.setMorePlausible(agent, w, getMorePlausible(agent, w));
                unionNDState.setLessPlausible(agent, w, getLessPlausible(agent, w));
            }
            for (World w : other.getWorlds()) {
                unionNDState.setMorePlausible(agent, w, other.getMorePlausible(agent, w));
                unionNDState.setLessPlausible(agent, w, other.getLessPlausible(agent, w));
            }
        }
        return unionNDState;
    }




    public void checkRelations() {
        checkTransitive();
        checkReflexive();
        checkWell();
    }

    public void checkTransitive() {
        for (String a : agents) {
            for (World u : worlds) {
                for (World v : getMorePlausible(a,u)) {
                    for (World w : getMorePlausible(a,v)) {
                        if (!isConnected(a,u,w)) {
                            throw new RuntimeException(a + " not transitive: " + u.getName() + ", " + v.getName() + ", " + w.getName());
                        }
                    }
                }
            }
        }
    }

    public void checkReflexive() {
        for (String a : agents) {
            for (World u : worlds) {
                if (!isConnected(a,u,u)) {
                    throw new RuntimeException(a + " not reflexive: " + u.getName());
                }
            }
        }
    }

    public void checkWell() {
        for (String a : agents) {
            for (World u : worlds) {
                for (World v : getMorePlausible(a,u)) {
                    for (World w : getMorePlausible(a,u)) {
                        if ((!isConnected(a,v,w)) && (!isConnected(a,w,v))) {
                            throw new RuntimeException(a + " has no edge: " + v.getName() + ", " + w.getName());
                        }
                    }
                }
                for (World v : getLessPlausible(a,u)) {
                    for (World w : getLessPlausible(a,u)) {
                        if ((!isConnected(a,v,w)) && (!isConnected(a,w,v))) {
                            throw new RuntimeException(a + " has no edge: " + v.getName() + ", " + w.getName());
                            //return false;
                        }
                    }
                }
            }
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        NDState other = (NDState) obj;

        //return equivalent(other);
        return false;
    }

    public Boolean bisimilar(NDState other) {
        if (equals(other)) {
            return true;
        }
        NDState unioned = union(other);

        Set<World> otherInitials = other.getDesignated();

        for (Set<World> block : unioned.refineSystem()) {
            if (Collections.disjoint(block, designated) != Collections.disjoint(block, otherInitials)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public int hashCode() {
        // THERE MAY BE SOME ROOM FOR IMPROVEMENT HERE...
        return 1;
    }



    public String toString() {
        StringBuilder str = new StringBuilder();

        List<World> worldsSorted = getWorlds().stream().collect(Collectors.toList());
        //worldsSorted.sort(Comparator.comparingInt(World::getId));

        Collections.sort(worldsSorted, new Comparator(){
            public int compare(Object o1, Object o2) {
                World w1 = (World) o1;
                World w2 = (World) o2;
                return w1.getName().compareTo(w2.getName());
            }
        });

        for (World t : worldsSorted) {
            str.append(designated.contains(t) ? "*" : " ");
            str.append(t);
            for (String agent : agents) {
                str.append(" " + agent + "{");
                for (World morePlausible : lessToMorePlausible.get(agent).get(t)) {
                    str.append(morePlausible.getName() + ",");
                }
                str.deleteCharAt(str.length() - 1);
                str.append("}");
            }
            str.append("\n");
 
        }
        str.deleteCharAt(str.length() - 1);
        return str.toString();
    }



    // FIND AND REMOVE ANY WORLDS THAT ARE NOT REACHABLE FROM ANY DESIGNATED WORLD
    public Void trim() {
        Set<World> keep = new HashSet<>(designated);
        Stack<World> todo = new Stack<>();
        for (World w : designated) {
            todo.push(w);
        }
        while (!todo.isEmpty()) {
            World current = todo.pop();
            for (String a : agents) {
                for (World next : lessToMorePlausible.get(a).get(current)) {
                    if (!keep.contains(next)) {
                        keep.add(next);
                        todo.push(next);
                    }
                }
                for (World next : moreToLessPlausible.get(a).get(current)) {
                    if (!keep.contains(next)) {
                        keep.add(next);
                        todo.push(next);
                    }
                }
            }
        }
        for (String a : agents) {
            for (World w : new HashSet<World>(worlds)) {
                if (!keep.contains(w)) {
                    worlds.remove(w);
                    lessToMorePlausible.get(a).remove(w);
                    moreToLessPlausible.get(a).remove(w);
                    Set<Fluent> fluents = w.getFluents();
                    valuationClasses.get(fluents).remove(w);
                    if (valuationClasses.get(fluents).isEmpty()) {
                        valuationClasses.remove(fluents);
                    }
                }
            }
        }
        return null;
    }





}

