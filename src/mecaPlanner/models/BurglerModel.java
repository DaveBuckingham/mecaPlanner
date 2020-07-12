package mecaPlanner.models;

import mecaPlanner.formulae.FluentAtom;
import mecaPlanner.state.NDState;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.actions.Action;
import mecaPlanner.agents.Agent;
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


public class BurglerModel extends AijModel {

    private static String TARGET_BOX = "box4";


    public BurglerModel() {
        super();
    }

    private boolean lessThanAll(Integer single, Set<Integer> many) {
        for (Integer i : many) {
            if (single >= i) {
                return false;
            }
        }
        return true;
    }

    private boolean greaterThanAll(Integer single, Set<Integer> many) {
        for (Integer i : many) {
            if (single <= i) {
                return false;
            }
        }
        return true;
    }
    
    public Set<Action> getPrediction(NDState ndState, Agent agent) {

        Set<Action> actions = new HashSet<>();

        Iterator<Integer> myRooms = getRoom(ndState, agent.getName()).iterator();
        if (!myRooms.hasNext()) {
            throw new RuntimeException("Modeled agent not at a room");
        }
        Integer myRoom = myRooms.next();
        if (myRooms.hasNext()) {
            throw new RuntimeException("Modeled agent uncertain about her room");
        }

        Iterator<Integer> boxRooms = getRoom(ndState, TARGET_BOX).iterator();
        if (!boxRooms.hasNext()) {
            System.out.println(ndState);
            throw new RuntimeException("Box not at a room");
        }
        Integer boxRoom = boxRooms.next();
        if (boxRooms.hasNext()) {
            Log.info("Modeled agent uncertain about box room");
            actions.add(Domain.getActionBySignature(agent, String.format("wait(room%d)", myRoom)));
            return actions;
        }

        if (myRoom == boxRoom) {
            actions.add(Domain.getActionBySignature(agent, String.format("wait(room%d)", myRoom)));
        }
        else if (myRoom == 6 && boxRoom == 8) {
            actions.add(Domain.getActionBySignature(agent, String.format("move(room%d,room%d)", myRoom, 8)));
        }
        else if (myRoom == 8 && boxRoom != 7) {
            actions.add(Domain.getActionBySignature(agent, String.format("move(room%d,room%d)", myRoom, 6)));
        }
        else if (myRoom > boxRoom) {
            actions.add(Domain.getActionBySignature(agent, String.format("move(room%d,room%d)", myRoom, myRoom - 1)));
        }
        else {  // myRoom < boxRoom
            actions.add(Domain.getActionBySignature(agent, String.format("move(room%d,room%d)", myRoom, myRoom + 1)));
        }
        return actions;

    }


}
