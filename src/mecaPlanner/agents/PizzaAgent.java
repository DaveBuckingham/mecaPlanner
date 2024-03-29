package mecaPlanner.agents;

import mecaPlanner.state.*;
import mecaPlanner.actions.*;
import mecaPlanner.formulae.Fluent;
import mecaPlanner.Log;
import mecaPlanner.Domain;

import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;


public class PizzaAgent extends Agent {

    public PizzaAgent(String agent, Domain domain) {
        super(agent, domain);
    }

    public Set<Action> getPrediction(PointedPlausibilityState eState) {
        PlausibilityState ndState = eState.getBeliefPerspective(agent);
        Set<Action> allActions = getSafeActions(ndState);
        Set<Action> prediction = new HashSet<>();
        for (Action a : allActions) {
            if (a.getName().equals("eat")) {
                prediction.add(a);
                return prediction;
            }
        }

        if (!necessarily(ndState, "door_open")) {
            prediction.add(getSafeActionBySignature("open_door()", ndState));
        }
        else if (necessarily(ndState, "at", agent, "room1")) {
            prediction.add(getSafeActionBySignature("move(human1,room1,room2)", ndState));
        }
        else if (necessarily(ndState, "at", agent, "room2")) {
            prediction.add(getSafeActionBySignature("move(human1,room2,room1)", ndState));
        }
        else {
            throw new RuntimeException("Pizza Agent failed to determine state");
        }
        return prediction;


    }


}
