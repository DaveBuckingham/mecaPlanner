package mecaPlanner.state;

import mecaPlanner.state.World;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;


public class Fluent {


    private String name;
    private List<String> parameters;

    public Fluent(String name, List<String> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public Fluent(String name, String ...parameters) {
        this(name, Arrays.asList(parameters));
    }

    public Fluent(String name) {
        this(name, new ArrayList<String>());
    }

    public String getName() {
        return this.name;
    }

    public List<String> getParameters() {
        return this.parameters;
    }

    public String getParameter(int i) {
        if (i < 0 || i >= parameters.size()) {
            throw new RuntimeException("atom parameter index out of bounds");
        }
        return parameters.get(i);
    }

    public Set<Fluent> getAllAtoms() {
        Set<Fluent> allAtoms = new HashSet<>();
        allAtoms.add(this);
        return allAtoms;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(name);

        str.append("(");
        if (parameters.size() > 0) {
            for (String p : parameters) {
                str.append(p);
                str.append(",");
            }
            str.deleteCharAt(str.length() - 1);
        }
        str.append(")");

        return str.toString();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Fluent otherFluent = (Fluent) obj;
        if (!this.getName().equals(otherAtom.getName())) {
            return false;
        }
        List<String> otherParameters = otherFluent.getParameters();
        if (this.parameters.size() != otherParameters.size()) {
            return false;
        }
        for (int i = 0; i < this.parameters.size(); i++) {
            if (!this.parameters.get(i).equals(otherParameters.get(i))) {
                return false;
            }
        }
        return true;
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = (31 * hash) + this.name.hashCode();
        for (String p : this.parameters) {
            hash = (31 * hash) + p.hashCode();
        }
        return hash;
    }


}
