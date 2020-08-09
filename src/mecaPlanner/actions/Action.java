package mecaPlanner.actions;

import mecaPlanner.formulae.BeliefFormula;
import mecaPlanner.formulae.FluentFormula;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.World;
import mecaPlanner.state.NDState;
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
            //if (!executable(state.getKripke(), w)) {
            if (!executable(w)) {
                return false;
            }
        }
        return true;
    }


    public Boolean isFullyObservant(String agent, EpistemicState state) {
        return (observesIf.containsKey(agent) && observesIf.get(agent).holds(state));
    }

    public Boolean isAware(String agent, EpistemicState state) {
        return (awareIf.containsKey(agent) && awareIf.get(agent).holds(state));
    }

    public Boolean isOblivious(String agent, EpistemicState state) {
        return ((!isFullyObservant(agent, state)) && (!isAware(agent, state)));
    }

    public Set<String>getAnyObservers(EpistemicState state) {
        Set<String> selected = new HashSet<>();
        for (String agent : domain.getAllAgents()) {
            if (isFullyObservant(agent, state) || isAware(agent, state)) {
                selected.add(agent);
            }
        }
        return selected;
    }

    public Set<String>getFullyObservant(EpistemicState state) {
        Set<String> selected = new HashSet<>();
        for (String agent : domain.getAllAgents()) {
            if (isFullyObservant(agent, state)) {
                selected.add(agent);
            }
        }
        return selected;
    }

    public Set<String>getAware(EpistemicState state) {
        Set<String> selected = new HashSet<>();
        for (String agent : domain.getAllAgents()) {
            if (isAware(agent, state)) {
                selected.add(agent);
            }
        }
        return selected;
    }

    public Set<String>getOblivious(EpistemicState state) {
        Set<String> selected = new HashSet<>();
        for (String agent : domain.getAllAgents()) {
            if (isOblivious(agent, state)) {
                selected.add(agent);
            }
        }
        return selected;
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

    public Action.UpdatedStateAndModels transition(EpistemicState beforeState, Map<String, Model> oldModels) {
        Log.debug("transition: " + getSignatureWithActor());

        KripkeStructure oldKripke = beforeState.getKripke();
        Set<World> oldWorlds = oldKripke.getWorlds();
        Set<World> obliviousWorlds = new HashSet<>();
        Set<World> observedWorlds = new HashSet<>();
        World newDesignatedWorld = null;
        Map<World, World> newWorldsToOld = new HashMap<World, World>();
        Map<World, World> oldWorldsToOblivious = new HashMap<World, World>();


        Set<FluentLiteral> applicableEffects = new HashSet<>();
        Set<FluentFormula> revealedConditions = new HashSet<>();
        revealedConditions.add(precondition);
        for (Map.Entry<FluentLiteral, FluentFormula> e : effects.entrySet()) {
            FluentLiteral effect = e.getKey();
            FluentFormula condition = e.getValue();
            boolean conditionHolds = condition.holds(designatedWorld);
            if (conditionHolds) {
                applicableEffects.add(effect);
            }
            if (!effect.holds(designatedWorld) && !condition.alwaysHolds()) {
                if (conditionHolds) {
                    revealedConditions.add(condition);
                }
                else {
                    revealedConditions.add(condition.negate());
                }
            }
        }
        //FluentFormula joinedConditions = FluentFormula.buildConjunction(revealedConditions);
        FluentFormula joinedConditions = new FluentFormulaAnd(revealedConditions);

        Map<String, Relation> newBeliefs = new HashMap<>();
        for (String a : domain.getAllAgents()) {
            newBeliefs.put(a, new Relation());
        }

        Map<String, Relation> newKnowledges = new HashMap<>();
        for (String a : domain.getAllAgents()) {
            newKnowledges.put(a, new Relation());
        }



        
        for (World oldWorld : oldWorlds) {
            if (joinedConditions.holds(oldWorld)) {
                World observedWorld = oldWorld.update(applicableEffects);
                observedWorlds.add(observedWorld);
                newWorldsToOld.put(observedWorld, oldWorld);
                if (oldWorld.equals(beforeState.getDesignatedWorld())) {
                    newDesignatedWorld = observedWorld;
                }
            }
        }

        assert(newDesignatedWorld != null);
        assert(observedWorlds.contains(newDesignatedWorld));

        Set<World> resultWorlds = new HashSet<World>(observedWorlds);

        if (!obliviousAgents.isEmpty()) {
            for (World oldWorld : oldWorlds) {
                World obliviousWorld = new World(oldWorld);
                obliviousWorlds.add(obliviousWorld);
                oldWorldsToOblivious.put(oldWorld, obliviousWorld);
            }
            for (String agent : domain.getAllAgents()) {
                for (World oldFromWorld: oldWorlds) {
                    World fromWorld = oldWorldsToOblivioius.get(oldFromWorld);
                    for (World oldToWorld: oldWorlds) {
                        World toWorld = oldWorldsToOblivious.get(oldToWorld);
                        if (oldKripke.isConnectedBelief(agent, oldFromWorld, oldToWorld) {
                            newBeliefs.get(agent).connect(fromWorld, toWorld);
                        }
                        if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld) {
                            newKnowledges.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                }
            }
            resultWorlds.addAll(obliviousWorlds);
        }
 





        Set<String> observantAgents = getFullyObservant(beforeState);
        Set<String> awareAgents = getAware(beforeState);
        Set<String> obliviousAgents = getOblivious(beforeState);

        Map<String, BeliefFormula> agentLearns = new HashSet<>();
        for (String agent : observantAgents) {
            FluentFormula joinedDetermines = new FluentFormulaAnd(determines);
            BeliefFormula joinedHears = new FluentFormulaAnd(announces);
            BeliefFormula knowsNotAnnounces = new BeliefFormulaKnows(agent, new BeliefFormulaNot(joinedHears));
            boolean reject = knowsNotAnnounces.holds(beforeState);
            if (reject) {
                agentLearns.put(agent, new BliefFormulaAnd(joinedDetermines, joinedConditions));
            }
            else {
                agentLearns.put(agent, new BliefFormulaAnd(joinedDetermines, joinedHears, joinedConditions));
            }
        }
        for (String agent : awareAgents) {
            agentLearns.put(agent, joinedConditions);
        }

        Set<String> observantAndAware = new HashSet<>();
        observantAndAware.addAll(observantAgents);
        observantAndAware.addAll(awareAgents);


RESET


        for (String agent : observantAndAware) {

            for (World fromWorld: observedWorlds) {
                World oldFromWorld = newWorldsToOld.get(fromWorld);

                for (World toWorld: observedWorlds) {
                    World oldToWorld = newWorldsToOld.get(toWorld);

                    boolean worldsNotDistinguished = determines.holds(fromWorld) == determines.holds(toWorld);

                    if ((oldKripke.isConnectedBelief(agent, oldFromWorld, oldToWorld)) ||
                        (reset && oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld))
                       ) {
                        if (worldsNotDistinguished) {
                            newBeliefs.get(agent).connect(fromWorld, toWorld);
                        }
                    }



                    if (oldKripke.isConnectedKnowledge(agent,
                                                       newWorldsToOld.get(fromWorld),
                                                       newWorldsToOld.get(toWorld))) {
                        if (worldsNotDistinguished) {
                            newKnowledges.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                }
            }
        }


        for (String agent : awareAgents) {

            for (World fromWorld: observedWorlds) {
                for (World toWorld: observedWorlds) {
                    if (oldKripke.isConnectedBelief(agent,
                                                    newWorldsToOld.get(fromWorld),
                                                    newWorldsToOld.get(toWorld))) {
                        newBeliefs.get(agent).connect(fromWorld, toWorld);
                    }
                    if (oldKripke.isConnectedKnowledge(agent,
                                                       newWorldsToOld.get(fromWorld),
                                                       newWorldsToOld.get(toWorld))) {
                        newKnowledges.get(agent).connect(fromWorld, toWorld);
                    }
                }
            }
        }


        for (String agent : obliviousAgents) {

            for (World fromWorld: observedWorlds) {
                for (World toWorld: obliviousWorlds) {
                    if (oldKripke.isConnectedBelief(agent,
                                                    newWorldsToOld.get(fromWorld),
                                                    newWorldsToOld.get(toWorld))) {
                        newBeliefs.get(agent).connect(fromWorld, toWorld);
                    }
                    if (oldKripke.isConnectedKnowledge(agent,
                                                       newWorldsToOld.get(fromWorld),
                                                       newWorldsToOld.get(toWorld))) {
                        newKnowledges.get(agent).connect(fromWorld, toWorld);
                        newKnowledges.get(agent).connect(toWorld, fromWorld);
                    }
                }
            }

            for (World fromWorld: observedWorlds) {
                for (World toWorld: observedWorlds) {
                    if (oldKripke.isConnectedKnowledge(agent,
                                                       newWorldsToOld.get(fromWorld),
                                                       newWorldsToOld.get(toWorld))) {
                        newKnowledges.get(agent).connect(fromWorld, toWorld);
                    }
                }
            }
        }


        KripkeStructure newKripke = new KripkeStructure(resultWorlds, newBeliefs, newKnowledges);

        EpistemicState newState = new EpistemicState(newKripke, newDesignatedWorld);

        assert(Test.checkRelations(domain, newState));


        Map<String, Model> newModels = new HashMap();

        for (String agent : oldModels.keySet()) {
            if (observantAgents.contains(agent)) {
                NDState perspective = beforeState.getBeliefPerspective(agent);
                // THIS IS WRONG, WE NEED A WAY TO PUT THE SENSED INFORMATION 
                // INSTEAD OF THE DETERMINES FORMULA...
                SensingAction informed = new SensingAction(this.name,
                                                           this.parameters,
                                                           this.actor,
                                                           this.cost,
                                                           this.precondition,
                                                           this.observesIf,
                                                           this.awareIf,
                                                           this.determines,
                                                           domain
                                                          );
                Model updatedModel = oldModels.get(agent).update(perspective, informed);
                newModels.put(agent, updatedModel);
            }
            else if (awareAgents.contains(agent)) {
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

