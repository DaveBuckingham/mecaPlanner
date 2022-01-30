package mecaPlanner.agents;

import java.util.Set;
import java.util.HashSet;

import mecaPlanner.state.NDState;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.Action;
import mecaPlanner.Domain;


public abstract class Agent implements java.io.Serializable {

    protected String agent;
    protected Domain domain;

    public Agent(String agent, Domain domain) {
        this.agent = agent;
        this.domain = domain;
    }

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

    // TAKE AN ACTION SIGNATURE (ITS NAME AND PARAMETERS) AND TRY TO FIND AND RETURN A CORRESPONDING ACTION.
    // THROWS AN EXCEPTION IF THE ACTION DOESN'T EXIST OR IF THE E-AGENT IS NOT CERTAIN THAT THE
    // ACTION CAN BE EXECUTED.
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

    public abstract Set<Action> getPrediction(EpistemicState eState);

    public Agent update(EpistemicState eState, Action action) {
        return this;
    }
}

