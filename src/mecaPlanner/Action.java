package mecaPlanner;

import mecaPlanner.formulae.*;
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
    private Formula precondition;
    private Map<String, Formula> observesIf;
    private Map<String, Formula> awareIf;
    private Map<Formula, Formula> determines;  // sensed formula --> condition
    private Map<Formula, Formula> announces;  // announcement --> condition
    private Map<Assignment, Formula> effects;


    public Action(String name,
                  List<String> parameters,
                  String actor,
                  int cost,
                  Formula precondition,
                  Map<String, Formula> observesIf,
                  Map<String, Formula> awareIf,
                  Map<Formula, Formula> determines,
                  Map<Formula, Formula> announces,
                  Map<Assignment, Formula> effects,
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

    public Formula getPrecondition() {
        return this.precondition;
    }

    public Map<Assignment, Formula> getEffects() {
        return this.effects;
    }

    public Map<String, Formula> getObserves() {
        return this.observesIf;
    }

    public Map<String, Formula> getAware() {
        return this.awareIf;
    }

    public Domain getDomain() {
        return this.domain;
    }

    // WE DO THIS HERE INSTEAD OF IN WORLD IN CASE WE DECIDE TO SWITCH
    // TO BELIEF FORMULA EFFECT CONDITIONS
    public Set<Assignment> getApplicableEffects(World world) {
        Set<Assignment> applicableEffects = new HashSet<>();
            for (Map.Entry<Assignment, Formula> e : effects.entrySet()) {
                Assignment assignment = e.getKey();
                Formula condition = e.getValue();
                if (condition.evaluate(world)) {
                    applicableEffects.add(assignment);
                }
            }
        return applicableEffects;
    }


    public int getCost() {
        return this.cost;
    }

    public Map<Formula, Formula> getDetermines() {
        return this.determines;
    }

    public Map<Formula, Formula> getAnnounces() {
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


    private class PartialResult {
        public KripkeStructure kripke;
        private Map<World,World> map;
        // GIVEN A NEW WORLD, WHAT OLD CLASS IS IT ASSOCIATED WITH FOR EACH AGENT
        private Map<World,Map<String, Set<World>>> postAssignments;
        // GIVEN AN OLD WORLD, WHAT WAS THE EQUIVALENCE CLASS CREATED FROM THAT WORLD FOR EACH AGENT
        private Map<String, Map<World,Set<World>>> roots;
        public PartialResult(KripkeStructure k,
                             Map<World,World> m,
                             Map<World, Map<String, Set<World>>> p,
                             Map<String, Map<World, Set<World>>> r
                            ){
            this.kripke = k;
            this.map = m;
            this.postAssignments = p;
            this.roots = r;
            //this.learnedObserver = l;
        }
        public Set<World> getWorlds() {
            return map.keySet();
        }
        public Set<World> getClassFromRoot(String agent, World w) {
            return roots.get(agent).get(w);
        }
        public Set<World> getPostAssignment(World w, String agent) {
            return postAssignments.get(w).get(agent);
        }
        public World getOldWorld(World w) {
            return map.get(w);
        }

    }


    
    private PartialResult partial(KripkeStructure oldKripke) {

        //assert(oldKripke.containsWorld(oldDesignated));
        //assert(precondition.evaluate(oldDesignated));


        Set<World> oldWorlds = oldKripke.getWorlds();

        // FOR EACH OLD WORLD, FOR EACH AGENT, BUILD FORMULA
        // CONTAINING ALL KNOWLEDGE LEARNED
        // AND ONE FOR BLIEF

        Map<World,Map<String, Formula>> learnedKnowledgeFormula = new HashMap<>();
        Map<World,Map<String, Formula>> learnedFormula = new HashMap<>();
        for (World world : oldWorlds) {
            learnedKnowledgeFormula.put(world, new HashMap<>());
            learnedFormula.put(world, new HashMap<>());
        }

        Map<World, Formula> learnedEffects = new HashMap<>();
        Map<World, Formula> learnedEffectConditions = new HashMap<>();
        Map<World, Formula> learnedDetermined = new HashMap<>();

        Map<String, Map<World, Formula>> learnedObserver = new HashMap<>();
        for (String agent : domain.getAllAgents()) {
            learnedObserver.put(agent, new HashMap<World, Formula>());
        }

        for (World oldWorld : oldWorlds) {

            // WHAT DO AWARE AND OBSERVERS LEARN FROM EFFECT PRECONDITINS
            Map<Fluent, Set<Formula>> fluentsPossibleChangers = new HashMap<>();
            for (Map.Entry<Assignment, Formula> e : effects.entrySet()) {
                Assignment assignment = e.getKey();
                Fluent target = assignment.getFluent();
                Boolean value = assignment.getValue();
                Formula condition = e.getValue();
                assert(!condition.isFalse());
                if (!fluentsPossibleChangers.containsKey(target)) {
                    fluentsPossibleChangers.put(target, new HashSet<Formula>());
                }
                if (oldWorld.alteredByAssignment(assignment) && !condition.isTrue()) {
                    // CONDITION WILL OFTEN BE "True", NO POINT IN STORING THAT
                    fluentsPossibleChangers.get(target).add(condition);
                }
            }
            Set<Formula> revealedConditions = new HashSet<>();
            for (Fluent fluent : fluentsPossibleChangers.keySet()) {
                Formula possibleChangersFormula = Formula.makeDisjunction(fluentsPossibleChangers.get(fluent));
                if (possibleChangersFormula.evaluate(oldWorld)) {
                    revealedConditions.add(possibleChangersFormula);
                }
                else {
                    revealedConditions.add(possibleChangersFormula.negate());
                }
            }
            learnedEffectConditions.put(oldWorld, AndFormula.make(revealedConditions));
            assert(learnedEffectConditions.get(oldWorld).evaluate(oldWorld));


            // WHAT DO OBSERVERS SENSE
            Set<Formula> groundDetermines = new HashSet<>();
            for (Map.Entry<Formula, Formula> e : determines.entrySet()) {
                Formula sensed = e.getKey();
                Formula condition = e.getValue();
                if (condition.evaluate(oldWorld)) {
                    if (sensed.evaluate(oldWorld)) {
                        groundDetermines.add(sensed);
                    }
                    else {
                        groundDetermines.add(sensed.negate());
                    }
                }
            }
            learnedDetermined.put(oldWorld, AndFormula.make(groundDetermines));
            assert(learnedDetermined.get(oldWorld).evaluate(oldWorld));

            Set<Formula> allLearnedEffects = new HashSet<>();
            for (Map.Entry<Assignment, Formula> e : effects.entrySet()) {
                Assignment a = e.getKey();
                Formula condition = e.getValue();
                if ((!oldWorld.alteredByAssignment(a)) || (!condition.evaluate(oldWorld))) {  // NOT IN PAPER
                    Fluent f = a.getFluent();
                    Boolean value = a.getValue();
                    if (value) {
                        allLearnedEffects.add(f);
                    }
                    else {
                        allLearnedEffects.add(f.negate());
                    }
                }
            }
            learnedEffects.put(oldWorld, AndFormula.make(allLearnedEffects));
            assert(learnedDetermined.get(oldWorld).evaluate(oldWorld));

            // WHAT DO AGENTS LEARN BECAUSE THEY KNOW THEIR OBSERVER STATUS
            for (String agent : domain.getAllAgents()) {

                Formula observerConditions = observesIf.get(agent);
                Formula awareConditions = awareIf.get(agent);

                if (isObservant(agent, oldWorld)){
                    // THERE'S PROBABLY A BETTER PLACE FOR THIS CHECK
                    if (isAware(agent, oldWorld)) {
                        throw new RuntimeException("Agent " + agent +
                                                   " is both fully and partially observant of action " +
                                                   getSignature());
                    }
                    learnedObserver.get(agent).put(oldWorld, AndFormula.make(observerConditions,
                                                                                  awareConditions.negate()));
                }
                else if (isAware(agent, oldWorld)){
                    learnedObserver.get(agent).put(oldWorld, AndFormula.make(observerConditions.negate(),
                                                                                  awareConditions));
                }
                else {
                    learnedObserver.get(agent).put(oldWorld, AndFormula.make(observerConditions.negate(),
                                                                                  awareConditions.negate()));
                }
                assert(learnedObserver.get(agent).get(oldWorld).evaluate(oldWorld));
            }


            // PUT TOGETHER ALL SOURCES OF KNOWLEDGE
            for (String agent : domain.getAllAgents()) {
                if (isObservant(agent, oldWorld)){
                    learnedKnowledgeFormula.get(oldWorld).put(agent, AndFormula.make(
                        learnedDetermined.get(oldWorld),
                        learnedEffectConditions.get(oldWorld),
                        learnedObserver.get(agent).get(oldWorld)));
                }
                else if (isAware(agent, oldWorld)){
                    learnedKnowledgeFormula.get(oldWorld).put(agent, AndFormula.make(
                        learnedEffectConditions.get(oldWorld),
                        learnedObserver.get(agent).get(oldWorld)));
                }
                else {
                    learnedKnowledgeFormula.get(oldWorld).put(agent, learnedObserver.get(agent).get(oldWorld));
                }
                assert(learnedKnowledgeFormula.get(oldWorld).get(agent).evaluate(oldWorld));
            }



            // PUT TOGETHER ALL SOURCES OF BELIEF

            Set<Formula> actualAnnouncements = new HashSet<>();
            for (Map.Entry<Formula, Formula> e : announces.entrySet()) {
                Formula announcement = e.getKey();
                Formula condition = e.getValue();
                if (condition.evaluate(oldWorld)) {
                    actualAnnouncements.add(announcement);
                }
            }
            Formula unifiedAnnouncement = AndFormula.make(actualAnnouncements);


            for (String agent : domain.getAllAgents()) {
                if (isObservant(agent, oldWorld)){
                    Formula knowsNotAnnouncements = new KnowsFormula(agent, unifiedAnnouncement.negate());
                    if (knowsNotAnnouncements.evaluate(oldKripke, oldWorld)) {
                        learnedFormula.get(oldWorld).put(agent, new Literal(true));
                    }
                    else {
                        learnedFormula.get(oldWorld).put(agent, unifiedAnnouncement);
                    }
                }
                else if (isAware(agent, oldWorld)){
                    learnedFormula.get(oldWorld).put(agent, new Literal(true));
                }
                else {
                    learnedFormula.get(oldWorld).put(agent, new Literal(true));
                }
            }

        }

        Map<String, Map<World,Set<World>>> roots = new HashMap<>();

        // GROUPED WORLDS (WITH POSSIBLE OVERLAPS) INTO NEW EQUIVALENCE CLASSES
        Map<String, Set<Set<World>>> equivalenceClasses = new HashMap<>();
        for (String agent : domain.getAllAgents()) {
            equivalenceClasses.put(agent, new HashSet<Set<World>>());
            roots.put(agent, new HashMap<World, Set<World>>());
            for (World oldWorld : oldWorlds) {
                Set<World> equivalent = new HashSet<>();
                //System.out.println("LK[" + agent + "]=" + learnedKnowledgeFormula.get(oldWorld).get(agent));
                assert(learnedKnowledgeFormula.get(oldWorld).get(agent).evaluate(oldWorld));
                assert(oldKripke.getKnownWorlds(agent, oldWorld).contains(oldWorld));
                for (World toWorld: oldKripke.getKnownWorlds(agent, oldWorld)) {
                    if (learnedKnowledgeFormula.get(oldWorld).get(agent).evaluate(toWorld)) {
                        equivalent.add(toWorld);
                    }
                }
                equivalenceClasses.get(agent).add(equivalent);
                roots.get(agent).put(oldWorld, equivalent);
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
        Map<World, Map<String, Set<World>>> postAssignments = new HashMap<>();
        for (World oldWorld : oldWorlds) {
            if (precondition.evaluate(oldWorld)) {
                for (Map<String, Set<World>> assignment : assignments.get(oldWorld)) {
                    World newWorld = oldWorld.update(getApplicableEffects(oldWorld));
                    newWorlds.add(newWorld);
                    newToOld.put(newWorld, oldWorld);
                    postAssignments.put(newWorld, assignment);
                }
            }
        }


        // KNOWLEDGE RELATIONS
        Map<String, Relation> newKnowledges = new HashMap<>();

        for (String agent : domain.getAllAgents()) {
            Relation newKnowledge = new Relation();
            for (World fromWorld : newWorlds) {
                for (World toWorld : newWorlds) {
                    if (postAssignments.get(fromWorld).get(agent).equals(
                        postAssignments.get(toWorld).get(agent))){
                        newKnowledge.connect(fromWorld, toWorld);
                    }
                }
            }
            newKnowledges.put(agent, newKnowledge);
        }




        // BELIEF RELATIONS
        Map<String, Relation> newBeliefs = new HashMap<>();
        for (String agent : domain.getAllAgents()) {
            Relation newBelief = new Relation();
            for (World fromWorld: newWorlds) {
                World oldFromWorld = newToOld.get(fromWorld);
                Formula learnedBelief = learnedFormula.get(oldFromWorld).get(agent);
                //System.out.println("LB[" + agent + "]=" + learnedBelief);

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
                    Log.debug("observant agent " + agent + " reset by " + getSignatureWithActor() +
                              " at " + fromWorld.toString());
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

            }
            newBeliefs.put(agent, newBelief);
        }

        KripkeStructure newKripke = new KripkeStructure(newWorlds, newBeliefs, newKnowledges);

        //System.out.println(newToOld);
        //return new Action.PartialResult(newKripke, newPartialDesignated, newToOld, learnedObserver);
        return new Action.PartialResult(newKripke, newToOld, postAssignments, roots);
    }




    public Action.UpdatedStateAndModels transition(EpistemicState beforeState, Map<String, Model> oldModels) {
        Log.debug("transition: " + getSignatureWithActor());

        assert(precondition.evaluate(beforeState));

        // UPDATE THE MODELS
        Map<String, Model> newModels = new HashMap();
        for (String agent : oldModels.keySet()) {
            Model updatedModel = oldModels.get(agent).update(beforeState, this);
            newModels.put(agent, updatedModel);
        }

        KripkeStructure oldKripke = beforeState.getKripke();
        World oldDesignated = beforeState.getDesignatedWorld();

        Map<String, Set<PartialResult>> agentSubmodels = new HashMap<>();
        Set<PartialResult> submodels = new HashSet<>();

        //Map<String, Map<World, Formula>> learnedObserver = new HashMap<>();
        //for (String agent : domain.getAllAgents()) {
        //    learnedObserver.put(agent, new HashMap<World, Formula>());
        //}

        Action.PartialResult actualPartial = this.partial(oldKripke);

        submodels.add(actualPartial);
        for (String agent : domain.getAllAgents()) {
            agentSubmodels.put(agent, new HashSet<PartialResult>());
            agentSubmodels.get(agent).add(actualPartial);
            //learnedObserver.get(agent).putAll(actualPartial.learnedObserver.get(agent));
        }

        World newDesignated = null;
        for (World w : actualPartial.getWorlds()) {
            if (actualPartial.getOldWorld(w) == oldDesignated) {
                for (String agent : domain.getAllAgents()) {
                    if (actualPartial.getPostAssignment(w, agent).equals(
                        actualPartial.getClassFromRoot(agent, oldDesignated))) {
                        newDesignated = w;
                    }
                }
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
                if (isOblivious(agent, actualPartial.map.get(newWorld))) {
                    anyOblivious = true;
                    break;
                }
            }
        }
        if (!anyOblivious) {
            assert(actualPartial.kripke.checkRelations());
            EpistemicState newState = new EpistemicState(actualPartial.kripke, newDesignated);
            newState.trim();
            newState.reduce();
            return new Action.UpdatedStateAndModels(newState, newModels);
        }


        // BUILD NULL ACTION AND USE PARTIAL TO GET OBLIVIOUS SUB-MODEL
        Map<String, Formula> nullObserverConditions = new HashMap<>();
        Map<String, Formula> nullAwareConditions = new HashMap<>();
        for (String agent : domain.getAllAgents()) {
            nullObserverConditions.put(agent, new Literal(true));
            nullAwareConditions.put(agent, new Literal(false));
        }

        Action nullAction = new Action("nullAction",                               // name
                                       new ArrayList<String>(),                    // parameters
                                       "nullActor",                                // actor
                                       1,                                          // cost
                                       new Literal(true),                          // preconditions 
                                       nullObserverConditions,                     // observesIf
                                       nullAwareConditions,                        // awareIf
                                       new HashMap<Formula, Formula>(),  // determines
                                       new HashMap<Formula, Formula>(), // announces
                                       new HashMap<Assignment, Formula>(),    // effects
                                       domain
                                      );
        Action.PartialResult obliviousPartial = nullAction.partial(oldKripke);
        KripkeStructure obliviousKripke = obliviousPartial.kripke;
        submodels.add(obliviousPartial);

        // BUILD HYPOTHETICAL MODELS
        for (String agent : domain.getAllAgents()) {
            boolean obliviousAnywhere = false;
            for (World w : oldKripke.getWorlds()) {
                if (isOblivious(agent,w)) {
                    obliviousAnywhere = true;
                    for (Action a : possibleActions(agent, oldKripke, w)) {
                        if (!a.equals(this)) {
                            Action.PartialResult hypotheticalPartial = a.partial(oldKripke);
                            agentSubmodels.get(agent).add(hypotheticalPartial);
                            submodels.add(hypotheticalPartial);
                            //newToOld.addAll(hypotheticalPartial.map);
                        }
                    }
                }
                if (obliviousAnywhere) {
                    agentSubmodels.get(agent).add(obliviousPartial);
                }
            }
        }

        Map<World, Action.PartialResult> worldsToSubmodels = new HashMap<>();
        for (PartialResult sub : submodels) {
            for (World w : sub.map.keySet()) {
                worldsToSubmodels.put(w, sub);
            }
        }
        KripkeStructure newKripke = new KripkeStructure(worldsToSubmodels.keySet(), oldKripke.getAgents());


        for (PartialResult sub : submodels) {
            for (String agent : domain.getAllAgents()) {
                for (World fromWorld : sub.getWorlds()) {
                    World oldFromWorld = sub.map.get(fromWorld);
                    if (isObservant(agent, oldFromWorld) || isAware(agent, oldFromWorld)) {
                        newKripke.connectBelief(agent, fromWorld, sub.kripke.getBelievedWorlds(agent, fromWorld));
                        newKripke.connectKnowledge(agent, fromWorld, sub.kripke.getKnownWorlds(agent, fromWorld));
                    }
                    else {
                        if (agentSubmodels.get(agent).contains(sub)) {
                            for (World toWorld : obliviousPartial.getWorlds()) {
                                World oldToWorld = obliviousPartial.map.get(toWorld);
                                if (oldKripke.isConnectedBelief(agent, oldFromWorld, oldToWorld)){
                                    // if there is a corresponding edge in the oblivious submodel beliefs, then
                                    // connect beliefs from submodels that belong to this agent to oblivious submodel
                                    newKripke.connectBelief(agent, fromWorld, toWorld);
                                }
                            }
                            for (PartialResult toSub : agentSubmodels.get(agent)) {
                                for (World toWorld : toSub.getWorlds()) {
                                    //System.out.println("??? " + fromWorld.getName() + " -> " + toWorld.getName());
                                    World oldToWorld = toSub.map.get(toWorld);
                                    if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)){
                                        // if there is a corresponding edge in the oblivious submodel knowledge, then
                                        // connect knowledge within and between submodels that belong to this agent
                                        //System.out.println("::: " + fromWorld.getName() + " -> " + toWorld.getName());
                                        newKripke.connectKnowledge(agent, fromWorld, toWorld);
                                    }
                                }
                            }
                        }
                        else {
                            for (World toWorld : sub.getWorlds()) {
                                World oldToWorld = sub.map.get(toWorld);
                                if (oldKripke.isConnectedBelief(agent, oldFromWorld, oldToWorld)){
                                    // if there is a corresponding edge in the oblivious submodel beliefs, then
                                    // connect beliefs within each submodel that doesn't belong to this agent
                                    newKripke.connectBelief(agent, fromWorld, toWorld);
                                }
                                if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)){
                                    // if there is a corresponding edge in the oblivious submodel knowledge, then
                                    // connect within (but not between) each submodel that doesn't belong to this agent
                                    newKripke.connectKnowledge(agent, fromWorld, toWorld);
                                }
                            }
                        }
                    }
                }
            }
        }

        assert(newKripke.checkRelations());
        EpistemicState newState = new EpistemicState(newKripke, newDesignated);
        newState.trim();
        newState.reduce();
        return new Action.UpdatedStateAndModels(newState, newModels);
    }


    // MOST OF THE TIME WE WON'T BE UPDATING AGENT MODELS
    public EpistemicState transition(EpistemicState beforeState) {
        Action.UpdatedStateAndModels result = transition(beforeState, new HashMap<String, Model>());
        return result.getState();
    }


    // FOR COMPUTING HYPOTHETICAL ACTIONS FOR OBLIVIOUS AGENTS
    private Set<Action> possibleActions(String agent, KripkeStructure kripke, World world) {
        assert (!kripke.getKnownWorlds(agent, world).isEmpty());
        Set<Action> actions = new HashSet<>();
        for (Action action : domain.getAllActions()) {
            Formula possiblyPreconditioned = new KnowsFormula(agent,
                action.getPrecondition().negate()).negate();
            Formula possiblyOblivious = AndFormula.make(
                new KnowsFormula(agent, action.observesIf.get(agent)).negate(),
                new KnowsFormula(agent, action.awareIf.get(agent)).negate());
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
        for (Map.Entry<String, Formula> o : observesIf.entrySet()) {
            str.append("\t\t");
            str.append(o.getKey());
            str.append(" if ");
            str.append(o.getValue());
            str.append("\n");
        }

        str.append("\tAware\n");
        for (Map.Entry<String, Formula> a : awareIf.entrySet()) {
            str.append("\t\t");
            str.append(a.getKey());
            str.append(" if ");
            str.append(a.getValue());
            str.append("\n");
        }

        str.append("\tDetermines\n");
        for (Map.Entry<Formula, Formula> e : determines.entrySet()) {
            Formula sensed = e.getKey();
            Formula condition = e.getValue();
            str.append("\t\t");
            str.append(sensed);
            str.append(" if ");
            str.append(condition);
            str.append("\n");
 
        }

        str.append("\tAnnounces\n");
        for (Map.Entry<Formula, Formula> e : announces.entrySet()) {
            Formula announcement = e.getKey();
            Formula condition = e.getValue();
            str.append("\t\t");
            str.append(announcement);
            str.append(" if ");
            str.append(condition);
            str.append("\n");
 
        }

        str.append("\tCauses\n");
        for (Map.Entry<Assignment, Formula> e : effects.entrySet()) {
            Assignment assignment = e.getKey();
            Formula condition = e.getValue();
            str.append("\t\t");
            str.append(assignment);
            str.append(" if ");
            str.append(condition);
            str.append("\n");
        }
 
        return str.toString();
    }

}

