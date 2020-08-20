package mecaPlanner.models.sheep;

import mecaPlanner.state.NDState;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.Action;
import mecaPlanner.Domain;
import mecaPlanner.formulae.FluentAtom;
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

    public Set<Action> getPrediction(NDState ndState) {
        Set<Action> predictions = new HashSet<>();
        predictions.add(getSinglePrediction(ndState));
        return predictions;
    }



    public Action getSinglePrediction(NDState ndState) {

        String dragonLocation = null;
        String sheepLocation = null;
        String knightLocation = null;
        boolean treasure = false;

        for (FluentAtom atom : domain.getAllAtoms()) {
            if (ndState.necessarily(atom)) {
                if (atom.getName().equals("at")) {
                    if (atom.getParameter(0).equals("sheep")) {
                        sheepLocation = atom.getParameter(1);
                    }
                    else if (atom.getParameter(0).equals("dragon")) {
                        dragonLocation = atom.getParameter(1);
                    }
                    else if (atom.getParameter(0).equals("knight")) {
                        knightLocation = atom.getParameter(1);
                    }
                }
                else if (atom.getName().equals("knight_has_treasure")) {
                    treasure = true;
                }
            }
        }

        if (dragonLocation == null) {
            throw new RuntimeException("failed to determine dragon location");
        }

        if (dragonLocation.equals(knightLocation)) {
            return domain.getActionBySignature("dragon", String.format("duel(%s)", dragonLocation));
        }


        if (treasure) {
            return domain.getActionBySignature("dragon", String.format("move(%s,%s)", dragonLocation, knightLocation));
        }

        if (sheepLocation == null) {
            return domain.getActionBySignature("dragon", "wait()");
        }

        if (dragonLocation.equals(sheepLocation)) {
            return domain.getActionBySignature("dragon", String.format("eat_sheep(%s)", sheepLocation));
        }


        if (dragonLocation.equals("hill")) {
            return domain.getActionBySignature("dragon", String.format("move(%s,%s)", dragonLocation, "field"));
        }

        if (dragonLocation.equals(sheepLocation)) {
            return domain.getActionBySignature("dragon", "eat_sheep()");
        }

        return domain.getActionBySignature("dragon", String.format("move(%s,%s)", dragonLocation, sheepLocation));

 

    }



}
