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


public class DragonModel extends Model {

    public DragonModel(String agent, Domain domain) {
        super(agent, domain);
    }

    public Set<Action> getPrediction(EpistemicState eState) {
        Set<Action> predictions = new HashSet<>();
        predictions.add(getSinglePrediction(eState));
        return predictions;
    }



    public Action getSinglePrediction(EpistemicState eState) {
        return null;

//        String dragonLocation = ndStas
//        String sheepLocation = null;
//        String knightLocation = null;
//        boolean sheep_alive = false;
//        boolean knight_alive = false;
//        boolean dueling = false;
//
//        for (FluentAtom atom : domain.getAllAtoms()) {
//            if (ndState.necessarily(atom)) {
//                if (atom.getName().equals("dueling")) {
//                    dueling = true;
//                }
//                else if (atom.getName().equals("sheep_alive")) {
//                    sheep_alive = true;
//                }
//                else if (atom.getName().equals("knight_alive")) {
//                    knight_alive = true;
//                }
//                else if (atom.getName().equals("at")) {
//                    if (atom.getParameter(0).equals("sheep")) {
//                        sheepLocation = atom.getParameter(1);
//                    }
//                    else if (atom.getParameter(0).equals("dragon")) {
//                        dragonLocation = atom.getParameter(1);
//                    }
//                    else if (atom.getParameter(0).equals("knight")) {
//                        knightLocation = atom.getParameter(1);
//                    }
//                }
//            }
//        }
//
//
//        if (dragonLocation == null) {
//            throw new RuntimeException("failed to determine dragon location");
//        }
//
//        if (dragonLocation.equals(sheepLocation)) {
//            return domain.getActionBySignature("dragon", String.format("eat_sheep(%s)", dragonLocation));
//        }
//
//        if (dragonLocation.equals(knightLocation)) {
//            if (dueling) {
//                return domain.getActionBySignature("dragon", String.format("eat_knight(%s)", dragonLocation));
//            }
//            return domain.getActionBySignature("dragon", String.format("duel(%s)", dragonLocation));
//        }
//
//        if (sheepLocation == null) {
//            return domain.getActionBySignature("dragon", "wait()");
//        }
//
//        return domain.getActionBySignature("dragon", String.format("move(%s,%s)", dragonLocation, sheepLocation));

    }



}
