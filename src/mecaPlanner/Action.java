package mecaPlanner;

import mecaPlanner.formulae.*;
import mecaPlanner.state.*;
import mecaPlanner.agents.Agent;
import mecaPlanner.Domain;

import java.util.List;
import java.util.ArrayList;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;



public class Action implements java.io.Serializable {

    private Domain domain;

    private String name;
    private List<String> parameters;
    private int cost;
    private String actor;
    private Formula precondition;
    private Map<String, Formula> observesIf;
    private Map<String, Formula> awareIf;
    private Map<Formula, Formula> determines;  // sensed formula --> condition
    private Map<Formula, Formula> announces;  // announcement --> condition
    private Map<Assignment, Formula> effects;


    public Action(String name,
                  List<String> parameters,
                  String actor,
                  int cost,
                  Formula precondition,
                  Map<String, Formula> observesIf,
                  Map<String, Formula> awareIf,
                  Map<Formula, Formula> determines,
                  Map<Formula, Formula> announces,
                  Map<Assignment, Formula> effects,
                  Domain domain
                 ) {
        assert(cost > 0);
        this.name = name;
        this.parameters = parameters;
        this.actor = actor;
        this.cost = cost;
        this.precondition = precondition;
        this.observesIf = observesIf;
        this.awareIf = awareIf;
        this.determines = determines;
        this.announces = announces;
        this.effects = effects;
        this.domain = domain;
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

    public Map<Assignment, Formula> getEffects() {
        return this.effects;
    }

    public Map<String, Formula> getObserves() {
        return this.observesIf;
    }

    public Map<String, Formula> getAware() {
        return this.awareIf;
    }

    public Domain getDomain() {
        return this.domain;
    }

    // WE DO THIS HERE INSTEAD OF IN WORLD IN CASE WE DECIDE TO SWITCH
    // TO BELIEF FORMULA EFFECT CONDITIONS
    public Set<Assignment> getApplicableEffects(World world) {
        Set<Assignment> applicableEffects = new HashSet<>();
            for (Map.Entry<Assignment, Formula> e : effects.entrySet()) {
                Assignment assignment = e.getKey();
                Formula condition = e.getValue();
                if (condition.evaluate(world)) {
                    applicableEffects.add(assignment);
                }
            }
        return applicableEffects;
    }


    public int getCost() {
        return this.cost;
    }

    public Map<Formula, Formula> getDetermines() {
        return this.determines;
    }

    public Map<Formula, Formula> getAnnounces() {
        return this.announces;
    }

    public boolean executable(State state) {
        return executable(state.getDesignatedWorld());
    }

    protected boolean executable(World world) {
        return precondition.evaluate(world);
    }

    public Boolean necessarilyExecutable(NDState state) {
        for (World w : state.getDesignatedWorlds()) {
            assert (w != null);
            if (!executable(w)) {
                return false;
            }
        }
        return true;
    }


    public Boolean isObservant(String agent, World world) {
        boolean observant = observesIf.containsKey(agent) && observesIf.get(agent).evaluate(world);
        return observant;
    }

    public Boolean isAware(String agent, World world) {
        return (awareIf.containsKey(agent) && awareIf.get(agent).evaluate(world));
    }

    public Boolean isOblivious(String agent, World world) {
        return (!isObservant(agent, world) && !isAware(agent, world));
    }



    public class UpdatedStateAndEAgents {
        private State updatedState;
        private Map<String, Agent> updatedAgents ;

        public UpdatedStateAndEAgents(State updatedState, Map<String, Model> updatedAgents) {
            this.updatedState = updatedState;
            this.updatedAgents = updatedAgents;
        }

        public State getState() {
            return updatedState;
        }
        public Map<String, Agent> getAgents() {
            return updatedAgents;
        }
    }


    private class PartialResult {
        public KripkeStructure kripke;
        private Map<World,World> map;
        // GIVEN A NEW WORLD, WHAT OLD CLASS IS IT ASSOCIATED WITH FOR EACH AGENT
        private Map<World,Map<String, Set<World>>> postAssignments;
        // GIVEN AN OLD WORLD, WHAT WAS THE EQUIVALENCE CLASS CREATED FROM THAT WORLD FOR EACH AGENT
        private Map<String, Map<World,Set<World>>> roots;
        public PartialResult(KripkeStructure k,
                             Map<World,World> m,
                             Map<World, Map<String, Set<World>>> p,
                             Map<String, Map<World, Set<World>>> r
                            ){
            this.kripke = k;
            this.map = m;
            this.postAssignments = p;
            this.roots = r;
            //this.learnedObserver = l;
        }
        public Set<World> getWorlds() {
            return map.keySet();
        }
        public Set<World> getClassFromRoot(String agent, World w) {
            return roots.get(agent).get(w);
        }
        public Set<World> getPostAssignment(World w, String agent) {
            return postAssignments.get(w).get(agent);
        }
        public World getOldWorld(World w) {
            return map.get(w);
        }

    }


    
    public Action.UpdatedStateAndEAgents transition(State beforeState, Map<String, Agent> oldAgents) {
        Log.debug("transition: " + getSignatureWithActor());
        assert(precondition.evaluate(beforeState));

        // UPDATE THE MODELS
        Map<String, Agent> newAgents = new HashMap();
        for (String agent : oldAgents.keySet()) {
            Agent updatedAgent = oldAgents.get(agent).update(beforeState, this);
            newAgents.put(agent, updatedAgent);
        }

        //newState.trim();
        //newState.reduce();
        return new Action.UpdatedStateAndEAgents(beforeState, newAgents);
    }


    // MOST OF THE TIME WE WON'T BE UPDATING AGENT MODELS
    public State transition(State beforeState) {
        Action.UpdatedStateAndEAgents result = transition(beforeState, new HashMap<String, Model>());
        return result.getState();
    }


//    // FOR COMPUTING HYPOTHETICAL ACTIONS FOR OBLIVIOUS AGENTS
//    private Set<Action> possibleActions(String agent, KripkeStructure kripke, World world) {
//        assert (!kripke.getKnownWorlds(agent, world).isEmpty());
//        Set<Action> actions = new HashSet<>();
//        for (Action action : domain.getAllActions()) {
//            Formula possiblyPreconditioned = new KnowsFormula(agent,
//                action.getPrecondition().negate()).negate();
//            Formula possiblyOblivious = AndFormula.make(
//                new KnowsFormula(agent, action.observesIf.get(agent)).negate(),
//                new KnowsFormula(agent, action.awareIf.get(agent)).negate());
//            if (possiblyPreconditioned.evaluate(kripke, world) && possiblyOblivious.evaluate(kripke, world)) {
//                actions.add(action);
//            }
//        }
//        return actions;
//    }




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
        StringBuilder str = new StringBuilder();

        str.append("Action: ");
        str.append(this.getSignature());

        str.append("\n\tOwner: ");
        str.append(actor);

        str.append("\n\tCost: ");
        str.append(cost);

        str.append("\n\tPrecondition: ");
        str.append(precondition);

        str.append("\n\tObserves\n");
        for (Map.Entry<String, Formula> o : observesIf.entrySet()) {
            str.append("\t\t");
            str.append(o.getKey());
            str.append(" if ");
            str.append(o.getValue());
            str.append("\n");
        }

        str.append("\tAware\n");
        for (Map.Entry<String, Formula> a : awareIf.entrySet()) {
            str.append("\t\t");
            str.append(a.getKey());
            str.append(" if ");
            str.append(a.getValue());
            str.append("\n");
        }

        str.append("\tDetermines\n");
        for (Map.Entry<Formula, Formula> e : determines.entrySet()) {
            Formula sensed = e.getKey();
            Formula condition = e.getValue();
            str.append("\t\t");
            str.append(sensed);
            str.append(" if ");
            str.append(condition);
            str.append("\n");
 
        }

        str.append("\tAnnounces\n");
        for (Map.Entry<Formula, Formula> e : announces.entrySet()) {
            Formula announcement = e.getKey();
            Formula condition = e.getValue();
            str.append("\t\t");
            str.append(announcement);
            str.append(" if ");
            str.append(condition);
            str.append("\n");
 
        }

        str.append("\tCauses\n");
        for (Map.Entry<Assignment, Formula> e : effects.entrySet()) {
            Assignment assignment = e.getKey();
            Formula condition = e.getValue();
            str.append("\t\t");
            str.append(assignment);
            str.append(" if ");
            str.append(condition);
            str.append("\n");
        }
 
        return str.toString();
    }

}

