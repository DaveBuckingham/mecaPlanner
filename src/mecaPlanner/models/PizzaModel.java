package mecaPlanner.models;

import mecaPlanner.formulae.FluentAtom;
import mecaPlanner.state.NDState;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.actions.Action;
import mecaPlanner.agents.Agent;
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


public class PizzaModel extends Model {

    public Set<Action> getPrediction(NDState ndState, Agent agent) {
        Set<Action> allActions = getSafeActions(ndState, agent);
        Set<Action> prediction = new HashSet<>();
        for (Action a : allActions) {
            if (a.getName().equals("eat")) {
                prediction.add(a);
                return prediction;
            }
        }

        if (!ndState.necessarily(new FluentAtom("door_open"))) {
            prediction.add(getSafeActionBySignature("open_door()", ndState, agent));
        }
        else if (ndState.necessarily(new FluentAtom("at", agent.getName(), "room1"))) {
            prediction.add(getSafeActionBySignature("move(room1,room2)", ndState, agent));
        }
        else if (ndState.necessarily(new FluentAtom("at", agent.getName(), "room2"))) {
            prediction.add(getSafeActionBySignature("move(room2,room1)", ndState, agent));
        }
        else {
            throw new RuntimeException("Pizza Model failed to determine state");
        }
        return prediction;


    }


}
