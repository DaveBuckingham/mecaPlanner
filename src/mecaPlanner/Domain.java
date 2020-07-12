package mecaPlanner;

import mecaPlanner.formulae.FluentAtom;
import mecaPlanner.formulae.BeliefFormula;
import mecaPlanner.formulae.GeneralFormula;
import mecaPlanner.actions.Action;
import mecaPlanner.agents.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class Domain {

    private static Set<FluentAtom> allAtoms;
    private static Set<FluentAtom> constants;
    private static Set<BeliefFormula> initiallyStatements;
    private static Set<GeneralFormula> goals;
    private static Map<Agent,Set<Action>> actions;
    private static Map<Agent, Map<String,Action>> actionsBySignature;

    private static Set<SystemAgent> systemAgents;
    private static Set<EnvironmentAgent> environmentAgents;
    private static Set<PassiveAgent> passiveAgents;
    private static List<Agent> nonPassiveAgents;
    private static Set<Agent> allAgents;

    private static Set<String> systemAgentNames;
    private static Set<String> environmentAgentNames;
    private static Set<String> passiveAgentNames;

    private static Map<String, Agent> agentsByName;





    public static void clear() {
        allAtoms = new HashSet<>();
        constants = new HashSet<>();
        initiallyStatements = new HashSet<>();
        goals = new HashSet<>();
        actions = new HashMap<>();
        actionsBySignature = new HashMap<>();;

        systemAgents = new HashSet<>();
        environmentAgents = new HashSet<>();
        passiveAgents = new HashSet<>();
        nonPassiveAgents = new ArrayList<>();
        allAgents = new HashSet<>();

        systemAgentNames = new HashSet<>();
        environmentAgentNames = new HashSet<>();
        passiveAgentNames = new HashSet<>();

        agentsByName = new HashMap<>();
    }


    public static Set<FluentAtom> getAllAtoms() {
        return allAtoms;
    }
    public static Set<FluentAtom> getConstants() {
        return constants;
    }
    public static Set<BeliefFormula> getInitiallyStatements() {
        return initiallyStatements;
    }

    public static Set<GeneralFormula> getGoals() {
        return goals;
    }

    public static Map<Agent,Set<Action>> getActionMap() {
        return actions;
    }

    public static Set<Action> getAllActions() {
        Set<Action> allActions = new HashSet<>();
        for (Set<Action> agentActions : actions.values()) {
            allActions.addAll(agentActions);
        }
        return allActions;
    }

    public static Set<Action> getAgentActions(Agent agent) {
        return actions.get(agent);
    }

    public static Action getActionBySignature(Agent agent, String signature) {
        if (!actionsBySignature.get(agent).containsKey(signature)) {
            throw new RuntimeException("unknown action signature: " + signature);
        }
        return actionsBySignature.get(agent).get(signature);

    }

    public static Set<SystemAgent> getSystemAgents() {
        return systemAgents;
    }

    public static Set<EnvironmentAgent> getEnvironmentAgents() {
        return environmentAgents;
    }

    public static Set<PassiveAgent> getPassiveAgents() {
        return passiveAgents;
    }

    public static List<Agent> getNonPassiveAgents() {
        return nonPassiveAgents;
    }

    public static Agent agentAtDepth(int depth) {
        return nonPassiveAgents.get(depth % nonPassiveAgents.size());
    }


    public static Set<Agent> getAllAgents() {
        return allAgents;
    }

    public static Set<Agent> getAgents() {
        return allAgents;
    }

    public static Agent getAgentByName(String name) {
        return agentsByName.get(name);
    }




    public static void addAtom(FluentAtom f) {
        allAtoms.add(f);
    }

    public static void addAtoms(Set<FluentAtom> atoms) {
        allAtoms.addAll(atoms);
    }

    public static void addConstant(FluentAtom f) {
        constants.add(f);
    }

    public static void addInitiallyStatement(BeliefFormula statement) {
        checkAtoms(statement);
        initiallyStatements.add(statement);
    }

    public static void addGoal(GeneralFormula newGoal) {
        checkAtoms(newGoal);
        goals.add(newGoal);
    }

    public static void addSystemAgent(SystemAgent agent) {
        assert (!allAgents.contains(agent));
        systemAgents.add(agent);
        systemAgentNames.add(agent.getName());
        agentsByName.put(agent.getName(), agent);
        allAgents.add(agent);
        nonPassiveAgents.add(agent);
        actions.put(agent, new HashSet<Action>());
        actionsBySignature.put(agent, new HashMap<String, Action>());;
    }

    public static void addEnvironmentAgent(EnvironmentAgent agent) {
        assert (!allAgents.contains(agent));
        environmentAgents.add(agent);
        environmentAgentNames.add(agent.getName());
        agentsByName.put(agent.getName(), agent);
        allAgents.add(agent);
        nonPassiveAgents.add(agent);
        actions.put(agent, new HashSet<Action>());
        actionsBySignature.put(agent, new HashMap<String, Action>());;
    }

    public static void addPassiveAgent(PassiveAgent agent) {
        assert (!allAgents.contains(agent));
        passiveAgents.add(agent);
        passiveAgentNames.add(agent.getName());
        agentsByName.put(agent.getName(), agent);
        allAgents.add(agent);
    }

    public static void addAction(Agent agent, Action newAction) {
        checkAtoms(newAction.getPrecondition());
        // SHOULD ALSO checkAtoms() FOR EFFECT CONDITIONS AND OBSERVES CONDITIONS
        assert(actions.containsKey(agent));
        actions.get(agent).add(newAction);
        actionsBySignature.get(agent).put(newAction.getSignature(), newAction);
    }

    public static boolean isSystemAgentName(String s) {
        return systemAgentNames.contains(s);
    }

    public static boolean isEnvironmentAgentName(String s) {
        return environmentAgentNames.contains(s);
    }

    public static boolean isPassiveAgentName(String s) {
        return passiveAgentNames.contains(s);
    }

    public static boolean isAgentName(String s) {
        return (systemAgentNames.contains(s) ||
                environmentAgentNames.contains(s) ||
                passiveAgentNames.contains(s));
    }




    private static void checkAtoms(GeneralFormula f) {
        for (FluentAtom a : f.getAllAtoms()) {
            //if (!allAtoms.contains(a) && !constants.contains(a)) {
            if (!allAtoms.contains(a)) {
                System.out.println("Undefined atom: \"" + a + "\". Defined atoms are: " + allAtoms);
                //for (FluentAtom f : allAtoms) {
                //    System.out.println(f);
                //}
                System.exit(1);
            }
        }
    }


    public static void printDomain() {
        StringBuilder str = new StringBuilder();

        str.append("SYSTEM AGENTS:\n");
        for (Agent a : systemAgents) {
            str.append(a.getName());
            str.append("\n");
        }
        str.append("\n");

        str.append("ENVIRONMENT AGENTS:\n");
        for (Agent a : environmentAgents) {
            str.append(a.getName());
            str.append("\n");
        }
        str.append("\n");

        str.append("INITIALLY:\n");
        for (BeliefFormula i : getInitiallyStatements()) {
            str.append(i.toString());
            str.append("\n");
        }
        str.append("\n");

        str.append("GOALS:\n");
        for (GeneralFormula g : goals) {
            str.append(g.toString());
            str.append("\n");
        }
        str.append("\n");

        for (Agent agent : actions.keySet()) {
            str.append("ACTIONS (" + agent.toString() + "):\n");
            for (Action action : actions.get(agent)) {
                str.append(action.toString());
                str.append("\n");
            }
            str.append("\n");
        }

        //return str.toString();
        System.out.println(str.toString());
    }





}

