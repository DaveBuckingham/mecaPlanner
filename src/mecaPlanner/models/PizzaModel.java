package mecaPlanner.models;

import mecaPlanner.state.*;
import mecaPlanner.formulae.booleanFormulae.BooleanFluent;
import mecaPlanner.Action;
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

    public PizzaModel(String agent, Domain domain) {
        super(agent, domain);
    }

    public Set<Action> getPrediction(NDState ndState) {
        Set<Action> allActions = getSafeActions(ndState);
        Set<Action> prediction = new HashSet<>();
        for (Action a : allActions) {
            if (a.getName().equals("eat")) {
                prediction.add(a);
                return prediction;
            }
        }

        if (!ndState.necessarily(new BooleanFluent("door_open"))) {
            prediction.add(getSafeActionBySignature("open_door()", ndState));
        }
        else if (ndState.necessarily(new BooleanFluent("at", agent, "room1"))) {
            prediction.add(getSafeActionBySignature("move(human1,room1,room2)", ndState));
        }
        else if (ndState.necessarily(new BooleanFluent("at", agent, "room2"))) {
            prediction.add(getSafeActionBySignature("move(human1,room2,room1)", ndState));
        }
        else {
            throw new RuntimeException("Pizza Model failed to determine state");
        }
        return prediction;


    }


}
