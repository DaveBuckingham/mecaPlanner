package mecaPlanner.state;

import mecaPlanner.formulae.beliefFormulae.*;
import mecaPlanner.Domain;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.io.File;
import java.io.FileWriter;

import java.io.IOException;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import depl.*;

public class Initialize {


    static class Type1<T> {
        private Fluent fluent;
        private List<T> values;

        public Fluent getFluent(){
            return fluent;
        }

        public List<T> getValues() {
            return values;
        }

        public Type1 (Fluent fluent, List<T> values) {
            this.fluent = fluent;
            this.values = values;
        }
    }

    static class Type2<T> {
        String agent;
        Type1<T> assignment;
        public Type2 (String agent, Type1<T> assignment){
            this.agent = agent;
            this.assignment = assignment;
        }
    }

    static class Type3<T> {
        String agent1;
        String agent2;
        Type1<T> assignment;
        public Type3 (String agent1, String agent2, Type1<T> assignment) {
            this.agent1 = agent1;
            this.agent2 = agent2;
            this.assignment = assignment;
        }
    }

    private static <T extends Object> Set<World> productWorlds(List<Type1<T>> ndAssignments) {
        Set<World> worlds = new HashSet<World>();
        return worlds;
    }


    public static NDState constructState(Set<BeliefFormula> initialStatements, Domain domain) {

        Set<String> agents = domain.getAllAgents();

        Set<World> designatedFrame;
        Map<String, Map<String, Set<World>>> frameMatrix = new HashMap<>();

        for (String rowIndex : agents) {
            Map<String, Set<World>> row = new HashMap<>();
            for (String columnIndex : agents) {
                Set<World> frame = new HashSet<>();
                row.put(columnIndex, frame);
            }
            frameMatrix.put(rowIndex, row);
        }

        for (BeliefFormula f : initialStatements) {

        }
        return null;
    }


//    // this function copied from Andrew Mao:
//    // https://stackoverflow.com/questions/1670862/obtaining-a-powerset-of-a-set-in-java
//    public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
//        List<T> list = new ArrayList<T>(originalSet);
//        int n = list.size();
//        Set<Set<T>> powerSet = new HashSet<Set<T>>();
//        for( long i = 0; i < (1 << n); i++) {
//            Set<T> element = new HashSet<T>();
//            for( int j = 0; j < n; j++ ) {
//                if( (i >> j) % 2 == 1 ) element.add(list.get(j));
//            }
//            powerSet.add(element); 
//        }
//        return powerSet;
//    }




}
