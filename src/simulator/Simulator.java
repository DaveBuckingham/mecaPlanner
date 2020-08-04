package simulator;

import mecaPlanner.state.NDState;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.World;
import mecaPlanner.state.Relation;
import mecaPlanner.models.BurglerModel;
import mecaPlanner.models.*;
import mecaPlanner.actions.*;
import mecaPlanner.search.Perspective;
import mecaPlanner.state.Initialize;
import mecaPlanner.formulae.*;
import mecaPlanner.Log;
import mecaPlanner.Domain;
import mecaPlanner.Solution;
import mecaPlanner.Test;

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

import java.io.IOException;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import depl.*;


public class Simulator {


    public static void main(String args[]) {



        if (args.length != 1) {
            throw new RuntimeException("expected single depl file parameter.");
        }

        String planFileName = args[0];

        DeplToProblem visitor = new DeplToProblem();
        Problem problem = visitor.buildProblem(deplFile);
        Domain domain = problem.getDomain();

        
        Scanner stdin = new Scanner(System.in);


        Set<Solution> solutions = new HashSet<>();
        EpistemicState currentState = problem.getStartState();
        Map<String, Model> models = problem.getStartingModels();
        GeneralFormula goal = problem.getGoal();
        int depth = 0;
        Solution plan = new Solution;


        while(!goal.holds(estate, depth)) {

            String currentAgent = domain.agentAtDepth(depth);

            displayState(domain, estate.getBeliefPerspective(currentAgent));

            // ROBOT'S TURN
            if (depth == problem.getSystemAgentIndex()) {
                Perspective robotPerspective = new Perspective(currentState, currentAgent);
                boolean foundPerspective = false;
                for (Solution s : solutions) {
                    if (perspective.equals(s.getPerspective())) {
                        action = s.getAction();
                        System.out.println(agent + ": " + action.getSignature() + "\n");
                        solutions = s.getChildren();
                        foundPerspective = true;
                        break;
                    }
                }
                assert(foundPerspective);
            }

            // HUMAN'S TURN
            else {
                List<Action> options = new ArrayList<>();
                Integer index = 0;
                for (Action a : models.get(agent).getPrediction(perspective.getAgentView())) {
                    System.out.println(index.toString() + ": " + a.getSignatureWithActor());
                    options.add(a);
                    index += 1;
                }
                assert(options.size() > 0);
                Integer selection = -1;
                if (options.size() > 1) {
                    while (selection < 0 || selection >= index) {
                        selection = stdin.nextInt();
                    }
                }
                else {
                    selection = 0;
                }
                action = options.get(selection);
                System.out.println(agent + ": " + action.getSignature() + "\n");
            }

            assert (action != null);

            Action.UpdatedStateAndModels transitionResult = action.transition(currentState, models);
            currentState = transitionResult.getState();
            models = transitionResult.getModels();

            depth += 1;
        }
        




    }

    private void displayState(Domain domain, NDState humanPerspective) {
        System.out.println("STATE:");
        for (FluentAtom atom : domain) {
            if (humanPerspective.necessarily(atom)) {
                System.out.println("\t" + atom);
            }
            else if (humanPerspective.possibly(atom)) {
                System.out.println("\tpossibly " + atom);
            }
        }
    }

    private void getHumanAction(Perspective perspective) {
        System.out.println("STATE:");
        System.out.println(currentState.getDesignatedWorld());
    }

}


