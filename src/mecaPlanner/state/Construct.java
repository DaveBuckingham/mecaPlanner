package mecaPlanner.state;

import mecaPlanner.formulae.beliefFormulae.*;
import mecaPlanner.formulae.localFormulae.*;
import mecaPlanner.Domain;
import mecaPlanner.Log;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;


import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

// ALGORITHM 5.1 FROM NGUYEN (2000) "Constructing the Least Models for Positive Modal Logic Programs" WITH L=KD45

public class Construct {

    static private class FormulaWorld extends World {
        Set<BeliefFormula> formulae;
        public FormulaWorld(Set<BeliefFormula> formulae) {
            super(new HashSet<Fluent>());
            this.formulae = formulae;
        }
        public FormulaWorld() {
            this(new HashSet<BeliefFormula>());
        }
        public Set<BeliefFormula> getFormulae() {
            return formulae;
        }
    }

    static private class Mode {
        private Boolean possibly;
        public Mode (Boolean p) {
            this.possibly = p;
        }
        public Boolean isPossibly() {
            return possibly;
        }
        public Boolean isNecessarily() {
            return !possibly;
        }
    }

    static private class Horn {
        List<Mode> modes;   // THIS IS "BACKWARDS", modes[0] IS THE THE RIGHT-MOST OPERATOR
        BeliefFormula head;
        Set<BeliefFormula> body;
        public Horn(List<Mode> m, BeliefFormula h, Set<BeliefFormula> b) {
            this.modes = m;
            this.head = h;
            this.body = b;
        }
        public Horn(BeliefFormula formula) {
            BeliefFormula current = formula;
            while (current != null) {
                if (current instanceof BeliefBelievesFormula) {
                    if (((BeliefBelievesFormula) f).getFormula() instanceof Fluent) {
                        return true;
                    }
                }
            }
            Collections.reverse(modes);
        }
        public Boolean hasModes() {
            return !modes.isEmtpy();
        }
        public Horn stripMode() {
            if (!hasModes()) {
                throw new RuntimeException("no mode to strip from Horn formula");
            }
            return new Horn(Arrays.copyOfRange(modes, 0, modes.length()-1), head, body);
        }
        public BeliefFormula getHead() {
            return head;
        }
        public Set<BeliefFormula> getBody() {
            return body;
        }
    }


    private static Domain domain; // DO WE NEED THIS?

    private Construct() { }

    private static Set<FormulaWorld> worlds;
    private static FormulaWorld designated;
    private static Map<String, Relation> relations;

    public static EpistemicState constructState(Domain d, Set<BeliefFormula> initialFormulae) {
        domain = d;
        worlds = new HashSet<FormulaWorld>();
        FormulaWorld tau = new FormulaWorld(initialFormulae);
        designated = tau;
        worlds.add(designated);

        FormulaWorld rho = new FormulaWorld();
        FormulaWorld omega = new FormulaWorld();
        worlds.add(rho);
        worlds.add(omega);
        for (String a : domain.getAllAgents()) {
            Relation r = new Relation();
            relations.put(a, new Relation());
            r.connect(tau,rho);
            r.connect(tau,omega);
            r.connect(rho,tau);
            r.connect(rho,omega);
            r.connect(omega,tau);
            r.connect(omega,rho);
            relations.put(a, r);
        }

        Boolean changed = true;
        while (changed) {
            for (FormulaWorld w : worlds) {
                for (BeliefFormula f : w.getFormulae()) {
                    Horn horn = parseHorn(f);
                    if (horn != null) {
                        continue;
                    }
                    BeliefFormula inner = parseNecessarily(f);
                    if (inner != null) {
                        continue;
                    }
                    inner = parsePossibly(f);
                    if (inner != null) {
                        continue;
                    }
                    throw new RuntimeException("illegal initial formula: " + f);
                }
            }
        }

        return null;
    }


    private static BeliefFormula parseNecessarily(BeliefFormula f) {
        return null;
    }
    private static BeliefFormula parsePossibly(BeliefFormula f) {
        return null;
    }

