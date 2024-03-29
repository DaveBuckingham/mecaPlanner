package tools;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import mecaPlanner.Log;
import mecaPlanner.Domain;
import mecaPlanner.Problem;
import mecaPlanner.state.NDState;
import mecaPlanner.state.State;
import mecaPlanner.formulae.Formula;
import mecaPlanner.formulae.Literal;

import depl.*;

// 1. Read a starting preorder state
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

        Set<State> states = problem.getStartStates();

        if (states.isEmpty()) {
            throw new RuntimeException("no start states");
        }

        if (states.size() > 1) {
            System.out.println("multiple start states:");
            for (State s : states) {
                System.out.println(s);
            }
            return;
        }

        State state = states.iterator().next();


        System.out.println("START STATE:");
        System.out.println(state);

        state.checkRelations();

        System.out.println("INIITIALLY:");
        for (Formula f : problem.getInitially()) {
            System.out.println(f + ": " + f.evaluate(state));
        }

        for (Action action : domain.getAllActions()) {
            System.out.println("ACTION: " + action.getSignature());
            state = action.transition(state);

            System.out.println("NEW STATE:");
            System.out.println(state);


            state.checkRelations();
        }

        System.out.println("GOALS:");
        for (Formula f : problem.getGoals()) {
            System.out.println(f + ": " + f.evaluate(state));
        }

    }
}
