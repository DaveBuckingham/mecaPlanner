package mecaPlanner.actions;

import mecaPlanner.formulae.*;
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

        World designatedWorld = beforeState.getDesignatedWorld();


        boolean nonTrueCondition = false;
        Set<FluentLiteral> applicableEffects = new HashSet<>();
        Set<FluentFormula> revealedConditions = new HashSet<>();
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
        if (!revealedConditions.isEmpty()) {
            revealedConditions.add(precondition);
            precondition = new FluentFormulaAnd(revealedConditions);
        }



        if (!precondition.alwaysHolds()) {
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

        Log.debug("sensing complete");


        assert(Test.checkRelations(domain, beforeState));
        System.out.println(beforeState);
        System.exit(1);





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
        Map<World, World> newWorldsToOblivious = new HashMap<World, World>();

        // AN EFFECT IS APPLICABLE IN WORLDS THAT SATISFY ITS PRECONDITIONS

        Set<String> observantAwareAgents = getFullyObservant(beforeState);
        observantAwareAgents.addAll(getAware(beforeState));
        Set<String> obliviousAgents = getOblivious(beforeState);


        for (World oldWorld : oldWorlds) {
            World observedWorld = oldWorld.update(applicableEffects);
            if (this.executable(oldWorld)) {
                observedWorlds.add(observedWorld);
                newWorldsToOld.put(observedWorld, oldWorld);
                if (oldWorld.equals(beforeState.getDesignatedWorld())) {
                    newDesignatedWorld = observedWorld;
                }
            }
            if (!obliviousAgents.isEmpty()) {
                World obliviousWorld = new World(oldWorld);
                obliviousWorlds.add(obliviousWorld);
                newWorldsToOld.put(obliviousWorld, oldWorld);
                newWorldsToOblivious.put(observedWorld, obliviousWorld);
            }
        }

            //System.out.println("=========");
            //System.out.println(oldWorlds);
            //System.out.println(obliviousWorlds);
            //System.out.println(observedWorlds);
            //System.out.println("=========");
            //System.exit(1);


        assert(newDesignatedWorld != null);
        assert(observedWorlds.contains(newDesignatedWorld));


 
        Map<String, Relation> newBeliefs = new HashMap<>();
        for (String a : domain.getAllAgents()) {
            newBeliefs.put(a, new Relation());
        }

        Map<String, Relation> newKnowledges = new HashMap<>();
        for (String a : domain.getAllAgents()) {
            newKnowledges.put(a, new Relation());
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

                for (World toWorld: observedWorlds) {
                    World oldToWorld = newWorldsToOld.get(toWorld);

                    if (oldKripke.isConnectedBelief(agent, oldFromWorld, oldToWorld)) {
                        newBeliefs.get(agent).connect(fromWorld, toWorld);
                    }
                    if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)) {
                        newKnowledges.get(agent).connect(fromWorld, toWorld);
                    }

                }
                //if (newBeliefs.get(agent).getToWorlds(fromWorld).isEmpty()) {
                //    System.out.println(fromWorld);
                //    System.out.println(oldFromWorld);
                //    System.out.println(agent);
                //    System.out.println(beforeState);
                //    System.out.println(observedWorlds);
                //    System.exit(1);
                //}
            }
        }

        for (String agent : obliviousAgents) {
            for (World fromWorld: observedWorlds) {
                World oldFromWorld = newWorldsToOld.get(fromWorld);

                for (World toWorld: observedWorlds) {
                    World oldToWorld = newWorldsToOld.get(toWorld);

                    if (oldKripke.isConnectedBelief(agent, oldFromWorld, oldToWorld)) {
                        newBeliefs.get(agent).connect(fromWorld, newWorldsToOblivious.get(toWorld));
                    }
                    if (oldKripke.isConnectedKnowledge(agent, oldFromWorld, oldToWorld)) {
                        newKnowledges.get(agent).connect(fromWorld, toWorld);
                        newKnowledges.get(agent).connect(fromWorld, newWorldsToOblivious.get(toWorld));
                        newKnowledges.get(agent).connect(newWorldsToOblivious.get(toWorld), fromWorld);
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

