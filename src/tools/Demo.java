package tools;

import mecaPlanner.Action;
import mecaPlanner.Domain;
import mecaPlanner.Problem;
import mecaPlanner.state.NDState;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.formulae.booleanFormulae.BooleanAtom;
import depl.*;

// RUN THE THREE EXAMPLES FORM THE KR2020 PAPER


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

        NDState bobsPerspective = endState.getBeliefPerspective("bob");
        System.out.println("Bob's perspective:");
        System.out.println(bobsPerspective);

        System.out.println("Bob believes necessarly tails?: " + 
            bobsPerspective.necessarily(new BooleanAtom("tails")));

        System.out.println("Bob believes possibly tails?: " + 
            bobsPerspective.possibly(new BooleanAtom("tails")));

        System.out.println("Bob believes necessarily key?: " + 
            bobsPerspective.necessarily(new BooleanAtom("key")));

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






// ONTIC SCENARIO
// Three agents, A, B, and C, are in a room with a door. The door is closed
// (represented by the fluent c) but not locked (¬l). Everyone believes (and knows)
// that the door is closed.  Agents A and B are watching the door, but C is not.
// For each agent i let the fluent watch(i), express that i is watch- ing the door.
// The fluents watch(A) and watch(B) are true in all worlds, but are not shown in
// the figure due to space constraints. Agents A and C know that the door is not
// locked but agent B does not, and believes that it is locked (l).

// AFTER AGENT A PERFORMS THE ONTIC ACTION OPEN DOOR
// Agents A and B have beliefs updated to reflect that the door is now open.
// Furthermore, agent B now knows that the door is unlocked. Agent C, who was not
// looking, still believes (but does not know) that the door is closed.


// SENSING SCENARIO
// There are two agents, A and B, in a room with a box containing a coin, which
// lies tails-up (t). The box is locked and can only be opened with a key. Agent A
// has a key (k), and knows it, but believes wrongly that the coin lies heads-up
// (¬t). Agent B does not believe that the coin is heads-up or that it is tails-up,
// but believes that Agent A does not have a key. Agent A performs the sensing
// action look in box:

// AFTER AGENT A SENSES
// Agent A knows that the coin is tails-up. As a partial observer, agent B is aware
// that the action has occurred, so now knows that agent A has the key and that
// agent A knows whether the coin lies tails-up. However, agent B still does not
// know (or have any belief about) whether the coin is tails-up.

// ANNOUNCEMENT SCENARIO
// There are three agents, A, B, and C. It is not raining (¬r). Agents A and B know
// that it is not raining. Agent C believes that it is not raining but does not
// know this. Agent A performs the tell raining action:

// AFTER AGENT ANNOUNCES THAT IT IS RAINING
// Agent C now has the false belief that it is raining. Agent B, however, who knew
// that it was not raining, rejected the announcement and still does not believe
// that it is raining.
