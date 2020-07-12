package mecaPlanner.models;

import mecaPlanner.state.NDState;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.actions.Action;
import mecaPlanner.agents.Agent;

import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;


public class TrivialModel extends Model {

    private Action waitAction;

    public TrivialModel() {
        super();
    }

    public Set<Action> getPrediction(NDState ndState, Agent agent) {

        return Model.getSafeActions(ndState, agent);

    }



}
