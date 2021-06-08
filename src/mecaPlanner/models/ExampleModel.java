package mecaPlanner.models;

import java.util.Set;
import java.util.HashSet;

import mecaPlanner.state.*;
import mecaPlanner.formulae.localFormulae.Fluent;
import mecaPlanner.Action;
import mecaPlanner.Domain;

public class ExampleModel extends Model {

    public ExampleModel(String agent, Domain domain) {
        super(agent, domain);
    }

    public Set<Action> getPrediction(EpistemicState eState) {
        NDState ndState = eState.getBeliefPerspective(agent);
        Set<Action> allActions = getSafeActions(ndState);
        Set<Action> prediction = new HashSet<>();

        for (Action a : allActions) {
            if (a.getName().equals("eat")) {
                prediction.add(a);
                return prediction;
            }
        }

        if (ndState.necessarily(new Fluent("at", "pizza", "roomB"))) {
            if (ndState.necessarily(new Fluent("at", "human1", "roomA"))) {
                prediction.add(getSafeActionBySignature("move(human1,roomA, hall1)", ndState));
                prediction.add(getSafeActionBySignature("move(human1,roomA, hall2)", ndState));
                return prediction;
            }
            else if (ndState.necessarily(new Fluent("at", "human1", "hall1"))) {
                prediction.add(getSafeActionBySignature("move(human1,hall1, roomB)", ndState));
                return prediction;
            }
            else if (ndState.necessarily(new Fluent("at", "human1", "hall2"))) {
                prediction.add(getSafeActionBySignature("move(human1,hall2, roomB)", ndState));
                return prediction;
            }
            throw new RuntimeException("Human model failed with pizza");
        }

        for (Action a : allActions) {
            if (a.getName().equals("wait")) {
                prediction.add(a);
                return prediction;
            }
        }
        throw new RuntimeException("Example model failed to determine action.");
    }
}
