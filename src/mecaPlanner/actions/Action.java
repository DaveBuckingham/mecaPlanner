package mecaPlanner.actions;

import mecaPlanner.formulae.BeliefFormula;
import mecaPlanner.formulae.FluentFormula;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.World;
import mecaPlanner.state.NDState;
import mecaPlanner.models.Model;
import mecaPlanner.Domain;

import java.util.List;
import java.util.ArrayList;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;



public abstract class Action implements java.io.Serializable {


    protected String name;
    protected List<String> parameters;
    protected int cost;
    protected String actor;
    protected BeliefFormula precondition;
    protected Map<String, FluentFormula> observesIf;
    protected Map<String, FluentFormula> awareIf;


    public Action(String name,
                  List<String> parameters,
                  String actor,
                  int cost,
                  BeliefFormula precondition,
                  Map<String, FluentFormula> observesIf,
                  Map<String, FluentFormula> awareIf
                 ) {
        assert(cost > 0);
        this.name = name;
        this.parameters = parameters;
        this.actor = actor;
        this.cost = cost;
        this.precondition = precondition;
        this.observesIf = observesIf;
        this.awareIf = awareIf;
    }

    public String getName() {
        return name;
    }

    public String getActor() {
        return this.actor;
    }

    public List<String> getParameters() {
        return this.parameters;
    }

    public BeliefFormula getPrecondition() {
        return this.precondition;
    }


    public int getCost() {
        return this.cost;
    }

    public boolean executable(EpistemicState state) {
        return precondition.holds(state);
    }

    protected boolean executable(KripkeStructure kripke, World world) {
        return precondition.holdsAtWorld(kripke, world);
    }

    public Boolean necessarilyExecutable(NDState state) {
        for (World w : state.getDesignatedWorlds()) {
            if (!executable(state.getKripke(), w)) {
                return false;
            }
        }
        return true;
    }


    public Boolean isFullyObservant(String agent, EpistemicState state) {
        return (observesIf.containsKey(agent) && observesIf.get(agent).holds(state));
    }

    public Boolean isAware(String agent, EpistemicState state) {
        return (awareIf.containsKey(agent) && awareIf.get(agent).holds(state));
    }

    public Boolean isOblivious(String agent, EpistemicState state) {
        return ((!isFullyObservant(agent, state)) && (!isAware(agent, state)));
    }

    public Set<String>getAnyObservers(EpistemicState state) {
        Set<String> selected = new HashSet<>();
        for (String agent : Domain.getAllAgents()) {
            if (isFullyObservant(agent, state) || isAware(agent, state)) {
                selected.add(agent);
            }
        }
        return selected;
    }

    public Set<String>getFullyObservant(EpistemicState state) {
        Set<String> selected = new HashSet<>();
        for (String agent : Domain.getAllAgents()) {
            if (isFullyObservant(agent, state)) {
                selected.add(agent);
            }
        }
        return selected;
    }

    public Set<String>getAware(EpistemicState state) {
        Set<String> selected = new HashSet<>();
        for (String agent : Domain.getAllAgents()) {
            if (isAware(agent, state)) {
                selected.add(agent);
            }
        }
        return selected;
    }

    public Set<String>getOblivious(EpistemicState state) {
        Set<String> selected = new HashSet<>();
        for (String agent : Domain.getAllAgents()) {
            if (isOblivious(agent, state)) {
                selected.add(agent);
            }
        }
        return selected;
    }


    public class UpdatedStateAndModels {
        private EpistemicState updatedState;
        private Map<String, Model> updatedModels ;

        public UpdatedStateAndModels(EpistemicState updatedState, Map<String, Model> updatedModels) {
            this.updatedState = updatedState;
            this.updatedModels = updatedModels;
        }

        public EpistemicState getState() {
            return updatedState;
        }
        public Map<String, Model> getModels() {
            return updatedModels;
        }
    }

    public abstract UpdatedStateAndModels transition(EpistemicState priorState, Map<String, Model> priorModels);


    public String getSignatureWithActor() {
        StringBuilder str = new StringBuilder();
        str.append(name);
        str.append("[");
        str.append(actor.getName());
        str.append("]");
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

    public String getSignature() {
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
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("Action: ");
        str.append(this.getSignature());

        str.append("\n\tOwner: ");
        str.append(actor.getName());

        str.append("\n\tPrecondition: ");
        str.append(precondition);

        str.append("\n\tObserves\n");
        for (Map.Entry<String, FluentFormula> o : observesIf.entrySet()) {
            str.append("\t\t");
            str.append(o.getKey().getName());
            str.append(" if ");
            str.append(o.getValue());
            str.append("\n");
        }

        str.append("\tAware\n");
        for (Map.Entry<String, FluentFormula> a : awareIf.entrySet()) {
            str.append("\t\t");
            str.append(a.getKey().getName());
            str.append(" if ");
            str.append(a.getValue());
            str.append("\n");
        }
        return str.toString();
    }



}

