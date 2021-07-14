package tools;

import mecaPlanner.state.*;
import mecaPlanner.models.Model;
import mecaPlanner.Action;
import mecaPlanner.search.Perspective;
import mecaPlanner.search.Search;
import mecaPlanner.formulae.beliefFormulae.BeliefFormula;
import mecaPlanner.formulae.localFormulae.*;
import mecaPlanner.Domain;
import mecaPlanner.Solution;
import mecaPlanner.Problem;
import mecaPlanner.Log;
import mecaPlanner.Transition;
import java.util.Arrays;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import java.io.File;
import java.io.FileWriter;

import java.io.IOException;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import depl.*;

// TEST THE BISIMULATION CODE USING EXAMPLES FROM THE TEXTBOOK

public class Bisimulations {


    public static void main(String args[]) {

        Log.setThreshold(Log.Level.DEBUG);

        testBisimulations();
    }


    private static void testBisimulations() {

        String agent = "agent_name";
        //Domain.addPassiveAgent(agent);

        EpistemicState vending_s = vendingS(agent);
        EpistemicState vending_t = vendingT(agent);
        EpistemicState vending_u = vendingU(agent);


        System.out.println("is S equivalent to T? should be True:");
        System.out.println(vending_s.equivalent(vending_t));

        System.out.println("is S equivalent to U? should be False:");
        System.out.println(vending_s.equivalent(vending_u));


        printers("agent");

    }


    // p. 452
    private static EpistemicState vendingS(String agent) {

        World world_s0 = new World("s0", new Fluent("pay"));
        World world_s1 = new World("s1");
        World world_s2 = new World("s2", new Fluent("beer"));
        World world_s3 = new World("s3", new Fluent("soda"));

        Set<World> worlds1 = new HashSet<>(Arrays.asList(world_s0, world_s1, world_s2, world_s3));

        Relation relation1 = new Relation();
        relation1.connect(world_s0, world_s1);
        relation1.connect(world_s1, world_s2);
        relation1.connect(world_s1, world_s3);
        relation1.connect(world_s2, world_s0);
        relation1.connect(world_s3, world_s0);

        Map<String, Relation> belief1 = new HashMap<>();
        belief1.put(agent, relation1);

        return new EpistemicState(new KripkeStructure(worlds1, belief1, belief1), world_s0);
    }

    private static EpistemicState vendingT(String agent) {
        World world_t0 = new World("t0", new Fluent("pay"));
        World world_t1 = new World("t1");
        World world_t2 = new World("t2", new Fluent("beer"));
        World world_t3 = new World("t3", new Fluent("beer"));
        World world_t4 = new World("t4", new Fluent("soda"));

        Set<World> worlds2 = new HashSet<>(Arrays.asList(world_t0, world_t1, world_t2, world_t3, world_t4));

        Relation relation2 = new Relation();
        relation2.connect(world_t0, world_t1);
        relation2.connect(world_t1, world_t2);
        relation2.connect(world_t1, world_t3);
        relation2.connect(world_t1, world_t4);
        relation2.connect(world_t2, world_t0);
        relation2.connect(world_t3, world_t0);
        relation2.connect(world_t4, world_t0);

        Map<String, Relation> belief2 = new HashMap<>();
        belief2.put(agent, relation2);

        return new EpistemicState(new KripkeStructure(worlds2, belief2, belief2), world_t0);
    }



    private static EpistemicState vendingU(String agent) {


        World world_u0 = new World("u0", new Fluent("pay"));
        World world_u1 = new World("u1");
        World world_u2 = new World("u2");
        World world_u3 = new World("u3", new Fluent("beer"));
        World world_u4 = new World("u4", new Fluent("soda"));

        Set<World> worlds2 = new HashSet<>(Arrays.asList(world_u0, world_u1, world_u2, world_u3, world_u4));

        Relation relation2 = new Relation();
        relation2.connect(world_u0, world_u1);
        relation2.connect(world_u0, world_u2);
        relation2.connect(world_u1, world_u3);
        relation2.connect(world_u2, world_u4);
        relation2.connect(world_u3, world_u0);
        relation2.connect(world_u4, world_u0);

        Map<String, Relation> belief2 = new HashMap<>();
        belief2.put(agent, relation2);

        return new EpistemicState(new KripkeStructure(worlds2, belief2, belief2), world_u0);

    }




        

