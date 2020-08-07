package mecaPlanner.actions;

import mecaPlanner.formulae.FluentFormula;
import mecaPlanner.formulae.BeliefFormula;
import mecaPlanner.formulae.FluentLiteral;
import mecaPlanner.formulae.BeliefFormulaBelieves;
import mecaPlanner.formulae.BeliefFormulaNot;
import mecaPlanner.formulae.BeliefFormulaAnd;
import mecaPlanner.formulae.BeliefFormulaKnows;
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



public class AnnouncementAction extends Action {

    private FluentFormula announces;

    public AnnouncementAction(String name,
                              List<String> parameters,
                              String actor,
                              int cost,
                              FluentFormula precondition,
                              Map<String, FluentFormula> observesIf,
                              Map<String, FluentFormula> awareIf,
                              FluentFormula announces,
                              Domain domain
                             ) {
        super(name, parameters, actor, cost, precondition, observesIf, awareIf, domain);
        this.announces = announces;
    }

    public FluentFormula getAnnounces() {
        return this.announces;
    }




    @Override
    public Action.UpdatedStateAndModels transition(EpistemicState beforeState, Map<String, Model> oldModels) {
        Log.debug("announcement transition: " + getSignatureWithActor());


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

            BeliefFormula believesNotPrecondition = new BeliefFormulaBelieves(agent,
                                                        new BeliefFormulaNot(getPrecondition()));

            BeliefFormula believesNotPreconditionAndAnnouncement = new BeliefFormulaBelieves(agent,
                                                                       new BeliefFormulaNot(
                                                                           new BeliefFormulaAnd(
                                                                               getPrecondition(),
                                                                               getAnnounces())));

            BeliefFormula knowsNotPreconditionAndAnnouncement = new BeliefFormulaKnows(agent,
                                                                    new BeliefFormulaNot(
                                                                        new BeliefFormulaAnd(
                                                                            getPrecondition(),
                                                                            getAnnounces())));


            for (World fromWorld: oldWorlds) {

                // (M,u) |= B_i ~a.pre
                boolean resetForPre = believesNotPrecondition.holdsAtWorld(oldKripke,fromWorld);

                // (M,u) |= K_i ~(a.pre ^ a.ann)
                boolean reject = knowsNotPreconditionAndAnnouncement.holdsAtWorld(oldKripke,fromWorld);

                // (M,u) |= B_i ~(a.pre ^ a.ann)
                boolean resetForAnn = believesNotPreconditionAndAnnouncement.holdsAtWorld(oldKripke,fromWorld);

                if (resetForPre || ((!reject) && resetForAnn)) {
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

                boolean reject = knowsNotPreconditionAndAnnouncement.holdsAtWorld(oldKripke, newWorldsToOld.get(fromWorld)); 

                for (World toWorld: observedWorlds) {

                    if (resetRelations.get(agent).isConnected(newWorldsToOld.get(fromWorld),
                                                              newWorldsToOld.get(toWorld))) { 
                        if (reject || getAnnounces().holdsAtWorld(oldKripke, newWorldsToOld.get(toWorld))) {
                            newBeliefs.get(agent).connect(fromWorld, toWorld);
                        }
                    }
                    if (oldKripke.isConnectedKnowledge(agent,
                                                       newWorldsToOld.get(fromWorld),
                                                       newWorldsToOld.get(toWorld))) {
                        newKnowledges.get(agent).connect(fromWorld, toWorld);
                    }
                }
            }
        }


        for (String agent : getAware(beforeState)) {
            BeliefFormula believesNotPrecondition = new BeliefFormulaBelieves(agent, new BeliefFormulaNot(getPrecondition()));
            for (World fromWorld: oldWorlds) {

                // [paper] (M,u) |= B_i ~a.pre
                boolean resetForPre = believesNotPrecondition.holdsAtWorld(oldKripke,fromWorld);

                if(resetForPre) {
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
                    if (resetRelations.get(agent).isConnected(newWorldsToOld.get(fromWorld),
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
                    if (oldKripke.isConnectedBelief(agent, newWorldsToOld.get(fromWorld), newWorldsToOld.get(toWorld))) {
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
                Model updatedModel = oldModels.get(agent).update(perspective, this);
                newModels.put(agent, updatedModel);
            }
            else if (awareAgents.contains(agent)) {
                NDState perspective = beforeState.getBeliefPerspective(agent);
                AnnouncementAction redacted = new AnnouncementAction(this.name,
                                                                     this.parameters,
                                                                     this.actor,
                                                                     this.cost,
                                                                     this.precondition,
                                                                     this.observesIf,
                                                                     this.awareIf,
                                                                     null,
                                                                     domain
                                                                    );
                Model updatedModel = oldModels.get(agent).update(perspective, redacted);
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
        str.append("Announcement ");
        str.append(super.toString());
        str.append("\tAnnounces\n");
        str.append("\t\t");
        str.append(announces);
        str.append("\n");
        return str.toString();
    }


}
