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
    protected Set<FluentFormula> determines;
    protected Set<BeliefFormula> announces;
    protected Map<FluentLiteral, FluentFormula> effects;


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
            assert (w != null);
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

    public EpistemicState transition(EpistemicState beforeState) {
        Action.UpdatedStateAndModels result = transition(beforeState, new HashMap<String, Model>());
        return result.getState();
    }


    // SHOULD TRIM UNREACHABLE WORLDS AT THE END OF TRANSITION
    // THESE CAN COME ABOUT IF EFFEC PRECONDITIONS CAUSE AGENTS TO BELIEVE
    // WORLDS NOT POSSIBLE EVEN THOUGH THEY WERE CREATED
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
                World observedWorld = oldWorld.update(getApplicableEffects(oldWorld));
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
        boolean anyOblivious = false;
        for (World world : observedWorlds) {
            if (anyObservers && anyAware && anyOblivious) {
                break;
            }
            for (String a : domain.getAllAgents()) {
                if (isObservant(a, world)){
                    anyObservers = true;
                }
                else if (isAware(a, world)){
                    anyAware = true;
                }
                else {
                    anyOblivious = true;
                }
            }
        }


        // IF THERE ARE ANY OBLIVOUS AGENTS, SET UP OBLIVIOUS WORLDS
        if (anyOblivious) {
            for (World w : oldWorlds) {
                World obliviousWorld = new World(w.getName() + "_", w);
                obliviousWorlds.add(obliviousWorld);
                oldWorldsToOblivious.put(w, obliviousWorld);
            }
            for (String a : domain.getAllAgents()) {
                for (World w : oldWorlds) {
                    World oblivousFrom = oldWorldsToOblivious.get(w);
                    for (World oldToWorld: oldKripke.getBelievedWorlds(a, w)) {
                        newBeliefs.get(a).connect(oblivousFrom, oldWorldsToOblivious.get(oldToWorld));
                    }
                    for (World oldToWorld: oldKripke.getKnownWorlds(a, w)) {
                        newKnowledges.get(a).connect(oblivousFrom, oldWorldsToOblivious.get(oldToWorld));
                    }
                }
            }
            resultWorlds.addAll(obliviousWorlds);
        }



        for (World fromWorld : observedWorlds) {
            World oldFromWorld = observedWorldsToOld.get(fromWorld);

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

            // GO FOR EACH AGENT, CONNECT TO-WORLDS FOR BELIEF AND KNOWLEDGE
            for (String agent : domain.getAllAgents()) {
                if (isObservant(agent, oldFromWorld)){
                    assert(anyObservers);

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
                    for (World toWorld: observedWorlds) {
                        World oldToWorld = observedWorldsToOld.get(toWorld);
                        if (oldKripke.isConnectedBelief(agent, oldFromWorld, oldToWorld)) {
                            if (learnedBeliefFormula.holdsAtWorld(oldKripke, oldToWorld)) {
                                newBeliefs.get(agent).connect(fromWorld, toWorld);
                            }
                        }
                    }

                    // IF NO CONNECTIONS WERE COPIED, AGENT LEARNED SOMETHING BELIEVED IMPOSSIBLE: BELIEF RESET
                    if (newBeliefs.get(agent).getToWorlds(fromWorld).isEmpty()) {
                        Log.debug("observant agent " + agent + " reset by " + getSignatureWithActor());
                        for (World toWorld: observedWorlds) {
                            World oldToWorld = observedWorldsToOld.get(toWorld);
                            if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)) {
                                if (learnedKnowledgeFormula.holdsAtWorld(oldKripke, oldToWorld)) {
                                    newBeliefs.get(agent).connect(fromWorld, toWorld);
                                }
                            }
                        }
                    }

                    // COPY CONNECTIONS FROM OLD KNOWLEDGE RELATION UNLESS TO-WORLD CONTRADICTS LEARNED KNOWLEDGE
                    for (World toWorld: observedWorlds) {
                        World oldToWorld = observedWorldsToOld.get(toWorld);
                        if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)) {
                            if (learnedKnowledgeFormula.holds(oldToWorld)) {
                                newKnowledges.get(agent).connect(fromWorld, toWorld);
                            }
                        }
                    }

                }
                else if (isAware(agent, oldFromWorld)){
                    assert(anyAware);

                    // SHOULD AWARE AGENTS LEARN REVEALED EFFECT CONDITIONS? COULD GO EITHER WAY.
                    // IF YES, THERE'S NO DISTINCTION BETWEEN OBSERVERS AND AWARE FOR PURELY ONTIC ACTIONS
                    // WE'LL SAY NO, EVEN THOUGH THIS PROBABLY CONTRADICTS THE ASSUMPTION
                    // IN THE KR2020 THAT THERE IS NO DIFFERENCE BETWEEN OBSERVERS AND AWARE FOR PURELY ONTIC ACTINOS

                    // COPY CONNECTIONS FROM OLD BELIEF RELATION
                    for (World toWorld: observedWorlds) {
                        World oldToWorld = observedWorldsToOld.get(toWorld);
                        if (oldKripke.isConnectedBelief(agent, oldFromWorld, oldToWorld)) {
                            newBeliefs.get(agent).connect(fromWorld, toWorld);
                        }
                    }

                    // IF NO CONNECTIONS WERE COPIED, AGENT LEARNED SOMETHING BELIEVED IMPOSSIBLE: BELIEF RESET
                    // SPECIFICALLY, A NEW WORLD HAS OUTGOING EDGES ONLY TO OLD WORLDS
                    if (newBeliefs.get(agent).getToWorlds(fromWorld).isEmpty()) {
                        Log.debug("aware agent " + agent + " reset by " + getSignatureWithActor());
                        for (World toWorld: observedWorlds) {
                            World oldToWorld = observedWorldsToOld.get(toWorld);
                            if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)) {
                                newBeliefs.get(agent).connect(fromWorld, toWorld);
                            }
                        }
                    }

                    // COPY CONNECTIONS FROM OLD KNOWLEDGE RELATION 
                    for (World toWorld: observedWorlds) {
                        World oldToWorld = observedWorldsToOld.get(toWorld);
                        if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)) {
                            newKnowledges.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                }
                else {  //oblivious
                    assert(anyOblivious);
                    for (World oldToWorld: oldWorlds) {
                        if (oldKripke.isConnectedBelief(agent, oldFromWorld, oldToWorld)) {
                            newBeliefs.get(agent).connect(fromWorld, oldWorldsToOblivious.get(oldToWorld));
                        }
                        if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)) {
                            newKnowledges.get(agent).connect(fromWorld, oldWorldsToOblivious.get(oldToWorld));
                            newKnowledges.get(agent).connect(oldWorldsToOblivious.get(oldToWorld), fromWorld);
                        }
                    }
                    for (World toWorld: observedWorlds) {
                        if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, observedWorldsToOld.get(toWorld))) {
                            newKnowledges.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                }
            }
        }



        //assert (resultWorlds.size() == (anyOblivious ? oldWorlds.size() * 2 : oldWorlds.size()));

        KripkeStructure newKripke = new KripkeStructure(resultWorlds, newBeliefs, newKnowledges);

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
            NDState perspective = beforeState.getBeliefPerspective(agent);
            Model updatedModel = oldModels.get(agent).update(perspective, this);
            newModels.put(agent, updatedModel);
            //// THIS IS WRONG, NEED TO HIDE INFO THE AGENT SHOULDN'T GET
            //if (isObservant(agent)) {
            //    NDState perspective = beforeState.getBeliefPerspective(agent);
            //    Model updatedModel = oldModels.get(agent).update(perspective, this);
            //    newModels.put(agent, updatedModel);
            //}
            //else if (isAware(agent)) {
            //    NDState perspective = beforeState.getBeliefPerspective(agent);
            //    Model updatedModel = oldModels.get(agent).update(perspective, this);
            //    newModels.put(agent, updatedModel);
            //}
            //else {
            //    newModels.put(agent, oldModels.get(agent));
            //}
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

        str.append("\tDetermines\n");
        for (FluentFormula ff : determines) {
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
        for (Map.Entry<FluentLiteral, FluentFormula> a : effects.entrySet()) {
            str.append("\t\t");
            str.append(a.getKey());
            str.append(" if ");
            str.append(a.getValue());
            str.append("\n");
        }
 
        return str.toString();
    }

}

