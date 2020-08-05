package simulator;

import mecaPlanner.state.NDState;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.state.World;
import mecaPlanner.models.Model;
import mecaPlanner.actions.Action;
import mecaPlanner.search.Perspective;
import mecaPlanner.search.Search;
import mecaPlanner.formulae.*;
import mecaPlanner.Log;
import mecaPlanner.Domain;
import mecaPlanner.Solution;
import mecaPlanner.Problem;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import depl.*;


public class Simulator {


    public static void main(String args[]) {



        if (args.length != 1) {
            throw new RuntimeException("expected single depl file parameter.");
        }

        String deplFile = args[0];

        DeplToProblem visitor = new DeplToProblem();
        Problem problem = visitor.buildProblem(deplFile);
        Domain domain = problem.getDomain();


        Set<Solution> solutions = new HashSet<>();
        EpistemicState currentState = problem.getStartState();
        Map<String, Model> models = problem.getStartingModels();
        GeneralFormula goal = problem.getGoal();
        int depth = 0;
        Solution plan = new Solution(problem);


        while(!goal.holds(currentState, depth)) {

            String currentAgent = domain.agentAtDepth(depth);

            displayState(domain, currentState.getBeliefPerspective(currentAgent));

            Action action = null;

            // ROBOT'S TURN
            if (depth % domain.getNonPassiveAgents().size() == problem.getSystemAgentIndex()) {
                Perspective robotPerspective = new Perspective(currentState, currentAgent);
                if (!plan.hasPerspective(robotPerspective)) {
                    Set<EpistemicState> robotPerspectiveStates =
                        currentState.getBeliefPerspective(currentAgent).getEpistemicStates();
                    Problem newProblem = new Problem(problem.getDomain(),
                                                     problem.getSystemAgentIndex(),
                                                     robotPerspectiveStates,
                                                     models,
                                                     problem.getGoals()
                                                    );
                    Search search = new Search();
                    plan = search.findSolution(problem);
                    if (plan == null) {
                        System.out.println("No solution found for estate:\n" + currentState);
                        System.exit(0);
                    }
                    if (!plan.hasPerspective(robotPerspective)) {
                        throw new RuntimeException("got a bad plan.");
                    }
                }

                action = plan.getAction(robotPerspective);
                plan = plan.getChild(robotPerspective);
            }

            // HUMAN'S TURN
            else {
                NDState humanPerspective = currentState.getBeliefPerspective(currentAgent);

                Set<Action> applicable = new HashSet<>();
                for (Action a : domain.getAgentActions(currentAgent)) {
                    if (a.necessarilyExecutable(humanPerspective)){
                        applicable.add(a);
                    }
                }

                Set<Action> predictions = models.get(currentAgent).getPrediction(humanPerspective);

                if (predictions.isEmpty()) {
                    throw new RuntimeException("model gave no applicable actions.");
                }
                if (applicable.isEmpty()) {
                    throw new RuntimeException("no applicable actions for s-agent");
                }
                if (!applicable.containsAll(predictions)) {
                    throw new RuntimeException("Agent doesn't think a predicted action is applicable");
                }

                action = getHumanAction(applicable, predictions);
            }

            if (action == null) {
                throw new RuntimeException("somehow failed to pick an action");
            }

            System.out.println(currentAgent + ": " + action.getSignature() + "\n");
            Action.UpdatedStateAndModels transitionResult = action.transition(currentState, models);
            currentState = transitionResult.getState();
            models = transitionResult.getModels();
            depth += 1;
        }
        

    }

    private static void displayState(Domain domain, NDState humanPerspective) {
        System.out.println("STATE:");
        for (FluentAtom atom : domain.getAllAtoms()) {
            if (humanPerspective.necessarily(atom)) {
                System.out.println("\t" + atom);
            }
            else if (humanPerspective.possibly(atom)) {
                System.out.println("\tpossibly " + atom);
            }
        }
        System.out.println("\n");
    }


    private static Action getHumanAction(Set<Action> applicable, Set<Action> predictions) {
        
        Scanner stdin = new Scanner(System.in);

        System.out.println("Pick an action. \"*\" indicates model predicitons.");

        List<Action> options = new ArrayList<>(applicable);
        for (Integer i = 0; i < options.size(); i++) {
            System.out.print(predictions.contains(options.get(i)) ? "* " : "  ");
            System.out.println(i + ": " + options.get(i).getSignatureWithActor());
        }

        Integer selection = -1;
        while (selection < 0 || selection >= options.size()) {
            selection = stdin.nextInt();
        }
        return options.get(selection);
    }

}


