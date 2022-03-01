package mecaPlanner.state;

import mecaPlanner.formulae.*;
import mecaPlanner.Domain;
import mecaPlanner.Log;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.Arrays;


import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;


public class Construct {

    private Construct() { }

    private static List<String> agents;
    private static Set<Fluent> allFluents;


    public static Set<State> constructStates(Set<Fluent> allFluents,
                                             List<Formula> formulas,
                                             List<String> agents) {

        Construct.agents = agents;
        Construct.allFluents = allFluents;
        NDWorld ndWorld = foo(formulas);
        Set<World> designatedWorlds = ndWorld.getWorlds();

        Set<State> states = new HashSet<>();
        for (World designated : designatedWorlds) {
            Set<World> worlds = new HashSet<>();
            worlds.add(designated);
            states.add(new State(agents, worlds, designated));
        }
        return states;
    }

    private static NDWorld foo(List<Formula> formulas) {
        NDWorld root = new NDWorld(Construct.allFluents);
        Map<String, Set<Formula>> knowsNecessarily = new HashMap<>();
        Map<String, Set<Formula>> believesNecessarily = new HashMap<>();
        //Map<String, Set<Formula>> knowsPossibly = new HashMap<>();
        //Map<String, Set<Formula>> believesPossibly = new HashMap<>();
        for (String a : agents) {
            knowsNecessarily.put(a, new HashSet<>());
            believesNecessarily.put(a, new HashSet<>());
            //knowsPossibly.put(a, new HashSet<>());
            //believesPossibly.put(a, new HashSet<>());
        }
        Stack<Formula> todo = new Stack<Formula>();
        for (Formula f : formulas) {
            todo.push(f);
        }
        //while (!todo.empty() && !grounded.isEmpty()) {
        while (!todo.empty()) {
            Formula formula = todo.pop();
            if (formula instanceof Literal) {
                Literal f = (Literal) formula;
                if (!f.getValue()) {
                    return null;
                }
            }
            else if (formula instanceof Fluent) {
                Fluent f = (Fluent) formula;
                if (!root.setFluent(f, true)) {
                    return null;
                }
            }
            else if (formula instanceof AndFormula) {
                AndFormula f = (AndFormula) formula;
                for (Formula conjunct : f.getFormulae()) {
                    todo.push(conjunct);
                }
            }
            else if (formula instanceof KnowsFormula) {
                KnowsFormula f = (KnowsFormula) formula;
                knowsNecessarily.get(f.getAgent()).add(f.getFormula());
            }
            else if (formula instanceof BelievesFormula) {
                BelievesFormula f = (BelievesFormula) formula;
                believesNecessarily.get(f.getAgent()).add(f.getFormula());
            }
            else if (formula instanceof NotFormula) {
                Formula negated = ((NotFormula) formula).getFormula();

                if (negated instanceof Literal) {
                    Literal f = (Literal) negated;
                    if (f.getValue()) {
                        return null;
                    }
                }
                else if (negated instanceof Fluent) {
                    Fluent f = (Fluent) negated;
                    if (!root.setFluent(f, false)) {
                        return null;
                    }
                }
                else if (negated instanceof AndFormula) {
                    throw new RuntimeException("Illegal disjunction");
                }
                else if (negated instanceof KnowsFormula) {
                    throw new RuntimeException("Illegal negative modal");
                    //KnowsFormula f = (KnowsFormula) negated;
                    //knowsPossibly.get(f.getAgent()).add(f.getFormula().negate());
                }
                else if (negated instanceof BelievesFormula) {
                    throw new RuntimeException("Illegal negative modal");
                    //BelievesFormula f = (BelievesFormula) negated;
                    //believesPossibly.get(f.getAgent()).add(f.getFormula().negate());
                }
            }
        }

        for (String a : agents) {
            List x = Arrays.asList(knowsNecessarily.get(a));
            root.setKnown(a, foo(x));
            //root.setKnown(a, foo(Arrays.asList(knowsNecessarily.get(a))));
            List y = Arrays.asList(knowsNecessarily.get(a));
            root.setBelieved(a, foo(y));
            //root.setBelieved(a, foo(Arrays.asList(believesNecessarily.get(a))));
        }

        return root;
    }

