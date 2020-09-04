package mecaPlanner.state;

import mecaPlanner.formulae.*;
import mecaPlanner.Domain;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.io.File;
import java.io.FileWriter;

import java.io.IOException;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import depl.*;

public class Initialize {

//    static class AgentFormulaPair {
//        private String agent;
//        private FluentFormula formula;
//        public AgentFormulaPair(String agent, FluentFormula formula) {
//            this.agent = agent;
//            this.formula = formula;
//        }
//        public String getAgent() {
//            return agent;
//        }
//        public FluentFormula getFormula() {
//            return formula;
//        }
//        public String toString() {
//            StringBuilder str = new StringBuilder("(");
//            str.append(agent.toString());
//            str.append(",");
//            str.append(formula.toString());
//            str.append(")");
//            return str.toString();
//        }
//        public boolean equals(Object obj) {
//            if (this == obj) {
//                return true;
//            }
//            if (obj == null || obj.getClass() != getClass()) {
//                return false;
//            }
//            AgentFormulaPair other = (AgentFormulaPair) obj;
//
//            return (agent.equals(other.getAgent()) && formula.equals(other.getFormula()));
//        }
//        public int hashCode() {
//            return agent.hashCode() * formula.hashCode();
//        }
//    }
//
//
//
//
//
//
//
//
//
//
//    // this function copied from Andrew Mao:
//    // https://stackoverflow.com/questions/1670862/obtaining-a-powerset-of-a-set-in-java
//    public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
//        List<T> list = new ArrayList<T>(originalSet);
//        int n = list.size();
//        Set<Set<T>> powerSet = new HashSet<Set<T>>();
//        for( long i = 0; i < (1 << n); i++) {
//            Set<T> element = new HashSet<T>();
//            for( int j = 0; j < n; j++ ) {
//                if( (i >> j) % 2 == 1 ) element.add(list.get(j));
//            }
//            powerSet.add(element); 
//        }
//        return powerSet;
//    }
//
//    public static FluentAtom isType1(BeliefFormula f) {
//        if (f instanceof FluentAtom) {
//            return (FluentAtom) f;
//        }
//        return null;
//    }
//
//    // A LITTLE FLEXIBILITY HERE, WE ALLOW C(f) or C(Bi(f))
//    public static FluentFormula isType2(BeliefFormula f) {
//        if (!(f instanceof BeliefFormulaCommon)) {
//            return null;
//        }
//        BeliefFormula inner = ((BeliefFormulaCommon)f).getFormula();
//        if (inner instanceof FluentFormula) { 
//            return (FluentFormula) inner;
//        }
//        if (!(inner instanceof BeliefFormulaBelieves)) { 
//            return null;
//        }
//        BeliefFormula innerInner = ((BeliefFormulaBelieves)inner).getFormula();
//        if (!(innerInner instanceof FluentFormula)) {
//            return null;
//        }
//        return (FluentFormula) innerInner;
//    }
//
//    // C(Bi(f) V Bi(~f))
//    public static AgentFormulaPair isType3(BeliefFormula f) {
//        if (!(f instanceof BeliefFormulaCommon)) {
//            return null;
//        }
//        BeliefFormula inner = ((BeliefFormulaCommon)f).getFormula();
//        if (!(inner instanceof BeliefFormulaOr)) {
//            return null;
//        }
//        List<BeliefFormula> elements = ((BeliefFormulaOr)inner).getFormulae();
//        if (!((elements.size() == 2) && (elements.get(0) instanceof BeliefFormulaBelieves) && (elements.get(1) instanceof BeliefFormulaBelieves))) {
//            return null;
//        }
//        BeliefFormulaBelieves leftBelieves = (BeliefFormulaBelieves)elements.get(0);
//        BeliefFormulaBelieves rightBelieves = (BeliefFormulaBelieves)elements.get(1);
//        if (!(leftBelieves.getAgent().equals(rightBelieves.getAgent()))) {
//            return null;
//        }
//        String agent = leftBelieves.getAgent();
//        if (!(leftBelieves.getFormula() instanceof FluentFormula && rightBelieves.getFormula() instanceof FluentFormulaNot)) {
//            return null;
//        }
//        FluentFormula leftFormula = (FluentAtom)leftBelieves.getFormula();
//        FluentFormulaNot rightFormulaNot = (FluentFormulaNot)rightBelieves.getFormula();
//        FluentFormula rightFormula = rightFormulaNot.getFormula();
//        if (!(leftFormula.equals(rightFormula))) {
//            return null;
//        }
//        return (new AgentFormulaPair(agent, leftFormula));
//    }
//
//    // C(~Bi(f) V ~Bi(~f))
//    public static AgentFormulaPair isType4(BeliefFormula f) {
//        if (!(f instanceof BeliefFormulaCommon)) {
//            return null;
//        }
//        BeliefFormula inner = ((BeliefFormulaCommon)f).getFormula();
//        if (!(inner instanceof BeliefFormulaAnd)) {
//            return null;
//        }
//        List<BeliefFormula> elements = ((BeliefFormulaAnd)inner).getFormulae();
//        if (!((elements.size() == 2) && (elements.get(0) instanceof BeliefFormulaNot) && (elements.get(1) instanceof BeliefFormulaNot))) {
//            return null;
//        }
//        BeliefFormulaNot leftNot = (BeliefFormulaNot)elements.get(0);
//        BeliefFormulaNot rightNot = (BeliefFormulaNot)elements.get(1);
//        if (!((leftNot.getFormula() instanceof BeliefFormulaBelieves) && (rightNot.getFormula() instanceof BeliefFormulaBelieves))) {
//            return null;
//        }
//        BeliefFormulaBelieves leftBelieves = (BeliefFormulaBelieves) leftNot.getFormula();
//        BeliefFormulaBelieves rightBelieves = (BeliefFormulaBelieves) rightNot.getFormula();
//        if (!(leftBelieves.getAgent().equals(rightBelieves.getAgent()))) {
//            return null;
//        }
//        String agent = leftBelieves.getAgent();
//        if (!(leftBelieves.getFormula() instanceof FluentFormula && rightBelieves.getFormula() instanceof FluentFormulaNot)) {
//            return null;
//        }
//        FluentFormula leftFormula = (FluentFormula)leftBelieves.getFormula();
//        FluentFormulaNot rightFormulaNot = (FluentFormulaNot)rightBelieves.getFormula();
//        FluentFormula rightFormula = rightFormulaNot.getFormula();
//        if (!(leftFormula.equals(rightFormula))) {
//            return null;
//        }
//        return (new AgentFormulaPair(agent, leftFormula));
//    }
//
//
//
//    public static EpistemicState constructState(Set<BeliefFormula> initialStatements,
//                                                Domain domain,
//                                                boolean assumeCommonKnowledge
//                                               ) throws Exception {
//
//
//        Set<FluentAtom> type1 = new HashSet<>();
//        Set<FluentFormula> type2 = new HashSet<>();
//
//        Map<String, Set<FluentFormula>> type3 = new HashMap<>();
//        Map<String, Set<FluentFormula>> type4 = new HashMap<>();
//
//
//        for (String agent : domain.getAllAgents()) {
//            type3.put(agent, new HashSet<FluentFormula>());
//            type4.put(agent, new HashSet<FluentFormula>());
//        }
//
//        // ALL ATOMS APPEARING IN STATEMENTS OF TYPE 3 OR 4
//        Set<FluentAtom> type34Atoms = new HashSet<>();
//
//        for (BeliefFormula initially : initialStatements) {
//            AgentFormulaPair p;
//            FluentFormula f;
//            FluentAtom a;
//
//            a = Initialize.isType1(initially);
//            if (a != null) {
//                type1.add(a);
//                continue;
//            }
//
//            f = Initialize.isType2(initially);
//            if (f != null) {
//                type2.add(f);
//                continue;
//            }
//
//            p = Initialize.isType3(initially);
//            if (p != null) {
//                type3.get(p.getAgent()).add(p.getFormula());
//                type34Atoms.addAll(p.getFormula().getAllAtoms());
//                continue;
//            }
//
//            p = Initialize.isType4(initially);
//            if (p != null) {
//                type4.get(p.getAgent()).add(p.getFormula());
//                type34Atoms.addAll(p.getFormula().getAllAtoms());
//                continue;
//            }
//
//            throw new Exception("Bad initially formula: " + initially);
//        }
//
//
//        for (FluentAtom f : type34Atoms) {
//            if (type2.contains(f)) {
//                System.err.println("illegal initial state, atom " + f + " appears in type 2 and type 3 or 4 statements");
//                System.exit(1);
//            }
//        }
//
//        for (String a : type3.keySet()) {
//            for (BeliefFormula f : type3.get(a)) {
//                if (type4.get(a).contains(f)) {
//                    System.err.println("illegal initial state, " + a + " knows whether and doesn't know whether " + f);
//                    System.exit(1);
//                }
//            }
//        }
//
//
//        if (assumeCommonKnowledge) {
//            // ASSUME A TYPE 2 STATEMENT FOR ANY ATOM NOT APPEARING IN A TYPE 3 OR 4 STATEMENT
//            // I.E. ASSUME C(f) IF THERE IS NO OTHER INFO ABOUT f
//            // THIS MAKES THE INPUT FILE MORE COMPACT AND AVOIDS MISTAKES
//            for (FluentAtom atom : domain.getAllAtoms()) {
//                if (!(type34Atoms.contains(atom))) {
//                    if (type1.contains(atom)) {
//                        type2.add(atom);
//                    }
//                    else {
//                        type2.add(new FluentFormulaNot(atom));
//                    }
//                }
//            }
//        }
//
//
//
//
//        // USE THIS TO SPEED UP SO WE DON'T HAVE TO CALCULATE POWER SET OF EVERY PROPOSITION
//
//        Set<FluentAtom> knownTrue = new HashSet<>();
//        Set<FluentAtom> knownFalse = new HashSet<>();
//        Set<FluentAtom> unknown = new HashSet<>();
//
//        for (FluentFormula common : type2) {
//
//            if (common instanceof FluentAtom) {
//                knownTrue.add((FluentAtom) common);
//            }
//            else if (common instanceof FluentFormulaNot) {
//                FluentFormula inner = ((FluentFormulaNot) common).getFormula();
//                if (!(inner instanceof FluentAtom)) {
//                    throw new Exception("type 2 statement not in conjunctinve form: " + common.toString());
//                }
//                knownFalse.add((FluentAtom) inner);
//            }
//            else if (common instanceof FluentLiteral) {
//                FluentLiteral literal = (FluentLiteral) common;
//                if (literal.getValue()) {
//                    knownTrue.add(literal.getAtom());
//                }
//                else {
//                    knownFalse.add(literal.getAtom());
//                }
//            }
//            else if (common instanceof FluentFormulaOr) {
//                for (FluentFormula inner : ((FluentFormulaOr) common).getFormulae()) {
//                    if ( !( inner instanceof FluentAtom ||
//                            inner instanceof FluentFormulaNot ||
//                            inner instanceof FluentLiteral ) ) {
//                        throw new Exception("type 2 statement not in conjunctinve form: " + common.toString());
//                    }
//                }
//            }
//            else {
//                throw new Exception("type 2 statement not in conjunctinve form: " + common.toString());
//            }
//        }
//
//        
//        for (FluentAtom atom : domain.getAllAtoms()) {
//            if (!(knownTrue.contains(atom) || knownFalse.contains(atom))) {
//                unknown.add(atom);
//            }
//        }
//
//
//
//
//        Set<World> allWorlds = new HashSet<>();
//        //for (Set<FluentAtom> interpretation : Initialize.powerSet(domain.getAllAtoms())) {
//        for (Set<FluentAtom> interpretation : Initialize.powerSet(unknown)) {
//
//            interpretation.addAll(knownTrue);
//            World possibleWorld = new World(interpretation);
//
//            boolean satisfied = true;
//
//            for (FluentFormula commonKnowledge : type2) {
//                // TRUST ITS IN DISJUNCTIVE FORM
//                if (!(commonKnowledge.holds(possibleWorld))) {
//                    satisfied = false;
//                    break;
//                }
//            }
//
//            if (satisfied) {
//                allWorlds.add(possibleWorld);
//            }
//        }
//
//
//
//        // THIS ISN'T QUITE HOW SON ET AL DO IT, BUT I DON'T REALLY UNDERSTAND THE "COVERAGE" THING
//        Map<String, Relation> beliefRelations = new HashMap<>();
//        for (String a : domain.getAgents()) {
//            beliefRelations.put(a, new Relation());
//        }
//
//        Map<String, Relation> knowledgeRelations = new HashMap<>();
//        for (String a : domain.getAgents()) {
//            knowledgeRelations.put(a, new Relation());
//        }
//
//
//
//        for (String agent : domain.getAgents()) {
//            for (World from : allWorlds) {
//                for (World to : allWorlds) {
//                    boolean distinguishes = false;
//
//                    Set<FluentFormula> distinguisherUnion = new HashSet<FluentFormula>(type3.get(agent));
//                    distinguisherUnion.addAll(type2);
//
//                    for (FluentFormula f : distinguisherUnion) {
//                        if (f.holds(from) != f.holds(to)) {
//                            distinguishes = true;
//                            break;
//                        }
//                    }
//
//                    if (!distinguishes) {
//                        knowledgeRelations.get(agent).connect(from, to);
//                        beliefRelations.get(agent).connect(from, to);
//                    }
//                }
//            }
//        }
//
//        for (World world : allWorlds) {
//            for (String agent : domain.getAllAgents()) {
//                assert(beliefRelations.get(agent).getEdges().containsKey(world));
//                assert(!beliefRelations.get(agent).getEdges().get(world).isEmpty());
//                assert(knowledgeRelations.get(agent).getEdges().containsKey(world));
//                assert(!knowledgeRelations.get(agent).getEdges().get(world).isEmpty());
//            }
//        }
//
//        KripkeStructure kripke = new KripkeStructure(allWorlds, beliefRelations, knowledgeRelations);
//
//        World designated = null;
//        
//        World type1World = new World(type1);
//
//        for (World w : allWorlds) {
//            if (w.equivalent(type1World)) {
//                designated = w;
//                break;
//            }
//        }
//
//        if (designated == null) {
//            throw new Exception("initially formulae incompatible with designated world (from type 1 statements)");
//        }
//
//        //World designated = null;
//
//        //for (World w : allWorlds) {
//        //    if (w.getFluents().containsAll(type1) && type1.containsAll(w.getFluents())) {
//        //        designated = w;
//        //        break;
//        //    }
//        //}
//
//
//        return (new EpistemicState(kripke, designated));
//    }


}
