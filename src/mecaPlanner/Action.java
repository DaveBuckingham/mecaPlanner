package mecaPlanner;

import mecaPlanner.formulae.beliefFormulae.*;
import mecaPlanner.formulae.localFormulae.*;
import mecaPlanner.state.*;
import mecaPlanner.models.Model;
import mecaPlanner.Domain;

import java.util.List;
import java.util.ArrayList;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;



public class Action implements java.io.Serializable {

    protected Domain domain;

    protected String name;
    protected List<String> parameters;
    protected int cost;
    protected String actor;
    protected LocalFormula precondition;
    protected Map<String, LocalFormula> observesIf;
    protected Map<String, LocalFormula> awareIf;
    protected Set<LocalFormula> determines;
    protected Set<BeliefFormula> announces;
    protected Map<Assignment, LocalFormula> effects;




    public Action(String name,
                  List<String> parameters,
                  String actor,
                  int cost,
                  LocalFormula precondition,
                  Map<String, LocalFormula> observesIf,
                  Map<String, LocalFormula> awareIf,
                  Set<LocalFormula> determines,
                  Set<BeliefFormula> announces,
                  Map<Assignment, LocalFormula> effects,
                  Domain domain
                 ) {
        assert(cost > 0);
        this.name = name;
        this.parameters = parameters;
        this.actor = actor;
        this.cost = cost;
        this.precondition = precondition;
        this.observesIf = observesIf;
        this.awareIf = awareIf;
        this.determines = determines;
        this.announces = announces;
        this.effects = effects;
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public String getActor() {
        return this.actor;
    }

    public List<String> getParameters() {
        return this.parameters;
    }

    public BeliefFormula getPrecondition() {
        return this.precondition;
    }

    public Map<Assignment, LocalFormula> getEffects() {
        return this.effects;
    }

    // DO THIS HERE INSTEAD OF IN WORLD IN CASE WE WANT TO SWITCH
    // TO BELIEF FORMULA EFFECT CONDITIONS
    public Set<Assignment> getApplicableEffects(World world) {
        Set<Assignment> applicableEffects = new HashSet<>();
            for (Map.Entry<Assignment, LocalFormula> e : effects.entrySet()) {
                Assignment assignment = e.getKey();
                LocalFormula condition = e.getValue();
                if (condition.evaluate(world)) {
                    applicableEffects.add(assignment);
                }
            }
        return applicableEffects;
    }


    public int getCost() {
        return this.cost;
    }

    public boolean executable(EpistemicState state) {
        return precondition.evaluate(state.getDesignatedWorld());
    }

    protected boolean executable(World world) {
        return precondition.evaluate(world);
    }

    public Boolean necessarilyExecutable(NDState state) {
        for (World w : state.getDesignatedWorlds()) {
            assert (w != null);
            if (!executable(w)) {
                return false;
            }
        }
        return true;
    }


    public Boolean isObservant(String agent, World world) {
        boolean observant = observesIf.containsKey(agent) && observesIf.get(agent).evaluate(world);
        return observant;
    }

    public Boolean isAware(String agent, World world) {
        return (awareIf.containsKey(agent) && awareIf.get(agent).evaluate(world));
    }

    public Boolean isOblivious(String agent, World world) {
        return (!isObservant(agent, world) && !isAware(agent, world));
    }



    public class UpdatedStateAndModels {
        private EpistemicState updatedState;
        private Map<String, Model> updatedModels ;

        public UpdatedStateAndModels(EpistemicState updatedState, Map<String, Model> updatedModels) {
            this.updatedState = updatedState;
            this.updatedModels = updatedModels;
        }

        public EpistemicState getState() {
            return updatedState;
        }
        public Map<String, Model> getModels() {
            return updatedModels;
        }
    }

    public EpistemicState transition(EpistemicState beforeState) {
        Action.UpdatedStateAndModels result = transition(beforeState, new HashMap<String, Model>());
        return result.getState();
    }


    private class PartialResult {
        public KripkeStructure kripke;
        public Map<World,World> map;
    }

    
    private PartialResult partial(KripkeStructure oldKripke) {

        Map<World,World> newToOld;
        Set<World> oldWorlds = oldKripke.getWorlds();
        Set<World> newWorlds = new HashSet<>();
        for (String a : domain.getAllAgents()) {
            newBeliefs.put(a, new Relation());
            newKnowledges.put(a, new Relation());
        }

        // FOR EACH OLD WORLD, IF PRECONDITIONS HOLD, MUTATE INTO NEW, USING CONDITIONAL EFFECTS
        for (World oldWorld : oldWorlds) {
            if (precondition.evaluate(oldWorld)) {
                World newWorld = oldWorld.update(getApplicableEffects(oldWorld));
                newWorlds.add(newWorlds);
                newWorldsToOld.put(newWorld, oldWorld);
            }
        }


        for (World fromWorld : newWorlds) {
            World oldFromWorld = map.get(fromWorld);

            // WHAT DO AWARE AND OBSERVERS LEARN FROM EFFECT PRECONDITINS
            // NEED TO ADD "EITHER THIS OR THAT WAS THE CAUSE, I KNOW A OR B"
            Set<LocalFormula> revealedConditions = new HashSet<>();
                for (Map.Entry<Assignment, LocalFormula> e : effects.entrySet()) {
                    Assignment assignment = e.getKey();
                    LocalFormula condition = e.getValue();
                    assert(!condition.isFalse());
                    if (oldFromWorld.alteredByAssignment(assignment) && !condition.isTrue()) {
                        // CONDITION WILL OFTEN BE "True", NO POINT IN STORING THAT
                        if (condition.evaluate(oldFromWorld)) {
                            revealedConditions.add(condition);
                        }
                        else {
                            revealedConditions.add(condition.negate());
                        }
                    }
                }
            }

            // WHAT DO OBSERVERS SENSE
            Set<LocalFormula> groundDetermines = new HashSet<>();
            if (anyObservers) {
                for (LocalFormula f : determines) {
                    if (f.evaluate(oldFromWorld)) {
                        groundDetermines.add(f);
                    }
                    else {
                        groundDetermines.add(f.negate());
                    }
                }
            }

            // FOR EACH AGENT, CONNECT TO-WORLDS FOR BELIEF AND KNOWLEDGE
            for (String agent : domain.getAllAgents()) {
                if (isObservant(agent, oldFromWorld)){

                    // WHAT DOES THE AGENT HEAR ANNOUNCED AND NOT REJECT
                    Set<BeliefFormula> acceptedAnnouncements = new HashSet<>();
                    for (BeliefFormula announcement : announces) {
                        BeliefFormula knowsNotAnnouncement = new BeliefKnowsFormula(agent, announcement.negate());
                        if (!knowsNotAnnouncement.evaluate(beforeState)) {
                            acceptedAnnouncements.add(announcement);
                        }
                    }

                    // WHAT DOES THE AGENT LEARN WITH CERTAINTY BY OBSERVING THE ACTION
                    // ASSUMING THAT ANNOUNCEMTNS MIGHT BE LIES
                    Set<LocalFormula> allKnowledgeLearned = new HashSet<>();
                    allKnowledgeLearned.addAll(revealedConditions);
                    allKnowledgeLearned.addAll(groundDetermines);
                    LocalFormula learnedKnowledgeFormula = LocalAndFormula.make(allKnowledgeLearned);

                    // WHAT DOES THE AGENT LEARN BY OBSERVING THE ACTION
                    // ASSUMING THAT NON-REJECTED ANNOUNCEMENTS ARE TRUE
                    Set<BeliefFormula> allBeliefLearned = new HashSet<>();
                    allBeliefLearned.addAll(allKnowledgeLearned);
                    allBeliefLearned.addAll(acceptedAnnouncements);
                    BeliefFormula learnedBeliefFormula = BeliefAndFormula.make(allBeliefLearned);

                    // COPY CONNECTIONS FROM OLD BELIEF RELATION UNLESS TO-WORLD CONTRADICTS LEARNED BELIEFS
                    for (World toWorld: newWorlds) {
                        World oldToWorld = map.get(toWorld);
                        if (oldKripke.isConnectedBelief(agent, oldFromWorld, oldToWorld)) {
                            if (learnedBeliefFormula.evaluate(oldKripke, oldToWorld)) {
                                newBeliefs.get(agent).connect(fromWorld, toWorld);
                            }
                        }
                    }

                    // IF NO CONNECTIONS WERE COPIED, AGENT LEARNED SOMETHING BELIEVED IMPOSSIBLE: BELIEF RESET
                    if (newBeliefs.get(agent).getToWorlds(fromWorld).isEmpty()) {
                        Log.debug("observant agent " + agent + " reset by " + getSignatureWithActor());
                        for (World toWorld: newWorlds) {
                            World oldToWorld = map.get(toWorld);
                            if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)) {
                                if (learnedBeliefFormula.evaluate(oldKripke, oldToWorld)) {
                                    newBeliefs.get(agent).connect(fromWorld, toWorld);
                                }
                            }
                        }
                    }
                    // SECOND BELIEF RESET
                    if (newBeliefs.get(agent).getToWorlds(fromWorld).isEmpty()) {
                        Log.debug("observant agent " + agent + " hard reset by " + getSignatureWithActor());
                        for (World toWorld: newWorlds) {
                            World oldToWorld = map.get(toWorld);
                            if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)) {
                                if (learnedKnowledgeFormula.evaluate(oldKripke, oldToWorld)) {
                                    newBeliefs.get(agent).connect(fromWorld, toWorld);
                                }
                            }
                        }
                    }


