package mecaPlanner.agents;

import mecaPlanner.state.*;
import mecaPlanner.actions.*;
import mecaPlanner.Domain;

import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;


public class TrivialAgent extends Agent {

    public TrivialAgent(String agent, Domain domain) {
        super(agent, domain);
    }

    private Action waitAction;


    public Set<Action> getPrediction(PointedPlausibilityState eState) {

        return getSafeActions(eState.getBeliefPerspective(agent));

    }



}
