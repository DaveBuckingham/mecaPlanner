package mecaPlanner.state;

import mecaPlanner.state.World;
import mecaPlanner.formulae.booleanFormulae.ObjectAtom;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;


public class Fluent{


    private String name;
    private List<ObjectAtom> parameters;

    public Fluent(String name, List<ObjectAtom> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public Fluent(String name, String ...strParams) {
        List<ObjectAtom> atomParams = new ArrayList<>();
        for (String s : strParams) {
            atomParams.add(new ObjectAtom(s));
        }
        this.name = name;
        this.parameters = atomParams;
    }

    public Fluent(String name) {
        this(name, new ArrayList<ObjectAtom>());
    }

    public String getName() {
        return this.name;
    }

    public List<ObjectAtom> getParameters() {
        return this.parameters;
    }

    public ObjectAtom getParameter(int i) {
        if (i < 0 || i >= parameters.size()) {
            throw new RuntimeException("atom parameter index out of bounds");
        }
        return parameters.get(i);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Fluent other = (Fluent) obj;
        return (name.equals(other.getName()) && parameters.equals(other.getParameters()));
    }


    @Override
    public int hashCode() {
        int result = name.hashCode() * 7;
        for (ObjectAtom p : parameters) {
            result = (31 * result) + p.hashCode();
        }
        return result;
    }



    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(name);

        str.append("(");
        if (parameters.size() > 0) {
            for (ObjectAtom p : parameters) {
                str.append(p);
                str.append(",");
            }
            str.deleteCharAt(str.length() - 1);
        }
        str.append(")");

        return str.toString();
    }




}
