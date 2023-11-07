package mecaPlanner.state;


import mecaPlanner.Log;
import mecaPlanner.formulae.Formula;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Stack;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Collections;
import java.util.Objects;

import mecaPlanner.formulae.Fluent;

public interface PointedAbstractState extends AbstractState {

    public World getDesignatedWorld();
    public AbstractState getBeliefPerspective(String agent);


}