    //private static Set<NDWorld> merge(Set<NDWorld> worlds) {
    //    Set<NDWorld> merged = new HashSet<>();
    //}




//     private static Set<NDWorld> foo(NDWorld rootWorld, List<Formula> formulas) {
//         Set<NDWorld> grounded = new HashSet<>();
//         grounded.add(rootWorld);
//         Map<String, Set<Formula>> knowsNecessarily = new HashMap<>();
//         Map<String, Set<Formula>> believesNecessarily = new HashMap<>();
//         Map<String, Set<Formula>> knowsPossibly = new HashMap<>();
//         Map<String, Set<Formula>> believesPossibly = new HashMap<>();
//         for (String a : agents) {
//             knowsNecessarily.put(a, new HashSet<>());
//             believesNecessarily.put(a, new HashSet<>());
//             knowsPossibly.put(a, new HashSet<>());
//             believesPossibly.put(a, new HashSet<>());
//         }
//         Stack<Formula> todo = new Stack<Formula>();
//         for (Formula f : formulas) {
//             todo.push(f);
//         }
//         while (!todo.empty() && !grounded.isEmpty()) {
//             Formula formula = todo.pop();
//             if (formula instanceof Literal) {
//                 Literal f = (Literal) formula;
//                 if (!f.getValue()) {
//                     return grounded;
//                 }
//             }
//             else if (formula instanceof Fluent) {
//                 Fluent f = (Fluent) formula;
//                 Set<NDWorld> newGrounded = new HashSet<>();
//                 for (NDWorld w : grounded) {
//                     if (w.setFluent(f, true)) {
//                         newGrounded.add(w);
//                     }
//                 }
//                 grounded = newGrounded;
//             }
//             else if (formula instanceof AndFormula) {
//                 AndFormula f = (AndFormula) formula;
//                 for (Formula conjunct : f.getFormulae()) {
//                     todo.push(conjunct);
//                 }
//             }
//             else if (formula instanceof KnowsFormula) {
//                 KnowsFormula f = (KnowsFormula) formula;
//                 knowsNecessarily.get(f.getAgent()).add(f.getFormula());
//             }
//             else if (formula instanceof BelievesFormula) {
//                 BelievesFormula f = (BelievesFormula) formula;
//                 believesNecessarily.get(f.getAgent()).add(f.getFormula());
//             }
//             else if (formula instanceof NotFormula) {
//                 Formula negated = ((NotFormula) formula).getFormula();
// 
//                 if (negated instanceof Literal) {
//                     Literal f = (Literal) negated;
//                     if (f.getValue()) {
//                         return grounded;
//                     }
//                 }
//                 else if (negated instanceof Fluent) {
//                     Fluent f = (Fluent) negated;
//                     Set<NDWorld> newGrounded = new HashSet<>();
//                     for (NDWorld w : grounded) {
//                         if (w.setFluent(f, false)) {
//                             newGrounded.add(w);
//                         }
//                     }
//                     grounded = newGrounded;
//                 }
//                 else if (negated instanceof AndFormula) {
//                     throw new RuntimeException("Illegal disjunction");
//                     //AndFormula f = (AndFormula) negated;
//                     //Set<NDWorld> newGrounded = new HashSet<>();
//                     //for (Formula f : f.getFormulae()) {
//                     //    newGrounded.addAll(recurse...
//                     //}
//                     //grounded = newGrounded;
//                 }
//                 else if (negated instanceof KnowsFormula) {
//                     KnowsFormula f = (KnowsFormula) negated;
//                     knowsPossibly.get(f.getAgent()).add(f.getFormula().negate());
//                 }
//                 else if (negated instanceof BelievesFormula) {
//                     BelievesFormula f = (BelievesFormula) negated;
//                     believesPossibly.get(f.getAgent()).add(f.getFormula().negate());
//                 }
//             }
//         }
// 
//         for (String a : agents) {
//             knowsNecessarily.put(a, new HashSet<>());
//             believesNecessarily.put(a, new HashSet<>());
//             knowsPossibly.put(a, new HashSet<>());
//             believesPossibly.put(a, new HashSet<>());
// 
// 
//         return grounded;
//     }



}

