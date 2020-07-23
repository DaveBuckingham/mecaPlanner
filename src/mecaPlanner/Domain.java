package mecaPlanner;

import mecaPlanner.models.Model;
import mecaPlanner.formulae.FluentAtom;
import mecaPlanner.formulae.FluentAtom;
import mecaPlanner.formulae.BeliefFormula;
import mecaPlanner.formulae.GeneralFormula;
import mecaPlanner.formulae.GeneralFormulaAnd;
import mecaPlanner.actions.Action;
import mecaPlanner.state.EpistemicState;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class Domain {

    private Set<FluentAtom> allAtoms;
    private Set<FluentAtom> constants;
    //private Set<BeliefFormula> initiallyStatements;
    private EpistemicState startState;
    private Set<GeneralFormula> goals;
    private Map<String,Set<Action>> actions;
    private Map<String, Map<String,Action>> actionsBySignature;

    private Set<String> allAgents;
    private Set<String> systemAgents;
    private Set<String> environmentAgents;
    private Set<String> passiveAgents;
    private List<String> nonPassiveAgents;

    private Map<String, Model> startingModels;


    public Domain() {
        allAtoms = new HashSet<>();
        constants = new HashSet<>();
        goals = new HashSet<>();
        actions = new HashMap<>();
        actionsBySignature = new HashMap<>();;

        allAgents = new HashSet<>();
        systemAgents = new HashSet<>();
        environmentAgents = new HashSet<>();
        passiveAgents = new HashSet<>();
        nonPassiveAgents = new ArrayList<>();

        startingModels = new HashMap<>();
    }

    public void check() {
        assert(!goals.isEmpty());
    }


    public Set<FluentAtom> getAllAtoms() {
        return allAtoms;
    }
    public Set<FluentAtom> getConstants() {
        return constants;
    }
    public EpistemicState getStartState() {
        return startState;
    }

    public Set<GeneralFormula> getGoals() {
        return goals;
    }

    public GeneralFormula getGoal() {
        if (goals.size() == 1) {
            return goals.iterator().next();
        }
        return new GeneralFormulaAnd(goals);
    }

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

    public Set<String> getSystemAgents() {
        return systemAgents;
    }

    public Set<String> getEnvironmentAgents() {
        return environmentAgents;
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

    public boolean isEnvironmentAgent(String agent) {
        return environmentAgents.contains(agent);
    }

    public boolean isEnvironmentAgent(int depth) {
        return environmentAgents.contains(agentAtDepth(depth));
    }

    public boolean isSystemAgent(String agent) {
        return systemAgents.contains(agent);
    }

    public boolean isSystemAgent(int depth) {
        return systemAgents.contains(agentAtDepth(depth));
    }



    public Set<String> getAllAgents() {
        return allAgents;
    }

    public Set<String> getAgents() {
        return allAgents;
    }

    public Map<String, Model> getStartingModels() {
        return startingModels;
    }




    public void addAtom(FluentAtom f) {
        allAtoms.add(f);
    }

    public void addAtoms(Set<FluentAtom> atoms) {
        allAtoms.addAll(atoms);
    }

    public void addConstant(FluentAtom f) {
        constants.add(f);
    }

    public void setStartState(EpistemicState state) {
        //checkAtoms(statement);
        //initiallyStatements.add(statement);
        this.startState = state;
    }

    public void addGoal(GeneralFormula newGoal) {
        checkAtoms(newGoal);
        goals.add(newGoal);
    }

    public void addSystemAgent(String agent) {
        assert (!allAgents.contains(agent));
        systemAgents.add(agent);
        allAgents.add(agent);
        nonPassiveAgents.add(agent);
        actions.put(agent, new HashSet<Action>());
        actionsBySignature.put(agent, new HashMap<String, Action>());;
    }

    public void addEnvironmentAgent(String agent, Model model) {
        assert (!allAgents.contains(agent));
        environmentAgents.add(agent);
        allAgents.add(agent);
        nonPassiveAgents.add(agent);
        actions.put(agent, new HashSet<Action>());
        actionsBySignature.put(agent, new HashMap<String, Action>());;
        startingModels.put(agent, model);
    }

    public void addPassiveAgent(String agent) {
        assert (!allAgents.contains(agent));
        passiveAgents.add(agent);
        allAgents.add(agent);
    }

    public void addAction(String agent, Action newAction) {
        checkAtoms(newAction.getPrecondition());
        // SHOULD ALSO checkAtoms() FOR EFFECT CONDITIONS AND OBSERVES CONDITIONS
        assert(actions.containsKey(agent));
        actions.get(agent).add(newAction);
        actionsBySignature.get(agent).put(newAction.getSignature(), newAction);
    }


    public boolean isAgent(String s) {
        return allAgents.contains(s);
    }




    // RECURSIVELY CHECK THAT EVERY ATOM IN THE FORMULA HAS BEEN DEFINED
    public void checkAtoms(GeneralFormula f) {
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


    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("SYSTEM AGENTS:\n");
        for (String a : systemAgents) {
            str.append(a);
            str.append("\n");
        }
        str.append("\n");

        str.append("ENVIRONMENT AGENTS:\n");
        for (String a : environmentAgents) {
            str.append(a);
            str.append("\n");
        }
        str.append("\n");

        str.append("INITIALLY:\n");
        str.append(startState.toString());
        str.append("\n");

        str.append("GOALS:\n");
        for (GeneralFormula g : goals) {
            str.append(g.toString());
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

