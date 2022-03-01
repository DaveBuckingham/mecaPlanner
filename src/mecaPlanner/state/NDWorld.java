package mecaPlanner.state;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

import mecaPlanner.formulae.Fluent;


public class NDWorld {

    private Set<Fluent> negative;
    private Set<Fluent> positive;
    private Set<Fluent> unknown;
    private Map<String, NDWorld> known;
    private Map<String, NDWorld> believed;
    private List<String> agents;

    //public NDWorld(Set<Fluent> allFluents, List<String> agents) {
    public NDWorld(Set<Fluent> allFluents) {
        unknown = new HashSet<Fluent>(allFluents);
        negative = new HashSet<Fluent>();
        positive = new HashSet<Fluent>();
        known = new HashMap<>();
        believed = new HashMap<>();
        agents = agents;
        //for (String a : agents) {
        //    known.put(a, new HashSet<NDWorld>());
        //    believed.put(a, new HashSet<NDWorld>());
        //}
    }

    public Set<Fluent> getNegative() {
        return negative;
    }

    public Set<Fluent> getPositive() {
        return positive;
    }

    public Set<Fluent> getUnknown() {
        return unknown;
    }

    public NDWorld getKnown(String a) {
        return known.get(a);
    }

    public NDWorld getBelieved(String a) {
        return believed.get(a);
    }

    public void setKnown(String a, NDWorld w) {
        known.put(a, w);
    }

    public void setBelieved(String a, NDWorld w) {
        believed.put(a, w);
    }

    //public void addKnown(String a, NDWorld child) {
    //    known.get(a).add(child);
    //}

    //public void addBelieved(String a, NDWorld child) {
    //    believed.get(a).add(child);
    //}


    private Boolean mergable(NDWorld other) {
        Set<Fluent> temp = new HashSet<>(negative);
        temp.retainAll(other.getPositive());
        if (!(temp.isEmpty())) {
            return false;
        }
        temp = new HashSet<>(positive);
        temp.retainAll(other.getNegative());
        if (!(temp.isEmpty())) {
            return false;
        }
        for (String a : agents) {
             if (!known.get(a).mergable(other.getKnown(a))) {
                 return false;
             }
             if (!believed.get(a).mergable(other.getBelieved(a))) {
                 return false;
             }
         }
        return true;
    }

    public Boolean merge(NDWorld other) {
       if (!mergable(other)) {
           return false;
       }
       for (String a : agents) {
            if (!known.get(a).merge(other.getKnown(a))) {
                throw new RuntimeException("merge broken");
            }
            if (!believed.get(a).merge(other.getBelieved(a))) {
                throw new RuntimeException("merge broken");
            }
        }
        unknown.removeAll(other.getPositive());
        positive.addAll(other.getPositive());
        unknown.removeAll(other.getNegative());
        negative.addAll(other.getNegative());
        return true;
    }

    public Boolean setFluent(Fluent fluent, Boolean value) {
        if (value) {
            if (negative.contains(fluent)) {
                return false;
            }
            positive.add(fluent);
        }
        else {
            if (positive.contains(fluent)) {
                return false;
            }
            negative.add(fluent);
        }
        unknown.remove(fluent);
        return true;
    }

    public Set<World> getWorlds() {
        Set<World> worlds = new HashSet<>();
        for (Set<Fluent> grounded : powerSet(unknown)) {
            grounded.addAll(positive);
            worlds.add(new World(grounded));
        }
        return worlds;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        NDWorld other = (NDWorld) obj;
        return (negative.equals(other.getNegative()) && 
                positive.equals(other.getPositive()) && 
                unknown.equals(other.getUnknown()));
    }


    @Override
    public int hashCode() {
        int result = 7;
        result = (31 * result) + negative.hashCode();
        result = (31 * result) + positive.hashCode();
        result = (31 * result) + unknown.hashCode();
        return result;
    }




    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("{");
        for (Fluent f : positive) {
            str.append(f);
            str.append(", ");
        }
        for (Fluent f : negative) {
            str.append("~" + f);
            str.append(", ");
        }
        for (Fluent f : unknown) {
            str.append("?" + f);
            str.append(", ");
        }
        str.delete(str.length()-2, str.length());
        str.append("}");
        return str.toString();
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

