package mecaPlanner.state;

import mecaPlanner.formulae.Formula;

import java.util.List;
import java.util.ArrayList;

import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;

import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Collections;


// A NON-POINTED KRIPKE MODEL, I.E. POSSIBLY MORE THAN ONE DESIGNATED WORLD


public class NDState implements java.io.Serializable {

    protected KripkeStructure kripkeStructure;
    private Set<World> designatedWorlds;

    public NDState(KripkeStructure kripkeStructure, Set<World> designatedWorlds) {
        assert(kripkeStructure.containsWorlds(designatedWorlds));
        for (World w : designatedWorlds) {
            assert (w != null);
        }
        this.kripkeStructure = kripkeStructure;
        this.designatedWorlds = designatedWorlds;
    }

    public NDState(NDState toCopy) {

        KripkeStructure kripkeToCopy = toCopy.getKripke();
        Map<World,World> originalToNew = new HashMap<>();
        Set<World> originalWorlds = kripkeToCopy.getWorlds();
        Set<World> newWorlds = new HashSet<World>();
        for (World original : originalWorlds) {
            World duplicate = new World(original);
            newWorlds.add(duplicate);
            originalToNew.put(original,duplicate);
        }
        beliefRelations = new HashMap<String,Relation>();
        knowledgeRelations = new HashMap<String,Relation>();
        for (String agent : kripkeToCopy.getBeliefRelations().keySet()) {
            Relation oldBelief = kripkeToCopy.getBeliefRelations().get(agent);
            Relation newBelief = new Relation();
            Relation oldKnowledge = kripkeToCopy.getKnowledgeRelations().get(agent);
            Relation newKnowledge = new Relation();
            for (World originalFrom : originalWorlds) {
                for (World originalTo : oldBelief.getToWorlds(originalFrom)) {
                    newBelief.connect(originalToNew.get(originalFrom), originalToNew.get(originalTo));
                }
                for (World originalTo : oldKnowledge.getToWorlds(originalFrom)) {
                    newKnowledge.connect(originalToNew.get(originalFrom), originalToNew.get(originalTo));
                }
            }
            beliefRelations.put(agent, newBelief);
            knowledgeRelations.put(agent, newKnowledge);
        }
        kripkeStructure = new KripkeStructure(newWorlds, beliefRelations, knowledgeRelations);
        designatedWorlds = new HashSet<World>();
        for (World d : toCopy.getDesignatedWorlds()) {
            designatedWorlds.add(originalToNew.get(d));
        }
        assert(kripkeStructure.containsWorlds(designatedWorlds));
    }

    public KripkeStructure getKripke() {
        return this.kripkeStructure;
    }

    public Set<World> getDesignatedWorlds() {
        return this.designatedWorlds;
    }

    public Set<World> getWorlds() {
        return kripkeStructure.getWorlds();
    }

    public Set<EpistemicState> getEpistemicStates() {
        Set<EpistemicState> states = new HashSet<EpistemicState>();
        for (World w : designatedWorlds) {
            states.add(new EpistemicState(new KripkeStructure(this.kripkeStructure), w));
        }
        return states;
    }

    public Boolean possibly(Formula formula) {
        for (World w : designatedWorlds) {
            if (formula.evaluate(this.kripkeStructure, w)) {
                return true;
            }
        }
        return false;
    }

    public Boolean necessarily(Formula formula) {
        for (World w : designatedWorlds) {
            if (!formula.evaluate(this.kripkeStructure, w)) {
                return false;
            }
        }
        return true;
    }


    public Void reduce() {
        Map<World,World> oldWorldsToNew = kripkeStructure.reduce();
        //assert(kripkeStructure.checkRelations());
        Set<World> newDesignated = new HashSet<World>();
        for (World w : designatedWorlds) {
            newDesignated.add(oldWorldsToNew.get(w));
        }
        this.designatedWorlds = newDesignated;
        return null;
    }

    // FIND AND REMOVE ANY WORLDS THAT ARE NOT REACHABLE FROM ANY DESIGNATED WORLD
    public Void trim() {
        Set<World> keep = new HashSet<>(designatedWorlds);
        Set<World> old;
        do {
            old = new HashSet<>(keep);
            for (World w : old) {
                keep.addAll(kripkeStructure.getChildren(w));
            }
        } while (old.size() != keep.size());
        Map<String, Relation> newBeliefs = new HashMap<String,Relation>();
        Map<String, Relation> newKnowledges = new HashMap<String,Relation>();
        for (String agent : kripkeStructure.getBeliefRelations().keySet()) {
            Relation oldBelief = kripkeStructure.getBeliefRelations().get(agent);
            Relation newBelief = new Relation();
            Relation oldKnowledge = kripkeStructure.getKnowledgeRelations().get(agent);
            Relation newKnowledge = new Relation();
            for (World from : keep) {
                for (World to : keep) {
                    if (oldBelief.isConnected(from, to)) {
                        newBelief.connect(from, to);
                    }
                    if (oldKnowledge.isConnected(from, to)) {
                        newKnowledge.connect(from, to);
                    }
                }
            }
            newBeliefs.put(agent, newBelief);
            newKnowledges.put(agent, newKnowledge);
        }
        kripkeStructure = new KripkeStructure(keep, newBeliefs, newKnowledges);
        //assert(kripkeStructure.checkRelations());
        return null;
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

        assert (this.kripkeStructure != other.getKripke());
        // IF THE KRIPKES ARE THE SAME OBJECT BUT THE DESIGNATED WORLDS DIFFER,
        // WE'LL PROCEDE TO equivalent(other), THAT'S OK, BECUASE KripkeStructure.union()
        // WILL DUPLICATE THE KRIPKE STRUCTURE.
        if (this.kripkeStructure == other.getKripke() && this.designatedWorlds == other.getDesignatedWorlds()) {
            return true;
        }
        return equivalent(other);
    }


    public Boolean equivalent(NDState other) {
        if (this.kripkeStructure == other.getKripke()) {
            other = new NDState(other);
        }

        KripkeStructure unionKripke = this.kripkeStructure.union(other.getKripke());

        Set<World> otherInitials = other.getDesignatedWorlds();

        for (Set<World> block : unionKripke.refineSystem()) {
            if (Collections.disjoint(block, designatedWorlds) != Collections.disjoint(block, otherInitials)) {
                return false;
            }
        }
        return true;
    }

    private Map<String, Relation> beliefRelations;

    private Map<String, Relation> knowledgeRelations;

    @Override
    public int hashCode() {
        // THERE MAY BE SOME ROOM FOR IMPROVEMENT HERE...
        return 1;
    }


    public String toStringCompact() {
        return toString();
    }

    @Override
    public String toString() {
        return kripkeStructure.toString(designatedWorlds);
    }


}

