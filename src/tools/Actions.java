package tools;

import mecaPlanner.state.*;
import mecaPlanner.agents.Agent;
import mecaPlanner.formulae.*;
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
        Map<String, Agent> models = domain.getEnvironmentAgents();
        int depth = 0;

        List<String> eagents = new ArrayList<>(models.keySet());

        boolean cont = true;


        while(cont) {

            System.out.println("\nCURRENT STATE:");
            System.out.println(currentState);

            List<Transformer> applicable = new ArrayList<>();
            for (Transformer action : domain.getTransformerList()) {
                if (action.executable(currentState)){
                    applicable.add(action);
                }
            }


            System.out.println("\nPICK ONE:");
            Scanner stdin = new Scanner(System.in);
            for (Integer i = 0; i < applicable.size(); i++) {
                System.out.println(i + ": " + applicable.get(i).getSignature());
            }
            for (Integer i = 0; i < eagents.size(); i++) {
                System.out.println((i + applicable.size()) + ": [" + eagents.get(i) + "]");
            }
            Integer selection = -1;

            try{
                selection = stdin.nextInt();
            }
            catch(Exception e){
                cont = false;
                continue;
            }
            if (selection < 0 || selection >= (applicable.size() + eagents.size())) {
                cont = false;
                continue;
            }

            if (selection < applicable.size()) {
                Transformer t = applicable.get(selection);
                if (!(t instanceof Action)) {
                    throw new RuntimeException("not an action");
                }
                Action action = (Action)t;
                System.out.println("EXECUTING: " + action.getSignatureWithActor());
                State newState = action.transition(currentState);
                currentState = newState;
            }
            else if (selection < applicable.size() + eagents.size()) {
                Agent model = models.get(eagents.get(selection - applicable.size()));
                System.out.print("PREDICTED: ");
                for (Action a : model.getPrediction(currentState)) {
                    System.out.print(a.getSignatureWithActor() + ", ");
                }
                System.out.println("");
            }
            else {
                throw new RuntimeException("somehow failed to select an action or model");
            }

        }

    }

}


