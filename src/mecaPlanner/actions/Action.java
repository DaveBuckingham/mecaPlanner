package mecaPlanner.actions;

import mecaPlanner.Planner;
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


public class Action<STATE extends PointedAbstractState> {

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

    public Domain getDomain() {
        return this.domain;
    }


    public int getCost() {
        return this.cost;
    }

    public boolean executable(STATE state) {
        return precondition.evaluate(state);
    }

    public void setEventModel(EventModel e) {
        assert (eventModel == null);
        this.eventModel = e;
    }

    public Boolean necessarilyExecutable(AbstractState state) {
        for (World w : state.getDesignated()) {
            assert (w != null);
            if (!precondition.evaluate(state, w)) {
                return false;
            }
        }
        return true;
    }




    public void setObservesCondition(String agent, Formula condition) {
        if (observesConditions.containsKey(agent)) {
            throw new RuntimeException("observe condition already set for " + agent);
        }
        observesConditions.put(agent, condition);
    }

    public void setAwareCondition(String agent, Formula condition) {
        if (awareConditions.containsKey(agent)) {
            throw new RuntimeException("aware condition already set for " + agent);
        }
        awareConditions.put(agent, condition);
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

    private PointedBimodalState intermediateTransition(PointedBimodalState state) {
        Map<World, Set<String>> fullObservers = new HashMap<>();
        Map<World, Set<String>> partialObservers = new HashMap<>();
        Map<World, Set<String>> nonObservers = new HashMap<>();
        Map<World, Map<String, Formula>> learnedKnowledge = new HashMap<>();     // (equation 4.11)
        for (World w : state.getWorlds()) {
             fullObservers.put(w, new HashSet<String>());
             partialObservers.put(w, new HashSet<String>());
             nonObservers.put(w, new HashSet<String>());
             learnedKnowledge.put(w, new HashMap<>());


             for (String a : domain.getAllAgents()) {
                 if (observesConditions.get(a).evaluate(w)) {
                     fullObservers.get(w).add(a);
                 }
                 else if (awareConditions.get(a).evaluate(w)) {
                     partialObservers.get(w).add(a);
                 }
                 else {
                     nonObservers.get(w).add(a);
                 }


                // Kdet^alpha_u (equation 4.6)
                Formula observation = null;
                if (determines == null) {
                    observation = new Literal(true);
                }
                else if (determines.evaluate(w)) {
                    observation = determines;
                }
                else {
                    observation = determines.negate();
                }


                // Keff^alpha_u (equation 4.7)
                Set<Formula> groundEffects = new HashSet<>();;
                for (Assignment assn : effects) {
                    Fluent f = assn.getFluent();
                    if (f.evaluate(w)) {
                        groundEffects.add(f);
                    }
                    else {
                        groundEffects.add(f.negate());
                    }
                }
                Formula learnedEffects = AndFormula.make(groundEffects);


                // phi_u-alpha-p (equation 4.8)
                Map<Fluent, Set<Formula>> m = new HashMap<>();
                for (Assignment eff : effects) {
                    Fluent f = eff.getFluent();
                    Formula c = eff.getCondition();
                    if (f.evaluate(w) != c.evaluate(w)) {
                        if (!m.containsKey(f)) {
                            m.put(f, new HashSet<>());
                        }
                        m.get(m).add(c);
                    }
                }

                // Keff^alpha_u (equation 4.9)
                Set<Formula> discernableConditions = new HashSet<>();;
                for (Map.Entry<Fluent, Set<Formula>> entry : m.entrySet()) {
                    Formula disjunction = Formula.makeDisjunction(entry.getValue());
                    if (disjunction.evaluate(w)) {
                        discernableConditions.add(disjunction);
                    }
                    else {
                        discernableConditions.add(disjunction.negate());
                    }
                }
                Formula learnedConditions = AndFormula.make(discernableConditions);



                // (equation 4.10)
                Set<Formula> learnedObservabilityConditions = new HashSet<>();
                    Formula phiF = observesConditions.get(a);
                    Formula phiP = awareConditions.get(a);
                if (fullObservers.get(w).contains(a)) {
                    learnedObservabilityConditions.add(phiF);
                }
                else if (partialObservers.get(w).contains(a)) {
                    learnedObservabilityConditions.add(phiP);
                }
                else if (nonObservers.get(w).contains(a)) {
                    learnedObservabilityConditions.add(AndFormula.make(phiF.negate(), phiP.negate()));
                }
                else {
                    throw new RuntimeException("unknown agent");
                }


                // (equation 4.11)
                Set<Formula> conjuncts = new HashSet<>();
                if (fullObservers.get(w).contains(a)) {
                    conjuncts.add(observation);
                    conjuncts.add(learnedEffects);
                    conjuncts.add(learnedConditions);
                }
                else if (partialObservers.get(w).contains(a)) {
                    conjuncts.add(learnedEffects);
                    conjuncts.add(learnedConditions);
                }
                conjuncts.addAll(learnedObservabilityConditions);

                learnedKnowledge.get(w).put(a, AndFormula.make(conjuncts));
            
            } // end loop over agents

        } // end loop over worlds


        // (equation 4.12)
        //Map<String, Formula> localPerspective = new HashMap<>();
        Map<String, Set<Set<World>>> localEquivalenceClasses = new HashMap<>();
        for (String i : domain.getAllAgents()) {
            Set<Set<World>> agentClasses = new HashSet<>();
            for (World u : state.getWorlds()) {
                 Set<World> worldClass = new HashSet<>();
                 for (World v : state.getKnown(i, u)){
                     if ((learnedKnowledge).get(u).get(i).evaluate(v)) {
                         worldClass.add(v);
                     }
                 }
                 agentClasses.add(worldClass);
            }
            localEquivalenceClasses.put(i, agentClasses);
         }

        
        return state;

    }


    // NEED TO GET THIS IMPLEMENTATION
    public PointedBimodalState transition(PointedBimodalState state) {
        return null;
    }

    public PointedPlausibilityState transition(PointedPlausibilityState state) {
        if (eventModel == null) {
            buildEventModel();
        }
        assert(eventModel != null);
        return eventModel.transition( (PointedPlausibilityState) state);
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

