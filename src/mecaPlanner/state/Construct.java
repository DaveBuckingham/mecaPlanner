package mecaPlanner.state;

import mecaPlanner.formulae.beliefFormulae.*;
import mecaPlanner.formulae.localFormulae.*;
import mecaPlanner.Domain;
import mecaPlanner.Log;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;


public class Construct {

    private Construct() { }

    private static class FormulaTree {
        Set<Fluent> fluents;
        Set<Fluent> negativeFluents;
        Map<FormulaTree, String> beliefs;
        Map<FormulaTree, String> negativeBeliefs;
        public FormulaTree(Set<Fluent> fluents,
                           Set<Fluent> negativeFluents,
                           Map<FormulaTree, String> beliefs,
                           Map<FormulaTree, String> negativeBeliefs) {
            this.fluents = fluents;
            this.negativeFluents = negativeFluents;
            this.beliefs = beliefs;
            this.negativeBeliefs = negativeBeliefs;
        }
        public FormulaTree negate() {
            return new FormulaTree(negativeFluents, fluents, negativeBeliefs, beliefs);
        }
        public Set<Fluent> getFluents() {
            return fluents;
        }
        public Set<Fluent> getNegativeFluents() {
            return negativeFluents;
        }
        public Map<FormulaTree, String> getBeliefs() {
            return beliefs;
        }
        public Map<FormulaTree, String> getNegativeBeliefs() {
            return negativeBeliefs;
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



    public static EpistemicState constructState(BeliefFormula formula) {
        //List<FormulaTree> formulaTrees = parseFormula(formula);
        List<PossiblyTree> possiblyTrees = new ArrayList<>();
        for (FormulaTree f : parseFormula(formula) {
            possiblyTrees.add(makePossibles(f));
        }
        return null;
    }

    private static List<FormulaTree> parseFormula(BeliefFormula formula) {
        List<FormulaTree> result = new ArrayList<>();
        if (formula instanceof Fluent) {
            Set<Fluent> fluents = new HashSet<>();
            fluents.add((Fluent) formula);
            result.add(new FormulaTree(fluents,
                                       new HashSet<Fluent>(),
                                       new HashMap<FormulaTree, String>(),
                                       new HashMap<FormulaTree, String>()));
        }
        else if (formula instanceof BeliefOrFormula) {
            BeliefOrFormula orFormula = (BeliefOrFormula) formula;
            for (BeliefFormula inner : orFormula.getFormulae()) {
                result.addAll(parseFormula(inner));
            }
        }
        else if (formula instanceof LocalOrFormula) {
            LocalOrFormula orFormula = (LocalOrFormula) formula;
            for (LocalFormula inner : orFormula.getFormulae()) {
                result.addAll(parseFormula(inner));
            }
        }
        else if (formula instanceof BeliefAndFormula || formula instanceof LocalAndFormula) {
            List<List<FormulaTree>> parsedInners = new ArrayList<>();;

            if (formula instanceof BeliefAndFormula) {
                BeliefAndFormula andFormula = (BeliefAndFormula) formula;
                for (BeliefFormula inner : andFormula.getFormulae()) {
                    parsedInners.add(parseFormula(inner));
                }
            }
            else {
                LocalAndFormula andFormula = (LocalAndFormula) formula;
                for (LocalFormula inner : andFormula.getFormulae()) {
                    parsedInners.add(parseFormula(inner));
                }
            }
            
            List<List<FormulaTree>> product = cartesianProduct(parsedInners);


            for (List<FormulaTree> option : product) {
                Set<Fluent> fluents = new HashSet<>();
                Set<Fluent> negativeFluents = new HashSet<>();
                Map<FormulaTree, String> beliefs = new HashMap<>();
                Map<FormulaTree, String> negativeBeliefs = new HashMap<>();

                for (FormulaTree t : option) {
                    fluents.addAll(t.getFluents());
                    negativeFluents.addAll(t.getNegativeFluents());
                    beliefs.putAll(t.getBeliefs());
                    negativeBeliefs.putAll(t.getNegativeBeliefs());
                }
                result.add(new FormulaTree(fluents, negativeFluents, beliefs, negativeBeliefs));
            }
        }
        else if (formula instanceof BeliefNotFormula || formula instanceof LocalNotFormula) {
            BeliefFormula negatedFormula;
            if (formula instanceof BeliefNotFormula) {
                negatedFormula = ((BeliefNotFormula)formula).getFormula();
            }
            else {
                negatedFormula = ((LocalNotFormula)formula).getFormula();
            }
            for (FormulaTree tree : parseFormula(negatedFormula)) {
                result.add(tree.negate());
            }
        }
        else if (formula instanceof BeliefBelievesFormula) {
            BeliefBelievesFormula beliefFormula = (BeliefBelievesFormula) formula;
            Map<FormulaTree, String> belief = new HashMap<>();
            for (FormulaTree t : parseFormula(beliefFormula.getFormula())) {
                belief.put(t, beliefFormula.getAgent());
            }
            result.add(new FormulaTree(new HashSet<Fluent>(),
                                       new HashSet<Fluent>(),
                                       belief,
                                       new HashMap<FormulaTree, String>()));
        }
        else {
            throw new RuntimeException("Can't build state from formula: " + formula.toString());
        }
        return result;
    }


    private static PossiblyTree makePossibles(FormulaTree formulaTree) {
        assert(checkConsistent(formulaTree.getBeliefs()));
        Map<PossiblyTree, String> possible = new HashSet<>();
        for (Map.Entry<FormulaTree, String> entry : formulaTree.getNegativeBeliefs()) {
            FormulaTree negativeBelief = entry.getKey();
            String agent = entry.getValue();
            possible.put(makePossible(negativeBelief.negate()), agent);
            Set<Map<PossiblyTree, String>> possibilitiesWithCertains = 
        }
    }

    private static boolean checkConsistent(FormulaTree f) {
        return true;
    }
}
