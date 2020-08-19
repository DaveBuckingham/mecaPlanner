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


public class SheepModel extends Model {

    public SheepModel(String agent, Domain domain) {
        super(agent, domain);
    }


    public Set<Action> getPrediction(NDState ndState) {

        String sheepLocation = null;
        String robotLocation = null;
        //boolean robotHasTurnip = false;

        for (FluentAtom atom : domain.getAllAtoms()) {
            if (ndState.necessarily(atom)) {
                //if (atom.getName().equals("robot_has_turnip")) {
                //    robotHasTurnip = true;
                //}
                if (atom.getName().equals("at")) {
                    if (atom.getParameter(0).equals("sheep")) {
                        sheepLocation = atom.getParameter(1);
                    }
                    else if (atom.getParameter(0).equals("robot")) {
                        robotLocation = atom.getParameter(1);
                    }
                }
            }
        }

        Set<Action> predictions = new HashSet<>();
        if (sheepLocation == null) {
            throw new RuntimeException("failed to determine sheep location");
        }
        if (robotLocation == null) {
            throw new RuntimeException("failed to determine robot location");
        }

        //if (!robotHasTurnip || robotLocation.equals(sheepLocation)) {
        if (robotLocation.equals(sheepLocation)) {
            predictions.add(domain.getActionBySignature("sheep", "wait()"));
            return predictions;
        }
        predictions.add(domain.getActionBySignature("sheep", String.format("move(%s,%s)", sheepLocation, robotLocation)));
        return predictions;
    }



}
