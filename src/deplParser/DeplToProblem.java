package depl;

import mecaPlanner.agents.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import mecaPlanner.*;
import mecaPlanner.formulae.*;
import mecaPlanner.state.*;
import mecaPlanner.actions.*;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.Iterator;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.javatuples.Pair;

import java.io.IOException;


public class DeplToProblem extends DeplBaseVisitor {

    // THESE GO IN THE PROBLEM
    private Domain domain;
    private Set<State> startStates;
    private List<Formula> initially;
    private List<Formula> goals;
    private List<TimeConstraint> timeConstraints;

    // THESE ARE USED AT PARSE-TIME ONLY
    private Set<Fluent> allFluents;
    private Integer agentIndex;
    private Map<Fluent, Boolean> constants;
    private Map<String, String> allObjects;     // object name --> object type
    private Map<String, TypeNode> typeDefs;     // object type --> objects of that type (TypeNode.getGroundings())
    private Stack<Map<String, String>> variableStack;


    public DeplToProblem() {
        super();
    }


    // HELPER FUNCTIONS
    


    // READ A PARAMETER LIST AND GET A LIST OF MAPS
    // EACH MAP MAPS FROM EACH VARIABLE NAME TO ONE GROUND OBJECT
    // THE LIST OF MAPS GIVES EACH COMBINATION OF POSSIBLE VARIABLE GROUNDINGS

    private List<LinkedHashMap<String, String>> getVariableMaps(DeplParser.VariableDefListContext ctx) {
        List<LinkedHashMap<String, String>> maps = new ArrayList<LinkedHashMap<String,String>>();

        List<String> varNames = new ArrayList<String>();
        List<List<String>> varGroundings = new ArrayList<List<String>>();
        for (DeplParser.VariableDefContext variableDefCtx : ctx.variableDef()) {
            String varName = variableDefCtx.VARIABLE().getText();
            varNames.add(varName);
            String varType = variableDefCtx.objectType().getText();
            if (!(typeDefs.containsKey(varType))) {
                throw new RuntimeException("Cannot ground variable " + varName + ", unknown type: " + varType);
            }
            varGroundings.add(typeDefs.get(varType).groundings);
        }
        
        List<Pair<String, String>> inequalities = new ArrayList<>();
        for (DeplParser.VariableInequalityContext variableInequalityCtx : ctx.variableInequality()) {
            String lhs = variableInequalityCtx.lhs.getText();
            String rhs = variableInequalityCtx.rhs.getText();
            inequalities.add(new Pair<String, String>(lhs, rhs));
        }

        for (List<String> grounding : cartesianProduct(varGroundings)) {
            LinkedHashMap<String,String> groundingMap = new LinkedHashMap<String,String>();
            Iterator<String> nameIterator = varNames.iterator();
            Iterator<String> groundingIterator = grounding.iterator();
            while(nameIterator.hasNext() && groundingIterator.hasNext()) {
                groundingMap.put(nameIterator.next(), groundingIterator.next());
            }

            if(nameIterator.hasNext() || groundingIterator.hasNext()) {
                throw new RuntimeException("variable binding broke!");
            }

            boolean passesInequalities = true;
            for (Pair<String,String> unequal : inequalities) {
                if (groundingMap.get(unequal.getValue0()).equals(groundingMap.get(unequal.getValue1()))) {
                    passesInequalities = false;
                    break;
                }
            }

            if (passesInequalities) {
                maps.add(groundingMap);
            }
        }
        if (maps.isEmpty()) {
            maps.add(new LinkedHashMap<String,String>());
        }

        return maps;
    }



