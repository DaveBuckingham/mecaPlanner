package mecaPlanner.formulae;

import mecaPlanner.state.World;
import mecaPlanner.state.PlausibilityState;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

public class Fluent extends Formula{


    private String name;
    private List<String> parameters;

    public Fluent(String name, List<String> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public Fluent(String ...strParams) {
        if (strParams.length < 1) {
            throw new RuntimeException("fluent needs a name");
        }
        this.name = strParams[0];
        this.parameters = Arrays.asList(Arrays.copyOfRange(strParams, 1, strParams.length));
    }

    public Fluent(String name) {
        this(name, new ArrayList<String>());
    }

    public String getName() {
        return this.name;
    }

    public Set<Fluent> getAllFluents() {
        Set<Fluent> allFluents = new HashSet<>();
        allFluents.add(this);
        return allFluents;
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

    public Formula negate() {
        return NotFormula.make(this);
    }

    public Boolean isFalse() {
        return false;
    }
    public Boolean isTrue() {
        return false;
    }

    public Boolean evaluate(World world) {
        return world.ground(this);
    }
    public Boolean evaluate(PlausibilityState model, World world) {
        return world.ground(this);
    }
 
    public Integer getHeight() {
        return 0;
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
        for (String p : parameters) {
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
            for (String p : parameters) {
                str.append(p);
                str.append(",");
            }
            str.deleteCharAt(str.length() - 1);
        }
        str.append(")");

        return str.toString();
    }


}
