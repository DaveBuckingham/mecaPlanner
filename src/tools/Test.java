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
// 4. For each action defined
//     a. Apply the single action
//     b. Check the state structure
// 5. Check if all goal formulae hold in the final state



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

        State state = problem.getStartState();

        System.out.println("START STATE:");
        System.out.println(state);

        System.out.println("TRANSITIVE: " + state.isTransitive());
        System.out.println("REFLEXIVE: " + state.isReflexive());
        System.out.println("WELL: " + state.isWell());

        System.out.println("INIITIALLY:");
        for (Formula f : problem.getInitially()) {
            System.out.println(f + ": " + f.evaluate(state));
        }

        List<Transformer> allActions = domain.getTransformerList();

        for (Transformer action : allActions) {
            System.out.println("ACTION: " + action.getSignature());
            state = action.transition(state);

            System.out.println("NEW STATE:");
            System.out.println(state);

            System.out.println("TRANSITIVE: " + state.isTransitive());
            System.out.println("REFLEXIVE: " + state.isReflexive());
            System.out.println("WELL: " + state.isWell());
        }

        System.out.println("GOALS:");
        for (Formula f : problem.getGoals()) {
            System.out.println(f + ": " + f.evaluate(state));
        }

    }
}
