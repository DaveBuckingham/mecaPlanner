package mecaPlanner;

import mecaPlanner.models.Model;
import mecaPlanner.formulae.FluentAtom;
import mecaPlanner.formulae.FluentAtom;
import mecaPlanner.formulae.BeliefFormula;
import mecaPlanner.formulae.GeneralFormula;
import mecaPlanner.formulae.GeneralFormulaAnd;
import mecaPlanner.state.EpistemicState;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class Domain implements java.io.Serializable {

    private Set<FluentAtom> allAtoms;
    //private Set<FluentAtom> constants;
    //private Set<BeliefFormula> initiallyStatements;
    private Map<String,Set<Action>> actions;
    private Map<String, Map<String,Action>> actionsBySignature;

    private Set<String> allAgents;
    private List<String> nonPassiveAgents;
    private Set<String> passiveAgents;



    public Domain() {
        allAtoms = new HashSet<>();
        //constants = new HashSet<>();
        actions = new HashMap<>();
        actionsBySignature = new HashMap<>();;

        allAgents = new HashSet<>();
        nonPassiveAgents = new ArrayList<>();
        passiveAgents = new HashSet<>();

    }

    public Set<FluentAtom> getAllAtoms() {
        return allAtoms;
    }

    //public Set<FluentAtom> getConstants() {
    //    return constants;
    //}


    public Map<String,Set<Action>> getActionMap() {
        return actions;
    }

    public Set<Action> getAllActions() {
        Set<Action> allActions = new HashSet<>();
        for (Set<Action> agentActions : actions.values()) {
            allActions.addAll(agentActions);
        }
        return allActions;
    }

    public Set<Action> getAgentActions(String agent) {
        return actions.get(agent);
    }

    public Set<Action> getAgentActions(int depth) {
        return actions.get(agentAtDepth(depth));
    }


    public Action getActionBySignature(String agent, String signature) {
        if (!actionsBySignature.get(agent).containsKey(signature)) {
            throw new RuntimeException("unknown action signature: " + signature);
        }
        return actionsBySignature.get(agent).get(signature);

    }

    public Set<String> getPassiveAgents() {
        return passiveAgents;
    }

    public List<String> getNonPassiveAgents() {
        return nonPassiveAgents;
    }

    public String agentAtDepth(int depth) {
        return nonPassiveAgents.get(depth % nonPassiveAgents.size());
    }

    // public boolean isEnvironmentAgent(String agent) {
    //     return environmentAgents.contains(agent);
    // }

    // public boolean isEnvironmentAgent(int depth) {
    //     return environmentAgents.contains(agentAtDepth(depth));
    // }

    // public boolean isSystemAgent(String agent) {
    //     return systemAgents.contains(agent);
    // }

    // public boolean isSystemAgent(int depth) {
    //     return systemAgents.contains(agentAtDepth(depth));
    // }



    public Set<String> getAllAgents() {
        return allAgents;
    }

    public Set<String> getAgents() {
        return allAgents;
    }

    public void addAtom(FluentAtom f) {
        allAtoms.add(f);
    }

    public void addAtoms(Set<FluentAtom> f) {
        allAtoms.addAll(f);
    }

    //public void addConstant(FluentAtom f) {
    //    constants.add(f);
    //}


    public void addAgent(String agent) {
        assert (!allAgents.contains(agent));
        allAgents.add(agent);
        nonPassiveAgents.add(agent);
        actions.put(agent, new HashSet<Action>());
        actionsBySignature.put(agent, new HashMap<String, Action>());;
    }

    public void addPassive(String agent) {
        assert (!allAgents.contains(agent));
        passiveAgents.add(agent);
        allAgents.add(agent);
    }

    public void addAction(String agent, Action newAction) {
        assert(actions.containsKey(agent));
        actions.get(agent).add(newAction);
        actionsBySignature.get(agent).put(newAction.getSignature(), newAction);
    }


    public boolean isAgent(String s) {
        return allAgents.contains(s);
    }

    public boolean isNonPassiveAgent(String s) {
        return nonPassiveAgents.contains(s);
    }





    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("AGENTS:\n");
        for (String a : nonPassiveAgents) {
            str.append(a);
            str.append("\n");
        }
        str.append("\n");

        str.append("PASSIVE:\n");
        for (String a : passiveAgents) {
            str.append(a);
            str.append("\n");
        }
        str.append("\n");


        for (String agent : actions.keySet()) {
            str.append("ACTIONS (" + agent.toString() + "):\n");
            for (Action action : actions.get(agent)) {
                str.append(action.toString());
                str.append("\n");
            }
            str.append("\n");
        }

        return str.toString();
        //System.out.println(str.toString());
    }





}

