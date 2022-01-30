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


// A MULTI-POINTED KRIPKE MODEL


public class NDState implements java.io.Serializable {

    protected Model<World> model;
    private Set<World> designated;

    public NDState(Model<World> model, Set<World> designated) {
        assert(model.getPoints().containsAll(designated));
        for (World w : designatedWorlds) {
            assert (w != null);
        }
        this.model = model;
        this.designated = designated;
    }

    public NDState(NDState toCopy) {
        model = new Model(toCopy.getModel());
        designatedWorlds = new HashSet<World>();
        for (World d : toCopy.getDesignatedWorlds()) {
            designatedWorlds.add(d.getChild());
        }
    }

    public Model<World> getModel() {
        return this.model;
    }

    public Set<World> getDesignated() {
        return this.designated;
    }

    public Set<World> getWorlds() {
        return model.getPoints();
    }

    public Set<State> getPointedStates() {
        Set<State> states = new HashSet<State>();
        for (World w : designatedWorlds) {
            states.add(new State(new Model(this.model), w));
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


//    public Void reduce() {
//        Map<World,World> oldWorldsToNew = model.reduce();
//        Set<World> newDesignated = new HashSet<World>();
//        for (World w : designatedWorlds) {
//            newDesignated.add(oldWorldsToNew.get(w));
//        }
//        this.designatedWorlds = newDesignated;
//        return null;
//    }
//
//    // FIND AND REMOVE ANY WORLDS THAT ARE NOT REACHABLE FROM ANY DESIGNATED WORLD
//    public Void trim() {
//        Set<World> keep = new HashSet<>(designatedWorlds);
//        Set<World> old;
//        do {
//            old = new HashSet<>(keep);
//            for (World w : old) {
//                keep.addAll(kripkeStructure.getChildren(w));
//            }
//        } while (old.size() != keep.size());
//        Map<String, Relation> newBeliefs = new HashMap<String,Relation>();
//        Map<String, Relation> newKnowledges = new HashMap<String,Relation>();
//        for (String agent : kripkeStructure.getBeliefRelations().keySet()) {
//            Relation oldBelief = kripkeStructure.getBeliefRelations().get(agent);
//            Relation newBelief = new Relation();
//            Relation oldKnowledge = kripkeStructure.getKnowledgeRelations().get(agent);
//            Relation newKnowledge = new Relation();
//            for (World from : keep) {
//                for (World to : keep) {
//                    if (oldBelief.isConnected(from, to)) {
//                        newBelief.connect(from, to);
//                    }
//                    if (oldKnowledge.isConnected(from, to)) {
//                        newKnowledge.connect(from, to);
//                    }
//                }
//            }
//            newBeliefs.put(agent, newBelief);
//            newKnowledges.put(agent, newKnowledge);
//        }
//        kripkeStructure = new KripkeStructure(keep, newBeliefs, newKnowledges);
//        //assert(kripkeStructure.checkRelations());
//        return null;
//    }




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
        if (this.model == other.getModel()) {
            other = new NDState(other);
        }

        Model unionModel = this.model.union(other.getModel());

        Set<World> otherInitials = other.getDesignated();

        for (Set<World> block : unionModel.refineSystem()) {
            if (Collections.disjoint(block, designated) != Collections.disjoint(block, otherInitials)) {
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
        return model.toString(designatedWorlds);
    }


}

