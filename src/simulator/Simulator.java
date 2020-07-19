package simulator;

import mecaPlanner.state.NDState;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.World;
import mecaPlanner.state.Relation;
import mecaPlanner.models.BurglerModel;
import mecaPlanner.agents.*;
import mecaPlanner.actions.*;
import mecaPlanner.Perspective;
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

    GeneralFormula goal;

    public static void main(String args[]) {


        String deplFileName = null;
        String planFileName = null;

        if (args.length != 2) {
            throw new RuntimeException("expected two args: a .depl file and a .plan file.");
        }

        for (String arg : args) {
            if (arg.matches(".*\\.depl")) {
                if (deplFileName != null) {
                    throw new RuntimeException("expected a single depl file.");
                }
                deplFileName = arg;
            }
            else if (arg.matches(".*\\.plan")) {
                if (planFileName != null) {
                    throw new RuntimeException("expected a single plan file.");
                }
                planFileName = arg;
            }
 
        }

        if (deplFileName == null) {
            throw new RuntimeException("expected a single depl file.");
        }

        if (planFileName == null) {
            throw new RuntimeException("expected a single plan file.");
        }


        CharStream inputStream = null;
        try {
            inputStream = CharStreams.fromFileName(deplFileName);
        }
        catch (IOException e) {
            System.err.println("failed to read input depl file: " + e.getMessage());
            System.exit(1);
        }


        DeplLexer lexer          = new DeplLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        DeplParser parser        = new DeplParser(tokens);
        ParseTree tree           = parser.init();

        DeplToDomain visitor     = new DeplToDomain();

        visitor.buildDomain(tree);

        Log.info("done loading domain");

        EpistemicState startState = null;
        try {
            startState = Initialize.constructState(true);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        
        Solution startSolution = null;
        try {
            FileInputStream fileIn = new FileInputStream(planFileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            startSolution = (Solution) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
           i.printStackTrace();
           return;
        } catch (ClassNotFoundException c) {
           System.out.println("Employee class not found");
           c.printStackTrace();
           return;
        }

        GeneralFormula goal = new GeneralFormulaAnd(Domain.getGoals());

        Scanner stdin = new Scanner(System.in);



        Set<Solution> solutions = new HashSet<>();
        solutions.add(startSolution);
        EpistemicState currentState = startState;
        int depth = 0;

        while (!solutions.isEmpty()) {

            System.out.println("STATE:");
            System.out.println(currentState);

            Agent agent = Domain.agentAtDepth(depth);

            Perspective perspective = new Perspective(currentState, agent);

            Action action = null;

            if (agent instanceof SystemAgent) {
                boolean foundPerspective = false;
                for (Solution s : solutions) {
                    if (perspective.equals(s.getPerspective())) {
                        action = s.getAction();
                        System.out.println("SYSTEM ACTION: " + action.getSignature() + "\n");
                        solutions = s.getChildren();
                        foundPerspective = true;
                        break;
                    }
                }
                assert(foundPerspective);
            }

            else {
                EnvironmentAgent eAgent = (EnvironmentAgent) agent;
                List<Action> options = new ArrayList<>();
                Integer index = 0;
                for (Action a : eAgent.getModel().getPrediction(perspective.getAgentView(), eAgent)) {
                    System.out.println(index.toString() + ": " + a.getSignatureWithActor());
                    options.add(a);
                    index += 1;
                }
                Integer selection = -1;
                while (selection < 0 || selection >= index) {
                    selection = stdin.nextInt();
                }
                action = options.get(selection);
                System.out.println("ENVIRONMENT ACTION: " + action.getSignature() + "\n");
            }

            assert (action != null);

            currentState = action.transition(currentState);

            depth += 1;
        }
        




    }


}