                    // COPY CONNECTIONS FROM OLD KNOWLEDGE RELATION UNLESS TO-WORLD CONTRADICTS LEARNED KNOWLEDGE
                    for (World toWorld: newWorlds) {
                        World oldToWorld = map.get(toWorld);
                        if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)) {
                            if (learnedKnowledgeFormula.evaluate(oldToWorld)) {
                                newKnowledges.get(agent).connect(fromWorld, toWorld);
                            }
                        }
                    }

                }
                else if (isAware(agent, oldFromWorld)){
                    assert(anyAware);

                    for (World toWorld: newWorlds) {
                        World oldToWorld = map.get(toWorld);
                        if (oldKripke.isConnectedBelief(agent, oldFromWorld, oldToWorld)) {
                            newBeliefs.get(agent).connect(fromWorld, toWorld);
                        }
                    }

                    // IF NO CONNECTIONS WERE COPIED, AGENT LEARNED SOMETHING BELIEVED IMPOSSIBLE: BELIEF RESET
                    // SPECIFICALLY, A NEW WORLD HAS OUTGOING EDGES ONLY TO OLD WORLDS
                    if (newBeliefs.get(agent).getToWorlds(fromWorld).isEmpty()) {
                        Log.debug("aware agent " + agent + " reset by " + getSignatureWithActor());
                        for (World toWorld: newWorlds) {
                            World oldToWorld = map.get(toWorld);
                            if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)) {
                                newBeliefs.get(agent).connect(fromWorld, toWorld);
                            }
                        }
                    }

                    // COPY CONNECTIONS FROM OLD KNOWLEDGE RELATION 
                    for (World toWorld: newWorlds) {
                        World oldToWorld = map.get(toWorld);
                        if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)) {
                            newKnowledges.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                }
                else {  //oblivious
                    for (World toWorld: newWorlds) {
                        if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, map.get(toWorld))) {
                            newKnowledges.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                }
            }
        }

        assert(newKripke.checkRelations());

        KripkeStructure newKripke = new KripkeStructure(newWorlds, newBeliefs, newKnowledges);
        return new Action.PartialResult(newKripke, map);
    }



    public Action.UpdatedStateAndModels transition(EpistemicState beforeState, Map<String, Model> oldModels) {
        Log.debug("transition: " + getSignatureWithActor());

//         for (World oldToWorld: oldWorlds) {
//             if (oldKripke.isConnectedBelief(agent, oldFromWorld, oldToWorld)) {
//                 newBeliefs.get(agent).connect(fromWorld, oldWorldsToOblivious.get(oldToWorld));
//             }
//             if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)) {
//                 newKnowledges.get(agent).connect(fromWorld, oldWorldsToOblivious.get(oldToWorld));
//                 newKnowledges.get(agent).connect(oldWorldsToOblivious.get(oldToWorld), fromWorld);
//             }
//         }

        Kripke oldModel = beforeState.getKripke();
        Action.PartialResults p = partial(oldModel);
        Kripke actionModel = p.kripke;
        Map<World,World> actionMap = p.map;

        // DONT DO IT HERE, CHECK WHILE BUILDING...
        boolean anyOblivious = false;
        for (World oldWorld : actionMap.values()) {
            for (String agent : domain.getAllAgents()) {
                if (isOblivious(agent, oldWorld)) {
                    anyOblivious = true;
                }
            }
        }

        Set<Kripke> hypotheticalModels = new HashSet<>();
        for (String agent : domain.getAllAgents()) {
        }




        EpistemicState newState = new EpistemicState(newKripke, newDesignatedWorld);

        if (!newKripke.checkRelations()) {
            System.out.println("BEFORE:");
            System.out.println(beforeState);
            System.out.println("ACTION:");
            System.out.println(this);
            System.out.println("AFTER:");
            System.out.println(newState);
            System.exit(1);
        }


        // UPDATE THE MODELS
        Map<String, Model> newModels = new HashMap();
        for (String agent : oldModels.keySet()) {
            Model updatedModel = oldModels.get(agent).update(beforeState, this);
            newModels.put(agent, updatedModel);
        }

        return new Action.UpdatedStateAndModels(newState, newModels);

    }




    public String getSignatureWithActor() {
        StringBuilder str = new StringBuilder();
        str.append(name);
        str.append("[");
        str.append(actor);
        str.append("]");
        str.append("(");
        if (parameters.size() > 0) {
            for (String p : parameters) {
                str.append(p);
                str.append(",");
            }
            str.deleteCharAt(str.length() - 1);
        }
        str.append(")");
        return str.toString();
    }

    public String getSignature() {
        StringBuilder str = new StringBuilder();
        str.append(name);
        str.append("(");
        if (parameters.size() > 0) {
            for (String p : parameters) {
                str.append(p);
                str.append(",");
            }
            str.deleteCharAt(str.length() - 1);
        }
        str.append(")");
        return str.toString();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Action other = (Action) obj;
        return (this.getSignatureWithActor() == other.getSignatureWithActor());
    }

    @Override
    public int hashCode() {
        return getSignatureWithActor().hashCode();
    }


    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("Action: ");
        str.append(this.getSignature());

        str.append("\n\tOwner: ");
        str.append(actor);

        str.append("\n\tCost: ");
        str.append(cost);

        str.append("\n\tPrecondition: ");
        str.append(precondition);

        str.append("\n\tObserves\n");
        for (Map.Entry<String, LocalFormula> o : observesIf.entrySet()) {
            str.append("\t\t");
            str.append(o.getKey());
            str.append(" if ");
            str.append(o.getValue());
            str.append("\n");
        }

        str.append("\tAware\n");
        for (Map.Entry<String, LocalFormula> a : awareIf.entrySet()) {
            str.append("\t\t");
            str.append(a.getKey());
            str.append(" if ");
            str.append(a.getValue());
            str.append("\n");
        }

        str.append("\tDetermines\n");
        for (LocalFormula ff : determines) {
            str.append("\t\t");
            str.append(ff);
            str.append("\n");
        }

        str.append("\tAnnounces\n");
        for (BeliefFormula bf : announces) {
            str.append("\t\t");
            str.append(bf);
            str.append("\n");
        }

        str.append("\tCauses\n");
        for (Map.Entry<Assignment, LocalFormula> e : effects.entrySet()) {
            Assignment assignment = e.getKey();
            LocalFormula condition = e.getValue();
            str.append("\t\t");
            str.append(assignment);
            str.append(" if ");
            str.append(condition);
            str.append("\n");
        }
 
        return str.toString();
    }

}

