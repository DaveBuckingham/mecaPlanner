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

    private static class PostWorld {
        public World oldWorld;
        public Action action;
        public List<Set<World>> eqClassAssignment;
        public PostWorld(World oldWorld, Action action, List<Set<World>> eqClassAssignment) {
            this.oldWorld = oldWorld;
            this.action = action;
            this.eqClassAssignment = eqClassAssignment;
        }
    }


    // THIS FUNCTION IS COPIED FROM PHILLIP MEISTER:
    // https://stackoverflow.com/questions/714108/cartesian-product-of-arbitrary-sets-in-java
    private static <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
        List<List<T>> resultLists = new ArrayList<List<T>>();
        if (lists.size() == 0) {
            resultLists.add(new ArrayList<T>());
            return resultLists;
        } else {
            List<T> firstList = lists.get(0);
            List<List<T>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));
            for (T condition : firstList) {
                for (List<T> remainingList : remainingLists) {
                    ArrayList<T> resultList = new ArrayList<T>();
                    resultList.add(condition);
                    resultList.addAll(remainingList);
                    resultLists.add(resultList);
                }
            }
        }
        return resultLists;
    }



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
            disjunctionF.get(agent).add(condition);
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
            disjunctionP.get(agent).add(condition);
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
            Map<World, LocalFormula> knowledgeByWorld = new HashMap<>();
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
                knowledgeByWorld.put(w, learnedKnowledgeFormula);
            }
            learnedKnowledge.put(agent, knowledgeByWorld);
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
        Map<World, Map<String, Set<Set<World>>>> containingClasses = new HashMap<>();
        for (World u : worlds) {
            Map<String, Set<Set<World>>> worldsWithContainingClasses = new HashMap<>();
            for (String agent : agents) {
                Set<Set<World>> classesContainingU = new HashSet<>();
                for (Map.Entry<World, Set<World>> entry : equivalenceClasses.get(agent).entrySet()) {
                    for (Set<World> eqClass : equivalenceClasses.get(agent).values()) {
                        if (eqClass.contains(u)) {
                            classesContainingU.add(eqClass);
                        }
                    }
                }
                worldsWithContainingClasses.put(agent, classesContainingU);
            }
            containingClasses.put(u, worldsWithContainingClasses);
        }


        // G_u
        Map<World, List<List<Set<World>>>> classAssignments = new HashMap<>();

        for (World w : containingClasses.keySet()) {
            List<List<Set<World>>> byAgent = new ArrayList<>(containingClasses.get(w).size());
            for (Map.Entry<String, Set<Set<World>>> entry : containingClasses.get(w).entrySet()) {
                byAgent.add(new ArrayList<Set<World>>(entry.getValue()));
            }
            classAssignments.put(w, cartesianProduct(byAgent));
        }


        // S^a
        Map<World, PostWorld> postWorlds = new HashMap<>();
        for (World w : worlds) {
            if (action.executable(w)) {
                for (List<Set<World>> classAssignment : classAssignments.get(w)) {
                    World newWorld = w.update(action.getApplicableEffects(w));
                    postWorlds.put(newWorld, new PostWorld(w, action, classAssignment));
                }
            }
        }


        // K^a_i
        Map<String, Relation> alphaKRelation = new HashMap<>();
        for (String agent : agents) {
            Relation relation = new Relation();
            for (Map.Entry<World, PostWorld> entry : postWorlds.entrySet()) {
                World u = entry.getKey();
                PostWorld uDef = entry.getValue();
                for (Map.Entry<World, PostWorld> entry2 : postWorlds.entrySet()) {
                    World v = entry2.getKey();
                    PostWorld vDef = entry2.getValue();
                    if (uDef.eqClassAssignment == vDef.eqClassAssignment) {
                        relation.connect(u,v);
                    }
                }
            }
            alphaKRelation.put(agent, relation);
        }

        System.out.println(alphaKRelation);


        System.exit(1);


        return null;

    }


    public static EpistemicState transition(EpistemicState inState, Action action) {
        KripkeStructure modelAlpha = intermediateTransition(inState.getKripke(), action);
        return null;
    }

}
