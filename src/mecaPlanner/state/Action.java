package mecaPlanner.state;

import mecaPlanner.formulae.*;
import mecaPlanner.state.*;
import mecaPlanner.agents.Agent;
import mecaPlanner.Domain;
import mecaPlanner.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

//import org.javatuples.Pair;


public class Action implements Transformer {

    private Domain domain;

    private String name;
    private List<String> parameters;
    private int cost;
    private String actor;
    private Formula precondition;

    private List<EventModel> effects;

    Map<String, Formula> observesConditions;
    Map<String, Formula> awareConditions;


    public Action(String name,
                  List<String> parameters,
                  String actor,
                  int cost,
                  Formula precondition,
                  Domain domain
                 ) {
        assert(cost > 0);
        this.name = name;
        this.parameters = parameters;
        this.actor = actor;
        this.cost = cost;
        this.precondition = precondition;
        this.domain = domain;
        effects = new ArrayList<EventModel>();
        observesConditions = new HashMap<>();
        awareConditions = new HashMap<>();
        for (String a : domain.getAgents()) {
            observesConditions.put(a, new Literal(false));
            awareConditions.put(a, new Literal(false));
        }
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

    public Formula getPrecondition() {
        return this.precondition;
    }

    public List<EventModel> getEffects() {
        return this.effects;
    }

    public Domain getDomain() {
        return this.domain;
    }


    public int getCost() {
        return this.cost;
    }

    public boolean executable(NDState model, World w) {
        return precondition.evaluate(model, w);
    }

    public boolean executable(State state) {
        return executable(state, state.getDesignatedWorld());
    }

    public Boolean necessarilyExecutable(NDState state) {
        for (World w : state.getDesignated()) {
            assert (w != null);
            if (!executable(state, w)) {
                return false;
            }
        }
        return true;
    }


    public State transition(State state) {
        for (EventModel e : effects) {
            // CHECK IF EXECUTABLE?
            state = e.transition(state);
        }
        return state;
    }


    public void setObservesCondition(String agent, Formula condition) {
        observesConditions.put(agent, condition);
    }

    public void setAwareCondition(String agent, Formula condition) {
        awareConditions.put(agent, condition);
    }



    public void addAnnouncementEffect(Formula announcement, Formula condition) {

        String name = "announce-" + announcement.toString();

        Event truthEvent = new Event(AndFormula.make(condition, announcement));
        Event lieEvent = new Event(AndFormula.make(condition, announcement.negate()));
        Event nullEvent = new Event(new Literal(true));
        Set<Event> events = new HashSet(Arrays.asList(truthEvent, lieEvent, nullEvent));
        Set<Event> designated = new HashSet(Arrays.asList(truthEvent, lieEvent));

        EventModel model = new EventModel(name, domain.getAgents(), events, designated);
        for (String agent : domain.getAgents()) {
            model.addEdge(agent, truthEvent, truthEvent, new Literal(true));
            model.addEdge(agent, lieEvent, lieEvent, new Literal(true));
            model.addEdge(agent, nullEvent, nullEvent, new Literal(true));

            Formula full = observesConditions.get(agent);
            Formula aware = awareConditions.get(agent);
            Formula oblivious = AndFormula.make(full.negate(), aware.negate());
            model.addEdge(agent, truthEvent, lieEvent, full.negate());
            model.addEdge(agent, lieEvent, truthEvent, new Literal(true));
            model.addEdge(agent, truthEvent, nullEvent, oblivious);
            model.addEdge(agent, lieEvent, nullEvent, oblivious);
        }

        effects.add(model);
    }

    public void addSensingEffect(Formula sensed, Formula condition) {

        String name = "sense-" + sensed.toString();

        Event truthEvent = new Event(AndFormula.make(condition, sensed));
        Event lieEvent = new Event(AndFormula.make(condition, sensed.negate()));
        Event nullEvent = new Event(new Literal(true));
        Set<Event> events = new HashSet(Arrays.asList(truthEvent, lieEvent, nullEvent));
        Set<Event> designated = new HashSet(Arrays.asList(truthEvent, lieEvent));

        EventModel model = new EventModel(name, domain.getAgents(), events, designated);
        for (String agent : domain.getAgents()) {
            model.addEdge(agent, truthEvent, truthEvent, new Literal(true));
            model.addEdge(agent, lieEvent, lieEvent, new Literal(true));
            model.addEdge(agent, nullEvent, nullEvent, new Literal(true));

            Formula full = observesConditions.get(agent);
            Formula aware = awareConditions.get(agent);
            Formula oblivious = AndFormula.make(full.negate(), aware.negate());
            model.addEdge(agent, truthEvent, lieEvent, full.negate());
            model.addEdge(agent, lieEvent, truthEvent, full.negate());
            model.addEdge(agent, truthEvent, nullEvent, oblivious);
            model.addEdge(agent, lieEvent, nullEvent, oblivious);
        }

        effects.add(model);
    }



    public void addOnticEffect(Set<Assignment> changes, Formula condition) {
        String name = "ontic-effect";

        Event onticEvent = new Event(condition, changes);
        Event nullEvent = new Event(new Literal(true));
        Set<Event> events = new HashSet(Arrays.asList(onticEvent, nullEvent));
        Set<Event> designated = new HashSet(Arrays.asList(onticEvent));

        EventModel model = new EventModel(name, domain.getAgents(), events, designated);
        for (String agent : domain.getAgents()) {
            model.addEdge(agent, onticEvent, onticEvent, new Literal(true));
            model.addEdge(agent, nullEvent, nullEvent, new Literal(true));

            Formula full = observesConditions.get(agent);
            Formula aware = awareConditions.get(agent);
            Formula oblivious = AndFormula.make(full.negate(), aware.negate());
            model.addEdge(agent, onticEvent, nullEvent, oblivious);
        }
        effects.add(model);
    }





    public String getSignatureWithActor() {
        StringBuilder str = new StringBuilder();
        str.append(name);
        str.append("[");
        str.append(actor);
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
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Action other = (Action) obj;
        return (this.getSignatureWithActor().equals(other.getSignatureWithActor()));
    }

    @Override
    public int hashCode() {
        return getSignatureWithActor().hashCode();
    }

    @Override
    public String toString() {
        return getSignatureWithActor();
    }

}

