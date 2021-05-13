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

    public static EpistemicState constructState(BeliefFormula formula) {
        FormulaTree formulaTree = parseFormula(formula);
        return null;
    }

    private static Set<FormulaTree> parseFormula(BeliefFormula formula) {
        Set<FormulaTree> result = new HashSet<>();
        if (formula instanceof Fluent) {
            Set<Fluent> fluents = new HashSet<>();
            fluents.add((Fluent) formula);
            result.add(new FormulaTree(fluents, new HashSet<FormulaTree>(), new HashSet<FormulaTree>()));
        }
        else if (formula instanceof BeliefOrFormula) {
            BeliefOrFormula orFormula = (BeliefOrFormula) formula;
            for (BeliefFormula inner : orFormula.getFormulae()) {
                result.add(parseFormula(inner));
            }
        }
        else if (formula instanceof LocalOrFormula) {
            LocalOrFormula orFormula = (LocalOrFormula) formula;
            for (LocalFormula inner : orFormula.getFormulae()) {
                result.add(parseFormula(inner));
            }
        }
        else if (formula instanceof BeliefAndFormula) {
            Set<Fluent> fluents = new HashSet<>();
            Set<FormulaTree> beliefs = new HashSet<>();
            Set<FormulaTree> negatives = new HashSet<>();
            BeliefAndFormula andFormula = (BeliefAndFormula) formula;
            for (BeliefFormula inner : andFormula.getFormulae()) {
                // HERE
                ParseFormula
                result.add(parseFormula(inner));
            }
        }
        else {
            throw new RuntimeException("Can't build state from formula: " formula.toString());
        }
        return result;
    }
}
