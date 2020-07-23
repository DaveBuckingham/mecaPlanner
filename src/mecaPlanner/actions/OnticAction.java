package mecaPlanner.actions;

import mecaPlanner.formulae.FluentFormula;
import mecaPlanner.formulae.BeliefFormula;
import mecaPlanner.formulae.FluentLiteral;
import mecaPlanner.formulae.FluentFormulaNot;
import mecaPlanner.formulae.FluentFormulaAnd;
import mecaPlanner.formulae.BeliefFormulaNot;
import mecaPlanner.formulae.BeliefFormulaBelieves;
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
                       String actor,
                       Integer cost,
                       BeliefFormula precondition,
                       Map<String, FluentFormula> observesIf,
                       Map<String, FluentFormula> awareIf,
                       Map<FluentLiteral, FluentFormula> effects,
                       //Set<FluentLiteral> effects
                       Domain domain
                      ) {
        super(name, parameters, actor, cost, precondition, observesIf, awareIf, domain);
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
    public Action.UpdatedStateAndModels transition(EpistemicState beforeState, Map<String, Model> oldModels) {
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

        Set<String> observantAwareAgents = getFullyObservant(beforeState);
        observantAwareAgents.addAll(getAware(beforeState));
        Set<String> obliviousAgents = getOblivious(beforeState);

        Map<String, Relation> resetRelations = new HashMap<>();
        for (String a : getAnyObservers(beforeState)) {
            resetRelations.put(a, new Relation());
        }

        
        Map<String, Relation> newBeliefs = new HashMap<>();
        for (String a : domain.getAllAgents()) {
            newBeliefs.put(a, new Relation());
        }

        Map<String, Relation> newKnowledges = new HashMap<>();
        for (String a : domain.getAllAgents()) {
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

            for (String agent : domain.getAllAgents()) {
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


        for (String agent : observantAwareAgents) {

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


        for (String agent : obliviousAgents) {

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

        Map<String, Model> newModels = new HashMap();

        for (String obliviousAgent : obliviousAgents) {
            if (domain.isEnvironmentAgent(obliviousAgent)) {
                newModels.put(obliviousAgent, oldModels.get(obliviousAgent));
            }
        }
        for (String observantAwareAgent : observantAwareAgents) {
            if (domain.isEnvironmentAgent(observantAwareAgent)) {
                NDState perspective = beforeState.getBeliefPerspective(observantAwareAgent);
                Model updatedModel = oldModels.get(observantAwareAgent).update(perspective, this);
                newModels.put(observantAwareAgent, updatedModel);
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

