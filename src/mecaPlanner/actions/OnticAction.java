package mecaPlanner.actions;

import mecaPlanner.formulae.FluentFormula;
import mecaPlanner.formulae.BeliefFormula;
import mecaPlanner.formulae.FluentLiteral;
import mecaPlanner.formulae.FluentFormulaNot;
import mecaPlanner.formulae.FluentFormulaAnd;
import mecaPlanner.formulae.BeliefFormulaNot;
import mecaPlanner.formulae.BeliefFormulaBelieves;
import mecaPlanner.agents.Agent;
import mecaPlanner.agents.EnvironmentAgent;
import mecaPlanner.models.Model;
import mecaPlanner.state.World;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.state.NDState;
import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.Relation;
import mecaPlanner.Domain;
import mecaPlanner.Test;
import mecaPlanner.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;


public class OnticAction extends Action implements java.io.Serializable{


    private Map<FluentLiteral, FluentFormula> effects;
    //private Set<FluentLiteral> effects;

    public OnticAction(String name,
                       List<String> parameters,
                       Agent actor,
                       Integer cost,
                       BeliefFormula precondition,
                       Map<Agent, FluentFormula> observesIf,
                       Map<Agent, FluentFormula> awareIf,
                       Map<FluentLiteral, FluentFormula> effects
                       //Set<FluentLiteral> effects
                      ) {
        super(name, parameters, actor, cost, precondition, observesIf, awareIf);
        this.effects = effects;
    }



    public Map<FluentLiteral, FluentFormula> getEffects() {
        return this.effects;
    }

    public Set<FluentLiteral> getApplicableEffects(World world) {
        Set<FluentLiteral> applicableEffects = new HashSet<FluentLiteral>();
        for (Map.Entry<FluentLiteral, FluentFormula> entry : effects.entrySet()) {
            if (entry.getValue().holds(world)) {       // SHOULD ALWAYS BE TRUE IF NO CONDITION, I.E. CONDITION = TRUE
                applicableEffects.add(entry.getKey());
            }
        }
        return applicableEffects;
    }




