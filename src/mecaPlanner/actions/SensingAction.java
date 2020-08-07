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

//import java.util.logging.Logger;


public class SensingAction extends Action {

    //private static final Logger LOGGER = Logger.getLogger( ClassName.class.getName() );

    private FluentFormula determines;

    public SensingAction(String name,
                  List<String> parameters,
                  String actor,
                  Integer cost,
                  FluentFormula precondition,
                  Map<String, FluentFormula> observesIf,
                  Map<String, FluentFormula> awareIf,
                  FluentFormula determines,
                  Domain domain
                 ) {
        super(name, parameters, actor, cost, precondition, observesIf, awareIf, domain);
        this.determines = determines;
    }

    public Action.UpdatedStateAndModels transition(EpistemicState beforeState, Map<String, Model> oldModels) {
        Log.debug("sensing transition: " + getSignatureWithActor());

        Map<String, FluentFormula> observesAndAware = new HashMap<>();
        observesAndAware.putAll(observesIf);
        observesAndAware.putAll(awareIf);

        if (precondition != new FluentFormulaTrue()) {
            SensingAction computeReset = new SensingAction(name,
                                                           parameters,
                                                           actor,
                                                           cost,
                                                           new FluentFormulaTrue(), // NO NEW PRECONDITION
                                                           observesAndAware,    // THESE LEARN PERCONDITIONS
                                                           new HashMap<String,FluentFormula>(),
                                                           precondition,        // SENSE THE OLD PRECONDITION
                                                           domain
                                                          );
            Action.UpdatedStateAndModels afterReset = computeReset.transition(beforeState, oldModels);
            beforeState = afterReset.getState();
            oldModels = afterReset.getModels();
        }

        //LOGGER.log(Level.FINE, "transitioning ontic action");

        assert(this.executable(beforeState));
        //if (!this.executable(before)) {
        //    throw new RuntimeException("action not exeutable");
        //}
        //if (!this.executable(before)) {
        //    Log.warning("trying to execute action with unsatisfied conditions");
        //    return(new EpistemicState(before));
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
                World observedWorld = new World(oldWorld);
                observedWorlds.add(observedWorld);
                newWorldsToOld.put(observedWorld, oldWorld);
                if (oldWorld.equals(beforeState.getDesignatedWorld())) {
                    newDesignatedWorld = observedWorld;
                }
            }
        }

        assert(newDesignatedWorld != null);
        assert(observedWorlds.contains(newDesignatedWorld));

        Set<String> observantAgents = getFullyObservant(beforeState);
        Set<String> awareAgents = getAware(beforeState);
        Set<String> obliviousAgents = getOblivious(beforeState);


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
        }



        for (String agent : observantAgents) {

            BeliefFormula believesNotSensed = new BeliefFormulaBelieves(agent, new BeliefFormulaNot(determines));

            for (World fromWorld: oldWorlds) {

                boolean reset = believesNotSensed.holdsAtWorld(oldKripke,fromWorld);

                for (World toWorld: observedWorlds) {

                    boolean worldsNotDistinguished = determines.holds(fromWorld) == determines.holds(toWorld);

                    World oldFromWorld = newWorldsToOld.get(fromWorld);
                    World oldToWorld = newWorldsToOld.get(toWorld);
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



    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Sensing ");
        str.append(super.toString());
        str.append("\tDetermines: ");
        str.append(determines);
        str.append("\n");
        return str.toString();
    }

}

