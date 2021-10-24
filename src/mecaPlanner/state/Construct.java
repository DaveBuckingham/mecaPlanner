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


public class Construct {


    private static Domain domain; // DO WE NEED THIS?

    private Construct() { }

    public static EpistemicState constructState(Domain d, Set<BeliefFormula> formulae) {
        domain = d;
        return null;
    }

//    private static Set<ModalTree> parseFormula(BeliefFormula formula) {
//        Set<ModalTree> result = new HashSet<>();
//
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
//    // HERE'S WHERE THE MAJIC (HOPEFULLY) HAPPENS:
//    // WE CONFLATE MODAL NECESSITY DISJUNCTION WITH MODAL POSSIBILITY CONJUNCTION,
//    // BOTH ARE REPRESENTED BY CHILD ARITY, AND WE COMBINE THEM
//    private static PossibleTree mergeModes(ModalTree m) {
//        Map<String, Set<PossibleTree>> mergedPossibilities = new HashMap<>();
//        for (String a : domain.getAllAgents()) {
//            Set<PossibleTree> merged = new HashSet<>();
//            Set<ModalTree> possibly = new HashSet<>();
//            for (ModalTree negativeBelief : m.getNegativeBeliefs(a)) {
//                possibly.add(negate(negativeBelief));
//            }
//            if (m.getBeliefs(a).isEmpty()) {
//                for (ModalTree p : possibly) {
//                    merged.add(mergeModes(p));
//                }
//            }
//            else if (possibly.isEmpty()) {
//                for (ModalTree b : m.getBeliefs(a)) {
//                    merged.add(mergeModes(b));
//                }
//            }
//            else {
//                for (ModalTree b : m.getBeliefs(a)) {
//                    for (ModalTree p : possibly) {
//                        merged.add(mergeModes(conjoin(b,p)));
//                    }
//                }
//            }
//            mergedPossibilities.put(a, merged);
//        }
//        return new PossibleTree(m.getTrueFluents(), m.getFalseFluents(), mergedPossibilities);
//    }
//
//
//    private static PossibleTree cascadeFluents(PossibleTree t, PossibleTree parent) {
//        Set<Fluent> fluents = new HashSet();
//        Map<String, Set<PossibleTree>> beliefs = new HashMap<>();
//
//        fluents.addAll(t.getTrueFluents());
//
//        if (parent != null) {
//            for (Fluent f : parent.getTrueFluents()) {
//                if (!t.getFalseFluents().contains(f)) {
//                    fluents.add(f);
//                }
//            }
//        }
//
//        for (String a : domain.getAllAgents()) {
//            Set<PossibleTree> cascadedChildren = new HashSet<>();
//            for (PossibleTree p : t.getBeliefs(a)) {
//                cascadedChildren.add(cascadeFluents(p, t));
//            }
//            beliefs.put(a, cascadedChildren);
//        }
//
//        return new PossibleTree(fluents, new HashSet<Fluent>(), beliefs);
//    }
//
//
//    private static EpistemicState makeState(PossibleTree p) {
//
//        // COPY INTO KRIPKE STRUCTURE
//
//        p = cascadeFluents(p, null);
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

