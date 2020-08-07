package mecaPlanner;

import mecaPlanner.state.EpistemicState;
import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.Relation;
import mecaPlanner.state.World;
import mecaPlanner.state.Initialize;
import mecaPlanner.actions.Action;
import mecaPlanner.formulae.FluentAtom;
import mecaPlanner.models.*;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import java.io.File;
import java.io.FileWriter;

import java.io.IOException;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import depl.*;

public class Test {


    public static void main(String args[]) {

        if (args.length < 1) {
            System.out.println("missing arg");
            return;
        }

        if (args[0].equals("rss")) {
            rss();
        }
        else if (args[0].equals("bisimulations")) {
            testBisimulations();
        }
        else if (args[0].equals("show-actions")) {
            if (args.length < 2) {
                System.out.println("actions test requires a depl file");
                return;
            }
            String deplFile = args[1];
            showActions(deplFile);

        }
        else if (args[0].equals("run-actions")) {
            if (args.length < 2) {
                System.out.println("actions test requires a depl file");
                return;
            }
            String deplFile = args[1];
            List<String> actionNames = new ArrayList<>();
            if (args.length > 2) {
                actionNames.addAll(Arrays.asList(Arrays.copyOfRange(args, 2, args.length)));
            }
            runActions(deplFile, actionNames);
        }
        else {
            System.out.println("unknown arg: " + args[0]);
        }
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


        //printers();

    }


    // p. 452
    private static EpistemicState vendingS(String agent) {

        World world_s0 = new World("s0", "pay");
        World world_s1 = new World("s1");
        World world_s2 = new World("s2", "beer");
        World world_s3 = new World("s3", "soda");

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
        World world_t0 = new World("t0", "pay");
        World world_t1 = new World("t1");
        World world_t2 = new World("t2", "beer");
        World world_t3 = new World("t3", "beer");
        World world_t4 = new World("t4", "soda");

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


        World world_u0 = new World("u0", "pay");
        World world_u1 = new World("u1");
        World world_u2 = new World("u2");
        World world_u3 = new World("u3", "beer");
        World world_u4 = new World("u4", "soda");

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

        World world_rrr = new World("rrr", "3");
        World world_prr = new World("prr", "2");
        World world_rpr = new World("rpr", "2");
        World world_rrp = new World("rrp", "2");
        World world_ppr = new World("ppr", "1");
        World world_ppp = new World("ppp", "0");
        World world_rpp = new World("rpp", "1");
        World world_prp = new World("prp", "1");

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

        printerKripke.reduce();

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
            System.out.println("Checking relation..." + checkRelations(domain, s));
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
            System.out.println("check relations: " + checkRelations(domain, currentState));
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
        Set<FluentAtom> world1Atoms = new HashSet<>();
        world1Atoms.add(new FluentAtom("atom1"));
        World world1 = new World(world1Atoms);

        Set<FluentAtom> world2Atoms = new HashSet<>();
        world2Atoms.add(new FluentAtom("atom2"));
        World world2 = new World(world2Atoms);

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
        Set<FluentAtom> world1Atoms = new HashSet<>();
        world1Atoms.add(new FluentAtom("closed"));
        world1Atoms.add(new FluentAtom("looking", "alice"));
        world1Atoms.add(new FluentAtom("looking", "bob"));
        World world1 = new World(world1Atoms);

        Set<FluentAtom> world2Atoms = new HashSet<>();
        world2Atoms.add(new FluentAtom("closed"));
        world2Atoms.add(new FluentAtom("looking", "alice"));
        world2Atoms.add(new FluentAtom("looking", "bob"));
        world2Atoms.add(new FluentAtom("locked"));
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
        Set<FluentAtom> world1Atoms = new HashSet<>();
        world1Atoms.add(new FluentAtom("tails"));
        world1Atoms.add(new FluentAtom("key"));
        World world1 = new World(world1Atoms);

        Set<FluentAtom> world2Atoms = new HashSet<>();
        world2Atoms.add(new FluentAtom("key"));
        World world2 = new World(world2Atoms);

        Set<FluentAtom> world3Atoms = new HashSet<>();
        world3Atoms.add(new FluentAtom("tails"));
        World world3 = new World(world3Atoms);

        World world4 = new World(new HashSet<FluentAtom>());

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
        World world0 = new World(new HashSet<FluentAtom>());

        Set<FluentAtom> world1Atoms = new HashSet<>();
        world1Atoms.add(new FluentAtom("raining"));
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









    // u implies some u->v
    public static boolean checkSerial(Relation relation, Set<World> worlds) {
        for (World u : worlds) {
            if (relation.getToWorlds(u).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // u->v and v->z implies u->z
    public static boolean checkTransitive(Relation relation, Set<World> worlds) {
        for (World u : worlds) {
            for (World v : worlds) {
                for (World z : worlds) {
                    if (relation.isConnected(u, v) && relation.isConnected(v, z)) {
                        if (!relation.isConnected(u, z)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    // u->v and u->z implies v->z
    public static boolean checkEuclidean(Relation relation, Set<World> worlds) {
        for (World u : worlds) {
            for (World v : worlds) {
                for (World z : worlds) {
                    if (relation.isConnected(u, v) && relation.isConnected(u, z)) {
                        if (!relation.isConnected(v, z)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean checkReflexive(Relation relation, Set<World> worlds) {
        for (World u : worlds) {
            if (!relation.isConnected(u, u)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkSymmetric(Relation relation, Set<World> worlds) {
        for (World u : worlds) {
            for (World v : worlds) {
                if (relation.isConnected(u, v)) {
                    if (!relation.isConnected(v, u)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean checkRelations(Domain domain, EpistemicState state) {
        Set<World> worlds = state.getKripke().getWorlds();
        for (String agent : domain.getAgents()) {
            Relation relation = state.getKripke().getBeliefRelations().get(agent);
            if (!checkSerial(relation, worlds)) {
                System.out.println("failed check: serial belief for agent " + agent);
                return false;
            }
            if (!checkTransitive(relation, worlds)) {
                System.out.println("failed check: transitive belief for agent " + agent);
                return false;
            }
            if (!checkEuclidean(relation, worlds)) {
                System.out.println("failed check: euclidean belief for agent " + agent);
                return false;
            }

            relation = state.getKripke().getKnowledgeRelations().get(agent);
            if (!checkReflexive(relation, worlds)) {
                System.out.println("failed check: reflexive knowledge for agent " + agent);
                return false;
            }
            if (!checkTransitive(relation, worlds)) {
                System.out.println("failed check: transitive knowledge for agent " + agent);
                return false;
            }
            if (!checkSymmetric(relation, worlds)) {
                System.out.println("failed check: symmetric knowledge for agent " + agent);
                return false;
            }
        }
        return true;
    }






}
