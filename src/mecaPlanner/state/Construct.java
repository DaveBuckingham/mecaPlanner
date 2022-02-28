package mecaPlanner.state;

import mecaPlanner.formulae.*;
import mecaPlanner.Domain;
import mecaPlanner.Log;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;


import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;


public class Construct {

    private Construct() { }

    private static Stack<Formula> todo;

    public static Set<State> constructStates(List<Formula> formulae) {
        todo = new Stack<>(formulae);
        propositionalFormulae;
    }

    private void propositionalFormulae() {
        while (formula = todo.pop()) {
            if (formula.instanceof(AndFormula)) {
                AndFormula a = (AndFormula) f;
                for (f : a.getFormulae()) {
                    todo.push(f);
                }
            }
            else if (formula.instanceof(
        }
    }




}

