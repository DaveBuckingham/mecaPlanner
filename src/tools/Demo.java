package tools;

import mecaPlanner.*;
import mecaPlanner.state.*;
import depl.*;


public class Demo {

    public static void main(String args[]) {
        onticExample();
        sensingExample();
        announcementExample();
    }

    private static void onticExample() {
        System.out.println("ONTIC EXAMPLE:\n");
        DeplToProblem deplParser = new DeplToProblem();
        Problem problem = deplParser.buildProblem("problems/kr/ontic.depl");
        Domain domain = problem.getDomain();

        EpistemicState startState = problem.getStartState();
        System.out.println(startState);
        pause();

        Action action =  domain.getActionBySignature("alice", "openDoor()");
        System.out.println(action);
        pause();

        EpistemicState endState = action.transition(startState);
        System.out.println(endState);
        pause();
    }

    private static void sensingExample() {
        System.out.println("SENSING EXAMPLE:\n");
        DeplToProblem deplParser = new DeplToProblem();
        Problem problem = deplParser.buildProblem("problems/kr/sensing.depl");
        Domain domain = problem.getDomain();

        EpistemicState startState = problem.getStartState();
        System.out.println(startState);
        pause();

        Action action =  domain.getActionBySignature("alice", "lookBox()");
        System.out.println(action);
        pause();
 
        EpistemicState endState = action.transition(startState);
        System.out.println(endState);
        pause();
    }


    private static void announcementExample() {
        System.out.println("ANNOUNCEMENT EXAMPLE:\n");
        DeplToProblem deplParser = new DeplToProblem();
        Problem problem = deplParser.buildProblem("problems/kr/announcement.depl");
        Domain domain = problem.getDomain();

        EpistemicState startState = problem.getStartState();
        System.out.println(startState);
        pause();

        Action action =  domain.getActionBySignature("alice", "tellRaining()");
        System.out.println(action);
        pause();
 
        EpistemicState endState = action.transition(startState);
        System.out.println(endState);
        pause();
    }




    private static void pause() {
        try {
            System.in.read();
        }
        catch (java.io.IOException e){
            System.out.println("yikes");
        }
    }
}