    private static Boolean isLegalModalAtom(BeliefFormula f) {
        if (f instanceof Fluent) {
            return true;
        }
        if (f instanceof BeliefBelievesFormula) {
            if (((BeliefBelievesFormula) f).getFormula() instanceof Fluent) {
                return true;
            }
        }
        if (f instanceof BeliefNotFormula) {
            BeliefFormula inner = ((BeliefNotFormula) f).getFormula();
            if (inner instanceof BeliefBelievesFormula) {
                BeliefFormula innerInner = ((BeliefBelievesFormula) inner).getFormula();
                if (innerInner instanceof BeliefNotFormula) {
                    if (((BeliefNotFormula) innerInner).getFormula() instanceof Fluent) {
                        return true;
                    }
                }
                if (innerInner instanceof LocalNotFormula) {
                    if (((LocalNotFormula) innerInner).getFormula() instanceof Fluent) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


//    private static void createEmptyTail(FormulaWorld tau, String agent) {
//        FormulaWorld rho = new FormulaWorld();
//        FormulaWorld omega = new FormulaWorld();
//        worlds.add(rho);
//        worlds.add(omega);
//        if (agent == null) {
//            for (String a : domain.getAllAgents()) {
//                relations.get(a).connect(tau,rho);
//                relations.get(a).connect(rho,omega);
//
//                for (FormulaWorld fromWorld : relations.get(a).getFromWorlds(tau)) {
//                }
//
//            }
//        }
//        else {
//            relations.get(agent).connect(rho,omega);
//        }
//    }

//        if (formula instanceof Fluent) {
//            Set<Fluent> fluents = new HashSet<>();
//            fluents.add((Fluent) formula);
//            Map<String, Set<ModalTree>> modalBeliefs = new HashMap<>();
//            Map<String, Set<ModalTree>> modalNegativeBeliefs = new HashMap<>();
//            for (String a : domain.getAllAgents()) {
//                modalBeliefs.put(a, new HashSet<ModalTree>());
//                modalNegativeBeliefs.put(a, new HashSet<ModalTree>());
//            }
//            result.add(new ModalTree(fluents,
//                                     new HashSet<Fluent>(),
//                                     modalBeliefs,
//                                     modalNegativeBeliefs
//                                    ));
//        }
//
//        else if (formula instanceof BeliefOrFormula) {
//            BeliefOrFormula orFormula = (BeliefOrFormula) formula;
//            for (BeliefFormula inner : orFormula.getFormulae()) {
//                result.addAll(parseFormula(inner));
//            }
//        }
//        else if (formula instanceof LocalOrFormula) {
//            LocalOrFormula orFormula = (LocalOrFormula) formula;
//            for (LocalFormula inner : orFormula.getFormulae()) {
//                result.addAll(parseFormula(inner));
//            }
//        }
//        else if (formula instanceof BeliefAndFormula || formula instanceof LocalAndFormula) {
//            List<ModalTree> parsedLeft = new ArrayList<>();;
//            List<ModalTree> parsedRight = new ArrayList<>();;
//
//            if (formula instanceof BeliefAndFormula) {
//                BeliefAndFormula andFormula = (BeliefAndFormula) formula;
//                andFormula = andFormula.binarize();
//                parsedLeft.addAll(parseFormula(andFormula.getFormulae().get(0)));
//                parsedRight.addAll(parseFormula(andFormula.getFormulae().get(1)));
//            }
//            else {
//                LocalAndFormula andFormula = (LocalAndFormula) formula;
//                andFormula = andFormula.binarize();
//                parsedLeft.addAll(parseFormula(andFormula.getFormulae().get(0)));
//                parsedRight.addAll(parseFormula(andFormula.getFormulae().get(1)));
//            }
//
//            for (ModalTree leftTree : parsedLeft) {
//                for (ModalTree rightTree : parsedRight) {
//                    result.add(conjoin(leftTree, rightTree));
//                }
//            }
//        }
//
//        else if (formula instanceof BeliefNotFormula || formula instanceof LocalNotFormula) {
//            BeliefFormula innerFormula;
//            if (formula instanceof BeliefNotFormula) {
//                innerFormula = ((BeliefNotFormula)formula).getFormula();
//            }
//            else {
//                innerFormula = ((LocalNotFormula)formula).getFormula();
//            }
//            for (ModalTree tree : parseFormula(innerFormula)) {
//                result.add(negate(tree));
//            }
//        }
//
//        else if (formula instanceof BeliefBelievesFormula) {
//            BeliefBelievesFormula beliefFormula = (BeliefBelievesFormula) formula;
//
//            Map<String, Set<ModalTree>> modalBeliefs = new HashMap<>();
//            Map<String, Set<ModalTree>> modalNegativeBeliefs = new HashMap<>();
//
//            for (String a : domain.getAllAgents()) {
//                if (beliefFormula.getAgent().equals(a)) {
//                    modalBeliefs.put(a, parseFormula(beliefFormula.getFormula()));
//                }
//                else {
//                    modalBeliefs.put(a, new HashSet<ModalTree>());
//                }
//                modalNegativeBeliefs.put(a, new HashSet<ModalTree>());
//            }
//            result.add(new ModalTree(new HashSet<Fluent>(),
//                                     new HashSet<Fluent>(),
//                                     modalBeliefs,
//                                     modalNegativeBeliefs
//                                    ));
//        }
//        else {
//            throw new RuntimeException("Can't build state from formula: " + formula.toString());
//        }
//        return result;
//    }
//
//

//    private static EpistemicState makeState() {
//
//        Set<World> worlds = new HashSet<>();
//        Map<String, Relation> beliefRelations = new HashMap<>();
//
//        for (String a : domain.getAllAgents()) {
//            beliefRelations.put(a, new Relation());
//        }
//
//        Stack<PossibleTree> treeStack = new Stack<>();
//        Stack<World> worldStack = new Stack<>();
//        treeStack.push(p);
//        World designated = new World(p.getTrueFluents());
//        worlds.add(designated);
//        worldStack.push(designated);
//        while (!treeStack.isEmpty()) {
//            PossibleTree currentTree = treeStack.pop();
//            World parentWorld  = worldStack.pop();
//            for (String a : domain.getAllAgents()) {
//                for (PossibleTree childTree : currentTree.getBeliefs(a)) {
//                    World childWorld = new World(childTree.getTrueFluents());
//                    worlds.add(childWorld);
//                    beliefRelations.get(a).connect(parentWorld, childWorld);
//                    treeStack.push(childTree);
//                    worldStack.push(childWorld);
//                }
//            }
//        }
//
//        // ADD REFLEXIVE CONNECTION TO ANY NON-SERIAL WORLD
//        for (World w : worlds) {
//            for (String a : domain.getAllAgents()) {
//                if (beliefRelations.get(a).getToWorlds(w).isEmpty()) {
//                    beliefRelations.get(a).connect(w, w);
//                }
//            }
//        }
//
//        // TRANSITIVE-EUCLIDEAN CLOSURE
//        for (String a : domain.getAllAgents()) {
//            boolean changed = true;
//            while (changed) {
//                changed = false;
//                Relation relation = new Relation(beliefRelations.get(a));
//                for (World fromA : worlds) {
//                    for (World toA : beliefRelations.get(a).getToWorlds(fromA)) {
//                        for (World fromB : worlds) {
//                            for (World toB : beliefRelations.get(a).getToWorlds(fromB)) {
//                                if (toA == fromB) {
//                                    if (!beliefRelations.get(a).isConnected(fromA, toB)) {
//                                        changed = true;
//                                        relation.connect(fromA, toB);
//                                    }
//                                }
//                                if (fromA == fromB) {
//                                    if (!beliefRelations.get(a).isConnected(toA, toB)) {
//                                        changed = true;
//                                        relation.connect(toA, toB);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                beliefRelations.put(a, relation);
//            }
//        }
//
//
//        // BUILD KNOWLEDGE RELATION: COPY AND REFLEXIVIZE AND SYMETRIZE K RELATION?
//
//        Map<String, Relation> knowledgeRelations = new HashMap<>();
//
//        for (String a : domain.getAllAgents()) {
//            Relation relation = new Relation(beliefRelations.get(a));
//            for (World from : worlds) {
//                relation.connect(from,from);
//                for (World to : beliefRelations.get(a).getToWorlds(from)) {
//                    relation.connect(from,to);
//                    relation.connect(to,from);
//                }
//            }
//            knowledgeRelations.put(a, relation);
//        }
//
//        KripkeStructure kripke = new KripkeStructure(worlds, beliefRelations, knowledgeRelations);
//        kripke.forceCheck();
//        EpistemicState state = new EpistemicState(kripke, designated);
//
//        state.reduce();
//        state.trim();
//
//        state.getKripke().forceCheck();
//
//        Log.debug("constructed state:\n" + state.toString());
//
//        return state;
//
//    }

 

}

// NEED TO COMPARE FORMULAE FOR IDNTITY EQUALITY!