    public static void printers(String agent) {

        World world_rrr = new World("rrr", new Fluent("3"));
        World world_prr = new World("prr", new Fluent("2"));
        World world_rpr = new World("rpr", new Fluent("2"));
        World world_rrp = new World("rrp", new Fluent("2"));
        World world_ppr = new World("ppr", new Fluent("1"));
        World world_ppp = new World("ppp", new Fluent("0"));
        World world_rpp = new World("rpp", new Fluent("1"));
        World world_prp = new World("prp", new Fluent("1"));

        Set<World> printerWorlds = new HashSet<>(Arrays.asList(world_rrr,
                                                                world_prr,
                                                                world_rpr,
                                                                world_rrp,
                                                                world_ppr,
                                                                world_ppp,
                                                                world_rpp,
                                                                world_prp));

        Relation printerRelation = new Relation();

        printerRelation.connectBack(world_rrr, world_prr);
        printerRelation.connectBack(world_rrr, world_rpr);
        printerRelation.connectBack(world_rrr, world_rrp);

        printerRelation.connectBack(world_prr, world_ppr);
        printerRelation.connectBack(world_prr, world_prp);
        printerRelation.connectBack(world_rpr, world_ppr);
        printerRelation.connectBack(world_rpr, world_rpp);
        printerRelation.connectBack(world_rrp, world_rpp);
        printerRelation.connectBack(world_rrp, world_prp);
        
        printerRelation.connectBack(world_ppp, world_ppr);
        printerRelation.connectBack(world_ppp, world_rpp);
        printerRelation.connectBack(world_ppp, world_prp);

        Map<String, Relation> printerBelief = new HashMap<>();
        printerBelief.put(agent, printerRelation);

        KripkeStructure printerKripke = new KripkeStructure(printerWorlds, printerBelief, printerBelief);

        System.out.println(printerKripke);

        printerKripke.reduce(world_rrr);

        System.out.println(printerKripke);
    }



    private static void showActions(String deplFile) {
        DeplToProblem visitor = new DeplToProblem();
        Problem problem = visitor.buildProblem(deplFile);
        Domain domain = problem.getDomain();
        for (Action action : domain.getAllActions()) {
            System.out.println(action);
        }
    }







    private static void runActions(String deplFile, List<String> actionNames) {


        DeplToProblem visitor = new DeplToProblem();
        Problem problem = visitor.buildProblem(deplFile);
        Domain domain = problem.getDomain();

        List<Action> inputActions = new ArrayList<>();

        for (String inputActionSignature : actionNames) {

            boolean foundAction = false;
            for (Action action : domain.getAllActions()) {
                if (inputActionSignature.equals(action.getSignature()) ||
                    inputActionSignature.equals(action.getSignatureWithActor())) {

                    foundAction = true;
                    inputActions.add(action);
                    break;
                }
            }
            if (!foundAction) {
                System.out.println("action not found: " + inputActionSignature);
                System.exit(1);
            }
        }


        System.out.println("START STATES:");
        for (EpistemicState s : problem.getStartStates()) {
            System.out.println(s);
            System.out.println("Checking relation..." + s.getKripke().checkRelations());
        }



        EpistemicState currentState = problem.getStartStates().iterator().next();

        for (Action action : inputActions) {
            System.out.print("ACTION: ");
            System.out.println(action.getSignatureWithActor());
            try {
                Action.UpdatedStateAndModels result = action.transition(currentState, problem.getStartingModels());
                currentState = result.getState();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }

            System.out.println(currentState);
            System.out.println("Checking relation..." + currentState.getKripke().checkRelations());
        }

    }


    private static void rss() {

        System.out.println("rss test not fully implemented");

//        EpistemicState onticStartState = constructOnticExampleStart();
//        Action onticAction = constructOnticExampleAction();
//        EpistemicState onticResultState = onticAction.transition(onticStartState);
//        System.out.println("ONTIC START STATE:");
//        System.out.println(onticStartState);
//        System.out.println("ONTIC ACTION:");
//        System.out.println(onticAction);
//        System.out.println("ONTIC RESULT STATE:");
//        System.out.println(onticResultState);
//
//        EpistemicState sensingStartState = constructOnticExampleStart();
//        Action sensingAction = constructOnticExampleAction();
//        EpistemicState sensingResultState = sensingAction.transition(sensingStartState);
//        System.out.println("SENSING START STATE:");
//        System.out.println(sensingStartState);
//        System.out.println("SENSING ACTION:");
//        System.out.println(sensingAction);
//        System.out.println("SENSING RESULT STATE:");
//        System.out.println(sensingResultState);
//
//        EpistemicState announcementStartState = constructOnticExampleStart();
//        Action announcementAction = constructOnticExampleAction();
//        EpistemicState announcementResultState = announcementAction.transition(announcementStartState);
//        System.out.println("ANNOUNCEMENT START STATE:");
//        System.out.println(announcementStartState);
//        System.out.println("ANNOUNCEMENT ACTION:");
//        System.out.println(announcementAction);
//        System.out.println("ANNOUNCEMENT RESULT STATE:");
//        System.out.println(announcementResultState);
    }

