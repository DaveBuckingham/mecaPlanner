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

    public Set<Action> getPrediction(NDState ndState) {
        Set<Action> predictions = new HashSet<>();
        Action p = getSinglePrediction(ndState);
        predictions.add(p);
        return predictions;
    }


    public Action getSinglePrediction(NDState ndState) {
        return null;

//        String dragonLocation = null;
//        String knightLocation = null;
//        boolean knight_alive = false;
//        boolean dueling = false;
//
//        for (FluentAtom atom : domain.getAllAtoms()) {
//            if (ndState.necessarily(atom)) {
//                if (atom.getName().equals("dueling")) {
//                    dueling = true;
//                }
//                else if (atom.getName().equals("knight_alive")) {
//                    knight_alive = true;
//                }
//                else if (atom.getName().equals("at")) {
//                    if (atom.getParameter(0).equals("dragon")) {
//                        dragonLocation = atom.getParameter(1);
//                    }
//                    else if (atom.getParameter(0).equals("knight")) {
//                        knightLocation = atom.getParameter(1);
//                    }
//                }
//            }
//        }
//
//        if (!knight_alive) {
//            return domain.getActionBySignature("knight", "wait()");
//        }
//
//        if (knightLocation == null) {
//            throw new RuntimeException("failed to determine knight location");
//        }
//
//        if (dragonLocation == null) {
//            return domain.getActionBySignature("knight", "wait()");
//        }
//
//        if (dragonLocation.equals(knightLocation)) {
//            return domain.getActionBySignature("knight", String.format("duel(%s)", knightLocation));
//        }
//
//        return domain.getActionBySignature("knight", String.format("move(%s,%s)", knightLocation, dragonLocation));

    }



}
