package mecaPlanner.formulae;

import mecaPlanner.state.World;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;


public class FluentAtom extends FluentFormula{


    private String name;
    private List<String> parameters;

    public FluentAtom(String name, List<String> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public FluentAtom(String name, String ...parameters) {
        this(name, Arrays.asList(parameters));
    }

    public FluentAtom(String name) {
        this(name, new ArrayList<String>());
    }

    public String getName() {
        return this.name;
    }

    public List<String> getParameters() {
        return this.parameters;
    }

    public String getParameter(int i) {
        if (i >= 0 && i < parameters.size()) {
            return parameters.get(i);
        }
        return "";
    }

    public Boolean holds(World world) {
        return world.containsAtom(this);
    }

    public Boolean alwaysHolds() {
        return false;
    }

    public FluentLiteral negate() {
        return new FluentLiteral(false, this);
    }


    public Set<FluentAtom> getAllAtoms() {
        Set<FluentAtom> allAtoms = new HashSet<>();
        if (!(this.name.equalsIgnoreCase("true") || this.name.equalsIgnoreCase("false"))) {
            allAtoms.add(this);
        }
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
        FluentAtom otherAtom = (FluentAtom) obj;
        if (!this.getName().equals(otherAtom.getName())) {
            return false;
        }
        List<String> otherParameters = otherAtom.getParameters();
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
