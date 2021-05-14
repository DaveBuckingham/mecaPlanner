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


import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import depl.*;

public class Finitary {

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

    private static class AgentFluent {
        public String agent;
        public Fluent fluent;
        public AgentFluent(String a, Fluent f) {
            this.agent = a;
            this.fluent = f;
        }
    }

    private static class AgentsFluent {
        public String agenti;
        public String agentj;
        public Fluent fluent;
        public AgentsFluent(String ai, String aj, Fluent f) {
            this.agenti = ai;
            this.agentj = aj;
            this.fluent = f;
        }
    }

    private static Set<Set<Fluent>> add(Set<Set<Fluent>> s, Fluent f) {
        Set<Set<Fluent>> n = new HashSet<>();
        for (Set<Fluent> ss : s) {
            Set<Fluent> nn = new HashSet<>(ss);
            nn.add(f);
            n.add(nn);
        }
        return n;
    }



    private static Set<Set<Fluent>> addPossibly(Set<Set<Fluent>> s, Fluent f) {
        Set<Set<Fluent>> n = new HashSet<>();
        for (Set<Fluent> ss : s) {
            Set<Fluent> nn1 = new HashSet<>(ss);
            Set<Fluent> nn2 = new HashSet<>(ss);
            nn2.add(f);
            n.add(nn1);
            n.add(nn2);
        }
        return n;
    }




    private static Fluent type1(BeliefFormula formula) {
        if (formula instanceof BeliefNotFormula) {
            return type2(((BeliefNotFormula) formula).getFormula());
        }
        if (formula instanceof LocalNotFormula) {
            return type2(((LocalNotFormula) formula).getFormula());
        }
        return null;
    }

