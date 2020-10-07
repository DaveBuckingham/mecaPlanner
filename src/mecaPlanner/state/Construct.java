package mecaPlanner.state;

import mecaPlanner.formulae.beliefFormulae.*;
import mecaPlanner.formulae.localFormulae.*;
import mecaPlanner.Domain;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import depl.*;

public class Construct {

//1.  ~p
//2.  p
//3.  Bi(~p)
//4.  Bi(p)
//5.  Pi(~p) & Pi(p)
//6.  BiBj(~p)
//7.  BiBj(p)
//8.  Bi( Pj(~p) & Pj(p) )
//9.  PiBj~p & PiBjp & ~Pi(Pj~p & Pjp)
//10. PiBj~p & ~PiBjp & Pi(Pj~p & Pjp)
//11. ~PiBj~p & PiBjp & Pi(Pj~p & Pjp)
//12. PiBj~p & PiBjp & Pi(Pj~p & Pjp)

    private static Map<String, Map<String, Map<Integer, Set<Fluent>>>> types;

    private class AgentFluent {
        public String agent;
        public Fluent fluent;
        public AgentFluent(String a, Fluent f) {
            this.agent = a;
            this.fluent = f;
        }
    }

    private class AgentsFluent {
        public String agenti;
        public String agentj;
        public Fluent fluent;
        public AgentFluent(String ai, String aj, Fluent f) {
            this.agenti = ai;
            this.agentj = aj;
            this.fluent = f;
        }
    }

    private static Fluent type1(BeliefFormula formula) {
        if (formula instanceof BeliefNotFormula) {
            return type2((BeliefNotFormula).getFormula());
        }
        return null;
    }

    private static Fluent type2(BeliefFormula formula) {
        if (formula instanceof Fluent) {
            return formula;
        }
        return null;
    }

    private static AgentFluent type3to4(int i, BeliefFormula formula) {
        if (formula instanceof BeliefBelievesFormula) {
            String agent = ((BeliefBelievesFormula) formula).getAgent();
            BeliefFormula sub = ((BeliefBelievesFormula) formula).getFormula();
            Fluent fluent;
            if (i==3) {
                fluent = type1(sub);
            }
            else if (i==4) {
                fluent = type2(sub);
            }
            else {
                throw new RuntimeException("illegal input");
            }
            if (fluent != null) {
                return new AgentFluent(agent, fluent);
            }
        }
        return null;
    }

    private static AgentFluent type5(BeliefFormula formula) {
        if (!(formula instanceof BeliefAndFormula)) {
            return null;
        }
        List<Formula> formulae = ((BeliefAndFormula) formula).getFormulae();
        if (formulae.size != 2) {
            return null;
        }
        BeliefFormula left = formulae[0];
        BeliefFormula right = formulae[1];

        if (!(left instanceof BeliefNotFormula && right instanceof BeliefNotFormula)) {
            return null;
        }
        left =  ((BeliefNotFormula) left).getFormula();
        right = ((BeliefNotFormula) left).getFormula();

        if (!(left instanceof BeliefBelievesFormula && right instanceof BeliefBelievesFormula)) {
            return null;
        }

        String agent = ((BeliefBelievesFormula) left).getAgent();
        if (!agent.equals(((BeliefBelievesFormula) right).getAgent()) {
            return null;
        }

        Fluent leftFluent = type2(((BeliefBelievesFormula) left).getFormula());
        Fluent rightFluent = type1(((BeliefBelievesFormula) left).getFormula());

        if (leftFluent == null || rightFluent == null || !leftFluent.equals(rightFluent)) {
            return null;
        }
        return new AgentFluent(agent, leftFluent);
    }

    private static AgentsFluent type6to8(int i, BeliefFormula formula) {
        if (!(formula instanceof BeliefBelievesFormula)) {
            return null;
        }
        String outerAgent = ((BeliefBelievesFormula) formula).getAgent();
        BeliefFormula innerFormula = ((BeliefBelievesFormula) formula).getFormula();


//5.  Pi(~p) & Pi(p)
//6.  BiBj(~p)
//7.  BiBj(p)
//8.  Bi( Pj(~p) & Pj(p) )
//9.  PiBj~p & PiBjp & ~Pi(Pj~p & Pjp)
//10. PiBj~p & ~PiBjp & Pi(Pj~p & Pjp)
//11. ~PiBj~p & PiBjp & Pi(Pj~p & Pjp)
//12. PiBj~p & PiBjp & Pi(Pj~p & Pjp)


//    private static void classify(BeliefFormula formula, boolean store) {
//        if (formula instanceof BeliefNotFormula) {
//            // 1
//            if (!(formula instanceof Fluent)) {
//                return;
//            }
//        }
//        if (formula instanceof Fluent) {
//            // 2
//            types.get("").get("").get(2).add((Fluent) formula);
//            return;
//        }
//        if (formula instanceof BeliefBelievesFormula) {
//            // 3,4,6,7,8
//        }
//        if (formula instanceof BeliefAndFormula) {
//            // 5,9,10,11,12
//        }
//        throw new RuntimeException("Invalid construction formula");
//    }

    public static EpistemicState constructState(Set<BeliefFormula> statements, Domain domain) {
        types = new HashMap<>();
        types.put("", new HashMap<>());
        types.get("").put("", new HashMap<>());
        types.get("").get("").put(2, new HashSet<>());
        for (String i : domain.getAllAgents()) {
            types.put(i, new HashMap<>());
            for (String j : domain.getAllAgents()) {
                types.get(i).put(j, new HashMap<>());
                for (Integer k=3; k<=12; k+=1) {
                    types.get(i).get(j).put(k, new HashSet<>());
                }
            }
        }

        for (BeliefFormula formula : statements) {
            classify(formula, true);
        }
        return null;
    }

}
