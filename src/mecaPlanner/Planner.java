package mecaPlanner;

import mecaPlanner.state.Initialize;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.formulae.*;
import mecaPlanner.actions.*;
import mecaPlanner.models.*;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.util.Properties;

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

public class Planner {

    Search search;

    static Boolean assumeCommonKnowledge = null;
    static Boolean reduceStates = null;
    static Boolean printSolution = null;



    public static void main(String args[]) {

        long startTime = System.currentTimeMillis();


        String deplFileName = null;
        String configFileName = null;
        String planFileName = null;

        if (args.length < 1 || args.length > 3) {
            throw new RuntimeException("expected 1 to 3 args: a .depl file, an optional .conf file, and an optional .plan file.");
        }

        for (String arg : args) {
            if (arg.matches(".*\\.conf")) {
                if (configFileName != null) {
                    throw new RuntimeException("expected a single config file or none.");
                }
                configFileName = arg;
            }
            else if (arg.matches(".*\\.depl")) {
                if (deplFileName != null) {
                    throw new RuntimeException("expected a single depl file.");
                }
                deplFileName = arg;
            }
            else if (arg.matches(".*\\.plan")) {
                if (planFileName != null) {
                    throw new RuntimeException("expected a single plan filename.");
                }
                planFileName = arg;
            }
            else {
                throw new RuntimeException("invalid argument: " + arg);
            }

        }

        if (deplFileName == null) {
            throw new RuntimeException("expected a single depl file.");
        }


        Properties prop = new Properties();

        if (configFileName != null)  {
            InputStream is = null;
            try {
                is = new FileInputStream(configFileName);
            } catch (FileNotFoundException ex) {
                throw new RuntimeException("config file not found: " + configFileName);
            }
            try {
                prop.load(is);
            } catch (IOException ex) {
                System.out.println("failed to read config file...using defaults");
            }
        }

        Planner.assumeCommonKnowledge = Boolean.parseBoolean(prop.getProperty("assumeCommonKnowledge", "true"));
        Planner.reduceStates = Boolean.parseBoolean(prop.getProperty("reduceStates", "false"));
        Planner.printSolution = Boolean.parseBoolean(prop.getProperty("printSolution", "true"));
        Log.setThreshold(prop.getProperty("logThreshold", "info"));
        Log.setOutput(prop.getProperty("logOutput", "stdout"));


        Log.info("loading domain");

//        CharStream inputStream = null;
//        try {
//            inputStream = CharStreams.fromFileName(deplFileName);
//        }
//        catch (IOException e) {
//            System.err.println("failed to read input depl file: " + e.getMessage());
//            System.exit(1);
//        }
//
//
//        DeplLexer lexer          = new DeplLexer(inputStream);
//        CommonTokenStream tokens = new CommonTokenStream(lexer);
//        DeplParser parser        = new DeplParser(tokens);
//        ParseTree tree           = parser.init();

        DeplToDomain visitor     = new DeplToDomain();
        visitor.buildDomain(deplFileName);

//        visitor.buildDomain(tree);

        Log.info("done loading domain");

        //Log.info("initializing models");
        //for (EnvironmentAgent agent : Domain.getEnvironmentAgents()) {
        //    agent.getModel().setup(agent);
        //}
        //Log.info("done initializing models");

        Log.info("building start state");

        Planner planner = new Planner();

        Log.info("done building start state");

        Log.info("starting search");

        long startSearchTime = System.currentTimeMillis();

        Set<Solution> solutions = null;

        try {
            solutions = planner.plan();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        // WE NEED A BETTER SYSTEM THAN THIS
        assert(solutions.size() == 1);

        Solution solution = solutions.iterator().next();

        System.out.println(solution);

        if (planFileName != null) {
            try {
                FileOutputStream fileOut = new FileOutputStream(planFileName);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(solution);
                out.close();
                fileOut.close();
            } catch (IOException i) {
                i.printStackTrace();
            }
        }




        long endTime = System.currentTimeMillis();

        double initTime = Double.valueOf(startSearchTime - startTime) / 1000;
        double searchTime = Double.valueOf(endTime - startSearchTime) / 1000;
        double totalTime = Double.valueOf(endTime - startTime) / 1000;

        Log.debug("INITIALIZE: " + initTime + "s");
        Log.debug("    SEARCH: " + searchTime + "s");
        Log.debug("     TOTAL: " + totalTime + "s");

    }



    public Planner() {
        this.search = new Search();

    }


    public Set<Solution> plan() throws Exception {

        EpistemicState startState = null;
        try {
            startState = Initialize.constructState(Planner.assumeCommonKnowledge);
            this.search = new Search();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        return this.search.findSolution(startState, new GeneralFormulaAnd(Domain.getGoals()));
    }

}