    private static Fluent type2(BeliefFormula formula) {
        if (formula instanceof Fluent) {
            return (Fluent) formula;
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
                return new Finitary.AgentFluent(agent, fluent);
            }
        }
        return null;
    }

    private static AgentFluent type5(BeliefFormula formula) {
        if (!(formula instanceof BeliefAndFormula)) {
            return null;
        }
        List<BeliefFormula> formulae = ((BeliefAndFormula) formula).getFormulae();
        if (formulae.size() != 2) {
            return null;
        }
        BeliefFormula left = formulae.get(0);
        BeliefFormula right = formulae.get(1);

        if (!(left instanceof BeliefNotFormula && right instanceof BeliefNotFormula)) {
            return null;
        }
        left =  ((BeliefNotFormula) left).getFormula();
        right = ((BeliefNotFormula) right).getFormula();

        if (!(left instanceof BeliefBelievesFormula && right instanceof BeliefBelievesFormula)) {
            return null;
        }

        String agent = ((BeliefBelievesFormula) left).getAgent();
        if (!agent.equals(((BeliefBelievesFormula) right).getAgent())) {
            return null;
        }

        Fluent leftFluent = type2(((BeliefBelievesFormula) left).getFormula());
        Fluent rightFluent = type1(((BeliefBelievesFormula) right).getFormula());

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

        AgentFluent sub;
        if (i == 6) {
            sub = type3to4(3, innerFormula);
        }
        else if (i == 7) {
            sub = type3to4(4, innerFormula);
        }
        else if (i == 8) {
            sub = type5(innerFormula);
        }
        else {
            throw new RuntimeException("illegal input");
        }
        if (sub == null) {
            return null;
        }
        return new AgentsFluent(outerAgent, sub.agent, sub.fluent);
    }
  
    private static AgentsFluent pij(int i, BeliefFormula formula) {
        if (!(formula instanceof BeliefNotFormula)) {
            return null;
        }
        BeliefFormula believesi = ((BeliefNotFormula) formula).getFormula();

        if (!(believesi instanceof BeliefBelievesFormula)) {
            return null;
        }
        String agenti = ((BeliefBelievesFormula) believesi).getAgent();
        BeliefFormula notbj = ((BeliefBelievesFormula) believesi).getFormula();

        if (!(notbj instanceof BeliefNotFormula)) {
            return null;
        }
        BeliefFormula inner = ((BeliefNotFormula) notbj).getFormula();
        AgentFluent sub;

        if (i == 1) {
            sub = type3to4(3, inner);    
        }
        else if (i == 2) {
            sub = type3to4(4, inner);    
        }
        else if (i == 3) {
            sub = type5(inner);    
        }
        else {
            throw new RuntimeException("illegal input");
        }
        if (sub == null) {
            return null;
        }
        return new AgentsFluent(agenti, sub.agent, sub.fluent);
    }


    private static AgentsFluent type9to12(int i, BeliefFormula formula) {
        //Log.warning(formula.toString());
        if (!(formula instanceof BeliefAndFormula)) {
            return null;
        }
        List<BeliefFormula> formulae = ((BeliefAndFormula) formula).getFormulae();
        if (formulae.size() != 3) {
            return null;
        }
        BeliefFormula left = formulae.get(0);
        BeliefFormula middle = formulae.get(1);
        BeliefFormula right = formulae.get(2);

        if (i == 9) {
            right = BeliefNotFormula.make(right);
        }
        else if (i == 10) {
            middle = BeliefNotFormula.make(middle);
        }
        else if (i == 11) {
            right = BeliefNotFormula.make(right);
        }
        else if (i != 12) {
            throw new RuntimeException("illegal input");
        }
        AgentsFluent leftSub = pij(1,left);
        AgentsFluent middleSub = pij(2,middle);
        AgentsFluent rightSub = pij(3,right);
        if (leftSub == null || middleSub == null || rightSub == null) {
            return null;
        }
        if (!(leftSub.agenti.equals(middleSub.agenti) &&
              leftSub.agenti.equals(rightSub.agenti) &&
              leftSub.agentj.equals(middleSub.agentj) &&
              leftSub.agentj.equals(rightSub.agentj) &&
              leftSub.fluent.equals(middleSub.fluent) &&
              leftSub.fluent.equals(rightSub.fluent))) {
            return null;
        }
        return leftSub;
    }

    public static EpistemicState constructState(Set<BeliefFormula> statements, Domain domain) {
        Map<String, Map<String, Map<Integer, Set<Fluent>>>> types = new HashMap<>();
        types.put("", new HashMap<>());
        types.get("").put("", new HashMap<>());
        types.get("").get("").put(1, new HashSet<>());
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

        // NEED TO CHECK FOR DUPLICATES AND MISSING STATEMENTS
        for (BeliefFormula formula : statements) {
            if (type1(formula) != null) {
                types.get("").get("").get(1).add(type1(formula));
                Log.debug("1");
            }
            else if (type2(formula) != null) {
                types.get("").get("").get(2).add(type2(formula));
                Log.debug("2");
            }
            else if (type3to4(3, formula) != null) {
                AgentFluent af = type3to4(3, formula);
                types.get(af.agent).get(af.agent).get(3).add(af.fluent);
                Log.debug("3");
            }
            else if (type3to4(4, formula) != null) {
                AgentFluent af = type3to4(4, formula);
                types.get(af.agent).get(af.agent).get(4).add(af.fluent);
                Log.debug("4");
            }
            else if (type5(formula) != null) {
                AgentFluent af = type5(formula);
                types.get(af.agent).get(af.agent).get(5).add(af.fluent);
                Log.debug("5");
            }
            else if (type6to8(6, formula) != null) {
                AgentsFluent af = type6to8(6, formula);
                types.get(af.agenti).get(af.agentj).get(6).add(af.fluent);
                Log.debug("6");
            }
            else if (type6to8(7, formula) != null) {
                AgentsFluent af = type6to8(7, formula);
                types.get(af.agenti).get(af.agentj).get(7).add(af.fluent);
                Log.debug("7");
            }
            else if (type6to8(8, formula) != null) {
                AgentsFluent af = type6to8(8, formula);
                types.get(af.agenti).get(af.agentj).get(8).add(af.fluent);
                Log.debug("8");
            }
            else if (type9to12(9, formula) != null) {
                AgentsFluent af = type9to12(9, formula);
                types.get(af.agenti).get(af.agentj).get(9).add(af.fluent);
                Log.debug("9");
            }
            else if (type9to12(10, formula) != null) {
                AgentsFluent af = type9to12(10, formula);
                types.get(af.agenti).get(af.agentj).get(10).add(af.fluent);
                Log.debug("10");
            }
            else if (type9to12(11, formula) != null) {
                AgentsFluent af = type9to12(11, formula);
                types.get(af.agenti).get(af.agentj).get(11).add(af.fluent);
                Log.debug("11");
            }
            else if (type9to12(12, formula) != null) {
                AgentsFluent af = type9to12(12, formula);
                types.get(af.agenti).get(af.agentj).get(12).add(af.fluent);
                Log.debug("12");
            }
            else {
                throw new RuntimeException("bad statement: " + formula);
            }
        }

        // MAKE SURE EACH P APPEARS IN ONE OF EACH STATEMENT CATEGORY


        // Vij
        Map<String, Map<String, Set<Set<Set<Fluent>>>>> frames = new HashMap<>();


        for (String i : domain.getAllAgents()) {
            frames.put(i, new HashMap<>());
            for (String j : domain.getAllAgents()) {
                frames.get(i).put(j, new HashSet<>());

                if (i.equals(j)) {
                    Set<Set<Fluent>> inner = new HashSet<>();
                    inner.add(types.get(i).get(j).get(4));
                    for (Fluent f : types.get(i).get(j).get(5)) {
                        inner = addPossibly(inner, f);
                    }
                    frames.get(i).get(j).add(inner);
                }
                else {
                    Set<Set<Fluent>> inner = new HashSet<>();
                    inner.add(types.get(i).get(j).get(7));
                    for (Fluent f : types.get(i).get(j).get(8)) {
                        inner = addPossibly(inner, f);
                    }
                    frames.get(i).get(j).add(inner);

                    for (Fluent f : types.get(i).get(j).get(9)) {
                        Set<Set<Set<Fluent>>> temp = new HashSet<>(frames.get(i).get(j));
                        for (Set<Set<Fluent>> t : temp) {
                            t = add(t, f);
                        }
                        frames.get(i).get(j).addAll(temp);
                    }
                    for (Fluent f : types.get(i).get(j).get(10)) {
                        Set<Set<Set<Fluent>>> temp = new HashSet<>(frames.get(i).get(j));
                        for (Set<Set<Fluent>> t : temp) {
                            t = addPossibly(t, f);
                        }
                        frames.get(i).get(j).addAll(temp);
                    }
                    for (Fluent f : types.get(i).get(j).get(11)) {
                        Set<Set<Set<Fluent>>> temp = new HashSet<>(frames.get(i).get(j));
                        for (Set<Set<Fluent>> t : frames.get(i).get(j)) {
                            t = add(t, f);
                        }
                        for (Set<Set<Fluent>> t : temp) {
                            t = addPossibly(t, f);
                        }
                        frames.get(i).get(j).addAll(temp);
                    }
                    for (Fluent f : types.get(i).get(j).get(12)) {
                        Set<Set<Set<Fluent>>> temp1 = new HashSet<>();
                        for (Set<Set<Fluent>> t : frames.get(i).get(j)) {
                            temp1.add(add(t, f));
                        }
                        Set<Set<Set<Fluent>>> temp2 = new HashSet<>();
                        for (Set<Set<Fluent>> t : frames.get(i).get(j)) {
                            temp2.add(addPossibly(t, f));
                        }
                        frames.get(i).get(j).addAll(temp1);
                        frames.get(i).get(j).addAll(temp2);
                    }
                }
            }
        }



        //Map<String, Map<String, List<Set<Set<World>>>>> subClasses = new HashMap<>();

        //for (String i : domain.getAllAgents()) {
        //    for (String j : domain.getAllAgents()) {
        //        for (Set<Set<Fluent>> valuationClass : frames.get(i).get(j)) {
        //            for (String k : domain.getAllAgents()) {
        //                if (!k.equals(j)) {
        //                    for (Set<Fluent> valuation : valuationClass) {
        //                                for (Set<Set<Fluent>> next : frames.get(j).get(k)) {
        //                        for (Set<World> subClass : subClasses) {
        //                            World w = new World(valuation);
        //                            subClass.add(w);
        //                        }

        //            }
        //        }
        //    }
        //}
        System.exit(1);


        return null;
    }


}