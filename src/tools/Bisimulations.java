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

        vending();
        printers();
    }


    private static void vending() {

        String agent = "agent_name";

        EpistemicState vending_s = vendingS(agent);
        EpistemicState vending_t = vendingT(agent);
        EpistemicState vending_u = vendingU(agent);


        System.out.println("is S equivalent to T? should be True:");
        System.out.println(vending_s.equivalent(vending_t));

        System.out.println("is S equivalent to U? should be False:");
        System.out.println(vending_s.equivalent(vending_u));

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




        

    public static void printers() {

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
        printerBelief.put("agent", printerRelation);

        KripkeStructure printerKripke = new KripkeStructure(printerWorlds, printerBelief, printerBelief);

        System.out.println("BEFORE:");
        System.out.println(printerKripke);

        printerKripke.reduce();

        System.out.println("AFTER:");
        System.out.println(printerKripke);
    }


}
