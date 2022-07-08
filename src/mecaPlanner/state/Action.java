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

    private Formula announcement;
    private Formula determines;
    private Set<Assignment> effects;

    Map<String, Formula> observesConditions;
    Map<String, Formula> awareConditions;

    EventModel eventModel;



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
        observesConditions = new HashMap<>();
        awareConditions = new HashMap<>();
        for (String a : domain.getAllAgents()) {
            observesConditions.put(a, new Literal(false));
            awareConditions.put(a, new Literal(false));
        }
        eventModel = null;
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

    private Formula getPrecondition() {
        return this.precondition;
    }

//    public List<EventModel> getEffects() {
//        return this.effects;
//    }

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
        Log.debug(getSignatureWithActor());
        if (eventModel == null) {
            buildEventModel();
        }
        assert(eventModel != null);
        State newState = eventModel.transition(state);
        return newState;
    }


    public void setObservesCondition(String agent, Formula condition) {
        observesConditions.put(agent, condition);
    }

    public void setAwareCondition(String agent, Formula condition) {
        awareConditions.put(agent, condition);
    }


    private void buildEventModel() {
        if (eventModel != null) {
            throw new RuntimeException("event model already built");
        }
        if (announcement != null) {
            Event truthEvent = new Event(AndFormula.make(precondition, announcement));
            Event lieEvent = new Event(AndFormula.make(precondition, announcement.negate()));
            Event nullEvent = new Event(new Literal(true));
            Set<Event> events = new HashSet(Arrays.asList(truthEvent, lieEvent, nullEvent));
            Set<Event> designated = new HashSet(Arrays.asList(truthEvent, lieEvent));

            EventModel model = new EventModel(name, domain.getAllAgents(), events, designated);
            for (String agent : domain.getAllAgents()) {
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
            eventModel = model;


        }
        else if (determines != null) {
            Event truthEvent = new Event(AndFormula.make(precondition, determines));
            Event lieEvent = new Event(AndFormula.make(precondition, determines.negate()));
            Event nullEvent = new Event(new Literal(true));
            Set<Event> events = new HashSet(Arrays.asList(truthEvent, lieEvent, nullEvent));
            Set<Event> designated = new HashSet(Arrays.asList(truthEvent, lieEvent));

            EventModel model = new EventModel(name, domain.getAllAgents(), events, designated);
            for (String agent : domain.getAllAgents()) {
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
            eventModel = model;
        }


        else if (effects != null) {
            Event onticEvent = new Event(precondition, effects);
            Event nullEvent = new Event(new Literal(true));
            Set<Event> events = new HashSet(Arrays.asList(onticEvent, nullEvent));
            Set<Event> designated = new HashSet(Arrays.asList(onticEvent));

            EventModel model = new EventModel(name, domain.getAllAgents(), events, designated);
            for (String agent : domain.getAllAgents()) {
                model.addEdge(agent, onticEvent, onticEvent, new Literal(true));
                model.addEdge(agent, nullEvent, nullEvent, new Literal(true));

                Formula full = observesConditions.get(agent);
                Formula aware = awareConditions.get(agent);
                Formula oblivious = AndFormula.make(full.negate(), aware.negate());
                model.addEdge(agent, onticEvent, nullEvent, oblivious);
            }
            eventModel = model;
        }

        else {
            // NOOP

            //Event nullEvent = new Event(new Literal(true));
            //Set<Event> events = new HashSet(Arrays.asList(nullEvent));
            //Set<Event> designated = new HashSet(Arrays.asList(nullEvent));
            //EventModel model = new EventModel(name, domain.getAllAgents(), events, designated);
            //for (String agent : domain.getAllAgents()) {
            //    model.addEdge(agent, nullEvent, nullEvent, new Literal(true));
            //}
            //eventModel = model;

            Event onticEvent = new Event(precondition, new HashSet<Assignment>()); // DOESN'T DO ANYTHING, BUT HAS PRECONDITION
            Event nullEvent = new Event(new Literal(true));
            Set<Event> events = new HashSet(Arrays.asList(onticEvent, nullEvent));
            Set<Event> designated = new HashSet(Arrays.asList(onticEvent));

            EventModel model = new EventModel(name, domain.getAllAgents(), events, designated);
            for (String agent : domain.getAllAgents()) {
                model.addEdge(agent, onticEvent, onticEvent, new Literal(true));
                model.addEdge(agent, nullEvent, nullEvent, new Literal(true));

                Formula full = observesConditions.get(agent);
                Formula aware = awareConditions.get(agent);
                Formula oblivious = AndFormula.make(full.negate(), aware.negate());
                model.addEdge(agent, onticEvent, nullEvent, oblivious);
            }
            eventModel = model;
        }

    }




    public void addAnnouncement(Formula formula) {
        assert (announcement == null && determines == null && effects == null);
        announcement = formula;
    }

    public void addDetermines(Formula formula) {
        assert (announcement == null && determines == null && effects == null);
        determines = formula;
    }

    public void addEffect(Assignment assignment) {
        assert (announcement == null && determines == null);
        if (effects == null) {
            effects = new HashSet<Assignment>();
        }
        effects.add(assignment);
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

