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

    protected Domain domain;

    protected String name;
    protected List<String> parameters;
    protected int cost;
    protected String actor;
    protected FluentFormula precondition;
    protected Map<String, FluentFormula> observesIf;
    protected Map<String, FluentFormula> awareIf;
    protected Map<FluentLiteral, FluentFormula> effects;
    protected Set<BeliefFormula> announces;


    public Action(String name,
                  List<String> parameters,
                  String actor,
                  int cost,
                  FluentFormula precondition,
                  Map<String, FluentFormula> observesIf,
                  Map<String, FluentFormula> awareIf,
                  Set<FluentFormula> determines,
                  Set<BeliefFormula> announces,
                  Map<FluentLiteral, FluentFormula> effects,
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

    public Map<FluentLiteral, FluentFormula> getEffects() {
        return this.effects;
    }

    public Set<FluentLiteral> getApplicableEffects(World world) {
        Set<FluentLiteral> applicableEffects = new HashSet<>();
            for (Map.Entry<FluentLiteral, FluentFormula> e : effects.entrySet()) {
                FluentLiteral effect = e.getKey();
                FluentFormula condition = e.getValue();
                if (condition.holds(world)) {
                    applicableEffects.add(effect);
                }
            }
        return applicableEffects;
    }


    public int getCost() {
        return this.cost;
    }

    public boolean executable(EpistemicState state) {
        return precondition.holds(state.getDesignatedWorld());
    }

    protected boolean executable(World world) {
        return precondition.holds(world);
    }

    public Boolean necessarilyExecutable(NDState state) {
        for (World w : state.getDesignatedWorlds()) {
            if (!executable(w)) {
                return false;
            }
        }
        return true;
    }


    public Boolean isObservant(String agent, World world) {
        return (observesIf.containsKey(agent) && observesIf.get(agent).holds(world));
    }

    public Boolean isAware(String agent, World world) {
        return (awareIf.containsKey(agent) && awareIf.get(agent).holds(world));
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


//1. copy old worlds into oblivious worlds
//2. hook up oblivious worlds as old
//4. for each world in new, for each agent:
//    determine if oblivious, aware, or observant
//    oblivious, hook aback to oblivious workds, knowledge three ways?
//    aware and observant: determine what is learned (not action preconditons though, no point)
//        aware: revealed effect conditions
//        observant: revealed effect conditions, sensed facts, non-rejected announcements
//    connect to to-worlds according to old relation, except if to-world violates learned
//    if no no-world connections, connect according to old knowledge relation, exept if violates learned
//5. trim

    public Action.UpdatedStateAndModels transition(EpistemicState beforeState, Map<String, Model> oldModels) {
        Log.debug("transition: " + getSignatureWithActor());

        KripkeStructure oldKripke = beforeState.getKripke();
        Set<World> oldWorlds = oldKripke.getWorlds();
        Set<World> obliviousWorlds = new HashSet<>();
        Set<World> observedWorlds = new HashSet<>();
        World newDesignatedWorld = null;
        Map<World, World> observedWorldsToOld = new HashMap<World, World>();
        Map<World, World> oldWorldsToOblivious = new HashMap<World, World>();
        Map<String, Relation> newBeliefs = new HashMap<>();
        Map<String, Relation> newKnowledges = new HashMap<>();
        for (String a : domain.getAllAgents()) {
            newBeliefs.put(a, new Relation());
            newKnowledges.put(a, new Relation());
        }




        // FOR EACH OLD WORLD, IF PRECONDITIONS HOLD, MUTATE INTO NEW, USING CONDITIONAL EFFECTS
        for (World oldWorld : oldWorlds) {
            if (precondition.holds(oldWorld)) {
                World observedWorld = oldWorld.update(getApplicableEffects(oldWordl));
                observedWorlds.add(observedWorld);
                observedWorldsToOld.put(observedWorld, oldWorld);
                if (oldWorld.equals(beforeState.getDesignatedWorld())) {
                    newDesignatedWorld = observedWorld;
                }
            }
        }
        assert(newDesignatedWorld != null);
        assert(observedWorlds.contains(newDesignatedWorld));
        Set<World> resultWorlds = new HashSet<World>(observedWorlds);


        // IN ANY OF THE NEW WORLDS, ARE THERE ANY OBSERVANT AGENTS? ANY AWARE? ANY OBLIVIOUS?
        boolean anyObservers = false;
        boolean anyAware = false;
        booldean anyOblivious = false;
        for (World world : observedWorlds) {
            if (anyObservers && anyAware && anyOblivious) {
                break;
            }
            for (String a : agents) {
                if (isObservant(agent, world)){
                    anyObservers = true;
                }
                else if (isAware(agent, world)){
                    anyAware = true;
                }
                else {
                    anyOblivious = true;
                }
            }
        }




        for (World fromWorld : observedWorlds) {
            World oldFromWorld = observedWorldsToOld(fromWorld);

            // WHAT DO AWARE AND OBSERVERS LEARN FROM EFFECT PRECONDITINS
            Set<FluentFormula> revealedConditions = new HashSet<>();
            if (anyObservers || anyAware) {
                for (Map.Entry<FluentLiteral, FluentFormula> e : effects.entrySet()) {
                    FluentLiteral effect = e.getKey();
                    FluentFormula condition = e.getValue();
                    if (!effect.holds(oldFromWorld) && !condition.alwaysHolds()) {
                        // CONDITION WILL OFTEN BE "True", NO POINT IN STORING THAT
                        if (condition.holds(oldFromWorld)) {
                            revealedConditions.add(condition);
                        }
                        else {
                            revealedConditions.add(condition.negate());
                        }
                    }
                }
            }



            // WHAT DO OBSERVERS SENSE
            Set<FluentFormula> groundDetermines = new HashSet<>();
            if (anyObservers) {
                for (FluentFormula f : determines) {
                    if (f.holds(oldFromWorld)) {
                        groundDetermines.add(f);
                    }
                    else {
                        groundDetermines.add(f.negate());
                    }
                }
            }


            // IF THERE ARE ANY OBLIVOUS AGENTS, SET UP OBLIVIOUS WORLDS
            if (anyOblivious) {
                for (World w : oldWorlds) {
                    World obliviousWorld = new World(w);
                    obliviousWorlds.add(obliviousWorld);
                    oldWorldsToOblivious.put(w, obliviousWorld);
                }
                for (String a : domain.getAllAgents()) {
                    for (World w : oldWorlds) {
                        World oblivousFrom = oldWorldsToOblivioius.get(w);
                        for (World oldToWorld: oldKripke.getBelievedWorlds(agent, w)) {
                            newBeliefs.get(agent).connect(oblivousFrom, oldWorldsToOblivious(oldToWorld));
                        }
                        for (World oldToWorld: oldKripke.getKnownWorlds(agent, w)) {
                            newKnowledges.get(agent).connect(oblivousFrom, oldWorldsToOblivious(oldToWorld));
                        }
                    }
                }
                resultWorlds.addAll(obliviousWorlds);
            }

            // GO FOR EACH AGENT, CONNECT TO-WORLDS FOR BELIEF AND KNOWLEDGE
            for (String agent : domain.getAllAgents()) {
                if (isObservant(agent, oldWorld)){

                    // WHAT DOES THE AGENT HEAR ANNOUNCED AND NOT REJECT
                    Set<BeliefFormula> acceptedAnnouncements = new HashSet<>();
                    for (BeliefFormula announcement : announces) {
                        BeliefFormula knowsNotAnnouncement = new BeliefFormulaKnows(agent, announcement.negate());
                        if (!knowsNotAnnouncement.holds(beforeState)) {
                            acceptedAnnouncements.add(announcement);
                        }
                    }

                    // WHAT DOES THE AGENT LEARN WITH CERTAINTY BY OBSERVING THE ACTION
                    // ASSUMING THAT ANNOUNCEMTNS MIGHT BE LIES
                    Set<FluentFormula> allKnowledgeLearned = new HashSet<>();
                    allKnowledgeLearned.addAll(revealedConditions);
                    allKnowledgeLearned.addAll(groundDetermines);
                    FluentFormula learnedKnowledgeFormula = new FluentFormulaAnd(allKnowledgeLearned);

                    // WHAT DOES THE AGENT LEARN BY OBSERVING THE ACTION
                    // ASSUMING THAT NON-REJECTED ANNOUNCEMENTS ARE TRUE
                    Set<BeliefFormula> allBeliefLearned = new HashSet<>();
                    allBeliefLearned.addAll(allKnowledgeLearned);
                    allBeliefLearned.addAll(acceptedAnnouncements);
                    BeliefFormula learnedBeliefFormula = new BeliefFormulaAnd(allBeliefLearned);

                    // COPY CONNECTIONS FROM OLD BELIEF RELATION UNLESS TO-WORLD CONTRADICTS LEARNED BELIEFS
                    for (World toWorld: observantWorlds) {
                        World oldtoWorld = observedWorldsToOld(toWorld);
                        if (oldKripke.isConnectedBelief(agent, oldFromWorld, oldToWorld)) {
                            if (learnedBeliefFormula.holds(oldToWorld)) {
                                newBeliefs.get(agent).connect(fromWorld, toWorld);
                            }
                        }
                    }

                    // IF NO CONNECTIONS WERE COPIED, AGENT LEARNED SOMETHING BELIEVED IMPOSSIBLE: BELIEF RESET
                    if (oldKripke.getBelievedWorlds(agent, oldFromWorld).isEmpty()) {
                        for (World toWorld: observantWorlds) {
                            World oldtoWorld = observedWorldsToOld(toWorld);
                            if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)) {
                                if (learnedBeliefFormula.holds(oldToWorld)) {
                                    newBeliefs.get(agent).connect(fromWorld, toWorld);
                                }
                            }
                        }
                    }

                    // COPY CONNECTIONS FROM OLD KNOWLEDGE RELATION UNLESS TO-WORLD CONTRADICTS LEARNED KNOWLEDGE
                    for (World toWorld: observantWorlds) {
                        World oldToWorld = observedWorldsToOld(toWorld);
                        if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)) {
                            if (learnedKnowledgeFormula.holds(oldToWorld)) {
                                newKnowledges.get(agent).connect(fromWorld, toWorld);
                            }
                        }
                    }

                }
                if (isAware(agent, oldWorld)){

                    // SHOULD AWARE AGENTS LEARN REVEALED EFFECT CONDITIONS? COULD GO EITHER WAY.
                    // IF YES, THERE'S NO DISTINCTION BETWEEN OBSERVERS AND AWARE FOR PURELY ONTIC ACTIONS
                    // WE'LL SAY NO, EVEN THOUGH THIS PROBABLY CONTRADICTS THE ASSUMPTION
                    // IN THE KR2020 THAT THERE IS NO DIFFERENCE BETWEEN OBSERVERS AND AWARE FOR PURELY ONTIC ACTINOS

                    // COPY CONNECTIONS FROM OLD BELIEF RELATION
                    for (World toWorld: observantWorlds) {
                        World oldtoWorld = observedWorldsToOld(toWorld);
                        if (oldKripke.isConnectedBelief(agent, oldFromWorld, oldToWorld)) {
                            newBeliefs.get(agent).connect(fromWorld, toWorld);
                        }
                    }

                    // IF NO CONNECTIONS WERE COPIED, AGENT LEARNED SOMETHING BELIEVED IMPOSSIBLE: BELIEF RESET
                    // SPECIFICALLY, A NEW WORLD HAS OUTGOING EDGES ONLY TO OLD WORLDS
                    if (oldKripke.getBelievedWorlds(agent, oldFromWorld).isEmpty()) {
                        for (World toWorld: observantWorlds) {
                            World oldtoWorld = observedWorldsToOld(toWorld);
                            if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)) {
                                newBeliefs.get(agent).connect(fromWorld, toWorld);
                            }
                        }
                    }

                    // COPY CONNECTIONS FROM OLD KNOWLEDGE RELATION 
                    for (World toWorld: observantWorlds) {
                        World oldtoWorld = observedWorldsToOld(toWorld);
                        if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)) {
                            newKnowledges.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                }
                else {  //oblivious
                    for (World oldToWorld: oldWorlds) {
                        if (oldKripke.isConnectedBelief(agent, oldFromWorld, oldToWorld)) {
                            newBeliefs.get(agent).connect(fromWorld, oldWorldsToOblivious.get(oldToWorld));
                        }
                        if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)) {
                            newKnowledge.get(agent).connect(fromWorld, oldWorldsToOblivious.get(oldToWorld));
                            newKnowledge.get(agent).connect(oldWorldsToOblivious.get(oldToWorld), fromWorld);
                        }
                    }
                    for (World toWorld: observedWorlds) {
                        if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, observedWorldsToOld.get(ToWorld))) {
                            newKnowledge.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                }
            }
        }


        KripkeStructure newKripke = new KripkeStructure(resultWorlds, newBeliefs, newKnowledges);

        EpistemicState newState = new EpistemicState(newKripke, newDesignatedWorld);

        assert(Test.checkRelations(domain, newState));


        // UPDATE THE MODELS
        Map<String, Model> newModels = new HashMap();
        for (String agent : oldModels.keySet()) {
            // THIS IS WRONG, NEED TO HIDE INFO THE AGENT SHOULDN'T GET
            if (isObservant(agent)) {
                NDState perspective = beforeState.getBeliefPerspective(agent);
                Model updatedModel = oldModels.get(agent).update(perspective, this);
                newModels.put(agent, updatedModel);
            }
            else if (isAware(agent)) {
                NDState perspective = beforeState.getBeliefPerspective(agent);
                Model updatedModel = oldModels.get(agent).update(perspective, this);
                newModels.put(agent, updatedModel);
            }
            else {
                newModels.put(agent, oldModels.get(agent));
            }
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

        str.append("\n\tPrecondition: ");
        str.append(precondition);

        str.append("\n\tObserves\n");
        for (Map.Entry<String, FluentFormula> o : observesIf.entrySet()) {
            str.append("\t\t");
            str.append(o.getKey());
            str.append(" if ");
            str.append(o.getValue());
            str.append("\n");
        }

        str.append("\tAware\n");
        for (Map.Entry<String, FluentFormula> a : awareIf.entrySet()) {
            str.append("\t\t");
            str.append(a.getKey());
            str.append(" if ");
            str.append(a.getValue());
            str.append("\n");
        }
        return str.toString();
    }



}
