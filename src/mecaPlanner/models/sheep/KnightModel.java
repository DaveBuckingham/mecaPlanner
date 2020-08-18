package mecaPlanner.models.sheep;

import mecaPlanner.state.NDState;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.Action;
import mecaPlanner.Domain;
import mecaPlanner.models.Model;

import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;


public class KnightModel extends Model {

    public KnightModel(String agent, Domain domain) {
        super(agent, domain);
    }

    private Action waitAction;


    public Set<Action> getPrediction(NDState ndState) {

        return getSafeActions(ndState);

    }



}
