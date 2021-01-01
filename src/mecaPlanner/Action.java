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

    Map<String, Map<World, LocalFormula>> learnedObserver;





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

        learnedObserver = new HashMap<>();

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
        //System.out.println("APPLICABLE:");
        //for (Assignment a : applicableEffects) {
        //    System.out.println(a);
        //}
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
        public PartialResult(KripkeStructure k, Map<World,World> m) {
            this.kripke = k;
            this.map = m;
        }
    }


    
    public PartialResult partial(KripkeStructure oldKripke) {

        Map<World, World> map = new HashMap<>();

        Set<World> oldWorlds = oldKripke.getWorlds();

        //Set<World> newWorlds = new HashSet<>();
        //Map<String, Relation> newBeliefs = new HashMap<>();
        //Map<String, Relation> newKnowledges = new HashMap<>();
        //for (String a : domain.getAllAgents()) {
        //    newBeliefs.put(a, new Relation());
        //    newKnowledges.put(a, new Relation());
        //}





        // FOR EACH OLD WORLD, FOR EACH AGENT, BUILD FORMULA
        // CONTAINING ALL KNOWLEDGE LEARNED
        // AND ONE FOR BLIEF

        Map<World,Map<String, LocalFormula>> learnedKnowledgeFormula = new HashMap<>();
        Map<World,Map<String, BeliefFormula>> learnedBeliefFormula = new HashMap<>();
        for (World world : oldWorlds) {
            learnedKnowledgeFormula.put(world, new HashMap<>());
            learnedBeliefFormula.put(world, new HashMap<>());
        }

        Map<World, LocalFormula> learnedEffectConditions = new HashMap<>();
        Map<World, LocalFormula> learnedDetermined = new HashMap<>();

        for (World oldWorld : oldWorlds) {

            // WHAT DO AWARE AND OBSERVERS LEARN FROM EFFECT PRECONDITINS
            Map<Fluent, Set<LocalFormula>> fluentsPossibleChangers = new HashMap<>();
            for (Map.Entry<Assignment, LocalFormula> e : effects.entrySet()) {
                Assignment assignment = e.getKey();
                Fluent target = assignment.getFluent();
                Boolean value = assignment.getValue();
                LocalFormula condition = e.getValue();
                assert(!condition.isFalse());
                if (!fluentsPossibleChangers.containsKey(target)) {
                    fluentsPossibleChangers.put(target, new HashSet<LocalFormula>());
                }
                if (oldWorld.alteredByAssignment(assignment) && !condition.isTrue()) {
                    // CONDITION WILL OFTEN BE "True", NO POINT IN STORING THAT
                    fluentsPossibleChangers.get(target).add(condition);
                }
            }
            Set<LocalFormula> revealedConditions = new HashSet<>();
            for (Fluent fluent : fluentsPossibleChangers.keySet()) {
                LocalFormula possibleChangersFormula = LocalOrFormula.make(fluentsPossibleChangers.get(fluent));
                if (possibleChangersFormula.evaluate(oldWorld)) {
                    revealedConditions.add(possibleChangersFormula);
                }
                else {
                    revealedConditions.add(possibleChangersFormula.negate());
                }
            }
            learnedEffectConditions.put(oldWorld, LocalAndFormula.make(revealedConditions));
            assert(learnedEffectConditions.get(oldWorld).evaluate(oldWorld));


            // WHAT DO OBSERVERS SENSE
            Set<LocalFormula> groundDetermines = new HashSet<>();
            for (LocalFormula f : determines) {
                if (f.evaluate(oldWorld)) {
                    groundDetermines.add(f);
                }
                else {
                    groundDetermines.add(f.negate());
                }
            }
            learnedDetermined.put(oldWorld, LocalAndFormula.make(groundDetermines));
            assert(learnedDetermined.get(oldWorld).evaluate(oldWorld));

            // WHAT DO AGENTS LEARN BECAUSE THEY KNOW THEIR OBSERVER STATUS
            for (String agent : domain.getAllAgents()) {
                learnedObserver.put(agent, new HashMap<World, LocalFormula>());

                LocalFormula observerConditions = observesIf.get(agent);
                LocalFormula awareConditions = awareIf.get(agent);

                if (isObservant(agent, oldWorld)){
                    assert (!isAware(agent, oldWorld));  // THERE'S PROBABLY A BETTER PLACE FOR THIS CHECK
                    learnedObserver.get(agent).put(oldWorld, LocalAndFormula.make(observerConditions,
                                                                                  awareConditions.negate()));
                }
                else if (isAware(agent, oldWorld)){
                    learnedObserver.get(agent).put(oldWorld, LocalAndFormula.make(observerConditions.negate(),
                                                                                  awareConditions));
                }
                else {
                    learnedObserver.get(agent).put(oldWorld, LocalAndFormula.make(observerConditions.negate(),
                                                                                  awareConditions.negate()));
                }
                assert(learnedObserver.get(agent).get(oldWorld).evaluate(oldWorld));
            }


            // PUT TOGETHER ALL SOURCES OF KNOWLEDGE
            for (String agent : domain.getAllAgents()) {
                if (isObservant(agent, oldWorld)){
                    learnedKnowledgeFormula.get(oldWorld).put(agent, LocalAndFormula.make(
                        learnedDetermined.get(oldWorld),
                        learnedEffectConditions.get(oldWorld),
                        learnedObserver.get(agent).get(oldWorld)));
                }
                else if (isAware(agent, oldWorld)){
                    learnedKnowledgeFormula.get(oldWorld).put(agent, LocalAndFormula.make(
                        learnedEffectConditions.get(oldWorld),
                        learnedObserver.get(agent).get(oldWorld)));
                }
                else {
                    learnedKnowledgeFormula.get(oldWorld).put(agent, new Literal(false));
                }
            }



            // PUT TOGETHER ALL SOURCES OF BELIEF
            for (String agent : domain.getAllAgents()) {
                if (isObservant(agent, oldWorld)){
                    BeliefFormula announcements = BeliefAndFormula.make(announces);
                    BeliefFormula knowsNotAnnouncements = new BeliefKnowsFormula(agent, announcements.negate());
                    if (knowsNotAnnouncements.evaluate(oldKripke, oldWorld)) {
                        learnedBeliefFormula.get(oldWorld).put(agent, new Literal(true));
                    }
                    else {
                        learnedBeliefFormula.get(oldWorld).put(agent, announcements);
                    }
                }
                else if (isAware(agent, oldWorld)){
                    learnedBeliefFormula.get(oldWorld).put(agent, new Literal(true));
                }
                else {
                    learnedBeliefFormula.get(oldWorld).put(agent, new Literal(false));
                }
            }

        }


        // GROUPED WORLDS (WITH POSSIBLE OVERLAPS) INTO NEW EQUIVALENCE CLASSES
        Map<Set<World>, Set<String>> equivalenceClasses = new HashMap<>();
        for (String agent : domain.getAllAgents()) {
            for (World oldWorld : oldWorlds) {
                Set<World> equivalent = new HashSet<>();
                for (World toWorld: oldKripke.getKnownWorlds(agent, oldWorld)) {
                    if (learnedKnowledgeFormula.get(oldWorld).get(agent).evaluate(toWorld)) {
                        equivalent.add(toWorld);
                    }
                }
                if (equivalenceClasses.containsKey(equivalent)) {
                    System.out.println("Y");
                    equivalenceClasses.get(equivalent).add(agent);
                }
                else {
                    System.out.println("N: " + agent);
                    for (World w : equivalent) {
                        System.out.println(w);
                    }
                    for (Set<World> s : equivalenceClasses.keySet()) {
                        System.out.println("--");
                        for (World w : s) {
                            System.out.println(w);
                        }
                    }
                    Set<String> agents = new HashSet<>();
                    agents.add(agent);
                    equivalenceClasses.put(equivalent, agents);
                }
            }
        }


        // KNOWLEDGE RELATIONS
        Map<String, Relation> newKnowledges = new HashMap<>();
        for (String agent : domain.getAllAgents()) {
            newKnowledges.put(agent, new Relation());
        }

//        for (Set<World> equivalent : equivalenceClasses.keySet()) {
//            Set<String> agents = equivalenceClasses.get(equivalent);
//            for (World w : equivalent) {
//                System.out.println(w);
//            }
//            for (String a : agents) {
//                System.out.println(a);
//            }
//        }
//        System.out.println("========");

        for (Set<World> equivalent : equivalenceClasses.keySet()) {
            Set<String> agents = equivalenceClasses.get(equivalent);
            Set<World> newEquivalent = new HashSet<>();
            for (World oldWorld : equivalent) {
                World transformedWorld = oldWorld.update(getApplicableEffects(oldWorld));
                map.put(transformedWorld, oldWorld);
                newEquivalent.add(transformedWorld);
            }

            for (String agent : agents) {
                for (World u : newEquivalent) {
                    for (World v : newEquivalent) {
                        assert (oldKripke.isConnectedKnowledge(agent, map.get(u), map.get(v)));
                        newKnowledges.get(agent).connect(u,v);
                    }
                }
            }
        }

        // BELIEF RELATIONS
        Map<String, Relation> newBeliefs = new HashMap<>();
        for (String agent : domain.getAllAgents()) {
            newBeliefs.put(agent, new Relation());
        }

        for (String agent : domain.getAllAgents()) {
            for (World fromWorld: map.keySet()) {
                World oldFromWorld = map.get(fromWorld);
                BeliefFormula learnedBelief = learnedBeliefFormula.get(oldFromWorld).get(agent);

                // COPY CONNECTIONS FROM OLD BELIEF RELATION UNLESS TO-WORLD CONTRADICTS LEARNED BELIEFS
                for (World toWorld: newKnowledges.get(agent).getToWorlds(fromWorld)) {
                    World oldToWorld = map.get(toWorld);
                    if (oldKripke.isConnectedBelief(agent, oldFromWorld, oldToWorld)) {
                        if (learnedBelief.evaluate(oldKripke, oldToWorld)) {
                            newBeliefs.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                }

                // IF NO CONNECTIONS WERE COPIED, AGENT LEARNED SOMETHING BELIEVED IMPOSSIBLE: BELIEF RESET
                if (newBeliefs.get(agent).getToWorlds(fromWorld).isEmpty()) {
                    Log.debug("observant agent " + agent + " reset by " + getSignatureWithActor());
                    for (World toWorld: newKnowledges.get(agent).getToWorlds(fromWorld)) {
                        World oldToWorld = map.get(toWorld);
                        if (learnedBelief.evaluate(oldKripke, oldToWorld)){
                            newBeliefs.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                }

                // SECOND BELIEF RESET
                if (newBeliefs.get(agent).getToWorlds(fromWorld).isEmpty()) {
                    Log.debug("observant agent " + agent + " hard reset by " + getSignatureWithActor());
                    for (World toWorld: newKnowledges.get(agent).getToWorlds(fromWorld)) {
                        World oldToWorld = map.get(toWorld);
                        if (learnedKnowledgeFormula.get(agent).get(oldFromWorld).evaluate(oldKripke, oldToWorld)){
                            newBeliefs.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                }
            }
        }

        Set<World> newWorlds = new HashSet<World>(map.keySet());
        KripkeStructure newKripke = new KripkeStructure(newWorlds, newBeliefs, newKnowledges);

        return new Action.PartialResult(newKripke, map);
    }



    public Action.UpdatedStateAndModels transition(EpistemicState beforeState, Map<String, Model> oldModels) {
        Log.debug("transition: " + getSignatureWithActor());

        // UPDATE THE MODELS
        Map<String, Model> newModels = new HashMap();
        for (String agent : oldModels.keySet()) {
            Model updatedModel = oldModels.get(agent).update(beforeState, this);
            newModels.put(agent, updatedModel);
        }

        KripkeStructure oldKripke = beforeState.getKripke();


//        // BUILD OBLIVIOUS KRIPKE
//        Map<String, Relation> obliviousBeliefs = new HashMap<>();
//        Map<String, Relation> obliviousKnowledges = new HashMap<>();
//        for (String a : domain.getAllAgents()) {
//            obliviousBeliefs.put(a, new Relation());
//            obliviousKnowledges.put(a, new Relation());
//        }
//        Set<World> obliviousWorlds = new HashSet<>();
//        Map<World,World> oldWorldsToOblivious = new HashMap<>();
//        for (World w : beforeState.getKripke().getWorlds()) {
//            World obliviousWorld = new World(w);
//            obliviousWorlds.add(obliviousWorld);
//            oldWorldsToOblivious.put(w, obliviousWorld);
//        }
//        for (String a : domain.getAllAgents()) {
//            for (World w : beforeState.getKripke().getWorlds()) {
//                World oblivousFrom = oldWorldsToOblivious.get(w);
//                for (World oldToWorld: oldKripke.getBelievedWorlds(a, w)) {
//                    obliviousBeliefs.get(a).connect(oblivousFrom, oldWorldsToOblivious.get(oldToWorld));
//                }
//                for (World oldToWorld: oldKripke.getKnownWorlds(a, w)) {
//                    obliviousKnowledges.get(a).connect(oblivousFrom, oldWorldsToOblivious.get(oldToWorld));
//                }
//            }
//        }
//        KripkeStructure obliviousKripke = new KripkeStructure(obliviousWorlds, obliviousBeliefs, obliviousKnowledges);
//        World obliviousDesignated = oldWorldsToOblivious.get(beforeState.getDesignatedWorld());




        Action.PartialResult actualPartial = this.partial(oldKripke);
        KripkeStructure newKripke = actualPartial.kripke;
        Map<World,World> map = actualPartial.map;


        World newDesignated = null;
        for (World w : newKripke.getWorlds()) {
            if (map.get(w).equals(beforeState.getDesignatedWorld())) {
                newDesignated = w;
                break;
            }
        }
        assert(newDesignated != null);


        // IF NO OBLIVIOUS AGENTS IN WORLDS WHERE ACTION WAS APPLICABLE,
        // WE CAN JUST RETURN THE PARTIAL KRIPKE
        boolean anyOblivious = false;
        for (World newWorld : map.keySet()) {
            if (anyOblivious) {
                break;
            }
            for (String agent : domain.getAllAgents()) {
                if (isOblivious(agent, map.get(newWorld))) {
                    anyOblivious = true;
                    break;
                }
            }
        }
        if (!anyOblivious) {
            return new Action.UpdatedStateAndModels(new EpistemicState(newKripke, newDesignated), newModels);
        }




        // BUILD NULL ACTION AND GET USE PARTIAL TO GET OBLIVIOUS SUB-MODEL
        Map<String, LocalFormula> nullObserverConditions = new HashMap<>();
        Map<String, LocalFormula> nullAwareConditions = new HashMap<>();
        for (String agent : domain.getAllAgents()) {
            nullObserverConditions.put(agent, new Literal(true));
            nullAwareConditions.put(agent, new Literal(false));
        }

        Action nullAction = new Action("nullAction",                            // name
                                       new ArrayList<String>(),                 // parameters
                                       "nullActor",                             // actor
                                       1,                                       // cost
                                       new Literal(true),                       // preconditions 
                                       nullObserverConditions,                  // observesIf
                                       nullAwareConditions,                     // awareIf
                                       new HashSet<LocalFormula>(),             // determines
                                       new HashSet<BeliefFormula>(),            // announces
                                       new HashMap<Assignment, LocalFormula>(), // effects
                                       domain
                                      );
        Action.PartialResult obliviousPartial = nullAction.partial(oldKripke);
        KripkeStructure obliviousKripke = obliviousPartial.kripke;
        newKripke.add(obliviousKripke);
        map.putAll(obliviousPartial.map);



        // BUILD HYPOTHETICAL MODELS
        for (World w : oldKripke.getWorlds()) {
            for (String agent : domain.getAllAgents()) {
                if (isOblivious(agent,w)) {
                     for (Action a : possibleActions(agent, obliviousKripke, w)) {
                         if (!a.equals(this)) {
                             Action.PartialResult partialResult = a.partial(obliviousKripke);
                             newKripke.add(partialResult.kripke);
                             map.putAll(partialResult.map);
                         }
                     }
                }
            }
        }

        // OBLIVIOUS AGENT INTRA- AND INTER-SUB-MODEL CONNECTIONS
        for (String agent : domain.getAllAgents()) {
            for (World fromWorld : map.keySet()) {
                 World oldFromWorld = map.get(fromWorld);
                 if (isOblivious(agent, fromWorld)) {
                     // BELIEF EDGES ONLY GO TO (AND WITHIN) THE OBLIVIOUS SUB-MODEL
                     for (World toWorld : obliviousKripke.getWorlds()) {
                         World oldToWorld = map.get(toWorld);
                         if (oldKripke.isConnectedBelief(agent, oldFromWorld, oldToWorld)) {
                             newKripke.connectBelief(agent, fromWorld, toWorld);
                         }
                     }
                     // KNOWLEDGE EDGES BETWEEN AND WITHIN ALL SUB-MODELS
                     for (World toWorld : map.keySet()) {
                         World oldToWorld = map.get(toWorld);
                         if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)) {
                             if (learnedObserver.get(agent).get(oldFromWorld).evaluate(oldToWorld)) {
                                 newKripke.connectKnowledge(agent, fromWorld, toWorld);
                             }
                         }
                     }

                 }
            }
        }

        EpistemicState newState = new EpistemicState(newKripke, newDesignated);


        newKripke.forceCheck();
        //if (!newKripke.checkRelations()) {
        //    System.out.println("BEFORE:");
        //    System.out.println(beforeState);
        //    System.out.println("ACTION:");
        //    System.out.println(this);
        //    System.out.println("AFTER:");
        //    System.out.println(newState);
        //    System.exit(1);
        //}

        return new Action.UpdatedStateAndModels(newState, newModels);
    }

    private Set<Action> possibleActions(String agent, KripkeStructure kripke, World world) {
        Set<Action> actions = new HashSet<>();
        for (Action action : domain.getAllActions()) {
            BeliefFormula possiblyPreconditioned = new BeliefKnowsFormula(agent,
                action.getPrecondition().negate()).negate();
            BeliefFormula possiblyOblivious = BeliefAndFormula.make(
                new BeliefKnowsFormula(agent, action.observesIf.get(agent)).negate(),
                new BeliefKnowsFormula(agent, action.awareIf.get(agent)).negate());

            if (possiblyPreconditioned.evaluate(kripke, world) && possiblyOblivious.evaluate(kripke, world)) {
                actions.add(action);
            }
        }
        return actions;
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

