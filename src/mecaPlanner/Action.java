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

    private Domain domain;

    private String name;
    private List<String> parameters;
    private int cost;
    private String actor;
    private LocalFormula precondition;
    private Map<String, LocalFormula> observesIf;
    private Map<String, LocalFormula> awareIf;
    private Set<LocalFormula> determines;
    private Set<BeliefFormula> announces;
    private Map<Assignment, LocalFormula> effects;






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

    public Map<String, LocalFormula> getObserves() {
        return this.observesIf;
    }

    public Map<String, LocalFormula> getAware() {
        return this.awareIf;
    }

    public Domain getDomain() {
        return this.domain;
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

    public Set<LocalFormula> getDetermines() {
        return this.determines;
    }

    public Set<BeliefFormula> getAnnounces() {
        return this.announces;
    }

    public boolean executable(EpistemicState state) {
        return executable(state.getDesignatedWorld());
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
        public Map<String, Map<World, LocalFormula>> learnedObserver;
        public PartialResult(KripkeStructure k, Map<World,World> m,  Map<String, Map<World, LocalFormula>> l){
            this.kripke = k;
            this.map = m;
            this.learnedObserver = l;
        }
        public Set<World> getWorlds() {
            return map.keySet();
        }
    }


    
    public PartialResult partial(KripkeStructure oldKripke) {


        Set<World> oldWorlds = oldKripke.getWorlds();


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

        Map<String, Map<World, LocalFormula>> learnedObserver = new HashMap<>();
        for (String agent : domain.getAllAgents()) {
            learnedObserver.put(agent, new HashMap<World, LocalFormula>());
        }

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
                    //learnedKnowledgeFormula.get(oldWorld).put(agent, new Literal(true));
                    learnedKnowledgeFormula.get(oldWorld).put(agent, learnedObserver.get(agent).get(oldWorld));
                }
                //if(!learnedKnowledgeFormula.get(oldWorld).get(agent).evaluate(oldWorld)) {
                //    System.out.println(agent + " learned:");
                //    System.out.println(learnedKnowledgeFormula.get(oldWorld).get(agent));
                //    System.out.println("at world:");
                //    System.out.println(oldWorld);
                //    throw new RuntimeException("learned knowledge violates world");
                //}
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
                    learnedBeliefFormula.get(oldWorld).put(agent, new Literal(true));
                }
                //System.out.println("------");
                //System.out.println(agent);
                //System.out.println(oldWorld);
                //System.out.println(learnedBeliefFormula.get(oldWorld).get(agent));
            }

        }


        // GROUPED WORLDS (WITH POSSIBLE OVERLAPS) INTO NEW EQUIVALENCE CLASSES
        Map<String, Set<Set<World>>> equivalenceClasses = new HashMap<>();
        for (String agent : domain.getAllAgents()) {
            equivalenceClasses.put(agent, new HashSet<Set<World>>());
            for (World oldWorld : oldWorlds) {
                Set<World> equivalent = new HashSet<>();
                assert(learnedKnowledgeFormula.get(oldWorld).get(agent).evaluate(oldWorld));
                assert(oldKripke.getKnownWorlds(agent, oldWorld).contains(oldWorld));
                for (World toWorld: oldKripke.getKnownWorlds(agent, oldWorld)) {
                    if (learnedKnowledgeFormula.get(oldWorld).get(agent).evaluate(toWorld)) {
                        equivalent.add(toWorld);
                    }
                }
                equivalenceClasses.get(agent).add(equivalent);
            }
        }


        // MAP EACH AGENT x WORLD TO THE SET OF EQUIVALENCE CLASSES CONTAINING THAT WORLD
        Map<String, Map<World, Set<Set<World>>>> containingClasses = new HashMap<>();
        for (String agent : domain.getAllAgents()) {
            containingClasses.put(agent, new HashMap<World, Set<Set<World>>>());
            for (World world : oldWorlds) {
                containingClasses.get(agent).put(world, new HashSet<Set<World>>());
                for (Set<World> eqClass : equivalenceClasses.get(agent)) {
                    if (eqClass.contains(world)) {
                        containingClasses.get(agent).get(world).add(eqClass);
                    }
                }
                assert(!containingClasses.get(agent).get(world).isEmpty());
            }
        }


        // MAP EACH AGENT x WORLD TO A LIST OF UNIQUE EQUIVALENCE CLASSES CONTAINING THAT WORLD
        Map<String, Map<World, List<Set<World>>>> containingClassesList = new HashMap<>();
        for (String agent : domain.getAllAgents()) {
            containingClassesList.put(agent, new HashMap<World, List<Set<World>>>());
            for (World world : oldWorlds) {
                containingClassesList.get(agent).put(world, new ArrayList<Set<World>>());
                for (Set<World> eqClass : containingClasses.get(agent).get(world)) {
                    containingClassesList.get(agent).get(world).add(eqClass);
                }
            }
        }



        // MAP EACH WORLD TO AN ASSIGNMENT TO AN EQUIVALENCE CLASS FOR EACH AGENT
        Map<World, Set<Map<String, Set<World>>>> assignments = new HashMap<>();
        Map<String, Integer> indeces = new HashMap<>();

        for (World world : oldWorlds) {
            for (String agent : domain.getAllAgents()) {
                indeces.put(agent, 0);
            }
            assignments.put(world, new HashSet<Map<String, Set<World>>>());
            boolean carry;
            do {
                Map<String, Set<World>> assignment = new HashMap<>();
                carry = true;
                for (String agent : domain.getAllAgents()) {
                    assignment.put(agent, containingClassesList.get(agent).get(world).get(indeces.get(agent)));
                    if (carry) {
                        indeces.put(agent, indeces.get(agent)+1);
                        if (indeces.get(agent) == containingClassesList.get(agent).get(world).size()) {
                            indeces.put(agent, 0);
                        }
                        else {
                            carry = false;
                        }
                    }
                }
                assignments.get(world).add(assignment);
            }
            while (!carry);
        }

        // NEW WORLDS
        Set<World> newWorlds = new HashSet<>();
        Map<World, World> newToOld = new HashMap<>();
        //Map<World, Set<World>> oldToNew = new HashMap<>();
        Map<World, Map<String, Set<World>>> postAssignments = new HashMap<>();
        for (World oldWorld : oldWorlds) {
            if (precondition.evaluate(oldWorld)) {
                //oldToNew.put(oldWorld, new HashSet<World>());
                for (Map<String, Set<World>> assignment : assignments.get(oldWorld)) {
                    World newWorld = oldWorld.update(getApplicableEffects(oldWorld));
                    newWorlds.add(newWorld);
                    newToOld.put(newWorld, oldWorld);
                    //oldToNew.get(oldWorld).add(newWorld);
                    postAssignments.put(newWorld, assignment);
                }
            }
        }

        // KNOWLEDGE RELATIONS
        Map<String, Relation> newKnowledges = new HashMap<>();

        for (String agent : domain.getAllAgents()) {
            Relation newKnowledge = new Relation();
            for (World fromWorld : newWorlds) {
                //if (isObservant(agent, fromWorld) || isAware(agent, fromWorld)) {
                    for (World toWorld : newWorlds) {
                        if (postAssignments.get(fromWorld).get(agent).equals(
                            postAssignments.get(toWorld).get(agent))){
                            newKnowledge.connect(fromWorld, toWorld);
                        }
                    }
                //}
            }
            newKnowledges.put(agent, newKnowledge);
        }




        // BELIEF RELATIONS
        Map<String, Relation> newBeliefs = new HashMap<>();
        for (String agent : domain.getAllAgents()) {
            Relation newBelief = new Relation();
            for (World fromWorld: newWorlds) {
                //if (isObservant(agent, fromWorld) || isAware(agent, fromWorld)) {
                //if (isObservant(agent, newToOld.get(fromWorld)) || isAware(agent, newToOld.get(fromWorld))) {
                    World oldFromWorld = newToOld.get(fromWorld);
                    BeliefFormula learnedBelief = learnedBeliefFormula.get(oldFromWorld).get(agent);

                    // COPY CONNECTIONS FROM OLD BELIEF RELATION UNLESS TO-WORLD CONTRADICTS LEARNED BELIEFS
                    for (World toWorld: newKnowledges.get(agent).getToWorlds(fromWorld)) {
                        World oldToWorld = newToOld.get(toWorld);
                        if (oldKripke.isConnectedBelief(agent, oldFromWorld, oldToWorld)) {
                            if (learnedBelief.evaluate(oldKripke, oldToWorld)) {
                                newBelief.connect(fromWorld, toWorld);
                            }
                        }
                    }

                    // IF NO CONNECTIONS WERE COPIED, AGENT LEARNED SOMETHING BELIEVED IMPOSSIBLE: BELIEF RESET
                    if (newBelief.getToWorlds(fromWorld).isEmpty()) {
                        Log.debug("observant agent " + agent + " reset by " + getSignatureWithActor() + " at " + fromWorld.toString());
                        for (World toWorld: newKnowledges.get(agent).getToWorlds(fromWorld)) {
                            World oldToWorld = newToOld.get(toWorld);
                            if (learnedBelief.evaluate(oldKripke, oldToWorld)){
                                newBelief.connect(fromWorld, toWorld);
                            }
                        }
                    }

                    // SECOND BELIEF RESET
                    if (newBelief.getToWorlds(fromWorld).isEmpty()) {
                        Log.debug("observant agent " + agent + " hard reset by " + getSignatureWithActor());
                        for (World toWorld: newKnowledges.get(agent).getToWorlds(fromWorld)) {
                            World oldToWorld = newToOld.get(toWorld);
                            if (learnedKnowledgeFormula.get(oldFromWorld).get(agent).evaluate(oldKripke, oldToWorld)){
                                newBelief.connect(fromWorld, toWorld);
                            }
                        }
                    }
                //}
            }
            newBeliefs.put(agent, newBelief);
        }

        KripkeStructure newKripke = new KripkeStructure(newWorlds, newBeliefs, newKnowledges);
        //KripkeStructure newKripke = new KripkeStructure(newWorlds, newKnowledges, newKnowledges);

        return new Action.PartialResult(newKripke, newToOld, learnedObserver);
    }



    public Action.UpdatedStateAndModels transition(EpistemicState beforeState, Map<String, Model> oldModels) {
        Log.debug("transition: " + getSignatureWithActor());

        //Map<World,World> newToOld = new HashMap<>();

        // UPDATE THE MODELS
        Map<String, Model> newModels = new HashMap();
        for (String agent : oldModels.keySet()) {
            Model updatedModel = oldModels.get(agent).update(beforeState, this);
            newModels.put(agent, updatedModel);
        }

        KripkeStructure oldKripke = beforeState.getKripke();

        Map<String, Set<PartialResult>> agentSubmodels = new HashMap<>();
        Set<PartialResult> submodels = new HashMap<>();

        Map<String, Map<World, LocalFormula>> learnedObserver = new HashMap<>();
        for (String agent : domain.getAllAgents()) {
            learnedObserver.put(agent, new HashMap<World, LocalFormula>());
        }

        Action.PartialResult actualPartial = this.partial(oldKripke);

        //newToOld.addAll(actualPartial.map);

        submodels.add(actualPartial);
        for (String agent : domain.getAllAgents()) {
            agentSubmodels.put(agent, new hashSet<PartialResult>());
            agentSubmodels.get(agent).add(actualPartial);
            learnedObserver.get(agent).putAll(actualPartial.learnedObserver.get(agent));
        }


        World newDesignated = null;
        for (World w : actualPartial.kripke.getWorlds()) {
            if (actualPartial.map.get(w).equals(beforeState.getDesignatedWorld())) {
                newDesignated = w;
                break;
            }
        }
        assert(newDesignated != null);


        // IF NO OBLIVIOUS AGENTS IN WORLDS WHERE ACTION WAS APPLICABLE,
        // WE CAN JUST RETURN THE PARTIAL KRIPKE
        boolean anyOblivious = false;
        for (World newWorld : actualPartial.getWorlds()) {
            if (anyOblivious) {
                break;
            }
            for (String agent : domain.getAllAgents()) {
                if (isOblivious(agent, newToOld.get(newWorld))) {
                    anyOblivious = true;
                    break;
                }
            }
        }
        if (!anyOblivious) {
            assert(actualPartial.kripke.checkRelations());
            return new Action.UpdatedStateAndModels(new EpistemicState(actualPartial.kripke, newDesignated), newModels);
        }


        // BUILD NULL ACTION AND  USE PARTIAL TO GET OBLIVIOUS SUB-MODEL
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
        submodels.add(obliviousPartial);
        //newToOld.addAll(obliviousPartial.map);



        // BUILD HYPOTHETICAL MODELS
        for (String agent : domain.getAllAgents()) {
            boolean obliviousAnywhere = false;
            for (World w : oldKripke.getWorlds()) {
                if (isOblivious(agent,w)) {
                    obliviousAnywhere = true;
                    for (Action a : possibleActions(agent, oldKripke, w)) {
                        if (!a.equals(this)) {
                            Action.PartialResult hypotheticalPartial = a.partial(oldKripke);
                            agentSubmodels.put(agent, hypotheticalPartial);
                            submodels.add(hypotheticalPartial);
                            //newToOld.addAll(hypotheticalPartial.map);
                        }
                    }
                }
                if (obliviousAnywhere) {
                    submodels.add(agent, obliviousPartial);
                }
            }
        }

        Map<World, Action.PartialResult> worldsToSubmodels = new HashMap<>();
        for (PartialResult sub : submodels) {
            for (world w : sub.map.keySet()) {
                worldsToSubmodels.put(w, sub);
            }
        }
        KripkeStructure newKripke = new KripkeStructure(worldsToSubmodels.keySet(), oldKripke.getAgents());


        // OBSERVERS COPY RELATIONS
        for (PartialResult sub : submodels) {
            for (String agent : domain.getAllAgents()) {
                for (World fromWorld : sub.getWorlds() {
                    World oldFromWorld = sub.map.get(fromWorld);
                    if (isObservant(agent, oldFromWorld) || isAware(agent, oldFromWorld)) {
                        newKripke.connectBelief(fromWorld, sub.kripke.getBelievedWorlds(agent, fromWorld));
                        newKripke.connectKnowledge(fromWorld, sub.kripke.getKnownWorlds(agent, fromWorld));
                    }
                    else {
                        if (agentSubmodels.get(agent).contains(sub)) {
                            for (World toWorld : obliviousPartial.getWorlds() {
                                World oldtoWorld = obliviousPartial.map.get(toWorld);
                                if (oldKripke.isConnectedBelief(oldFromWorld, oldToWorld)){
                                    // if there is a corresponding edge in the oblivious submodel beliefs, then
                                    // connect beliefs from submodels that belong to this agent to oblivious submodel
                                    newKripke.connectBelief(fromWorld, toWorld);
                                }
                            }
                            for (World toWorld : oldToNew(oldToWorld)) {
                                if (agentSubmodels.get(agent).contains(worldsToSubmodels.get(toWorld))) {
                                    // if there is a corresponding edge in the oblivious submodel knowledge, then
                                    // connect knowledge within and between submodels that belong to this agent
                                    newKripke.connectKnowledge(fromWorld, toWorld);
                                }
                            }
                        }
                        else {
                            for (World oldToWorld : oldKripke.getWorlds()) {
                                if (oldKripke.isConnectedBelief(oldFromWorld, oldToWorld)){
                                    for (World toWorld : oldToNew(oldToWorld)) {
                                        if (!agentSubmodels.get(agent).contains(worldsToSubmodels.get(toWorld))) {
                                            // if there is a corresponding edge in the oblivious submodel beliefs, then
                                            // connect beliefs within each submodel that doesn't belong to this agent
                                            newKripke.connectBelief(fromWorld, toWorld);
                                        }
                                    }
                                }

                            }
                            for (World toWorld : sub.getWorlds()) {
                                if (oldKripke.isConnectedBelief(oldFromWorld, sub.map.get(oldToWorld))){
                                    // if there is a corresponding edge in the oblivious submodel knowledge, then
                                    // connect within (but not between) each submodel that doesn't belong to this agent
                                    newKripke.connectKnowledge(fromWorld, toWorld);
                                }
                            }
                        }
                    }
                }
            }
        }



        EpistemicState newState = new EpistemicState(newKripke, newDesignated);

        if (!newKripke.checkRelations()) {
            System.out.println("action broke kripke:");
            System.out.println(this);
            System.out.println(newDesignated);
            System.out.println(newKripke);
            System.out.println(isOblivious("human1", map.get(newDesignated)));
            for (World toWorld : obliviousKripke.getWorlds()) {
                System.out.println(toWorld);
            }
            System.exit(1);
        }
        //assert(newKripke.checkRelations());
        //newKripke.forceCheck();
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
        assert (!kripke.getKnownWorlds(agent, world).isEmpty());
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
        return (this.getSignatureWithActor().equals(other.getSignatureWithActor()));
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

