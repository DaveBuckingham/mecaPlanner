package tools;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import mecaPlanner.Log;
import mecaPlanner.state.Transformer;
import mecaPlanner.Domain;
import mecaPlanner.Problem;
import mecaPlanner.state.NDState;
import mecaPlanner.state.State;
import mecaPlanner.formulae.Formula;

import depl.*;

// 1. Read an starting preorder state
// 2. Check start state structure
// 3. Check if all initial formulae hold in the state
// 4. Apply the single action
// 5. Check the new state structure
// 6. Check if all goal formulae hold in the new state



public class Test {

    public static void main(String args[]) {
        Log.setThreshold("warning");

        if (args.length != 1) {
            throw new RuntimeException("expected single depl file parameter.");
        }
        String deplFile = args[0];
        DeplToProblem visitor = new DeplToProblem();
        Problem problem = visitor.buildProblem(deplFile);
        Domain domain = problem.getDomain();

        State startState = problem.getStartState();

        System.out.println("START STATE:");
        System.out.println(startState);

        System.out.println("TRANSITIVE: " + startState.isTransitive());
        System.out.println("REFLEXIVE: " + startState.isReflexive());
        System.out.println("WELL: " + startState.isWell());

        System.out.println("INIITIALLY:");
        for (Formula f : problem.getInitially()) {
            System.out.println(f + ": " + f.evaluate(startState));
        }

        Set<Transformer> allActions = new HashSet<Transformer>(domain.getAllActions());
        allActions.addAll(domain.getEventModels());
        if (allActions.isEmpty()) {
            System.out.println("NO ACTION...ABORTING");
            return;
        }
        if (allActions.size() > 1) {
            System.out.println("MULTIPLE ACTIONS...ABORTING");
            return;
        }

        Transformer action = allActions.iterator().next();
        System.out.println("ACTION:");
        System.out.println(action);

        State newState = action.transition(startState);

        System.out.println("NEW STATE:");
        System.out.println(newState);

        System.out.println("TRANSITIVE: " + newState.isTransitive());
        System.out.println("REFLEXIVE: " + newState.isReflexive());
        System.out.println("WELL: " + newState.isWell());

        System.out.println("GOALS:");
        for (Formula f : problem.getGoals()) {
            System.out.println(f + ": " + f.evaluate(newState));
        }

    }
}
