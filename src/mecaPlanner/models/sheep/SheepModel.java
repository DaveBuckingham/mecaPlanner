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

    private Action waitAction;


    public Set<Action> getPrediction(NDState ndState) {

        String sheepLocation = null;
        String turnipLocation = null;

        for (FluentAtom atom : domain.getAllAtoms()) {
            if (atom.getName().equals("at")) {
                if (atom.getParameter(0).equals("sheep")) {
                    sheepLocation = atom.getParameter(1);
                }
                else if (atom.getParameter(0).equals("turnip")) {
                    turnipLocation = atom.getParameter(1);
                }
            }
        }

        Set<Action> predictions = new HashSet<>();
        if (sheepLocation == null || turnipLocation == null) {
            throw new RuntimeException("failed to determine sheep or turnip location");
        }

        if (turnipLocation.equals(sheepLocation)) {
            predictions.add(domain.getActionBySignature("sheep", "wait()"));
            return predictions;
        }
        predictions.add(domain.getActionBySignature("sheep", "move(%s,%s)".format(sheepLocation, turnipLocation)));
        return predictions;
    }



}
