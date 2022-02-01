package tools;

import java.util.Set;

import mecaPlanner.Log;
import mecaPlanner.Action;
import mecaPlanner.Domain;
import mecaPlanner.Problem;
import mecaPlanner.state.NDState;
import mecaPlanner.state.State;
import depl.*;


// READ A DEPL WITH A SINGLE ACTION,
// PRINT THE START STATE,
// EXECUTE THE ACTION
// PRINT THE RESULT


public class Example {

    public static void main(String args[]) {
        //Log.setThreshold("debug");

        if (args.length < 1) {
            System.out.println("requires a depl file");
            return;
        }
        String deplFile = args[0];

        DeplToProblem deplParser = new DeplToProblem();
        Problem problem = deplParser.buildProblem(deplFile);
        Domain domain = problem.getDomain();

        State startState = problem.getStartState();

        if (startState.checkRelations()) {
            System.out.println("VALID START STATE:");
            System.out.println(startState);
        }
        else {
            System.out.println("BAD START STATE:");
            System.out.println(startState);
            return;
        }

        Set<Action> allActions = domain.getAllActions();
        if (allActions.size() != 1) {
            System.out.println("depl must contain a single action");
            return;
        }
        Action action = allActions.iterator().next();

        System.out.println(action);

        State endState = action.transition(startState);
        System.out.println(endState);
    }
}
