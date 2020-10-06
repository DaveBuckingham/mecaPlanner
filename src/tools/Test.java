package tools;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import mecaPlanner.Log;
import mecaPlanner.Action;
import mecaPlanner.Domain;
import mecaPlanner.Problem;
import mecaPlanner.state.NDState;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.formulae.booleanFormulae.BooleanAtom;
import depl.*;


// THIS IS SIMILAR TO EXAMPLE: READS A DEPL WITH A SINGLE ACTION
// HOWEVER, THIS ALSO READS A POST-STATE FROM THE DEPL
// AFTER EXECUTING THE ACTION, MAKE SURE THE RESULT IS THE EQUIVALENT TO THE POST-STATE


public class Test {

    public static void main(String args[]) {
        Log.setThreshold("warning");

        String[] depls = new String[] {
            "problems/kr/ontic.depl",
            "problems/kr/sensing.depl",
            "problems/kr/announcement.depl",
            "problems/kr/box.depl"
        };

        for (String deplFile : depls) {

            System.out.print(deplFile);
            System.out.print("...");

            DeplToProblem deplParser = new DeplToProblem();
            Problem problem = deplParser.buildProblem(deplFile);
            Domain domain = problem.getDomain();

            EpistemicState startState = problem.getStartState();

            if (!startState.getKripke().checkRelations()) {
                System.out.println("bad start state:");
                System.out.println(startState);
                return;
            }

            Set<Action> allActions = domain.getAllActions();
            if (allActions.size() != 1) {
                System.out.println("depl must contain a single action");
                return;
            }
            Action action = allActions.iterator().next();

            EpistemicState endState = action.transition(startState);

            assert(endState.getKripke().checkRelations());

            EpistemicState postState = domain.getPostState();

            if (postState == null) {
                System.out.println("no post state in depl");
                return;
            }

            // THIS IS IMPORTANT, equivalent() CAN GIVE FALSE POSITIVES OTHERWISE
            if (!postState.getKripke().checkRelations()) {
                System.out.println("bad post state:");
                System.out.println(postState);
                return;
            }

            Boolean equivalent = endState.equivalent(postState);

            assert (equivalent == postState.equivalent(endState));

            System.out.print(equivalent ? "PASS\n" : "FAIL\n");
        }
    }
}
