package mecaPlanner;

import mecaPlanner.formulae.beliefFormulae.*;
import mecaPlanner.formulae.localFormulae.*;
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



public class Transition {

    private static KripkeStructure intermediateTransition(KripkeStructure inModel, Action action) {
        Set<World> worlds = inModel.getWorlds();
        Set<String> agents  = inModel.getAgents();
        

        // SCRIPT-K-ALPHA-U-DET
        Map<World, LocalFormula>              learnedKnowledgeDetermined = new HashMap<>();

        // PHI-U-ALPHA-F
        Map<World, Map<Fluent, LocalFormula>> possibleCauses             = new HashMap<>();

        // SCRIPT-K-ALPHA-U-EFF
        Map<World, LocalFormula>              learnedKnowledgeEffects    = new HashMap<>();

        // PHI-I-ALPHA-F
        Map<String, LocalFormula>             observesConditions         = new HashMap<>();

        // PHI-I-ALPHA-P
        Map<String, LocalFormula>             awareConditions            = new HashMap<>();

        // SCRIPT-K-ALPHA-I-U-OBS
        Map<String, Map<World, LocalFormula>> learnedKnowledgeObserver   = new HashMap<>();

        // SCRIPT-K-ALPHA-I-U
        Map<String, Map<World, LocalFormula>> learnedKnowledge           = new HashMap<>();



        // SCRIPT-K-ALPHA-U-DET
        {
        for (World w : worlds) {
            Set<LocalFormula> conjunction = new HashSet<>();
            for (LocalFormula determined : action.getDetermines()) {
                if (determined.evaluate(w)) {
                    conjunction.add(determined);
                }
                else {
                    conjunction.add(determined.negate());
                }
            }
            learnedKnowledgeDetermined.put(w, LocalAndFormula.make(conjunction));
        }
        }


        // PHI-U-ALPHA-F
        {
        for (World w : worlds) {
            Map<Fluent, Set<LocalFormula>> disjunction = new HashMap<>();
            for (Map.Entry<Assignment, LocalFormula> entry : action.getEffects().entrySet()) {
                Fluent       fluent    = entry.getKey().getFluent();
                Boolean      value     = entry.getKey().getValue();
                LocalFormula condition = entry.getValue();
                if (fluent.evaluate(w) != value) {
                    if (!disjunction.containsKey(fluent)) {
                        disjunction.put(fluent, new HashSet<LocalFormula>());
                    }
                    disjunction.get(fluent).add(condition);
                }
            }
            Map<Fluent, LocalFormula> fluentsToConditions = new HashMap<>();
            for (Map.Entry<Fluent, Set<LocalFormula>> entry : disjunction.entrySet()) {
                fluentsToConditions.put(entry.getKey(), LocalOrFormula.make(entry.getValue()));
            }
            possibleCauses.put(w, fluentsToConditions);
        }


        // SCRIPT-K-ALPHA-U-EFF
        for (World w : worlds) {
            Set<LocalFormula> conjunction = new HashSet<>();
            Map<Fluent, LocalFormula> fluentsToConditions = possibleCauses.get(w);
            for (Map.Entry<Fluent, LocalFormula> entry : fluentsToConditions.entrySet()) {
                Fluent       fluent    = entry.getKey();
                LocalFormula condition = entry.getValue();
                if (condition.evaluate(w)) {
                    conjunction.add(condition);
                }
                else {
                    conjunction.add(condition.negate());
                }
            }
            learnedKnowledgeEffects.put(w, LocalAndFormula.make(conjunction));
        }
        }


        // PHI-I-ALPHA-F
        {
        Map<String, Set<LocalFormula>> disjunctionF = new HashMap<>();
        for (Map.Entry<String, LocalFormula> entry : action.getObserves().entrySet()) {
            String       agent     = entry.getKey();
            LocalFormula condition = entry.getValue();
            if (!disjunctionF.containsKey(agent)) {
                disjunctionF.put(agent, new HashSet<LocalFormula>());
            }
            else {
                disjunctionF.get(agent).add(condition);
            }
        }
        for (Map.Entry<String, Set<LocalFormula>> entry : disjunctionF.entrySet()) {
            observesConditions.put(entry.getKey(), LocalOrFormula.make(entry.getValue()));
        }


        // PHI-I-ALPHA-P
        Map<String, Set<LocalFormula>> disjunctionP = new HashMap<>();
        for (Map.Entry<String, LocalFormula> entry : action.getAware().entrySet()) {
            String       agent     = entry.getKey();
            LocalFormula condition = entry.getValue();
            if (!disjunctionP.containsKey(agent)) {
                disjunctionP.put(agent, new HashSet<LocalFormula>());
            }
            else {
                disjunctionP.get(agent).add(condition);
            }
        }
        for (Map.Entry<String, Set<LocalFormula>> entry : disjunctionP.entrySet()) {
            awareConditions.put(entry.getKey(), LocalOrFormula.make(entry.getValue()));
        }


        // SCRIPT-K-ALPHA-I-U-OBS
        for (String agent : agents) {
            assert(observesConditions.containsKey(agent));
            assert(awareConditions.containsKey(agent));
            Map<World, LocalFormula> worldsToFormulae = new HashMap<>();
            for (World w : worlds) {
                if (action.isObservant(agent, w)) {
                    worldsToFormulae.put(w, LocalAndFormula.make(observesConditions.get(agent),
                                                                 awareConditions.get(agent).negate()));
                }
                else if (action.isAware(agent, w)) {
                    worldsToFormulae.put(w, LocalAndFormula.make(observesConditions.get(agent).negate(),
                                                                 awareConditions.get(agent)));
                }
                else {
                    worldsToFormulae.put(w, LocalAndFormula.make(observesConditions.get(agent).negate(),
                                                                 awareConditions.get(agent).negate()));
                }
            }
            learnedKnowledgeObserver.put(agent, worldsToFormulae);
        }
        }


        // SCRIPT-K-ALPHA-I-U
        for (String agent : agents) {
            for (World w : worlds) {
                LocalFormula learnedKnowledgeFormula;
                if (action.isObservant(agent, w)) {
                    learnedKnowledgeFormula = LocalAndFormula.make(learnedKnowledgeDetermined.get(w),
                                                                   learnedKnowledgeEffects.get(w),
                                                                   learnedKnowledgeObserver.get(agent).get(w));
                }
                else if (action.isAware(agent, w)) {
                    learnedKnowledgeFormula = LocalAndFormula.make(learnedKnowledgeEffects.get(w),
                                                                   learnedKnowledgeObserver.get(agent).get(w));
                }
                else {
                    learnedKnowledgeFormula = learnedKnowledgeObserver.get(agent).get(w);
                }
                learnedKnowledge.get(agent).put(w, learnedKnowledgeFormula);
            }
        }


        // TEST THEOREM 1
        for (String agent : agents) {
            for (World w : worlds) {
                assert (learnedKnowledge.get(agent).get(w).evaluate(w));
            }
        }


        // C_iu
        Map<String, Map<World, Set<World>>> equivalenceClasses = new HashMap<>();
        for (String agent : agents) {
            Map<World, Set<World>> perAgent = new HashMap<>();
            for (World u : worlds) {
                Set<World> toWorlds = new HashSet<>();
                for (World v : inModel.getKnownWorlds(agent, u)) {
                    if (learnedKnowledge.get(agent).get(u).evaluate(v)) {
                        toWorlds.add(v);
                    }
                }
                perAgent.put(u, toWorlds);
            }
            equivalenceClasses.put(agent, perAgent);
        }


        // Q_iu
        Map<String, Map<World, Set<World>>> containingClasses = new HashMap<>();
        for (String agent : agents) {
            for (World w : worlds) {
                Map<World, Set<World>> equivalenceClass = equivalenceClasses.get(agent);
            }

        }



        return null;
    }


    public static EpistemicState transition(EpistemicState inState, Action action) {
        return null;
    }

}