    @Override
    public Action.UpdatedStateAndModels transition(EpistemicState beforeState, Map<EnvironmentAgent, Model> oldModels) {
        Log.debug("ontic transition: " + getSignatureWithActor());


        assert(this.executable(beforeState));
        //if (!this.executable(before)) {
        //    throw new RuntimeException("action not exeutable");
        //}
        //if (!this.executable(beforeState)) {
        //    Log.warning("trying to execute action with unsatisfied conditions");
        //    return(new EpistemicState(beforeState));
        //}

        KripkeStructure oldKripke = beforeState.getKripke();
        Set<World> oldWorlds = oldKripke.getWorlds();
        Set<World> obliviousWorlds = new HashSet<>();
        Set<World> observedWorlds = new HashSet<>();
        World newDesignatedWorld = null;
        Map<World, World> newWorldsToOld = new HashMap<World, World>();

        for (World oldWorld : oldWorlds) {
            World obliviousWorld = new World(oldWorld);
            obliviousWorlds.add(obliviousWorld);
            newWorldsToOld.put(obliviousWorld, oldWorld);
            if (this.executable(oldKripke, oldWorld)) {
                World observedWorld = oldWorld.update(this.getApplicableEffects(oldWorld));
                observedWorlds.add(observedWorld);
                newWorldsToOld.put(observedWorld, oldWorld);
                if (oldWorld.equals(beforeState.getDesignatedWorld())) {
                    newDesignatedWorld = observedWorld;
                }
            }
        }


        assert(newDesignatedWorld != null);
        assert(observedWorlds.contains(newDesignatedWorld));

        Set<Agent> observantAwareAgents = getFullyObservant(beforeState);
        observantAwareAgents.addAll(getAware(beforeState));
        Set<Agent> obliviousAgents = getOblivious(beforeState);

        Map<Agent, Relation> resetRelations = new HashMap<>();
        for (Agent a : getAnyObservers(beforeState)) {
            resetRelations.put(a, new Relation());
        }

        
        Map<Agent, Relation> newBeliefs = new HashMap<>();
        for (Agent a : Domain.getAllAgents()) {
            newBeliefs.put(a, new Relation());
        }

        Map<Agent, Relation> newKnowledges = new HashMap<>();
        for (Agent a : Domain.getAllAgents()) {
            newKnowledges.put(a, new Relation());
        }



        Map<World, FluentFormula> conditionsFormulae = new HashMap<>();
        for (World w : oldWorlds) {
            List<FluentFormula> conditions = new ArrayList<>();
            for (Map.Entry<FluentLiteral, FluentFormula> e : effects.entrySet()) {
                FluentLiteral effect = e.getKey();
                FluentFormula condition = e.getValue();
                if (!effect.holds(w)) {
                    if (condition.holds(w)) {
                        conditions.add(condition);
                    }
                    else {
                        conditions.add(new FluentFormulaNot(condition));
                    }
                }
            }
            conditionsFormulae.put(w, new FluentFormulaAnd(conditions));
        }



        Set<World> resultWorlds = new HashSet<World>(observedWorlds);


        if (!obliviousAgents.isEmpty()) {
            resultWorlds.addAll(obliviousWorlds);

            for (Agent agent : Domain.getAllAgents()) {
                for (World fromWorld: obliviousWorlds) {
                    for (World toWorld: obliviousWorlds) {
                        if (oldKripke.isConnectedBelief(agent, newWorldsToOld.get(fromWorld), newWorldsToOld.get(toWorld))) {
                            newBeliefs.get(agent).connect(fromWorld, toWorld);
                        }
                        if (oldKripke.isConnectedKnowledge(agent, newWorldsToOld.get(fromWorld), newWorldsToOld.get(toWorld))) {
                            newKnowledges.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                }
            }
        }


        for (Agent agent : observantAwareAgents) {

            BeliefFormula believesNotPrecondition = new BeliefFormulaBelieves(agent, 
                                                        new BeliefFormulaNot(getPrecondition()));
            for (World fromWorld: oldWorlds) {

                BeliefFormula believesNotEffectConditions = new BeliefFormulaBelieves(agent, 
                                                                new BeliefFormulaNot(conditionsFormulae.get(fromWorld)));
                if(believesNotPrecondition.holdsAtWorld(oldKripke, fromWorld) ||
                   believesNotEffectConditions.holdsAtWorld(oldKripke,fromWorld)) {
                    for (World toWorld: oldWorlds) {
                        if (oldKripke.isConnectedKnowledge(agent, fromWorld, toWorld)) {
                            resetRelations.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                }
                else {
                    for (World toWorld: oldWorlds) {
                        if (oldKripke.isConnectedBelief(agent, fromWorld, toWorld)) {
                            resetRelations.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                }
            }

            for (World fromWorld: observedWorlds) {
                for (World toWorld: observedWorlds) {
                    // IFF THE TOWORLD DOESN'T CONTRADICT FACTS REVEALED BY
                    // THE ACTION'S EXECUTION IN THE OLD FROMWORLD BY VIRTUE OF THE ACTION'S PRECONDITIONS,
                    // THE AGENT WILL BELIEVE THE TOWOLRD TO BE POSSIBLE IN THE NEW FROMWORLD.
                    if (resetRelations.get(agent).isConnected(newWorldsToOld.get(fromWorld),
                                                              newWorldsToOld.get(toWorld))) {
                        if (conditionsFormulae.get(newWorldsToOld.get(fromWorld)).holds(toWorld)) {
                            newBeliefs.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                    if (oldKripke.isConnectedKnowledge(agent, 
                                                       newWorldsToOld.get(fromWorld),
                                                       newWorldsToOld.get(toWorld))) {
                        if (conditionsFormulae.get(newWorldsToOld.get(fromWorld)).holds(toWorld)) {
                            newKnowledges.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                }
            }
        }


        for (Agent agent : obliviousAgents) {

            for (World fromWorld: observedWorlds) {
                for (World toWorld: obliviousWorlds) {
                    if (oldKripke.isConnectedBelief(agent, newWorldsToOld.get(fromWorld), newWorldsToOld.get(toWorld))) {
                        newBeliefs.get(agent).connect(fromWorld, toWorld);
                    }
                    if (oldKripke.isConnectedKnowledge(agent, newWorldsToOld.get(fromWorld), newWorldsToOld.get(toWorld))) {
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
                        if (conditionsFormulae.get(newWorldsToOld.get(fromWorld)).holds(toWorld)) {
                            newKnowledges.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                }
            }

        }

        KripkeStructure newKripke = new KripkeStructure(resultWorlds, newBeliefs, newKnowledges);

        EpistemicState newState = new EpistemicState(newKripke, newDesignatedWorld);

        assert(Test.checkRelations(newState));

        Map<EnvironmentAgent, Model> newModels = new HashMap();

        for (Agent obliviousAgent : obliviousAgents) {
            if (obliviousAgent instanceof EnvironmentAgent) {
                EnvironmentAgent eagent = (EnvironmentAgent) obliviousAgent;
                newModels.put(eagent, oldModels.get(eagent));
            }
        }
        for (Agent observantAwareAgent : observantAwareAgents) {
            if (observantAwareAgent instanceof EnvironmentAgent) {
                EnvironmentAgent eagent = (EnvironmentAgent) observantAwareAgent;
                NDState perspective = beforeState.getBeliefPerspective(eagent);
                Model updatedModel = oldModels.get(eagent).update(perspective, this);
                newModels.put(eagent, updatedModel);
            }

        }

        return new Action.UpdatedStateAndModels(newState, newModels);

    }




    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Ontic ");
        str.append(super.toString());
        str.append("\tEffects\n");
        for (Map.Entry<FluentLiteral, FluentFormula> e : effects.entrySet()) {
        //for (FluentLiteral e : effects) {
            str.append("\t\t");
            //str.append(e);
            str.append(e.getKey());
            str.append(" if ");
            str.append(e.getValue());
            str.append("\n");
        }
        return str.toString();
    }



}

