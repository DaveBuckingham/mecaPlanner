package mecaPlanner.state;

import mecaPlanner.formulae.*;
import mecaPlanner.Domain;
import mecaPlanner.Log;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;


import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;


public class Construct {

    private static Domain domain;

    private Construct() { }



    public static Set<EpistemicState> constructStates(Domain d, List<Formula> formulae) {
        throw new RuntimeException("State construction not implemented");
        //return null;
    }



    // THIS FUNCTION IS COPIED FROM MARKSPACE:
    // https://codereview.stackexchange.com/questions/164647/powerset-all-subsets-of-a-set-in-java
    static <T> Set<Set<T>> powerSet( Set<T> set ) {
        T[] element = (T[]) set.toArray();
        final int SET_LENGTH = 1 << element.length;
        Set<Set<T>> powerSet = new HashSet<>();
        for( int binarySet = 0; binarySet < SET_LENGTH; binarySet++ ) {
            Set<T> subset = new HashSet<>();
            for( int bit = 0; bit < element.length; bit++ ) {
                int mask = 1 << bit;
                if( (binarySet & mask) != 0 ) {
                    subset.add( element[bit] );
                }
            }
            powerSet.add( subset );
        }
        return powerSet;
    }

}