    public static EpistemicState constructTestState() {
        World world1 = new World(new Fluent("atom1"));

        World world2= new World(new Fluent("atom2"));

        Set<World> worlds = new HashSet<>();
        worlds.add(world1);
        worlds.add(world2);

        String agent_r = "robot1";
        String agent_h   = "human1";

        Set<String> agents = new HashSet<>();
        agents.add(agent_r);
        agents.add(agent_h);

        Map<String, Relation> beliefRelations = new HashMap<>();
        for (String a : agents) {
            beliefRelations.put(a, new Relation());
        }

        Map<String, Relation> knowledgeRelations = new HashMap<>();
        for (String a : agents) {
            knowledgeRelations.put(a, new Relation());
        }

        knowledgeRelations.get(agent_r).connect(world1, world1);
        knowledgeRelations.get(agent_r).connect(world2, world2);

        knowledgeRelations.get(agent_h).connect(world1, world1);
        knowledgeRelations.get(agent_h).connect(world2, world2);
        knowledgeRelations.get(agent_h).connect(world1, world2);
        knowledgeRelations.get(agent_h).connect(world2, world1);

        beliefRelations.get(agent_r).connect(world1, world1);
        beliefRelations.get(agent_r).connect(world2, world2);

        beliefRelations.get(agent_h).connect(world2, world2);
        beliefRelations.get(agent_h).connect(world1, world2);

        KripkeStructure kripke = new KripkeStructure(worlds, beliefRelations, knowledgeRelations);

        return new EpistemicState(kripke, world1);
    }





    public static EpistemicState constructOnticExampleStart() {
        Set<Fluent> world1Atoms = new HashSet<>();
        world1Atoms.add(new Fluent("closed"));
        world1Atoms.add(new Fluent("looking", "alice"));
        world1Atoms.add(new Fluent("looking", "bob"));
        World world1 = new World(world1Atoms);

        Set<Fluent> world2Atoms = new HashSet<>();
        world2Atoms.add(new Fluent("closed"));
        world2Atoms.add(new Fluent("looking", "alice"));
        world2Atoms.add(new Fluent("looking", "bob"));
        world2Atoms.add(new Fluent("locked"));
        World world2 = new World(world2Atoms);

        Set<World> worlds = new HashSet<>();
        worlds.add(world1);
        worlds.add(world2);

        String alice = "alice";
        String bob   = "bob";
        String carol = "carol";

        Set<String> agents = new HashSet<>();
        agents.add(alice);
        agents.add(bob);
        agents.add(carol);


        Map<String, Relation> beliefRelations = new HashMap<>();
        for (String a : agents) {
            beliefRelations.put(a, new Relation());
        }

        Map<String, Relation> knowledgeRelations = new HashMap<>();
        for (String a : agents) {
            knowledgeRelations.put(a, new Relation());
        }


        knowledgeRelations.get(alice).connect(world1, world1);
        knowledgeRelations.get(alice).connect(world2, world2);

        knowledgeRelations.get(bob).connect(world1, world1);
        knowledgeRelations.get(bob).connect(world2, world2);
        knowledgeRelations.get(bob).connect(world1, world2);
        knowledgeRelations.get(bob).connect(world2, world1);

        knowledgeRelations.get(carol).connect(world1, world1);
        knowledgeRelations.get(carol).connect(world2, world2);

        beliefRelations.get(alice).connect(world1, world1);
        beliefRelations.get(alice).connect(world2, world2);

        beliefRelations.get(bob).connect(world2, world2);
        beliefRelations.get(bob).connect(world1, world2);

        beliefRelations.get(carol).connect(world1, world1);
        beliefRelations.get(carol).connect(world2, world2);

        KripkeStructure kripke = new KripkeStructure(worlds, beliefRelations, knowledgeRelations);

        return new EpistemicState(kripke, world1);
    }


