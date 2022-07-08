package tools;

import mecaPlanner.state.*;
import mecaPlanner.state.State;
//import mecaPlanner.models.Model;
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

// LOAD A DEPL, ASK USER TO SELECT AN ACTION, SHOW TRANSITION, REPEAT

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
        State currentState = problem.getStartState();
        //Map<String, Model> models = problem.getStartingModels();
        int depth = 0;

        boolean cont = true;

        while(cont) {

            //currentState.getKripke().forceCheck();
            //currentState.getKripke().checkRelations();
            System.out.println(currentState);

            List<Transformer> applicable = new ArrayList<>();
            for (Transformer action : domain.getTransformerList()) {
                if (action.executable(currentState)){
                    applicable.add(action);
                }
            }







                for (EventModel e : domain.getEventModels()) {
                    applicable.add(e);
                }


                Scanner stdin = new Scanner(System.in);
                for (Integer i = 0; i < applicable.size(); i++) {
                    System.out.println(i + ": " + applicable.get(i).getSignature());
                }
                Integer selection = -1;

                try{
                    selection = stdin.nextInt();
                }
                catch(Exception e){
                    cont = false;
                    continue;
                }
                if (selection < 0 || selection >= applicable.size()) {
                    cont = false;
                    continue;
                }

                Transformer action = applicable.get(selection);

                if (action == null) {
                    throw new RuntimeException("somehow failed to select an action");
                }

                State newState = action.transition(currentState);
                //newState.normalize();
                //newState.trim();
                //currentState.normalize();
                //if (currentState.bisimilar(newState)) {
                //    System.out.println("bisimilar!");
                //}
                //else {
                //    System.out.println("not bisimilar");
                //}
                currentState = newState;

        }

    }

}


