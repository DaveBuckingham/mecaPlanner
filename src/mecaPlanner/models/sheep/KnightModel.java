package mecaPlanner.models.sheep;

import mecaPlanner.state.NDState;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.Action;
import mecaPlanner.Domain;
import mecaPlanner.models.Model;
import mecaPlanner.formulae.FluentAtom;

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
        //predictions.add(getSinglePrediction(ndState));
        predictions.add(domain.getActionBySignature("knight", "wait()"));
        return predictions;
    }


    public Action getSinglePrediction(NDState ndState) {

        String dragonLocation = null;
        String knightLocation = null;

        for (FluentAtom atom : domain.getAllAtoms()) {
            if (ndState.necessarily(atom)) {
                if (atom.getName().equals("at")) {
                    if (atom.getParameter(0).equals("dragon")) {
                        dragonLocation = atom.getParameter(1);
                    }
                    else if (atom.getParameter(0).equals("knight")) {
                        knightLocation = atom.getParameter(1);
                    }
                }
            }
        }

        if (knightLocation == null) {
            throw new RuntimeException("failed to determine knight location");
        }

        if (dragonLocation == null) {
                return domain.getActionBySignature("knight", "wait()");
        }

        if (dragonLocation.equals(knightLocation)) {
            return domain.getActionBySignature("knight", String.format("joust(%s)", knightLocation));
        }

        if (knightLocation.equals("forest")) {
            return domain.getActionBySignature("knight", "wait()");
        }

        return domain.getActionBySignature("knight", String.format("move(%s,%s)", knightLocation, dragonLocation));

    }



}
