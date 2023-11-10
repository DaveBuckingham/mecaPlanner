package mecaPlanner.agents;

import mecaPlanner.state.*;
import mecaPlanner.actions.*;
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


public class SlipAgent extends Agent {

    public SlipAgent(String agent, Domain domain) {
        super(agent, domain);
    }

    public Set<Action> getPrediction(PointedPlausibilityState eState) {
        PlausibilityState ndState = eState.getBeliefPerspective(agent);
        Set<Action> allActions = getSafeActions(ndState);
        Set<Action> prediction = new HashSet<>();


        if (necessarily(ndState, "at", agent, "back")) {
            if (possibly(ndState, "spill", "a1")) {
                prediction.add(getSafeActionBySignature("move(h,back,a2)", ndState));
            }
            else {
                prediction.add(getSafeActionBySignature("move(h,back,a1)", ndState));
            }
        }
        else if (necessarily(ndState, "at", agent, "a1")) {
            prediction.add(getSafeActionBySignature("move(h,a1,front)", ndState));
        }
        else if (necessarily(ndState, "at", agent, "a2")) {
            prediction.add(getSafeActionBySignature("move(h,a2,a3)", ndState));
        }
        else if (necessarily(ndState, "at", agent, "a3")) {
            prediction.add(getSafeActionBySignature("move(h,a3,front)", ndState));
        }
        else if (necessarily(ndState, "at", agent, "front")) {
            prediction.add(getSafeActionBySignature("wait(h)", ndState));
        }
        else {
            throw new RuntimeException("Slip Agent failed to determine state");
        }
        return prediction;


    }


}
