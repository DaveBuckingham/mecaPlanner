package mecaPlanner;

import mecaPlanner.agents.Agent;
import mecaPlanner.state.State;
import mecaPlanner.state.EventModel;
import mecaPlanner.state.Action;
import mecaPlanner.state.Transformer;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class Domain implements java.io.Serializable {

    private List<Transformer> transformerList;

    private Set<EventModel> eventModels;

    private Map<String,Set<Action>> actions;
    private Map<String, Map<String,Action>> actionsBySignature;

    private List<String> turnOrder;

    private List<String> allAgents;

    private Set<String> systemAgents;
    private Map<String, Agent> environmentAgents;
    private Set<String> passiveAgents;

    private List<Boolean> systemAgentIndeces;




    public Domain() {
        eventModels = new HashSet<>();
        actions = new HashMap<>();
        actionsBySignature = new HashMap<>();
        transformerList = new ArrayList<>();

        turnOrder = new ArrayList<>();
        allAgents = new ArrayList<>();
        systemAgents = new HashSet<>();
        environmentAgents = new HashMap<>();
        passiveAgents = new HashSet<>();

        systemAgentIndeces = new ArrayList<>();

    }


    public Map<String,Set<Action>> getActionMap() {
        return actions;
    }

    public Set<Action> getActions(String agent) {
        return actions.get(agent);
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

    public Set<Action> getAgentActions(Integer time) {
        return getAgentActions(agentAtTime(time));
    }


    //public Set<Action> getAgentActions(int depth) {
    //    return actions.get(agentAtDepth(depth));
    //}


    public Action getActionBySignature(String agent, String signature) {
        if (!actionsBySignature.get(agent).containsKey(signature)) {
            throw new RuntimeException("unknown action signature: " + signature);
        }
        return actionsBySignature.get(agent).get(signature);

    }

    public Map<String, Agent> getEnvironmentAgents() {
        return environmentAgents;
    }

    public List<String> getTurnOrder() {
        return turnOrder;
    }

    //public Set<String> getPassiveAgents() {
    //    return passiveAgents;
    //}


    public String agentAtTime(int i) {
        return turnOrder.get(i % turnOrder.size());
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



    public List<String> getAllAgents() {
        return allAgents;
    }


    public void addSystemAgent(String name) {
        assert (!allAgents.contains(name));
        systemAgents.add(name);
        allAgents.add(name);
        turnOrder.add(name);
        systemAgentIndeces.add(true);
        actions.put(name, new HashSet<Action>());
        actionsBySignature.put(name, new HashMap<String, Action>());
    }

    public void addEnvironmentAgent(String name, Agent model) {
        assert (!allAgents.contains(name));
        environmentAgents.put(name, model);
        allAgents.add(name);
        turnOrder.add(name);
        systemAgentIndeces.add(false);
        actions.put(name, new HashSet<Action>());
        actionsBySignature.put(name, new HashMap<String, Action>());
    }

    public void addPassiveAgent(String name) {
        assert (!allAgents.contains(name));
        passiveAgents.add(name);
        allAgents.add(name);
    }

    public void addEventModel(EventModel e) {
        eventModels.add(e);
        transformerList.add(e);
    }

    public Set<EventModel> getEventModels() {
        return eventModels;
    }

    public void addAction(Action newAction) {
        String agent = newAction.getActor();
        assert(actions.containsKey(agent));
        actions.get(agent).add(newAction);
        actionsBySignature.get(agent).put(newAction.getSignature(), newAction);
        transformerList.add(newAction);
    }

    public List<Transformer> getTransformerList() {
        return transformerList;
    }

    public boolean isAgent(String s) {
        return allAgents.contains(s);
    }

    public boolean isSystemAgentIndex(Integer i) {
        return systemAgentIndeces.get(i % systemAgentIndeces.size());
    }

    public boolean isNonPassiveAgent(String s) {
        return ((systemAgents.contains(s) || environmentAgents.containsKey(s)) && !passiveAgents.contains(s));
    }



    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("AGENTS:\n");
        for (String a : turnOrder) {
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
    }





}

