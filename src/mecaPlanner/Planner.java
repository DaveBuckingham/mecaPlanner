package mecaPlanner;

import mecaPlanner.state.EpistemicState;
import mecaPlanner.formulae.*;
import mecaPlanner.models.*;
import mecaPlanner.search.Search;

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


    public static void main(String args[]) {

        long startTime = System.currentTimeMillis();

        Log.setThreshold("info");
        //Log.setThreshold("debug");


        // PROCESS COMMAND LINE ARGUMENTS
        // WE EXECT A SINGLE .depl FILE WITH THE PROBLEM DESCRIPTION
        // AND AN OPTIONAL .plan FILE NAME WHERE WE SHOULD WRITE THE SOLUTION

        String deplFileName = null;
        String planFileName = null;

        if (args.length < 1 || args.length > 2) {
            throw new RuntimeException("expected 1 or 2 args: a .depl file, and an optional .plan file.");
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





        Log.info("loading domain");


        DeplToProblem visitor     = new DeplToProblem();
        Problem problem = visitor.buildProblem(deplFileName);

        //System.out.println(problem.getDomain().getNonPassiveAgents());
        //System.out.println(problem.getSystemAgentIndex());
        //System.exit(1);


        Log.info("done loading problem");

        Log.info("starting search");

        long startSearchTime = System.currentTimeMillis();


        Search search = new Search();

        Solution solution = search.findSolution(problem);

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




}



