package mecaPlanner.formulae;

import mecaPlanner.state.EpistemicState;
import mecaPlanner.formulae.FluentAtom;

import mecaPlanner.*;


import java.util.Objects;

import java.util.Set;


public abstract class GeneralFormula implements java.io.Serializable {

    public abstract Boolean holds(EpistemicState state, Integer timestep);

    public abstract Set<FluentAtom> getAllAtoms();

}
