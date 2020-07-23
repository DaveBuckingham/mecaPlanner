package mecaPlanner.models;

import mecaPlanner.formulae.FluentAtom;
import mecaPlanner.state.NDState;
import mecaPlanner.Domain;

import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;


public abstract class AijModel extends Model {

    final Integer MIN_ROOM = 1;
    final Integer MAX_ROOM = 8;

    public AijModel(String agent, Domain domain) {
        super(agent, domain);
    }

    protected Set<Integer> getRoom(NDState state, String locatable) {
        Set<Integer> possibleRooms = new HashSet<>();
        for (Integer roomNumber = MIN_ROOM; roomNumber <= MAX_ROOM; roomNumber++) {
            FluentAtom atom = new FluentAtom("at", locatable, "room" + roomNumber.toString());
            if (state.possibly(atom)) {
                possibleRooms.add(roomNumber);
            }
        }
        return possibleRooms;
    }


}
