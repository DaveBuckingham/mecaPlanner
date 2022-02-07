package mecaPlanner.agents;

import mecaPlanner.state.*;
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


public class CleanAgent extends Agent {

    public CleanAgent(String agent, Domain domain) {
        super(agent, domain);
    }

    public Set<Action> getPrediction(State eState) {
        NDState ndState = eState.getBeliefPerspective(agent);
        Set<Action> allActions = getSafeActions(ndState);
        Set<Action> prediction = new HashSet<>();
        for (Action a : allActions) {
            if (a.getName().equals("vacuum")) {
                prediction.add(a);
                return prediction;
            }
        }

        for (Action a: allActions) {
            if (a.getName().equals("roomba_livingR_to_kitchen")) {
                String to_room = "kitchen";
                if (ndState.possibly(new Fluent("dirty", to_room))) {
                    prediction.add(a);
                } 
            }
            else if (a.getName().equals("roomba_livingR_to_bedR")) {
                String to_room = "bedR";
                if (ndState.possibly(new Fluent("dirty", to_room))) {
                    prediction.add(a);
                } 
            }
            else if (a.getName().equals("roomba_kitchen_to_livingR()") ) {
                prediction.add(a);
            }
            else if (a.getName().equals("roomba_bedR_to_livingR()")) {
                prediction.add(a);
            }
        }
        if(!prediction.isEmpty()) {
            return prediction;
        }

        // if in the bedroom: move living room 
        // if in the kitchen: move living room

        prediction.add(getSafeActionBySignature("wait(roomba,livingR)", ndState));
        return prediction;

        
    }

}
