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


    private static Set<Action> getHypotheticalActions(Domain domain, String agent, Action actual, EpistemicState state) {
        Set<Action> hypotheticalActions = new HashSet<>();
        for (Action action : domain.getAllActions()) {
            boolean anyOblivious = false;
            for (World u : state.getWorlds()) {
                if (!action.equals(actual)) {
                    if (actual.isOblivious(agent, u) && action.isOblivious(agent, u)) {
                        if (action.executable(u)){
                            hypotheticalActions.add(action);
                        }
                    }
                }
            }
        }
        return hypotheticalActions;
    }


    private static Map<World, PostWorld> map;
    //private static Map<World, Set<World>> inverse;


    private static KripkeStructure intermediateTransition(KripkeStructure oldModel, Action action) {
        Set<World> oldWorlds = oldModel.getWorlds();
        Set<String> agents  = oldModel.getAgents();

        if (map == null) {
            map = new HashMap<>();
        }

//        if (inverse == null) {
//            inverse = new HashMap<>();
//        }
        

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
        for (World w : oldWorlds) {
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
        for (World w : oldWorlds) {
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
        for (World w : oldWorlds) {
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
            for (World w : oldWorlds) {
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
            for (World w : oldWorlds) {
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
            for (World w : oldWorlds) {
                assert (learnedKnowledge.get(agent).get(w).evaluate(w));
            }
        }


        // C_iu
        Map<String, Map<World, Set<World>>> equivalenceClasses = new HashMap<>();
        for (String agent : agents) {
            Map<World, Set<World>> perAgent = new HashMap<>();
            for (World u : oldWorlds) {
                Set<World> toWorlds = new HashSet<>();
                for (World v : oldModel.getKnownWorlds(agent, u)) {
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
        for (World u : oldWorlds) {
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
        Set<World> alphaWorlds = new HashSet<>();
        for (World w : oldWorlds) {
            //inverse.put(w, new HashSet<World>())
            if (action.executable(w)) {;
                for (List<Set<World>> classAssignment : classAssignments.get(w)) {
                    World newWorld = w.update(action.getApplicableEffects(w));
                    alphaWorlds.add(newWorld);
                    map.put(newWorld, new PostWorld(w, action, classAssignment));
                    //inverse.get(w).add(newWorld);
                }
            }
        }


        // K^a_i
        Map<String, Relation> alphaKRelation = new HashMap<>();
        for (String agent : agents) {
            Relation relation = new Relation();
            for (World u : alphaWorlds) {
                PostWorld uDef = map.get(u);
                for (World v : alphaWorlds) {
                    PostWorld vDef = map.get(v);
                    if (uDef.eqClassAssignment == vDef.eqClassAssignment) {
                        relation.connect(u,v);
                    }
                }
            }
            alphaKRelation.put(agent, relation);
        }



        // {B}^a_iu
        Map<String, Map<World, BeliefFormula>> acquiredBelief = new HashMap<>();
        for (String agent : agents) {
            Map<World, BeliefFormula> perAgent = new HashMap<>();
            for (World u : oldWorlds) {
                if (action.isObservant(agent, u)) {
                    perAgent.put(u, BeliefAndFormula.make(action.getAnnounces()));
                }
                else {
                    perAgent.put(u, new Literal(true));
                }
            }
            acquiredBelief.put(agent, perAgent);
        }



        // B^a_i
        Map<String, Relation> alphaBRelation = new HashMap<>();
        for (String agent : agents) {
            Relation relation = new Relation();

            for (World u : alphaWorlds) {
                World oldU = map.get(u).oldWorld;
                BeliefFormula learned = acquiredBelief.get(agent).get(oldU);

                // B^a1_i
                for (World v : alphaKRelation.get(agent).getToWorlds(u)) {
                    World oldV = map.get(v).oldWorld;
                    if (oldModel.isConnectedBelief(agent, oldU, oldV)) {
                        if (learned.evaluate(oldModel, oldV)) {
                            relation.connect(u,v);
                        }
                    }
                }

                // B^a2_i
                if (relation.deadEnd(u)) {
                    for (World v : alphaKRelation.get(agent).getToWorlds(u)) {
                        World oldV = map.get(v).oldWorld;
                        if (learned.evaluate(oldModel, oldV)) {
                            relation.connect(u,v);
                        }
                    }
                }

                // B^a_i
                if (relation.deadEnd(u)) {
                    for (World v : alphaKRelation.get(agent).getToWorlds(u)) {
                        relation.connect(u,v);
                    }
                }
            }

            alphaBRelation.put(agent, relation);
        }

        return new KripkeStructure(alphaWorlds, alphaBRelation, alphaKRelation);
    }


    public static EpistemicState transition(EpistemicState inState, Action actualAction) {
        
        // CHECK EQUATION 22, ACTION PRECONDITIONS ARE SATISFIED
        assert(actualAction.executable(inState.getDesignatedWorld()));

        Set<String> agents  = inState.getKripke().getAgents();
        Domain domain = actualAction.getDomain();


        // M^a
        KripkeStructure modelActual = intermediateTransition(inState.getKripke(), actualAction);

        World designatedAlpha = null;
        for (World w : modelActual.getWorlds()) {
            // SHOLD WE USE "==" INSTEAD OF ".equals"?
            if (map.get(w).oldWorld.equals(inState.getDesignatedWorld())) {
                assert(designatedAlpha == null);
                designatedAlpha = w;
            }
        }
        assert(designatedAlpha != null);


        // SHORTCUT: IF NO OBLIVIOUS AGENTS CAN JUST RETURN INTERMEDIATE TRANSITION
        boolean anyOblivious = false;
        for (World w : modelActual.getWorlds()) {
            for (String agent : agents) {
                if (actualAction.isOblivious(agent, map.get(w).oldWorld)) {
                    anyOblivious = true;
                    break;
                }
            }
            if (anyOblivious) {
                break;
            }
        }
        if (!anyOblivious) {
            return new EpistemicState(modelActual, designatedAlpha);
        }


        // BUILD NULL ACTION
        Map<String, LocalFormula> nullObserverConditions = new HashMap<>();
        Map<String, LocalFormula> nullAwareConditions = new HashMap<>();
        for (String agent : agents) {
            nullObserverConditions.put(agent, new Literal(true));
            nullAwareConditions.put(agent, new Literal(false));
        }
        Action nullAction = new Action("nullAction",                            // name
                                       new ArrayList<String>(),                 // parameters
                                       "nullActor",                             // actor
                                       1,                                       // cost
                                       new Literal(true),                       // preconditions 
                                       nullObserverConditions,                  // observesIf
                                       nullAwareConditions,                     // awareIf
                                       new HashSet<LocalFormula>(),             // determines
                                       new HashSet<BeliefFormula>(),            // announces
                                       new HashMap<Assignment, LocalFormula>(), // effects
                                       domain
                                      );

        
        // M^0
        KripkeStructure modelNull = intermediateTransition(inState.getKripke(), nullAction);


        // H_i
        Map<String, Set<Action>> hypotheticalActions = new HashMap<>();
        Map<String, Map<Action, KripkeStructure>> hypotheticalModels = new HashMap<>();
        for (String agent : agents) {
            hypotheticalActions.put(agent, getHypotheticalActions(domain, agent, actualAction, inState));
            Map<Action, KripkeStructure> models = new HashMap<>();
            for (Action hypotheticalAction : hypotheticalActions.get(agent)) {
                //models.add(intermediateTransition(inState.getKripke(), hypotheticalAction));
                models.put(hypotheticalAction, intermediateTransition(inState.getKripke(), hypotheticalAction));
            }
            if (!models.isEmpty()) {
                models.put(nullAction, modelNull);
            }
            models.put(actualAction, modelActual);
            hypotheticalModels.put(agent, models);
        }


        // S'
        Set<World> newWorlds = new HashSet<>();
        for (Map.Entry<String, Map<Action, KripkeStructure>> entry : hypotheticalModels.entrySet()) {
            for (KripkeStructure model : entry.getValue().values()) {
                newWorlds.addAll(model.getWorlds());
            }
        }


        // K'_i
        Map<String, Relation> newKRelation = new HashMap<>();
        for (String agent : agents) {
            Relation relation = new Relation();
            for (World u : newWorlds) {
                assert(map.containsKey(u));
                Action uAction = map.get(u).action;
                KripkeStructure uModel = hypotheticalModels.get(agent).get(uAction);
                World uOld = map.get(u).oldWorld;

                // K'_iF
                if (uAction.isObservant(agent, uOld) || uAction.isAware(agent, uOld)) {
                    for (World v : uModel.getKnownWorlds(agent, u)) {
                        relation.connect(u,v);
                    }
                }

                // K'_iO
                else {
                    for (World v : newWorlds) {
                        assert(map.containsKey(v));
                        Action vAction = map.get(v).action;
                        KripkeStructure vModel = hypotheticalModels.get(agent).get(vAction);
                        World vOld = map.get(v).oldWorld;
                        if ((uAction == vAction) || (hypotheticalActions.get(agent).contains(uAction) && 
                                                     hypotheticalActions.get(agent).contains(vAction))) {
                            for (World uNull : modelNull.getWorlds()) {
                                for (World vNull : modelNull.getKnownWorlds(agent, uNull)) {
                                    if (map.get(uNull).oldWorld == uOld && map.get(vNull).oldWorld == vOld) {
                                        relation.connect(u,v);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            newKRelation.put(agent, relation);
        }



        // B'_i
        Map<String, Relation> newBRelation = new HashMap<>();
        for (String agent : agents) {
            Relation relation = new Relation();
            for (World u : newWorlds) {
                assert(map.containsKey(u));
                Action uAction = map.get(u).action;
                KripkeStructure uModel = hypotheticalModels.get(agent).get(uAction);
                World uOld = map.get(u).oldWorld;

                // B'_iF
                if (uAction.isObservant(agent, uOld) || uAction.isAware(agent, uOld)) {
                    for (World v : uModel.getBelievedWorlds(agent, u)) {
                        relation.connect(u,v);
                    }
                }

                // B'_iO
                else {
                    for (World v : newWorlds) {
                        assert(map.containsKey(v));
                        Action vAction = map.get(v).action;
                        KripkeStructure vModel = hypotheticalModels.get(agent).get(vAction);
                        World vOld = map.get(v).oldWorld;
                        if ((hypotheticalActions.get(agent).contains(uAction) && (uAction == nullAction)) ||
                            ((uAction == vAction) && (!hypotheticalActions.get(agent).contains(uAction)))) {
                            for (World uNull : modelNull.getWorlds()) {
                                for (World vNull : modelNull.getBelievedWorlds(agent, uNull)) {
                                    if (map.get(uNull).oldWorld == uOld && map.get(vNull).oldWorld == vOld) {
                                        relation.connect(u,v);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            newBRelation.put(agent, relation);
        }

        KripkeStructure newModel = new KripkeStructure(newWorlds, newBRelation, newKRelation);
        EpistemicState newState = new EpistemicState(newModel, designatedAlpha);

        return newState;
    }

}
