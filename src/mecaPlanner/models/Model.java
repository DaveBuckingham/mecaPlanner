package mecaPlanner.models;

import mecaPlanner.state.NDState;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.Action;
import mecaPlanner.Domain;


import java.util.Objects;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;


public abstract class Model implements java.io.Serializable {

    protected String agent;
    protected Domain domain;


    public Model(String agent, Domain domain) {
        this.agent = agent;
        this.domain = domain;
    }

    // REQUIREMENTS:
    // 1. each action requires and deletes at least one atom in atoms
    // 2. each action creates some different atom in atoms
    // 3. each atom is created by at least one action
    // 4. each atom is a precondition of (and is deleted by) at least one action
//    protected final Map<FluentAtom, Map<FluentAtom, int>> computeDistances(Set<OnticAction> actions,
//                                                                           Set<FluentAtom> atoms) {
//        Map<FluentAtom, Map<FluentAtom, int>> distanceMap = new HashMap<>();
//
//        Map<FluentAtom, Set<Action>> removedBy = new HashMap<>();
//        Map<Action, Set<FluentAtom>> adds = new HashMap<>();
//
//        for (Action action : actions) {
//            adds.put(action, new HashSet<FluentAtom>());
//        }
//
//        for (FluentAtom atom : atoms) {
//            removedBy.put(atom, new HashSet<Action>());
//        }
//
//        for (Action action : actions) {
//            for (BeliefFormula precondition : action.getPreconditions) {
//                if (precondition instanceof FluentAtom) {
//                    FluentAtom atomicPrecondition = (FluentAtom) precondition;
//                    if (atoms.contains(atomicPrecondition)) {
//                        for (FluentLiteral effect : action.getEffects()) {
//                            if (atomicPrecondition.equals(effect.getAtom()) && effect.getValue().equals(false)) {
//                                removedBy.get(atomicPrecondition).add(action);
//                                break;
//                            }
//                        }
//                    }
//                }
//            }
//            for (FluentLiteral effect : action.getEffects()) {
//                if (effect.getValue().equals(true) && atoms.contains(effect.getAtom())) {
//                    adds.get(action).add(effect.getAtom());
//                }
//            }
//        }
//    }
//
//    Set<FluentAtom> badAtoms = new HashSet<>(atoms);
//    for (Action action : actions) {
//        boolean hasPrecondition = false;
//        boolean hasEffect = false;
//        for (FluentAtom atom : atoms) {
//            for (BeliefFormula precondition : action.getPreconditions) {
//                if (precondition instanceof FluentAtom) {
//                    FluentAtom atomicPrecondition = (FluentAtom) precondition;
//                    if (atoms.contains(atomicPrecondition)) {
//                    }
//                }
//            }
//        }
//    }



    // GET ALL ACTIONS WHOSE PRECONDITIONS ARE SATISFIED IN ALL DESIGANTED WORLDS
    public Set<Action> getSafeActions(NDState ndState) {
        Set<Action> safeActions = new HashSet<Action>();
        for (Action action : domain.getAgentActions(agent)) {
            if (action.necessarilyExecutable(ndState)){
                safeActions.add(action);
            }
        }
        return safeActions;
    }

    public Action getSafeActionBySignature(String signature, NDState ndState) {
        Action action = domain.getActionBySignature(agent, signature.replaceAll("\\s+",""));
        if (!action.necessarilyExecutable(ndState)) {
            throw new RuntimeException("requested action " + 
                                       signature + " not necessarily executable for agent " +
                                       agent.toString() + " in ndState: " + ndState);
        }
        return action;
    }

    // public static Set<Action> getActionSetBySignature(String signature, EpistemicState eState, String agent) {
    //     Set<Action> singleton = new HashSet<>();
    //     singleton.add(getSafeActionBySignature(signature, eState, agent));
    //     return singleton;
    // }

    public abstract Set<Action> getPrediction(NDState ndState);

    public Model update(NDState perspective, Action action) {
        return this;
    }


}

