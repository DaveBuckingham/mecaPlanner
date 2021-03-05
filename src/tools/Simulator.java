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

// INTERACT WITH USER, MODELED AS AN ENVIRONMENT AGENT

public class Simulator {


    public static void main(String args[]) {

        Log.setThreshold("info");
        //Log.setThreshold("debug");

        // READ AND PARSE A .depl FILE
        if (args.length != 1) {
            throw new RuntimeException("expected single depl file parameter.");
        }
        String deplFile = args[0];
        DeplToProblem visitor = new DeplToProblem();
        Problem problem = visitor.buildProblem(deplFile);
        Domain domain = problem.getDomain();
        final BeliefFormula goal = problem.getGoal();

        // VARIABLES TO TRACK THE SYSTEM STATE
        EpistemicState currentState = problem.getStartState();
        Map<String, Model> models = problem.getStartingModels();
        int depth = 0;

        // START WITH AN EMPTY PLAN
        Solution plan = new Solution(problem); 

        // TO HELP KEEP TRACK OF AGENTS
        final int numAgents = domain.getNonPassiveAgents().size();
        //if (numAgents != 2) {
        //    throw new RuntimeException("simulator requires a single s-agent and a single e-agent");
        //}

        // THESE WILL BE 0 AND 1, OR 1 AND 0
        final int sIndex = problem.getSystemAgentIndex();
        //final int eIndex = 1 - sIndex;


        // MAIN LOOP, CONTINUE UNTIL THE GOAL IS ACHIEVED
        while(!goal.evaluate(currentState)) {

            String currentAgent = domain.agentAtDepth(depth);

            // DRAW THE STATE FROM THE HUMAN'S PERSPECTIVE
            //displayState(domain, currentState.getBeliefPerspective(domain.agentAtDepth(eIndex)));
            displayState(domain, currentState);

            // THE ACTION TAKEN BY THE CURRENT AGENT
            Action action = null;

            // IF IT'S THE ROBOT'S TURN
            if (depth % numAgents == sIndex) {
                Perspective robotView = new Perspective(currentState, currentAgent);

                // IF OUR CURRENT PLAN DOESN'T KNOW WHAT TO DO, RUN THE PLANNER
                if (!plan.hasPerspective(robotView)) {
                    System.out.println("PLANNING...");

                    // WE'LL HAVE TO BUILD THIS FROM ADE BELIEF
                    Set<EpistemicState> robotPerspectiveStates =
                        currentState.getBeliefPerspective(currentAgent).getEpistemicStates();

                    Search search = new Search();
                    Problem newProblem = new Problem(problem.getDomain(),
                                                     sIndex,
                                                     robotPerspectiveStates,
                                                     models,
                                                     problem.getGoals()
                                                    );
                    plan = search.findSolution(newProblem);
                    if (plan == null) {
                        System.out.println("NO SOLUTION FOUND, ABORTING.");
                        System.exit(0);
                    }
                    if (!plan.hasPerspective(robotView)) {
                        throw new RuntimeException("got a bad plan.");
                    }
                    System.out.println("SOLUTION FOUND.");
                }

                // THE PLAN GIVES US THE ROBOT'S ACTION
                action = plan.getAction(robotView);

                // THE PLAN IS A TREE, PROCEDE ACCORDING TO THE STATE
                plan = plan.getChild(robotView);
            }


            // OTHERWISE, IT'S AN E-AGENT'S HUMAN'S TURN
            else {

                // WHAT THE E-AGENT BELIEVES, ACCORDING TO THE CURRENT STATE
                NDState humanPerspective = currentState.getBeliefPerspective(currentAgent);

                // AN ACTION IS APPLICABLE IN THE CURRENT STATE IF
                // ITS PRECONDITIONS ARE SATISFIED AND IF THE HUMAN BELIEVES
                // THAT ITS PRECONDITIONS ARE SATISFIED (WE MIGHT WANT TO RELAX THIS)
                Set<Action> applicable = new HashSet<>();
                for (Action a : domain.getAgentActions(currentAgent)) {
                    if (a.necessarilyExecutable(humanPerspective)){
                        applicable.add(a);
                    }
                }

                // THE CURRENT PLAN ASSUMES THE HUMAN WILL PERFORM A PREDICTED ACTION
                Set<Action> predictions = models.get(currentAgent).getPrediction(humanPerspective);

                for (Action a : predictions) {
                    if (!a.getActor().equals(currentAgent)) {
                        throw new RuntimeException("model returned an action for the wrong agent");
                    }
                }

                if (predictions.isEmpty()) {
                    throw new RuntimeException("model gave no applicable actions.");
                }
                if (applicable.isEmpty()) {
                    throw new RuntimeException("no applicable actions for s-agent");
                }
                if (!applicable.containsAll(predictions)) {
                    System.out.println("Agent doesn't think a predicted action is applicable:");
                    System.out.println("Predictions:");
                    System.out.println(predictions);
                    System.out.println("State");
                    System.out.println(currentState);
                    System.exit(1);
                }

                // ASK THE USER TO SELECT AN ACTION
                action = getHumanAction(currentAgent, applicable, predictions);
            }

            if (action == null) {
                throw new RuntimeException("somehow failed to select an action");
            }

            // EXECUTE THE ACTION
            System.out.println(currentAgent + " ACTS: " + action.getSignature());
            System.out.println();
            Action.UpdatedStateAndModels transitionResult = action.transition(currentState, models);
            currentState = transitionResult.getState();
            models = transitionResult.getModels();

            depth += 1;
        }

        // GOAL ACHIEVED
        //displayState(domain, currentState.getBeliefPerspective(domain.agentAtDepth(eIndex)));
        displayState(domain, currentState);
        System.out.println("ACHIEVED GOAL: " + goal + "\n");
        
    }


    // PRINT OUT THE CURRENT STATE
    private static void displayState(Domain domain, NDState humanPerspective) {
        System.out.println("STATE:");
        System.out.println(humanPerspective);
        //for (FluentAtom atom : domain.getAllAtoms()) {
        //    if (humanPerspective.necessarily(atom)) {
        //        System.out.println("\t" + atom);
        //    }
        //    else if (humanPerspective.possibly(atom)) {
        //        System.out.println("\tpossibly " + atom);
        //    }
        //}
    }


    // GET AN ACTION SELECTION FROM THE USER
    private static Action getHumanAction(String agent, Set<Action> applicable, Set<Action> predictions) {
        Scanner stdin = new Scanner(System.in);
        System.out.println("PICK AN ACTION FOR " + agent + ", \"*\" INDICATES MODEL PREDICITONS:");
        List<Action> options = new ArrayList<>(applicable);
        for (Integer i = 0; i < options.size(); i++) {
            System.out.print(predictions.contains(options.get(i)) ? "* " : "  ");
            System.out.println(i + ": " + options.get(i).getSignature());
        }
        Integer selection = -1;
        while (selection < 0 || selection >= options.size()) {
            selection = stdin.nextInt();
        }
        return options.get(selection);
    }

}