    // THIS FUNCTION IS COPIED FROM PHILLIP MEISTER:
    // https://stackoverflow.com/questions/714108/cartesian-product-of-arbitrary-sets-in-java
    private static <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
        List<List<T>> resultLists = new ArrayList<List<T>>();
        if (lists.size() == 0) {
            resultLists.add(new ArrayList<T>());
            return resultLists;
        } else {
            List<T> firstList = lists.get(0);
            List<List<T>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));
            for (T condition : firstList) {
                for (List<T> remainingList : remainingLists) {
                    ArrayList<T> resultList = new ArrayList<T>();
                    resultList.add(condition);
                    resultList.addAll(remainingList);
                    resultLists.add(resultList);
                }
            }
        }
        return resultLists;
    }



    // FOR STORING A TYPE DEFINITION, PARENT IS THE SUPERTYPE
    // GROUNDINGS ARE ALL OBJECTS OF THE TYPE
    // THE TYPE NAME ITSELF IS THE KEY TO THIS OBJECT IN A MAP
    private class TypeNode {
        private String parent;
        private List<String> groundings;
        public TypeNode(String parent) {
            this.parent = parent;
            this.groundings = new ArrayList<String>();
        }
        public String getParent() {
            return parent;
        }
        public List<String> getGroundings() {
            return groundings;
        }
    }


    // INPUT subtype COULD BE A TYPE OR AN OBJECT, supertype IS A TYPE
    private boolean isa(String subtype, String supertype) {
        if (allObjects.containsKey(subtype)) {
            subtype = allObjects.get(subtype);
        }
        assert typeDefs.containsKey(subtype);
        while (!subtype.equals(supertype)) {
            subtype = typeDefs.get(subtype).getParent();
            if (subtype == null) {
                return false;
            }
            assert typeDefs.containsKey(subtype);
        }
        return true;
    }



    public Problem buildProblem (String deplFileName) {
        
        CharStream inputStream = null;
        try {
            inputStream = CharStreams.fromFileName(deplFileName);
        }
        catch (IOException e) {
            System.err.println("failed to read input depl file: " + e.getMessage());
            System.exit(1);
        }

        DeplLexer lexer          = new DeplLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        DeplParser parser        = new DeplParser(tokens);


        parser.removeErrorListeners ();
        parser.addErrorListener (new BaseErrorListener ()
        {
            @Override
            public void syntaxError (final Recognizer <?,?> recognizer,
                                     Object sym,
                                     int line,
                                     int pos,
                                     String msg,
                                     RecognitionException e)
            {
                throw new RuntimeException ("\nDEPL SYNTAX ERROR: line:" + line + ", Position:" + pos + ". " + msg);
            }
        });

        ParseTree tree           = parser.init();

        this.domain = new Domain();
        this.startStates = new HashSet<State>();
        this.initially = new ArrayList<>();
        this.goals = new ArrayList<>();
        this.timeConstraints = new ArrayList<>();

        this.agentIndex = 0;
        this.allObjects = new HashMap<>();

        allFluents = new HashSet<>();
        this.constants = new HashMap<>();
        this.variableStack = new Stack<Map<String, String>>();

        this.typeDefs = new HashMap<String, TypeNode>();
        this.typeDefs.put("Object", new TypeNode(null));

        visit(tree);

        for (State s : startStates) {
            for (Formula f : initially) {
                if (!f.evaluate(s)) {
                    throw new RuntimeException("initially formula not satisfied: " + f.toString());
                }
            }
        }

        return new Problem(domain, startStates, goals, timeConstraints);
    }



    // TOP-LEVEL OF THE GRAMMAR

    @Override public Void visitInit(DeplParser.InitContext ctx) {
        visit(ctx.typesSection());
        visit(ctx.objectsSection());
        visit(ctx.agentsSection());
        if (ctx.passiveSection() != null) {visit(ctx.passiveSection());}
        visit(ctx.fluentsSection());
        if (ctx.constantsSection() != null) {visit(ctx.constantsSection());}
        if (ctx.startStateSection() != null) {visit(ctx.startStateSection());}
        if (ctx.initiallySection() != null) {visit(ctx.initiallySection());}
        visit(ctx.goalsSection());
        visit(ctx.actionsSection());

        return null;
    }



    // TYPES

    @Override public Void visitTypesSection(DeplParser.TypesSectionContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override public Void visitTypeDefinition(DeplParser.TypeDefinitionContext ctx) {
        String subtype = ctx.objectType(0).getText();
        String supertype = ctx.objectType(1).getText();
        this.typeDefs.put(subtype, new TypeNode(supertype));
        return null;
    }



    // OBJECTS

    @Override public Void visitObjectsSection(DeplParser.ObjectsSectionContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override public Void visitObjectDefinition(DeplParser.ObjectDefinitionContext ctx) {
        String objectName = ctx.objectName().getText();
        String objectType = ctx.objectType().getText();
        allObjects.put(objectName, objectType);
        while (objectType != null) {
            if (!typeDefs.containsKey(objectType)) {
                throw new RuntimeException("object type or supertype " + objectType + " not defined");
            }
            typeDefs.get(objectType).groundings.add(objectName);
            objectType = typeDefs.get(objectType).parent;
        }
        return null;
    }



    // AGENTS

    @Override public Void visitAgentsSection(DeplParser.AgentsSectionContext ctx) {
        visitChildren(ctx);

        if (domain.getTurnOrder().isEmpty()) {
            throw new RuntimeException("no agents defined");
        }

        return null;
    }

    @Override public Void visitAgentDef(DeplParser.AgentDefContext ctx) {
        visitChildren(ctx);
        String agent = ctx.objectName().getText();
        if (!allObjects.containsKey(agent)) {
            throw new RuntimeException("agent " + agent + " is not a defined object.");
        }
        if (ctx.UPPER_NAME() == null) {
            domain.addSystemAgent(agent);
        }
        else {
            String modelClassName = "mecaPlanner.agents." + ctx.UPPER_NAME().getText();
            try {
                Constructor constructor = Class.forName(modelClassName).getConstructor(String.class, Domain.class);
                Agent eAgentModel = (Agent) constructor.newInstance(agent, domain);
                domain.addEnvironmentAgent(agent, eAgentModel);
            }
            catch(Exception ex) {
                System.out.println(ex.toString());
                System.exit(1);
            }
        }
        return null;
    }

    @Override public Void visitPassiveSection(DeplParser.PassiveSectionContext ctx) {
        visitChildren(ctx);
        return null;
    }


    @Override public Void visitPassiveDef(DeplParser.PassiveDefContext ctx) {
        String name = ctx.objectName().getText();
        if (!allObjects.containsKey(name)) {
            throw new RuntimeException("passive agent " + name + " is not a defined object.");
        }
        domain.addPassiveAgent(name);
        return null;
    }



    // FLUENTS

    @Override public Void visitFluentsSection(DeplParser.FluentsSectionContext ctx) {
        for (DeplParser.ExpandableFluentContext fluentsCtx : ctx.expandableFluent()) {
            for (Fluent fluent : (Set<Fluent>) visit(fluentsCtx)) {
                if (allObjects.containsKey(fluent.getName())) {
                    Log.warning("Fluent name already used for object: " + fluent.getName());
                }
                if (constants.containsKey(fluent)) {
                    throw new RuntimeException("Can't define fluent, already a constant: " + fluent);
                }
                allFluents.add(fluent);
            }
        }
        return null;
    }

    @Override public List<String> visitExpandableObject(DeplParser.ExpandableObjectContext ctx) {
        if (ctx.objectName() == null) {
            return typeDefs.get(ctx.objectType().getText()).getGroundings();
        }
        List<String> objects = new ArrayList<>();
        objects.add(ctx.objectName().getText());
        return objects;
    }

    @Override public Set<Fluent> visitExpandableFluent(DeplParser.ExpandableFluentContext ctx) {
        String name = ctx.LOWER_NAME().getText();
        Set<Fluent> expandedFluents = new HashSet<>();
        if (ctx.expandableObject() == null) {
            expandedFluents.add(new Fluent(name));
            return expandedFluents;
        }
        List<List<String>> expandedParameters = new ArrayList<List<String>>();
        for (DeplParser.ExpandableObjectContext objCtx : ctx.expandableObject()) {
            expandedParameters.add((List<String>) visit(objCtx));
        }
        for (List<String> expansion : cartesianProduct(expandedParameters)) {
            expandedFluents.add(new Fluent(name, expansion));
        }
        return expandedFluents;
    }



    // CONSTANTS

    @Override public Void visitConstantsSection(DeplParser.ConstantsSectionContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override public Void visitConstant(DeplParser.ConstantContext ctx) {
        for (Fluent fluent : (Set<Fluent>) visit(ctx.expandableFluent())) {
            if (allFluents.contains(fluent)) {
                throw new RuntimeException("Constant previously defined as fluent: " + fluent);
            }
            if (ctx.OP_NOT() == null) {
                constants.put(fluent, true);
            }
            else {
                constants.put(fluent, false);
            }
        }
        return null;
    }



    // START STATE

    @Override public Void visitStartStateSection(DeplParser.StartStateSectionContext ctx) {
        for (DeplParser.ModelContext modelCtx : ctx.model()) {
            NDState ndState = (NDState) visit(modelCtx);
            startStates.addAll(ndState.getStates());
        }
        for (DeplParser.StateDefContext stateDefCtx : ctx.stateDef()) {
            //System.out.println("BUILDING STATE...");
            State state = (State) visit(stateDefCtx);
            startStates.add(state);
        }
        return null;
    }

    @Override public NDState visitModel(DeplParser.ModelContext ctx) {
        Map<String,World> worlds = new HashMap<>();
        Set<World> designatedWorlds = new HashSet<>();;
        for (DeplParser.WorldContext worldCtx : ctx.world()) {
            World world = (World) visit(worldCtx);
            worlds.put(world.getName(),world);
            if (worldCtx.STAR() != null) {
                designatedWorlds.add(world);
            }
        }

        if (designatedWorlds.isEmpty()) {
            throw new RuntimeException("initial state has no designaged worlds");
        }

        Set<World> worldSet = new HashSet<World>(worlds.values());
        NDState startState = new NDState(domain.getAllAgents(), worldSet, designatedWorlds);

        for (DeplParser.RelationContext relationCtx : ctx.relation()) {
            String agent = (String) visit(relationCtx.agent());

            for (DeplParser.RelateContext relateCtx : relationCtx.relate()) {
                String fromWorldName = relateCtx.from.getText();
                if (!worlds.containsKey(fromWorldName)) {
                    throw new RuntimeException("unknown world: " + fromWorldName);
                }
                String toWorldName = relateCtx.to.getText();
                if (!worlds.containsKey(toWorldName)) {
                    throw new RuntimeException("unknown world: " + toWorldName);
                }
                startState.addMorePlausibleTransitive(agent, worlds.get(fromWorldName), worlds.get(toWorldName));
            }

        }

        startState.trim();
        if (startState.normalize()) {
            // THIS ISN'T NECESSARILY A PROBLEM,
            // THIS CHECK IS JUST HERE BECAUSE I'M CURIOUS TO SEE WHEN IT HAPPENS
            System.out.println("start state not normal");
            //System.exit(1);
        }
        startState.reduce();

        return startState;
    }


    @Override public World visitWorld(DeplParser.WorldContext ctx) {
        String worldName = ctx.LOWER_NAME().getText();
        Set<Fluent> fluents = new HashSet<>();

        for (DeplParser.FluentContext fluentCtx : ctx.fluent()) {
            Fluent fluent = (Fluent) visit(fluentCtx);
            if (!allFluents.contains(fluent)) {
                throw new RuntimeException("unknown fluent: " + fluentCtx.getText());
            }
            fluents.add(fluent);
        }
 
        World world = new World(worldName, fluents);
        return world;
    }

    @Override public State visitStateDef(DeplParser.StateDefContext ctx) {

        Set<Fluent> trueFluents = new HashSet<>();
        for (DeplParser.FluentContext fluentCtx : ctx.fluent()) {
            trueFluents.add((Fluent) visit(fluentCtx));
        }
        World designatedWorld = new World("d", trueFluents);
        Set<World> worlds = new HashSet<>();
        worlds.add(designatedWorld);
        State state = new State(domain.getAllAgents(), worlds, designatedWorld);

        for (DeplParser.StateAssertionContext assertionCtx : ctx.stateAssertion()) {
            List<String> agents = new ArrayList<>();
            for (DeplParser.AgentContext agentCtx : assertionCtx.agent()) {
                agents.add((String) visit(agentCtx));
            }
            if (assertionCtx.doubts != null) {
                Fluent f = (Fluent) visit(assertionCtx.fluent());
                state = addDoubt(state, agents, f);
            }
            else if (assertionCtx.believes != null) {
                Fluent f = (Fluent) visit(assertionCtx.fluent());
                Boolean value = assertionCtx.OP_NOT() == null;
                state = addBelief(state, agents, f, value);
            }
            else if (assertionCtx.knows != null) {
                Formula f = (Formula) visit(assertionCtx.formula());
                state = addKnowledge(state, agents, f);
            }
            else {
                throw new RuntimeException("unknown start assertion");
            }
        }

        state.trim();
        if (state.normalize()) {
            // THIS ISN'T NECESSARILY A PROBLEM,
            // THIS CHECK IS JUST HERE BECAUSE I'M CURIOUS TO SEE WHEN IT HAPPENS
            System.out.println("start state not normal");
            //System.exit(1);
        }
        state.reduce();

        return state;
    }

    private State addDoubt(State state, List<String> agents, Fluent f) {
        Event actual = new Event("u", new Literal(true));
        Set<Formula> disjuncts = new HashSet<>();
        for (String a : agents) {
            disjuncts.add(new KnowsFormula(a, f));
            disjuncts.add(new KnowsFormula(a, f.negate()));
        }
        Formula someoneKnows = Formula.makeDisjunction(disjuncts);
        Event alternative = new Event("v", someoneKnows, new Assignment(f, f.negate()));
        Set<Event> events = new HashSet<>(Arrays.asList(actual, alternative));
        Set<Event> designatedEvents = new HashSet<>(Arrays.asList(actual));
        EventModel model = new EventModel("doubts-" + f, domain.getAllAgents(), events, designatedEvents);
        for (String a : agents) {
            model.addEdge(a, actual, alternative);
            model.addEdge(a, alternative, actual);
        }
        return model.transition(state);
    }

    private State addBelief(State state, List<String> agents, Fluent f, boolean val) {
        Event trueEvent = new Event("t", f);
        Event falseEvent = new Event("f", f.negate());
        Set<Event> events = new HashSet<>(Arrays.asList(trueEvent, falseEvent));
        String name = "believes-" + (!val ? "!" : "") + f;
        EventModel model = new EventModel(name, domain.getAllAgents(), events, events);
        for (String a : agents) {
            if (val) {
                model.addEdge(a, falseEvent, trueEvent);
            }
            else {
                model.addEdge(a, trueEvent, falseEvent);
            }
        }
        return model.transition(addDoubt(state, agents, f));
    }

    private State addKnowledge(State state, List<String> agents, Formula f) {
        Event trueEvent = new Event("t", f);
        Event falseEvent = new Event("f", f.negate());
        Set<Event> events = new HashSet<>(Arrays.asList(trueEvent, falseEvent));
        Set<Event> designatedEvents = new HashSet<>(Arrays.asList(trueEvent, falseEvent));
        EventModel model = new EventModel("knows-" + f, domain.getAllAgents(), events, designatedEvents);
        //System.out.println("adding: " + model.getName());
        for (String a : domain.getAllAgents()) {
            if (!agents.contains(a)) {
                //System.out.println("not agent: " + a);
                model.addEdge(a, trueEvent, falseEvent);
                model.addEdge(a, falseEvent, trueEvent);
            }
        }
        return model.transition(state);
    }


    @Override public Void visitInitiallySection(DeplParser.InitiallySectionContext ctx) {
        for (DeplParser.FormulaContext formulaCtx : ctx.formula()) {
            initially.add((Formula) visit(formulaCtx));
        }
        return null;
    }


    // GOALS
    @Override public Void visitGoal(DeplParser.GoalContext ctx) {
        if (ctx.formula() == null) {
            TimeConstraint constraint = (TimeConstraint) visit(ctx.timeConstraint());
            timeConstraints.add(constraint);
        }
        else {
            Formula goal = (Formula) visit(ctx.formula());
            goals.add(goal);
        }
        return null;
    }


    // ACTIONS
    @Override public Void visitActionDef(DeplParser.ActionDefContext ctx) {
        String actionName = ctx.LOWER_NAME().getText();
        for (LinkedHashMap<String,String> actionVariableMap : getVariableMaps(ctx.actionScope)) {
            variableStack.push(actionVariableMap);

            List<String> actionParameters = new ArrayList<String>(actionVariableMap.values());

            String owner = (String) visit(ctx.owner);
            if (!domain.isNonPassiveAgent(owner)) {
                throw new RuntimeException(actionName + " has unknown owner " + owner);
            }

            Integer cost = ctx.cost == null ? 1 : Integer.parseInt(ctx.cost.getText());

            List<Formula> preconditionList = new ArrayList<>();
            if (ctx.precondition != null) {
                for (LinkedHashMap<String,String> preconditionVariableMap : getVariableMaps(ctx.preconditionScope)) {
                    variableStack.push(preconditionVariableMap);
                    preconditionList.add((Formula) visit(ctx.precondition));
                    variableStack.pop();
                }
            }
            Formula precondition = AndFormula.make(preconditionList);
            if (precondition.isFalse()) {
                continue;
            }

            Action action = new Action(actionName, actionParameters, owner, cost, precondition, domain);

            for (DeplParser.ObservesDefContext obsCtx : ctx.observesDef()) {
                for (Map<String,String> variableMap : getVariableMaps(obsCtx.variableDefList())) {
                    variableStack.push(variableMap);
                    String agent = (String) visit(obsCtx.groundableObject());
                    if (!domain.getAllAgents().contains(agent)) {
                        throw new RuntimeException("Undefined observer " + agent);
                    }
                    Formula condition = obsCtx.condition == null ? new Literal(true) : (Formula) visit(obsCtx.condition);
                    action.setObservesCondition(agent, condition);
                    variableStack.pop();
                }
            }

            for (DeplParser.AwareDefContext awaCtx : ctx.awareDef()) {
                for (Map<String,String> variableMap : getVariableMaps(awaCtx.variableDefList())) {
                    variableStack.push(variableMap);
                    String agent = (String) visit(awaCtx.groundableObject());
                    if (!domain.getAllAgents().contains(agent)) {
                        throw new RuntimeException("Undefined aware agent " + agent);
                    }
                    Formula condition = awaCtx.condition == null ? new Literal(true) : (Formula) visit(awaCtx.condition);
                    action.setAwareCondition(agent, condition);
                    variableStack.pop();
                }
            }

            for (DeplParser.DeterminesDefContext detCtx : ctx.determinesDef()) {
                Formula formula = (Formula) visit(detCtx.determined);
                action.addDetermines(formula);
            }

            for (DeplParser.AnnouncesDefContext annCtx : ctx.announcesDef()) {
                Formula formula = (Formula) visit(annCtx.announced);
                action.addAnnouncement(formula);
            }

            for (DeplParser.CausesDefContext effCtx : ctx.causesDef()) {
                for (Map<String,String> variableMap : getVariableMaps(effCtx.variableDefList())) {
                    variableStack.push(variableMap);
                    Fluent fluent = (Fluent)visit(effCtx.fluent());
                    if (effCtx.OP_NOT() != null) {
                        if (effCtx.formula() != null) {
                            throw new RuntimeException("negated effect can't have a fomula");
                        }
                        action.addEffect(new Assignment(fluent, false));
                    }
                    else {
                        if (effCtx.formula() != null) {
                            Formula formula = (Formula)visit(effCtx.formula());
                            action.addEffect(new Assignment(fluent, formula));
                        }
                        else {
                            action.addEffect(new Assignment(fluent, true));
                        }
                    }
                    variableStack.pop();
                }
            }
            variableStack.pop();
            domain.addAction(action);

            //if (actionName.equals("move")) {
            //    System.out.println(action);
            //    System.exit(1);
            //}
        }
        return null;
    }

 


    @Override public Void visitEventModelDef(DeplParser.EventModelDefContext ctx) {
        Map<String,Event> events = new HashMap<>();   // EVENT NAMES TO EVENTS, WE USE THE NAMES FOR DEFINING RELATIONS
        Set<Event> designated = new HashSet<>();

        for (DeplParser.EventContext eventCtx : ctx.event()) {
            Event e = (Event) visit(eventCtx);
            String eventName = e.getName();
            if (events.containsKey(eventName)) {
                throw new RuntimeException("multiple events named " + eventName);
            }
            events.put(eventName, e);
            if (eventCtx.STAR() != null) {
                designated.add(e);
            }
        }

        String name = ctx.LOWER_NAME().getText();


        EventModel eventModel =  new EventModel(name, domain.getAllAgents(), new HashSet<Event>(events.values()), designated);

        for (DeplParser.EventRelationContext relationCtx : ctx.eventRelation()) {
            String agent = (String) visit(relationCtx.agent());
            for (DeplParser.EdgeContext edgeCtx : relationCtx.edge()) {
                String from = edgeCtx.from.getText();
                String to = edgeCtx.to.getText();
                if (!events.containsKey(from)) {
                    throw new RuntimeException("undefined event: " + from);
                }
                if (!events.containsKey(to)) {
                    throw new RuntimeException("undefined event: " + to);
                }
                Formula condition = edgeCtx.formula() == null ? new Literal(true) : (Formula) visit(edgeCtx.formula());
                eventModel.addEdge(agent, events.get(from), events.get(to), condition);
            }
        }

        domain.addEventModel(eventModel);
        return null;
    }

    @Override public Event visitEvent(DeplParser.EventContext ctx) {
        String name = ctx.LOWER_NAME().getText();
        Formula precondition = (Formula) visit(ctx.formula());
        Set<Assignment> assignments = new HashSet<>();
        if (ctx.deletes != null) {
            for (Fluent f : (Set<Fluent>) visit(ctx.deletes)) {
                assignments.add(new Assignment(f, false));
            }
            for (Fluent f : (Set<Fluent>) visit(ctx.adds)) {
                assignments.add(new Assignment(f, true));
            }
        }
        else {
            for (DeplParser.AssignmentContext assignmentCtx : ctx.assignment()) {
                assignments.add((Assignment) visit(assignmentCtx));
            }
        }
        return new Event(name, precondition, assignments);
    }

    @Override public Assignment visitAssignment(DeplParser.AssignmentContext ctx) {
        Formula formula = (Formula) visit(ctx.formula());
        Fluent fluent = (Fluent) visit(ctx.fluent());
        return new Assignment(fluent, formula);
    }

    @Override public Set<Fluent> visitAtoms(DeplParser.AtomsContext ctx) {
        Set<Fluent> atoms = new HashSet<>();
        for (DeplParser.FluentContext fluentCtx : ctx.fluent()) {
            atoms.add((Fluent) visit(fluentCtx));
        }
        return atoms;
    }


    @Override public String visitGroundableObject(DeplParser.GroundableObjectContext ctx) {
        if (ctx.objectName() != null) {
            String name = ctx.objectName().getText();
            if (!allObjects.containsKey(name)) {
                throw new RuntimeException("undefined object: " + name);
            }
            return name;
        }

        String variable = ctx.VARIABLE().getText();
        String grounding = null;
        Stack<Map<String, String>> tempStack = new Stack<Map<String, String>>();

        while (!variableStack.empty()) {
            Map<String, String> variableMap = variableStack.pop();
            tempStack.push(variableMap);
            if (variableMap.containsKey(variable)) {
                grounding = variableMap.get(variable);
                break;
            }
        }

        while (!tempStack.empty()) {
            variableStack.push(tempStack.pop());
        }

        if (grounding == null) {
            throw new RuntimeException("undefined variable: " + variable);
        }
        else {
            return grounding;
        }
    }

    @Override public String visitAgent(DeplParser.AgentContext ctx) {
        String agentName = ctx.getText();
        if (!domain.isAgent(agentName)) {
            throw new RuntimeException("unknown agent grounding '" + agentName + "' in formula: " + ctx.getText());
        }
        return agentName;
    }

    @Override public String visitGroundableAgent(DeplParser.GroundableAgentContext ctx) {
        String agentName = (String) visit(ctx.groundableObject());
        if (!domain.isAgent(agentName)) {
            throw new RuntimeException("unknown agent grounding '" + agentName + "' in formula: " + ctx.getText());
        }
        return agentName;
    }


    @Override public Fluent visitFluent(DeplParser.FluentContext ctx) {
        if (ctx.fluent() != null) {
            return (Fluent) visit(ctx.fluent());
        }
        String fluentName = ctx.LOWER_NAME().getText();
        List<String> parameters = new ArrayList<>();
        for (DeplParser.GroundableObjectContext objCtx : ctx.groundableObject()) {
            parameters.add((String) visit(objCtx));
        }
        Fluent fluent = new Fluent(fluentName, parameters);
        if (constants.containsKey(fluent)) {
            return new Literal(constants.get(fluent));
        }
        if (!allFluents.contains(fluent)) {
            throw new RuntimeException("unknown fluent: " + fluent);
        }
        return fluent;
    }

    @Override public Formula visitFluentFormula(DeplParser.FluentFormulaContext ctx) {
        Fluent fluent = (Fluent) visit(ctx.fluent());
//        if (constants.containsKey(fluent)) {
//            return new Literal(constants.get(fluent));
//        }
//        if (!allFluents.contains(fluent)) {
//            throw new RuntimeException("unknown fluent: " + fluent);
//        }
        return fluent;
    }

    @Override public Formula visitTrueFormula(DeplParser.TrueFormulaContext ctx) {
        return new Literal(true);
    }

    @Override public Formula visitFalseFormula(DeplParser.FalseFormulaContext ctx) {
        return new Literal(false);
    }

    @Override public Formula visitParensFormula(DeplParser.ParensFormulaContext ctx) {
        return (Formula) visit(ctx.formula());
    }

    @Override public Formula visitNotFormula(DeplParser.NotFormulaContext ctx) {
        Formula inner = (Formula) visit(ctx.formula());
        return (NotFormula.make(inner));
    }

    @Override public Formula visitAndFormula(DeplParser.AndFormulaContext ctx) {
        List<Formula> subFormulae = new ArrayList<>();
        for (DeplParser.FormulaContext subFormula : ctx.formula()) {
            subFormulae.add((Formula) visit(subFormula));
        }
        return (AndFormula.make(subFormulae));
    }

    @Override public Formula visitOrFormula(DeplParser.OrFormulaContext ctx) {
        List<Formula> subFormulae = new ArrayList<>();
        for (DeplParser.FormulaContext subFormula : ctx.formula()) {
            subFormulae.add((Formula) visit(subFormula));
        }
        return Formula.makeDisjunction(subFormulae);
    }

    @Override public Formula visitImpliesFormula(DeplParser.ImpliesFormulaContext ctx) {
        List<Formula> subFormulae = new ArrayList<>();
        Formula leftFormula = (Formula) visit(ctx.formula().get(0));
        Formula rightFormula = (Formula) visit(ctx.formula().get(1));
        return (Formula.makeDisjunction(Arrays.asList(leftFormula.negate(), rightFormula)));
    }

    @Override public Formula visitKnowsFormula(DeplParser.KnowsFormulaContext ctx) {
        Formula inner = (Formula) visit(ctx.formula());
        String agentName = (String) visit(ctx.groundableAgent());
        return new KnowsFormula(agentName, inner);
    }

    @Override public Formula visitSafeFormula(DeplParser.SafeFormulaContext ctx) {
        Formula inner = (Formula) visit(ctx.formula());
        String agentName = (String) visit(ctx.groundableAgent());
        return new SafeFormula(agentName, inner);
    }

    @Override public Formula visitBelievesFormula(DeplParser.BelievesFormulaContext ctx) {
        Formula inner = (Formula) visit(ctx.formula());
        String agentName = (String) visit(ctx.groundableAgent());
        return new BelievesFormula(agentName, inner);
    }

    @Override public Formula visitKnowsDualFormula(DeplParser.KnowsDualFormulaContext ctx) {
        Formula inner = (Formula) visit(ctx.formula());
        String agentName = (String) visit(ctx.groundableAgent());
        return NotFormula.make(new KnowsFormula(agentName, NotFormula.make(inner)));
    }

    @Override public Formula visitSafeDualFormula(DeplParser.SafeDualFormulaContext ctx) {
        Formula inner = (Formula) visit(ctx.formula());
        String agentName = (String) visit(ctx.groundableAgent());
        return NotFormula.make(new SafeFormula(agentName, NotFormula.make(inner)));
    }

    @Override public Formula visitBelievesDualFormula(DeplParser.BelievesDualFormulaContext ctx) {
        Formula inner = (Formula) visit(ctx.formula());
        String agentName = (String) visit(ctx.groundableAgent());
        return NotFormula.make(new BelievesFormula(agentName, NotFormula.make(inner)));
    }



    // TIME FORMULAE

    @Override public TimeConstraint visitTimeConstraint(DeplParser.TimeConstraintContext ctx) {
        return new TimeConstraint((TimeConstraint.Inequality) visit(ctx.inequality()),
                                  Integer.parseInt(ctx.INTEGER().getText()));
    }

    @Override public TimeConstraint.Inequality visitInequalityEq(DeplParser.InequalityEqContext ctx) {
        return TimeConstraint.Inequality.EQ;
    }

    @Override public TimeConstraint.Inequality visitInequalityEq2(DeplParser.InequalityEq2Context ctx) {
        return TimeConstraint.Inequality.EQ;
    }

    @Override public TimeConstraint.Inequality visitInequalityNe(DeplParser.InequalityNeContext ctx) {
        return TimeConstraint.Inequality.NE;
    }

    @Override public TimeConstraint.Inequality visitInequalityNe2(DeplParser.InequalityNe2Context ctx) {
        return TimeConstraint.Inequality.NE;
    }

    @Override public TimeConstraint.Inequality visitInequalityLt(DeplParser.InequalityLtContext ctx) {
        return TimeConstraint.Inequality.LT;
    }

    @Override public TimeConstraint.Inequality visitInequalityLte(DeplParser.InequalityLteContext ctx) {
        return TimeConstraint.Inequality.LTE;
    }

    @Override public TimeConstraint.Inequality visitInequalityGt(DeplParser.InequalityGtContext ctx) {
        return TimeConstraint.Inequality.GT;
    }

    @Override public TimeConstraint.Inequality visitInequalityGte(DeplParser.InequalityGteContext ctx) {
        return TimeConstraint.Inequality.GTE;
    }

}
