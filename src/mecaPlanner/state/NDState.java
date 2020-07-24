package mecaPlanner.state;

import mecaPlanner.formulae.BeliefFormula;

import java.util.List;
import java.util.ArrayList;

import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;

import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Collections;


// A NON-POINTED KRIPKE MODEL, I.E. POSSIBLY MORE THAN ONE DESIGNATED WORLD


public class NDState implements java.io.Serializable {

    protected KripkeStructure kripkeStructure;
    private Set<World> designatedWorlds;

    public NDState(KripkeStructure kripkeStructure, Set<World> designatedWorlds) {
        assert(kripkeStructure.containsWorlds(designatedWorlds));
        this.kripkeStructure = kripkeStructure;
        this.designatedWorlds = designatedWorlds;
    }

    public KripkeStructure getKripke() {
        return this.kripkeStructure;
    }

    public Set<World> getDesignatedWorlds() {
        return this.designatedWorlds;
    }

    public Set<EpistemicState> getEpistemicStates() {
        Set<EpistemicState> states = new HashSet<EpistemicState>();
        for (World w : designatedWorlds) {
            states.add(new EpistemicState(this.kripkeStructure, w));
        }
        return states;
    }

    public Boolean possibly(BeliefFormula formula) {
        for (World w : designatedWorlds) {
            if (formula.holdsAtWorld(this.kripkeStructure, w)) {
                return true;
            }
        }
        return false;
    }

    public Boolean necessarily(BeliefFormula formula) {
        for (World w : designatedWorlds) {
            if (!formula.holdsAtWorld(this.kripkeStructure, w)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        NDState other = (NDState) obj;
        // THIS WILL CATCH PROBLEM CAUSED BY EPISTEMICSTATE.GETPERSPECTIVE() REUSING
        // KRIPKE, BUT ONLY IF DESIGNATED WORLDS ARE THE SAME. MIGHT BE A BUG
        // IN CASE TWO NDSTATES HAVE THE SAME KRIPKE BUT DIFFERENT DESIGNATED WORLDS:
        // COULD GO THROUG TO EQUIVALENT() AND THROUGH TO UNION() WHERE
        // WILL BREAK BECAUSE BOTH KRIPKE'S HAVE SAME WORLDS (SINCE THEY ARE THE SAME KRIPKE).
        if (this.kripkeStructure == other.getKripke() && this.designatedWorlds == other.getDesignatedWorlds()) {
            return true;
        }
        return equivalent(other);
    }


    public Boolean equivalent(NDState other) {
        //assert (this.kripkeStructure != other.getKripke());
        KripkeStructure unionKripke = this.kripkeStructure.union(other.getKripke());

        Set<World> otherInitials = other.getDesignatedWorlds();

        for (Set<World> block : unionKripke.refineSystem()) {
            if (Collections.disjoint(block, designatedWorlds) != Collections.disjoint(block, otherInitials)) {
                return false;
            }
        }
        return true;
    }


    public String toStringCompact() {
        return toString();
    }

    @Override
    public String toString() {
        return kripkeStructure.toString(designatedWorlds);
    }


}

