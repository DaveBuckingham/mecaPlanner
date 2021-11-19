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

// STATIC METHODS TO BUILD A STATE FROM A SET OF FLUENTS

public class Construct {

    private static Domain domain;

    private Construct() { }

    private static class ModalTree {
        Set<Fluent> trueFluents;
        Set<Fluent> falseFluents;
        Map<String, Set<ModalTree>> beliefs;         // ARITY REPRESENTS DISJUNCTION
        Map<String, Set<ModalTree>> negativeBeliefs; // ARITY REPRESENTS CONJUNCTION
        public ModalTree(Set<Fluent> trueFluents,
                           Set<Fluent> falseFluents,
                           Map<String, Set<ModalTree>> beliefs,
                           Map<String, Set<ModalTree>> negativeBeliefs) {

            Set<Fluent> intersection = new HashSet<Fluent>(trueFluents);
            intersection.retainAll(falseFluents);
            assert(intersection.isEmpty());

            this.trueFluents = trueFluents;
            this.falseFluents = falseFluents;
            this.beliefs = beliefs;
            this.negativeBeliefs = negativeBeliefs;
        }
        public ModalTree(Set<Fluent> fluents) {
            Map<String, Set<ModalTree>> modalBeliefs = new HashMap<>();
            Map<String, Set<ModalTree>> modalNegativeBeliefs = new HashMap<>();
            for (String a : domain.getAllAgents()) {
                modalBeliefs.put(a, new HashSet<ModalTree>());
                modalNegativeBeliefs.put(a, new HashSet<ModalTree>());
            }
            this.trueFluents = fluents;
            this.falseFluents = new HashSet<Fluent>();
            this.beliefs = modalBeliefs;
            this.negativeBeliefs = modalNegativeBeliefs;

        }
        public ModalTree() {
            this(new HashSet<Fluent>());
        }

        public Boolean isTrue() {
            if (!(trueFluents.isEmpty() && falseFluents.isEmpty())) {
                return false;
            }
            for (String a : domain.getAllAgents()) {
                if (!beliefs.get(a).isEmpty()) {
                    return false;
                }
                if (!negativeBeliefs.get(a).isEmpty()) {
                    return false;
                }
            }
            return true;
        }


