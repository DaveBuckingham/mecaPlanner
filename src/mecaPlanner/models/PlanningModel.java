package mecaPlanner.models;

import mecaPlanner.formulae.FluentAtom;
import mecaPlanner.formulae.GeneralFormula;
import mecaPlanner.state.NDState;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.actions.Action;

import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;


public abstract class PlanningModel extends Model {

    Map<String,Map<EpistemicState,Set<Action>>> memo = new HashMap<>();

    private Set<Action> getPlannedActions(NDState ndState, String agent, GeneralFormula goal) {
        if (!memo.containsKey(agent)) {
            memo.put(agent, new HashMap<EpistemicState, Set<Action>>());
        }
        Set<EpistemicState> possibleStates = ndState.getEpistemicStates();
        if (!memo.get(agent).containsKey(ndState)) {
            Set<Action> succesfulActions = new HashSet<>();
            //memo.get(agent).addAll(search(ndState, agent, goal));
        }
        return memo.get(agent).get(ndState);
    }

    private Map<NDState, Set<Action>> search(NDState ndState, String agent, GeneralFormula goal) {
        HashMap<NDState, Set<Action>> policy = new HashMap<>();
        // IF WE USE 'NECESSARILY' HERE, MIGHT KEEP SEARCHING EVEN IF
        // AGENT KNOWS GOAL WILL BE REACHED OVER MULTIPLE STATES
        //if (ndState.possibly(goal)) {
        //    policy.put(NDState, new HashSet<Action>());
        //    return policy;
        //}
        Set<Action> successfulActions = new HashSet<>();
        for (Action a :  getSafeActions(ndState, agent)) {
        }
        return null;
    }


}
