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


    static class Type1 {
        private Fluent fluent;

        public Fluent getFluent(){
            return fluent;
        }

        public Type1 (Fluent fluent) {
            this.fluent = fluent;
        }
    }

    static class Type1Boolean extends Type1 {
        private List<Boolean> values;
        public List<Boolean> getValues() {
            return values;
        }
        public Type1 (Fluent fluent, List<Boolean> values) {
            super(fluent);
            this.values = values;
        }
    }

    static class Type1Integer extends Type1 {
        private List<Integer> values;
        public List<Integer> getValues() {
            return values;
        }
        public Type1 (Fluent fluent, List<Integer> values) {
            super(fluent);
            this.values = values;
        }
    }


    static class Type1Object extends Type1 {
        private List<String> values;
        public List<String> getValues() {
            return values;
        }
        public Type1 (Fluent fluent, List<String> values) {
            super(fluent);
            this.values = values;
        }
    }



    static class Type2 {
        String agent;
        Type1 assignment;
        public Type2 (String agent, Type1 assignment){
            this.agent = agent;
            this.assignment = assignment;
        }
    }

    static class Type3 {
        String agent1;
        String agent2;
        Type1 assignment;
        public Type3 (String agent1, String agent2, Type1 assignment) {
            this.agent1 = agent1;
            this.agent2 = agent2;
            this.assignment = assignment;
        }
    }

    private static Set<World> productWorlds(List<Type1> ndAssignments) {
        Set<World> worlds = new HashSet<World>();
        int[] indeces = new int[ndAssignments.length()]; // TRUSTING THIS TO INITIALIZE AS ZEROS
        boolean done = false;
        while (!done) {
            Map<Fluent, Boolean> booleanFluents = new HashMap<>();
            Map<Fluent, Integer> integerFluents = new HashMap<>();
            Map<Fluent, String> objectFluents = new HashMap<>();
            for (int fluentIndex = 0; fluentIndex < ndAssignments.size(); fluentIndex+=1) {
                Type1 assignment = ndAssignments.get(fluentIndex);
                if (assignment instanceof Type1Boolean) {
                    List<Boolean> values = ((Type1Boolean)assignment).getValues();
                    booleanFluents.put(assignment.getFluent(), values.get(indeces[fluentIndex]));
                }
            }

            int fluentIndex = 0;
            do {
                indeces[fluentIndex] += 1;
                if (indeces[fluentIndex] == ndAssignments.get(fluentIndex).getValuues().size()) {
                    indeces[fluentIndex] = 0;
                    fluentIndex += 1;
                    if (fluentIndex == ndAssignments.size()) {
                        done = true;
                    }
                }
            } while (indeces[fluentIndex] == 0 && fluentIndex < ndAssignments.length());

        }
    }


    public static NDState constructState(Set<BeliefFormula> initialStatements, Domain domain) {

        Set<String> agents = domain.getAllAgents();

        Set<Type1> trueFacts;
        Set<World> designatedFrame;
        Map<String, Map<String, Set<Type1>>> beliefMatrix = new HashMap<>();
        Map<String, Map<String, Set<World>>> frameMatrix = new HashMap<>();

        for (String rowIndex : agents) {
            Map<String, Set<Type1>> beliefRow = new HashMap<>();
            Map<String, Set<World>> frameRow = new HashMap<>();
            for (String columnIndex : agents) {
                Set<Type1> beliefs = new HashSet<>();
                Set<World> frame = new HashSet<>();
                beliefRow.put(columnIndex, beliefs);
                frameRow.put(columnIndex, frame);
            }
            frameMatrix.put(rowIndex, row);
        }

        for (BeliefFormula statement : initialStatements) {
            if (statement instanceof BeliefFormulaBelieves) {
                BeliefBelievesFormula outer = (BeliefBelievesFormula) statement;
                String outerAgent = outer.getAgent();
                if (outer.getFormula() instanceof BeliefFormulaBelieves) {
                    BeliefBelievesFormula inner = (BeliefBelievesFormula) outer.getFormula();
                    String innerAgent = inner.getAgent();
                    Type1 assignment = parseAssignment(inner.getFormula());
                    beliefMatrix.get(outerAgent).get(innerAgent).put(assignment);
                }
                else {
                    Type1 assignment = parseAssignment(outer.getFormula());
                    beliefMatrix.get(outerAgent).get(outerAgent).put(assignment);
                }
            }
            else {
                Type1 assignment = parseAssignment(statement); 
                // PARSEaSSIGNMENT SHOULD DO LOTS OF CHECKS, FOR TYPES ETC
                trueFacts.put(assignment);
            }
        }


        // COPY MISSING ASSIGNMENTS FROM DESIGNATED WORLD


        for (String rowIndex : agents) {
            for (String columnIndex : agents) {
                Set<World> possibleWorlds = productWorlds(beliefMatrix.get(rowIndex).get(columnIndex));
                frameMatrix.get(rowIndex).get(columnIndex).put(possibleWorlds);
            }
        }



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
