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

    public OnticAction(String name,
                       List<String> parameters,
                       String actor,
                       Integer cost,
                       FluentFormula precondition,
                       Map<String, FluentFormula> observesIf,
                       Map<String, FluentFormula> awareIf,
                       Map<FluentLiteral, FluentFormula> effects,
                       Domain domain
                      ) {
        super(name, parameters, actor, cost, precondition, observesIf, awareIf, domain);
        this.effects = effects;
    }



    public Map<FluentLiteral, FluentFormula> getEffects() {
        return this.effects;
    }



    @Override
    public Action.UpdatedStateAndModels transition(EpistemicState beforeState, Map<String, Model> oldModels) {
        Log.debug("ontic transition: " + getSignatureWithActor());


        if (!precondition.equals(new FluentFormulaTrue())) {

            Map<String, FluentFormula> observesOrAware = new HashMap<>();
            for (String agent : domain.getAllAgents()) {
                observesOrAware.put(agent, new FluentFormulaOr(observesIf.get(agent), awareIf.get(agent)));
            }

            SensingAction computeReset = new SensingAction(name+"[preconditions]",
                                                           parameters,
                                                           actor,
                                                           cost,
                                                           precondition,
                                                           observesOrAware,    // THESE LEARN PERCONDITIONS
                                                           new HashMap<String,FluentFormula>(),
                                                           precondition,        // SENSE THE PRECONDITION
                                                           domain
                                                          );
            Action.UpdatedStateAndModels afterReset = computeReset.transition(beforeState, oldModels);
            beforeState = afterReset.getState();
            oldModels = afterReset.getModels();
        }






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

        // AN EFFECT IS APPLICABLE IN WORLDS THAT SATISFY ITS PRECONDITIONS

        Map<World, Set<FluentLiteral>> applicableEffects = new HashMap<>();
        for (World w : oldWorlds) {
            applicableEffects.put(w, new HashSet<FluentLiteral>());
            for (Map.Entry<FluentLiteral, FluentFormula> e : effects.entrySet()) {
                FluentLiteral effect = e.getKey();
                FluentFormula condition = e.getValue();
                if (condition.holds(w)) {
                    applicableEffects.get(w).add(effect);
                }
            }
        }


        for (World oldWorld : oldWorlds) {
            World obliviousWorld = new World(oldWorld);
            obliviousWorlds.add(obliviousWorld);
            newWorldsToOld.put(obliviousWorld, oldWorld);
            if (this.executable(oldKripke, oldWorld)) {
                World observedWorld = oldWorld.update(applicableEffects.get(oldWorld));
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

        
        Map<String, Relation> newBeliefs = new HashMap<>();
        for (String a : domain.getAllAgents()) {
            newBeliefs.put(a, new Relation());
        }

        Map<String, Relation> newKnowledges = new HashMap<>();
        for (String a : domain.getAllAgents()) {
            newKnowledges.put(a, new Relation());
        }



        Map<World, FluentFormula> revealedConditions = new HashMap<>();
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
                        conditions.add(condition.negate());
                    }
                }
            }
            revealedConditions.put(w, new FluentFormulaAnd(conditions));
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


            for (World fromWorld: observedWorlds) {
                World oldFromWorld = newWorldsToOld.get(fromWorld);

                BeliefFormula believesNotEffectConditions = new BeliefFormulaBelieves(agent, 
                                                                new BeliefFormulaNot(revealedConditions.get(fromWorld)));


                for (World toWorld: observedWorlds) {
                    World oldToWorld = newWorldsToOld.get(toWorld);





                    if (resetRelations.get(agent).isConnected(newWorldsToOld.get(fromWorld),
                                                              newWorldsToOld.get(toWorld))) {
                        if (revealedConditions.get(newWorldsToOld.get(fromWorld)).holds(toWorld)) {
                            newBeliefs.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                    if (oldKripke.isConnectedKnowledge(agent, 
                                                       newWorldsToOld.get(fromWorld),
                                                       newWorldsToOld.get(toWorld))) {
                        if (revealedConditions.get(newWorldsToOld.get(fromWorld)).holds(toWorld)) {
                            newKnowledges.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                }
            }
        }

        // AN OBLIVIOUS AGENT COMES TO KNOW THE POSSIBILITY OF ACTION EFFECTS
        // ACTION PRECONDITIONS AND EFFECT CONDITIONS ARE NOT RELEVANT
        // BECAUSE OBLIVIOUS KNOWLEDGE UPDATE IS TO CAPTURE ACTUAL CHANGES TO THE ENVIRONMENT

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
                        if (revealedConditions.get(newWorldsToOld.get(fromWorld)).holds(toWorld)) {
                            newKnowledges.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                }
            }

        }

        KripkeStructure newKripke = new KripkeStructure(resultWorlds, newBeliefs, newKnowledges);

        EpistemicState newState = new EpistemicState(newKripke, newDesignatedWorld);

        assert(Test.checkRelations(domain, newState));

        Map<String, Model> newModels = new HashMap();

        for (String agent : oldModels.keySet()) {
            if (observantAwareAgents.contains(agent)) {
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

