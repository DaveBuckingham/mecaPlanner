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
        Map<String, Set<FormulaTree>> beliefs;
        Map<String, Set<FormulaTree>> negatives;
        public FormulaTree(Set<Fluent> fluents,
                           Map<String, Set<FormulaTree>> beliefs,
                           Map<String, Set<FormulaTree>> negatives) {
            this.fluents = fluents;
            this.beliefs = beliefs;
            this.negatives = negatives;
        }
        public Set<Fluent> getFluents() {
            return fluents;
        }
        public Map<String, Set<FormulaTree>> getBeliefs() {
            return beliefs;
        }
        public Map<String, Set<FormulaTree>> getNegatives() {
            return negatives;
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
        List<FormulaTree> formulaTree = parseFormula(formula);
        return null;
    }

    private static List<FormulaTree> parseFormula(BeliefFormula formula) {
        List<FormulaTree> result = new ArrayList<>();
        if (formula instanceof Fluent) {
            Set<Fluent> fluents = new HashSet<>();
            fluents.add((Fluent) formula);
            result.add(new FormulaTree(fluents, new HashMap<String, FormulaTree>(), new HashMap<String, FormulaTree>()));
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
                Map<String, FormulaTree> beliefs = new HashMap<>();
                Map<String, FormulaTree> negatives = new HashMap<>();

                for (FormulaTree t : option) {
                    fluents.addAll(t.getFluents());
                    for 
                    for (Map.Entry<String, FormulaTree> entry : t.getBeliefs()) {
                        if (beliefs.hasKey(entry.getKey())) {
                            
                        }
                    }
                    beliefs.addAll(t.getBeliefs());
                    negatives.addAll(t.getNegatives());
                }
                result.add(new FormulaTree(fluents, beliefs, negatives));
            }
        }
        else if (formula instanceof BeliefNotFormula || formula instanceof LocalNotFormula) {
            BeliefFormulate negatedFormula;
            if (formula instanceof BeliefNotFormula) {
                negatedFormula = ((BeliefNotFormula)notFormula).getFormula();
            }
            else {
                negatedFormula = ((LocalNotFormula)notFormula).getFormula();
            }
            for (FormulaTree tree : parseFormula(negatedFormula)) {
                Set<Fluent> negatedFluents = new HashSet<>();
                for (Fluent f : tree.getFluents()) {
                    negatedFluents.add(f.negate());
                }
                result.add(new FormulaTree(negatedFluents, f.getNegatives(), f.getBeliefs());
            }
        }
        else if (formula instanceof BeliefBelievesFormula) {
            BeliefBelievesFormula beliefFormula = (BeliefBelievesFormula) formula;
            ...
        }
        else {
            throw new RuntimeException("Can't build state from formula: " + formula.toString());
        }
        return result;
    }
}
