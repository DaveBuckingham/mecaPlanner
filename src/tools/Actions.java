package tools;

import mecaPlanner.state.NDState;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.models.Model;
import mecaPlanner.Action;
import mecaPlanner.search.Perspective;
import mecaPlanner.search.Search;
import mecaPlanner.formulae.beliefFormulae.BeliefFormula;
import mecaPlanner.Domain;
import mecaPlanner.Solution;
import mecaPlanner.Problem;
import mecaPlanner.Log;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import depl.DeplToProblem;


public class Actions {


    public static void main(String args[]) {

        Log.setThreshold("warning");

        if (args.length != 1) {
            throw new RuntimeException("expected single depl file parameter.");
        }
        String deplFile = args[0];
        DeplToProblem visitor = new DeplToProblem();
        Problem problem = visitor.buildProblem(deplFile);
        Domain domain = problem.getDomain();

        // VARIABLES TO TRACK THE SYSTEM STATE
        EpistemicState currentState = problem.getStartState();



        Map<String, Model> models = problem.getStartingModels();
        int depth = 0;

        while(true) {

            currentState.getKripke().forceCheck();
            System.out.println(currentState);

            List<Action> applicable = new ArrayList<>();
            for (Set<Action> agentActions : domain.getActionMap().values()) {
                for (Action a : agentActions) {
                    if (a.getPrecondition().evaluate(currentState)){
                        applicable.add(a);
                    }
                }
            }



            Scanner stdin = new Scanner(System.in);
            for (Integer i = 0; i < applicable.size(); i++) {
                System.out.println(i + ": " + applicable.get(i).getSignature());
            }
            Integer selection = -1;
            while (selection < 0 || selection >= applicable.size()) {
                try{
                selection = stdin.nextInt();
                }
                catch(Exception e){
                    System.exit(0);
                }
                if (selection < 0 || selection >= applicable.size()) {
                    System.exit(0);
                }
            }
            Action action = applicable.get(selection);

            if (action == null) {
                throw new RuntimeException("somehow failed to select an action");
            }

            Action.UpdatedStateAndModels transitionResult = action.transition(currentState, models);
            currentState = transitionResult.getState();

        }

    }


}


