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
import mecaPlanner.Transition;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import depl.DeplToProblem;

// RUN THE KR2021 EXAMPLES

public class KR2021 {


    public static void main(String args[]) {

        Log.setThreshold("warning");

        DeplToProblem visitor = new DeplToProblem();


        System.out.println("========== BICYCLE-1 ==========\n");

        Problem problem1 = visitor.buildProblem("problems/kr2021/bicycle1.depl");
        Domain domain1 = problem1.getDomain();
        EpistemicState state1 = problem1.getStartState();
        Action action_tell =  domain1.getActionBySignature("m", "tell()");
        Action action_look =  domain1.getActionBySignature("t", "look()");

        System.out.println("State s1:");
        System.out.println(state1);

        System.out.println("applying action " + action_tell.getSignatureWithActor() + "...\n");
        EpistemicState state1p = Transition.transition(state1, action_tell);

        System.out.println("State s1':");
        System.out.println(state1p);

        System.out.println("applying action " + action_look.getSignatureWithActor() + "...\n");
        EpistemicState state1pp = Transition.transition(state1p, action_look);

        System.out.println("State s1'':");
        System.out.println(state1pp);


        System.out.println("========== BICYCLE-2 ==========\n");

        Problem problem2 = visitor.buildProblem("problems/kr2021/bicycle2.depl");
        Domain domain2 = problem2.getDomain();
        EpistemicState state2 = problem1.getStartState();
        Action action_look2 =  domain2.getActionBySignature("t", "look2()");

        System.out.println("State s2:");
        System.out.println(state2);

        System.out.println("applying action " + action_look2.getSignatureWithActor() + "...\n");
        EpistemicState state2p = Transition.transition(state2, action_look2);

        System.out.println("State s2':");
        System.out.println(state2p);


        System.out.println("========== BICYCLE-3 ==========\n");

        Problem problem3 = visitor.buildProblem("problems/kr2021/bicycle3.depl");
        Domain domain3 = problem3.getDomain();
        EpistemicState start3 = problem3.getStartState();
        assert(start3.equals(state1p));
        Action action_look3 =  domain3.getActionBySignature("t", "look()");
        assert(action_look3.equals(action_look));

        System.out.println("State s1':");
        System.out.println(start3);

        System.out.println("applying action " + action_look.getSignatureWithActor() + "...\n");
        EpistemicState state3 = Transition.transition(start3, action_look3);

        System.out.println("State s3:");
        System.out.println(state3);




    }


}