    private static EpistemicState constructSensingExampleStart() {
        Set<Fluent> world1Atoms = new HashSet<>();
        world1Atoms.add(new Fluent("tails"));
        world1Atoms.add(new Fluent("key"));
        World world1 = new World(world1Atoms);

        Set<Fluent> world2Atoms = new HashSet<>();
        world2Atoms.add(new Fluent("key"));
        World world2 = new World(world2Atoms);

        Set<Fluent> world3Atoms = new HashSet<>();
        world3Atoms.add(new Fluent("tails"));
        World world3 = new World(world3Atoms);

        World world4 = new World(new HashSet<Fluent>());

        Set<World> worlds = new HashSet<>();
        worlds.add(world1);
        worlds.add(world2);
        worlds.add(world3);
        worlds.add(world4);

        String alice = "alice";
        String bob   = "bob";

        Set<String> agents = new HashSet<>();
        agents.add(alice);
        agents.add(bob);


        Map<String, Relation> beliefRelations = new HashMap<>();
        for (String a : agents) {
            beliefRelations.put(a, new Relation());
        }

        Map<String, Relation> knowledgeRelations = new HashMap<>();
        for (String a : agents) {
            knowledgeRelations.put(a, new Relation());
        }



        knowledgeRelations.get(alice).connect(world1, world1);
        knowledgeRelations.get(alice).connect(world1, world2);
        knowledgeRelations.get(alice).connect(world2, world2);
        knowledgeRelations.get(alice).connect(world2, world1);
        knowledgeRelations.get(alice).connect(world3, world3);
        knowledgeRelations.get(alice).connect(world3, world4);
        knowledgeRelations.get(alice).connect(world4, world4);
        knowledgeRelations.get(alice).connect(world4, world3);

        knowledgeRelations.get(bob).connect(world1, world1);
        knowledgeRelations.get(bob).connect(world1, world2);
        knowledgeRelations.get(bob).connect(world1, world3);
        knowledgeRelations.get(bob).connect(world1, world4);
        knowledgeRelations.get(bob).connect(world2, world2);
        knowledgeRelations.get(bob).connect(world2, world1);
        knowledgeRelations.get(bob).connect(world2, world4);
        knowledgeRelations.get(bob).connect(world2, world3);
        knowledgeRelations.get(bob).connect(world3, world3);
        knowledgeRelations.get(bob).connect(world3, world4);
        knowledgeRelations.get(bob).connect(world3, world1);
        knowledgeRelations.get(bob).connect(world3, world2);
        knowledgeRelations.get(bob).connect(world4, world4);
        knowledgeRelations.get(bob).connect(world4, world3);
        knowledgeRelations.get(bob).connect(world4, world2);
        knowledgeRelations.get(bob).connect(world4, world1);


        beliefRelations.get(alice).connect(world1, world2);
        beliefRelations.get(alice).connect(world2, world2);
        beliefRelations.get(alice).connect(world3, world4);
        beliefRelations.get(alice).connect(world4, world4);

        beliefRelations.get(bob).connect(world1, world3);
        beliefRelations.get(bob).connect(world1, world4);
        beliefRelations.get(bob).connect(world2, world4);
        beliefRelations.get(bob).connect(world2, world3);
        beliefRelations.get(bob).connect(world3, world3);
        beliefRelations.get(bob).connect(world3, world4);
        beliefRelations.get(bob).connect(world4, world4);
        beliefRelations.get(bob).connect(world4, world3);

        KripkeStructure kripke = new KripkeStructure(worlds, beliefRelations, knowledgeRelations);


        return new EpistemicState(kripke, world1);
    }


    private static EpistemicState constructAnnouncementExampleStart() {
        World world0 = new World(new HashSet<Fluent>());

        Set<Fluent> world1Atoms = new HashSet<>();
        world1Atoms.add(new Fluent("raining"));
        World world1 = new World(world1Atoms);


        Set<World> worlds = new HashSet<>();
        worlds.add(world0);
        worlds.add(world1);

        String alice = "alice";
        String bob   = "bob";
        String carol = "carol";

        Set<String> agents = new HashSet<>();
        agents.add(alice);
        agents.add(bob);
        agents.add(carol);

        Map<String, Relation> beliefRelations = new HashMap<>();
        for (String a : agents) {
            beliefRelations.put(a, new Relation());
        }

        Map<String, Relation> knowledgeRelations = new HashMap<>();
        for (String a : agents) {
            knowledgeRelations.put(a, new Relation());
        }


        knowledgeRelations.get(alice).connect(world0, world0);
        knowledgeRelations.get(bob).connect(world0, world0);
        knowledgeRelations.get(carol).connect(world0, world0);

        knowledgeRelations.get(alice).connect(world1, world1);
        knowledgeRelations.get(bob).connect(world1, world1);
        knowledgeRelations.get(carol).connect(world1, world1);

        knowledgeRelations.get(carol).connect(world0, world1);
        knowledgeRelations.get(carol).connect(world1, world0);

        beliefRelations.get(alice).connect(world0, world0);
        beliefRelations.get(bob).connect(world0, world0);
        beliefRelations.get(carol).connect(world0, world0);

        beliefRelations.get(alice).connect(world1, world1);
        beliefRelations.get(bob).connect(world1, world1);
        beliefRelations.get(carol).connect(world1, world1);

        KripkeStructure kripke = new KripkeStructure(worlds, beliefRelations, knowledgeRelations);

        return new EpistemicState(kripke, world0);
    }












}
