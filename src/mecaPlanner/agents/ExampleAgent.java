package mecaPlanner.agents;

import java.util.Set;
import java.util.HashSet;

import mecaPlanner.state.*;
import mecaPlanner.actions.*;
import mecaPlanner.formulae.Fluent;
import mecaPlanner.Domain;

public class ExampleAgent extends Agent {

    public ExampleAgent(String agent, Domain domain) {
        super(agent, domain);
    }

    public Set<Action> getPrediction(State eState) {
        NDState ndState = eState.getBeliefPerspective(agent);
        Set<Action> allActions = getSafeActions(ndState);
        Set<Action> prediction = new HashSet<>();

        for (Action a : allActions) {
            if (a.getName().equals("eat")) {
                prediction.add(a);
                return prediction;
            }
        }

        if ((new Fluent("at", "pizza", "roomB")).necessarily(ndState)) {
            if ((new Fluent("at", "human1", "roomA")).necessarily(ndState)) {
                prediction.add(getSafeActionBySignature("move(human1,roomA, hall1)", ndState));
                prediction.add(getSafeActionBySignature("move(human1,roomA, hall2)", ndState));
                return prediction;
            }
            else if ((new Fluent("at", "human1", "hall1")).necessarily(ndState)) {
                prediction.add(getSafeActionBySignature("move(human1,hall1, roomB)", ndState));
                return prediction;
            }
            else if ((new Fluent("at", "human1", "hall2")).necessarily(ndState)) {
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