        public Set<Fluent> getTrueFluents() {
            return trueFluents;
        }
        public Set<Fluent> getFalseFluents() {
            return falseFluents;
        }
        public Map<String, Set<ModalTree>> getBeliefs() {
            return beliefs;
        }
        public Map<String, Set<ModalTree>> getNegativeBeliefs() {
            return negativeBeliefs;
        }
        public Set<ModalTree> getBeliefs(String a) {
            return beliefs.get(a);
        }
        public Set<ModalTree> getNegativeBeliefs(String a) {
            return negativeBeliefs.get(a);
        }

        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("({");
            for (Fluent f : trueFluents) {
                str.append(f);
                str.append(",");
            }
            if (!trueFluents.isEmpty()) {
                str.deleteCharAt(str.length() - 1);
            }
            str.append("},{");
            for (Fluent f : falseFluents) {
                str.append(f);
                str.append(",");
            }
            if (!falseFluents.isEmpty()) {
                str.deleteCharAt(str.length() - 1);
            }
            str.append("},{");
            for (Map.Entry<String, Set<ModalTree>> entry : beliefs.entrySet()) {
                //str.append(entry.getKey());
                for (ModalTree t : entry.getValue()) {
                    str.append(t);
                    str.append(",");
                }
            }
            //str.deleteCharAt(str.length() - 1);
            str.append("},{");
            for (Map.Entry<String, Set<ModalTree>> entry : negativeBeliefs.entrySet()) {
                //str.append(entry.getKey());
                for (ModalTree t : entry.getValue()) {
                    str.append(t);
                    str.append(",");
                }
            }
            //str.deleteCharAt(str.length() - 1);
            str.append("})");
            return str.toString();
        }
    }


    private static class PossibleTree {
        Set<Fluent> trueFluents;
        Set<Fluent> falseFluents;
        Map<String, Set<PossibleTree>> beliefs;
        public PossibleTree(Set<Fluent> trueFluents,
                           Set<Fluent> falseFluents,
                           Map<String, Set<PossibleTree>> beliefs) {

            Set<Fluent> intersection = new HashSet<Fluent>(trueFluents);
            intersection.retainAll(falseFluents);
            if (!intersection.isEmpty()) {
                throw new RuntimeException("Possible tree node has contradictory fluents:" + intersection.toString());
            }

            this.trueFluents = trueFluents;
            this.falseFluents = falseFluents;
            this.beliefs = beliefs;
        }
        public Set<Fluent> getTrueFluents() {
            return trueFluents;
        }
        public Set<Fluent> getFalseFluents() {
            return falseFluents;
        }
        public Map<String, Set<PossibleTree>> getBeliefs() {
            return beliefs;
        }
        public Set<PossibleTree> getBeliefs(String a) {
            return beliefs.get(a);
        }

        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("({");
            for (Fluent f : trueFluents) {
                str.append(f);
                str.append(",");
            }
            if (!trueFluents.isEmpty()) {
                str.deleteCharAt(str.length() - 1);
            }
            str.append("},{");
            for (Fluent f : falseFluents) {
                str.append(f);
                str.append(",");
            }
            if (!falseFluents.isEmpty()) {
                str.deleteCharAt(str.length() - 1);
            }
            str.append("},{");
            for (Map.Entry<String, Set<PossibleTree>> entry : beliefs.entrySet()) {
                for (PossibleTree t : entry.getValue()) {
                    str.append(t);
                    str.append(",");
                }
            }
            str.append("})");
            return str.toString();
        }
    }

    private static ModalTree negate(ModalTree m) {
        return new ModalTree(m.getFalseFluents(), m.getTrueFluents(), m.getNegativeBeliefs(), m.getBeliefs());
    }


    public static Set<EpistemicState> constructStates(Domain d, BeliefFormula formula) {
        domain = d;
        Set<EpistemicState> result = new HashSet<>();
        Set<ModalTree> trees = parseFormula(formula);
        for (ModalTree t : trees) {
            if (t != null) {
                System.out.println(t);
                PossibleTree p = mergeModes(t);
                if (p != null) {
                    result.add(makeState(p));
                }
            }
        }
        return result;
    }


    private static Set<ModalTree> parseFormula(BeliefFormula formula) {

        if (formula instanceof Literal) {
            Literal l = (Literal) formula;
            if (l.getValue()) {
                return new HashSet<ModalTree>();
            }
            else {
                return null;
            }
        }

        if (formula instanceof Fluent) {
            Set<Fluent> fluents = new HashSet<>();
            fluents.add((Fluent) formula);
            Set<ModalTree> result = new HashSet<>();
            result.add(new ModalTree(fluents));
            return result;
        }

        else if (formula instanceof BeliefAndFormula || formula instanceof LocalAndFormula) {
            Set<BeliefFormula> disjuncts = new HashSet<>();

            if (formula instanceof BeliefAndFormula) {
                for (BeliefFormula f : ((BeliefAndFormula)formula).getFormulae()) {
                    disjuncts.add(f.negate());
                }
            }
            else {
                for (LocalFormula f : ((LocalAndFormula)formula).getFormulae()) {
                    disjuncts.add(f.negate());
                }
            }
            return parseFormula(BeliefOrFormula.make(disjuncts).negate());
        }

        else if (formula instanceof BeliefOrFormula || formula instanceof LocalOrFormula) {
            Set<ModalTree> inners = new HashSet<>();

            if (formula instanceof BeliefOrFormula) {
                for (BeliefFormula f : ((BeliefOrFormula)formula).getFormulae()) {
                    inners.addAll(parseFormula(f));
                }
            }
            else {
                for (LocalFormula f : ((LocalOrFormula)formula).getFormulae()) {
                    inners.addAll(parseFormula(f));
                }
            }

            Set<ModalTree> result = new HashSet<>();
            for (Set<ModalTree> p : powerSet(inners)) {
                if (!p.isEmpty()) {                // NEED THIS?, MAKE PART OF CONJOIN?
                    if (p.size() == 1) {          // NEED THIS?, MAKE PART OF CONJOIN?
                        result.addAll(p);
                    }
                    else {
                        result.add(conjoin(p));
                    }
                }
            }
            return result;
        }


//        else if (formula instanceof BeliefOrFormula || formula instanceof LocalOrFormula) {
//            Set<BeliefFormula> conjuncts = new HashSet<>();
//
//            if (formula instanceof BeliefOrFormula) {
//                for (BeliefFormula f : ((BeliefOrFormula)formula).getFormulae()) {
//                    conjuncts.add(f.negate());
//                }
//            }
//            else {
//                for (LocalFormula f : ((LocalOrFormula)formula).getFormulae()) {
//                    conjuncts.add(f.negate());
//                }
//            }
//            return parseFormula(BeliefAndFormula.make(conjuncts).negate());
//        }
//
//        else if (formula instanceof BeliefAndFormula || formula instanceof LocalAndFormula) {
//            Set<ModalTree> inners = new HashSet<>();
//
//            if (formula instanceof BeliefAndFormula) {
//                for (BeliefFormula f : ((BeliefAndFormula)formula).getFormulae()) {
//                    inners.add(parseFormula(f));
//                }
//            }
//            else {
//                for (LocalFormula f : ((LocalAndFormula)formula).getFormulae()) {
//                    inners.add(parseFormula(f));
//                }
//            }
//            return conjoin(inners);
//        }

        else if (formula instanceof BeliefNotFormula) {
            Set<ModalTree> p = parseFormula(((BeliefNotFormula)formula).getFormula());
            Set<ModalTree> result = new HashSet<>();
            if (p == null) {
                return result;
            }
            if (p.isEmpty()) {
                return null;
            }
            for (ModalTree t : p) {
                result.add(negate(t));
            }
            return result;
        }

        // COMBINE ABOVE AND BELOW...

        else if (formula instanceof LocalNotFormula) {
            Set<ModalTree> p = parseFormula(((LocalNotFormula)formula).getFormula());
            Set<ModalTree> result = new HashSet<>();
            if (p == null) {
                return result;
            }
            if (p.isEmpty()) {
                return null;
            }
            for (ModalTree t : p) {
                result.add(negate(t));
            }
            return result;
        }


        else if (formula instanceof BeliefBelievesFormula) {
            BeliefBelievesFormula beliefFormula = (BeliefBelievesFormula) formula;

            Map<String, Set<ModalTree>> modalBeliefs = new HashMap<>();
            Map<String, Set<ModalTree>> modalNegativeBeliefs = new HashMap<>();

            for (String a : domain.getAllAgents()) {
                modalBeliefs.put(a, new HashSet<ModalTree>());
                modalNegativeBeliefs.put(a, new HashSet<ModalTree>());
                if (beliefFormula.getAgent().equals(a)) {
                    modalBeliefs.get(a).addAll(parseFormula(beliefFormula.getFormula()));
                }
            }
            Set<ModalTree> result = new HashSet<>();
            result.add(new ModalTree(new HashSet<Fluent>(),
                                 new HashSet<Fluent>(),
                                 modalBeliefs,
                                 modalNegativeBeliefs
                                ));
            return result;
        }
        else {
            throw new RuntimeException("Can't build state from formula: " + formula.toString());
        }
    }

    private static ModalTree conjoin(ModalTree left, ModalTree right) {
        Set<ModalTree> both = new HashSet<>();
        both.add(left);
        both.add(right);
        return conjoin(both);
    }

    private static ModalTree conjoin(Set<ModalTree> trees) {

        for (ModalTree t : trees) {
            if (t == null) {
                return null;
            }
        }

        Set<Fluent> unionedTrueFluents = new HashSet<>();;
        Set<Fluent> unionedFalseFluents = new HashSet<>();;
        for (ModalTree t : trees) {
            unionedTrueFluents.addAll(t.getTrueFluents());
            unionedFalseFluents.addAll(t.getFalseFluents());
        }

        Set<Fluent> intersection = new HashSet<Fluent>(unionedTrueFluents);
        intersection.retainAll(unionedFalseFluents);
        if (!intersection.isEmpty()) {
            return null;
        }

        Map<String, Set<ModalTree>> crossedBeliefs = new HashMap<>();
        for (String a : domain.getAllAgents()) {
            Set<ModalTree> unionedBeliefs = new HashSet<>();
            for (ModalTree t : trees) {
                unionedBeliefs.addAll(t.getBeliefs(a));
            }
            Set<ModalTree> crossed = new HashSet<>();
            for (Set<ModalTree> possibility : powerSet(unionedBeliefs)) {
                for (ModalTree x : possibility) {
                }
                if (!(possibility.isEmpty())) {             // DO WE NEED THIS?
                    ModalTree c = conjoin(possibility);
                    if (c != null) {
                        crossed.add(c);
                    }
                }
            }
            // SHOULD WE RETURN NULL IF THEY ALL FAILED?
            crossedBeliefs.put(a, crossed);
        }


        Map<String, Set<ModalTree>> unionedNegativeBeliefs = new HashMap<>();
        for (String a : domain.getAllAgents()) {
            Set<ModalTree> negativeBeliefs = new HashSet<>();
            for (ModalTree t : trees) {
                negativeBeliefs.addAll(t.getNegativeBeliefs(a));
            }
            unionedNegativeBeliefs.put(a, negativeBeliefs);
        }

        return new ModalTree(unionedTrueFluents, unionedFalseFluents, crossedBeliefs, unionedNegativeBeliefs);
    }




    // HERE'S WHERE THE MAJIC (HOPEFULLY) HAPPENS:
    // WE CONFLATE MODAL NECESSITY DISJUNCTION WITH MODAL POSSIBILITY CONJUNCTION,
    // BOTH ARE REPRESENTED BY CHILD ARITY, AND WE COMBINE THEM
    private static PossibleTree mergeModes(ModalTree m) {
        Map<String, Set<PossibleTree>> mergedPossibilities = new HashMap<>();
        for (String a : domain.getAllAgents()) {
            Set<PossibleTree> merged = new HashSet<>();
            Set<ModalTree> possibly = new HashSet<>();
            for (ModalTree negativeBelief : m.getNegativeBeliefs(a)) {
                possibly.add(negate(negativeBelief));
            }
            if (m.getBeliefs(a).isEmpty()) {
                for (ModalTree p : possibly) {
                    PossibleTree x = mergeModes(p);
                    if(x == null) {
                        return null;
                    }
                    merged.add(x);
                }
            }
            else if (possibly.isEmpty()) {
                Set<PossibleTree> t = new HashSet<>();
                for (ModalTree b : m.getBeliefs(a)) {
                    merged.add(mergeModes(b));
                    PossibleTree x = mergeModes(b);
                    if(x != null) {
                        t.add(x);
                    }
                }
                if (t.size() == 0) {
                    return null;
                }
                merged.addAll(t);
            }
            else {
                for (ModalTree p : possibly) {
                    Set<PossibleTree> t = new HashSet<>();
                    for (ModalTree b : m.getBeliefs(a)) {
                        ModalTree conjoined = conjoin(b,p);
                        if(conjoined != null) {
                            PossibleTree x = mergeModes(conjoined);
                            if(x != null) {
                                t.add(x);
                            }
                        }
                    }
                    if (t.size() == 0) {
                        return null;
                    }
                    merged.addAll(t);
                }
            }
            mergedPossibilities.put(a, merged);
        }
        return new PossibleTree(m.getTrueFluents(), m.getFalseFluents(), mergedPossibilities);
    }


    private static PossibleTree cascadeFluents(PossibleTree t, PossibleTree parent) {
        Set<Fluent> fluents = new HashSet();
        Map<String, Set<PossibleTree>> beliefs = new HashMap<>();

        fluents.addAll(t.getTrueFluents());

        if (parent != null) {
            for (Fluent f : parent.getTrueFluents()) {
                if (!t.getFalseFluents().contains(f)) {
                    fluents.add(f);
                }
            }
        }

        for (String a : domain.getAllAgents()) {
            Set<PossibleTree> cascadedChildren = new HashSet<>();
            for (PossibleTree p : t.getBeliefs(a)) {
                cascadedChildren.add(cascadeFluents(p, t));
            }
            beliefs.put(a, cascadedChildren);
        }

        return new PossibleTree(fluents, new HashSet<Fluent>(), beliefs);
    }


    private static EpistemicState makeState(PossibleTree p) {

        // COPY INTO KRIPKE STRUCTURE

        p = cascadeFluents(p, null);

        Set<World> worlds = new HashSet<>();
        Map<String, Relation> beliefRelations = new HashMap<>();

        for (String a : domain.getAllAgents()) {
            beliefRelations.put(a, new Relation());
        }

        Stack<PossibleTree> treeStack = new Stack<>();
        Stack<World> worldStack = new Stack<>();
        treeStack.push(p);
        World designated = new World(p.getTrueFluents());
        worlds.add(designated);
        worldStack.push(designated);
        while (!treeStack.isEmpty()) {
            PossibleTree currentTree = treeStack.pop();
            World parentWorld  = worldStack.pop();
            for (String a : domain.getAllAgents()) {
                for (PossibleTree childTree : currentTree.getBeliefs(a)) {
                    World childWorld = new World(childTree.getTrueFluents());
                    worlds.add(childWorld);
                    beliefRelations.get(a).connect(parentWorld, childWorld);
                    treeStack.push(childTree);
                    worldStack.push(childWorld);
                }
            }
        }

        // ADD REFLEXIVE CONNECTION TO ANY NON-SERIAL WORLD
        for (World w : worlds) {
            for (String a : domain.getAllAgents()) {
                if (beliefRelations.get(a).getToWorlds(w).isEmpty()) {
                    beliefRelations.get(a).connect(w, w);
                }
            }
        }

        // TRANSITIVE-EUCLIDEAN CLOSURE
        for (String a : domain.getAllAgents()) {
            boolean changed = true;
            while (changed) {
                changed = false;
                Relation relation = new Relation(beliefRelations.get(a));
                for (World fromA : worlds) {
                    for (World toA : beliefRelations.get(a).getToWorlds(fromA)) {
                        for (World fromB : worlds) {
                            for (World toB : beliefRelations.get(a).getToWorlds(fromB)) {
                                if (toA == fromB) {
                                    if (!beliefRelations.get(a).isConnected(fromA, toB)) {
                                        changed = true;
                                        relation.connect(fromA, toB);
                                    }
                                }
                                if (fromA == fromB) {
                                    if (!beliefRelations.get(a).isConnected(toA, toB)) {
                                        changed = true;
                                        relation.connect(toA, toB);
                                    }
                                }
                            }
                        }
                    }
                }
                beliefRelations.put(a, relation);
            }
        }


        // BUILD KNOWLEDGE RELATION: COPY AND REFLEXIVIZE AND SYMETRIZE K RELATION?

        Map<String, Relation> knowledgeRelations = new HashMap<>();

        for (String a : domain.getAllAgents()) {
            Relation relation = new Relation(beliefRelations.get(a));
            for (World from : worlds) {
                relation.connect(from,from);
                for (World to : beliefRelations.get(a).getToWorlds(from)) {
                    relation.connect(from,to);
                    relation.connect(to,from);
                }
            }
            knowledgeRelations.put(a, relation);
        }

        KripkeStructure kripke = new KripkeStructure(worlds, beliefRelations, knowledgeRelations);
        assert(kripke.checkRelations());
        EpistemicState state = new EpistemicState(kripke, designated);

        state.reduce();
        state.trim();

        assert(state.getKripke().checkRelations());

        Log.debug("constructed state:\n" + state.toString());

        return state;

    }


    // THIS FUNCTION IS COPIED FROM MARKSPACE:
    // https://codereview.stackexchange.com/questions/164647/powerset-all-subsets-of-a-set-in-java
    static <T> Set<Set<T>> powerSet( Set<T> set ) {
        T[] element = (T[]) set.toArray();
        final int SET_LENGTH = 1 << element.length;
        Set<Set<T>> powerSet = new HashSet<>();
        for( int binarySet = 0; binarySet < SET_LENGTH; binarySet++ ) {
            Set<T> subset = new HashSet<>();
            for( int bit = 0; bit < element.length; bit++ ) {
                int mask = 1 << bit;
                if( (binarySet & mask) != 0 ) {
                    subset.add( element[bit] );
                }
            }
            powerSet.add( subset );
        }
        return powerSet;
    }

}

