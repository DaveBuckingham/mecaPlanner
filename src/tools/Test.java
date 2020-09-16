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



public class Test {

    public static void main(String args[]) {
        Log.setThreshold("warning");

        String[] depls = new String[] {
            "problems/kr/ontic.depl",
            "problems/kr/sensing.depl",
            "problems/kr/announcement.depl"
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

            EpistemicState postState = domain.getPostState();

            if (postState == null) {
                System.out.println("no post state in depl");
                return;
            }

            Boolean equivalent = endState.equivalent(postState);

            System.out.print(equivalent ? "PASS\n" : "FAIL\n");
        }
    }
}
