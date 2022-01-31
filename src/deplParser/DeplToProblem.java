package depl;

import mecaPlanner.agents.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import mecaPlanner.*;
import mecaPlanner.formulae.*;
import mecaPlanner.state.*;

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

import java.io.IOException;


public class DeplToProblem extends DeplBaseVisitor {

    // THESE GO IN THE PROBLEM
    private Domain domain;
    private Integer systemAgentIndex;
    private Set<State> startStates;
    private Map<String, Agent> startingModels;
    private Set<Formula> goals;
    private Set<TimeConstraint> timeConstraints;

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
            maps.add(groundingMap);
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
        this.systemAgentIndex = null;
        this.startStates = new HashSet<State>();
        this.startingModels = new HashMap<>();
        this.goals = new HashSet<>();
        this.timeConstraints = new HashSet<>();

        this.agentIndex = 0;
        this.allObjects = new HashMap<>();

        allFluents = new HashSet<>();
        this.constants = new HashMap<>();
        this.variableStack = new Stack<Map<String, String>>();

        this.typeDefs = new HashMap<String, TypeNode>();
        this.typeDefs.put("Object", new TypeNode(null));

        visit(tree);

        return new Problem(domain, systemAgentIndex, startStates, startingModels, goals, timeConstraints);
    }



    // TOP-LEVEL OF THE GRAMMAR

    @Override public Void visitInit(DeplParser.InitContext ctx) {
        visit(ctx.typesSection());
        visit(ctx.objectsSection());
        visit(ctx.agentsSection());
        if (ctx.passiveSection() != null) {visit(ctx.passiveSection());}
        visit(ctx.fluentsSection());
        if (ctx.constantsSection() != null) {visit(ctx.constantsSection());}
        visit(ctx.initiallySection());
        if (ctx.postSection() != null) {visit(ctx.postSection());}
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

        if (domain.getAllAgents().isEmpty()) {
            throw new RuntimeException("no agents defined");
        }

        if (this.systemAgentIndex == null) {
            throw new RuntimeException("no system agent defined");
        }

        assert(domain.getAllAgents().containsAll(domain.getNonPassiveAgents()));

        return null;
    }

    @Override public Void visitAgentDef(DeplParser.AgentDefContext ctx) {
        visitChildren(ctx);
        String agent = ctx.objectName().getText();

        if (!allObjects.containsKey(agent)) {
            throw new RuntimeException("agent " + agent + " is not a defined object.");
        }

        if (ctx.UPPER_NAME() == null) {
            if (this.systemAgentIndex != null) {
                throw new RuntimeException("cannot define multiple system agents");
            }
            this.systemAgentIndex = this.agentIndex;
        }
        else {
            String modelClassName = "mecaPlanner.models." + ctx.UPPER_NAME().getText();
            try {
                Constructor constructor = Class.forName(modelClassName).getConstructor(String.class, Domain.class);
                Agent eAgentModel = (Agent) constructor.newInstance(agent, domain);
                startingModels.put(agent, eAgentModel);
            }
            catch(Exception ex) {
                System.out.println(ex.toString());
                System.exit(1);
            }
        }
        domain.addAgent(agent);
        this.agentIndex += 1;
        return null;
    }

    @Override public Void visitPassiveSection(DeplParser.PassiveSectionContext ctx) {
        visitChildren(ctx);
        assert(domain.getAllAgents().containsAll(domain.getNonPassiveAgents()));
        return null;
    }


    @Override public Void visitPassiveDef(DeplParser.PassiveDefContext ctx) {
        String name = ctx.objectName().getText();
        if (!allObjects.containsKey(name)) {
            throw new RuntimeException("passive agent " + name + " is not a defined object.");
        }
        domain.addPassive(name);
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



    // INITIALLY

    @Override public Void visitInitiallySection(DeplParser.InitiallySectionContext ctx) {
        for (DeplParser.StartStateDefContext stateCtx : ctx.startStateDef()) {
            if (stateCtx.model() != null) {
                NDState ndState = (NDState) visit(stateCtx.model());
                startStates.addAll(ndState.getStates());
            }
            else {
                Set<State> constructedStates = (Set<State>) visit(stateCtx.initiallyDef());
                startStates.addAll(constructedStates);
            }
        }
        return null;
    }

    @Override public Void visitPostSection(DeplParser.PostSectionContext ctx) {
        Log.warning("Post-state construction and checking not implemented");
        return null;
    }

    @Override public Set<State> visitInitiallyDef(DeplParser.InitiallyDefContext ctx) {
        Log.warning("State construction not implemented");
        return null;
        // List<Formula> initialFormulae = new ArrayList<>();
        // for (DeplParser.BeliefFormulaContext formulaCtx : ctx.formula()) {
        //     Formula formula = (Formula) visit(formulaCtx);
        //     initialFormulae.add(formula);
        // }
        // Set<EpistemicState> states = Construct.constructStates(domain, initialFormulae);
        // if (states.isEmpty()) {
        //     throw new RuntimeException("constructed model is null...");
        // }
        // for (EpistemicState s : states) {
        //     for (Formula f : initialFormulae) {
        //         if (!f.evaluate(s)) {
        //             throw new RuntimeException("model construction failed.");
        //         }
        //     }
        // }
        // return states;
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
        NDState startState = new NDState(new HashSet<String>(domain.getAllAgents()), worldSet, designatedWorlds);

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

        // Log.debug("reducing start state...");
        // startState.reduce();
        // startState.getKripke().forceCheck();
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
    @Override public Void visitActionDefinition(DeplParser.ActionDefinitionContext ctx) {
        String actionName = ctx.LOWER_NAME().getText();
        for (LinkedHashMap<String,String> actionVariableMap : getVariableMaps(ctx.variableDefList())) {
            variableStack.push(actionVariableMap);

            List<String> actionParameters = new ArrayList<String>(actionVariableMap.values());

            String owner = null;

            int cost = 1;
            List<Formula> preconditionList = new ArrayList<>();
            Map<String, List<Formula>> observesLists = new HashMap<>();
            Map<String, List<Formula>> awareLists = new HashMap<>();
            for (String a : domain.getAgents()) {
                observesLists.put(a, new ArrayList<Formula>());
                awareLists.put(a, new ArrayList<Formula>());
            }

            Map<Formula, Formula> determines = new HashMap<>();
            Map<Formula, Formula> announces = new HashMap<>();
            Map<Assignment, Formula> effects = new HashMap<>();

            for (DeplParser.ActionFieldContext fieldCtx : ctx.actionField()) {

                if (fieldCtx.ownerActionField() != null) {
                    owner = (String) visit(fieldCtx.ownerActionField().groundableObject());
                    if (!domain.isNonPassiveAgent(owner)) {
                        throw new RuntimeException("action " + actionName + " owner " + owner +
                                                   " not a declared system or environment agent.");
                    }
                }

                else if (fieldCtx.costActionField() != null) {
                    cost = Integer.parseInt(fieldCtx.costActionField().INTEGER().getText());
                }


                else if (fieldCtx.preconditionActionField() != null) {
                    DeplParser.PreconditionActionFieldContext preCtx = fieldCtx.preconditionActionField();
                    for (Map<String,String> variableMap : getVariableMaps(preCtx.variableDefList())) {
                        variableStack.push(variableMap);
                        preconditionList.add((Formula) visit(preCtx.formula()));
                        variableStack.pop();
                    }
                }

                else if (fieldCtx.observesActionField() != null) {
                    DeplParser.ObservesActionFieldContext obsCtx = fieldCtx.observesActionField();
                    for (Map<String,String> variableMap : getVariableMaps(obsCtx.variableDefList())) {
                        variableStack.push(variableMap);
                        String agentName = (String) visit(obsCtx.groundableObject());
                        if (!domain.getAllAgents().contains(agentName)) {
                            throw new RuntimeException("Observer \"" + agentName + "\" is not a defined agent.");
                        }
                        Formula condition;
                        if (obsCtx.condition == null) {
                            condition = new Literal(true);
                        }
                        else {
                            condition = (Formula) visit(obsCtx.condition);
                        }
                        if (!condition.isFalse()) {
                            observesLists.get(agentName).add(condition);
                        }
                        variableStack.pop();
                    }
                }

                else if (fieldCtx.awareActionField() != null) {
                    DeplParser.AwareActionFieldContext awaCtx = fieldCtx.awareActionField();
                    for (Map<String,String> variableMap : getVariableMaps(awaCtx.variableDefList())) {
                        variableStack.push(variableMap);
                        String agentName = (String) visit(awaCtx.groundableObject());
                        Formula condition;
                        if (awaCtx.condition == null) {
                            condition = new Literal(true);
                        }
                        else {
                            condition = (Formula) visit(awaCtx.condition);
                        }
                        if (!condition.isFalse()) {
                            awareLists.get(agentName).add(condition);
                        }
                        variableStack.pop();
                    }
                }

                else if (fieldCtx.determinesActionField() != null) {
                    DeplParser.DeterminesActionFieldContext detCtx = fieldCtx.determinesActionField();
                    for (Map<String,String> variableMap : getVariableMaps(detCtx.variableDefList())) {
                        variableStack.push(variableMap);
                        Formula condition;
                        if (detCtx.condition == null)  {
                            condition = new Literal(true);
                        }
                        else {
                            condition = (Formula) visit(detCtx.condition);
                        }
                        if (!(condition.isFalse())){
                            Formula sensed = (Formula) visit(detCtx.determined);
                            determines.put(sensed, condition);
                        }
                        variableStack.pop();
                    }
                }

                else if (fieldCtx.announcesActionField() != null) {
                    DeplParser.AnnouncesActionFieldContext annCtx = fieldCtx.announcesActionField();
                    for (Map<String,String> variableMap : getVariableMaps(annCtx.variableDefList())) {
                        variableStack.push(variableMap);
                        Formula condition;
                        if (annCtx.condition == null)  {
                            condition = new Literal(true);
                        }
                        else {
                            condition = (Formula) visit(annCtx.condition);
                        }
                        if (!(condition.isFalse())){
                            Formula announcement = (Formula) visit(annCtx.announced);
                            announces.put(announcement, condition);
                        }
                        variableStack.pop();
                    }
                }

                else if (fieldCtx.causesActionField() != null) {
                    DeplParser.CausesActionFieldContext effCtx = fieldCtx.causesActionField();
                    for (Map<String,String> variableMap : getVariableMaps(effCtx.variableDefList())) {
                        variableStack.push(variableMap);
                        Formula condition;
                        if (effCtx.condition == null)  {
                            condition = new Literal(true);
                        }
                        else {
                            condition = (Formula) visit(effCtx.condition);
                        }
                        if (!(condition.isFalse())){
                            Fluent fluent = (Fluent) visit(effCtx.fluent());
                            boolean isAddEffect = (effCtx.OP_NOT() == null);
                            effects.put(new Assignment(fluent, isAddEffect), condition);
                        }
                        variableStack.pop();
                    }
                }

                else {
                    throw new RuntimeException("invalid action field, somehow a syntax error didn't get caught?");
                }
            }

            Formula precondition = AndFormula.make(preconditionList);

            if (owner == null) {
                throw new RuntimeException("illegal action definition, no owner: " + actionName);
            }

            Map<String, Formula> observes = new HashMap<>();
            Map<String, Formula> aware = new HashMap<>();
            for (String a : domain.getAgents()) {
                observes.put(a, (Formula.makeDisjunction(observesLists.get(a))));
                aware.put(a, (Formula.makeDisjunction(awareLists.get(a))));
            }

            assert (actionName != null);
            assert (actionParameters != null);
            assert (owner != null);
            assert (precondition != null);
            assert (observes != null);
            assert (aware != null);
            assert (determines != null);
            assert (announces != null);
            assert (effects != null);
            assert (domain != null);


            if (!precondition.isFalse()) {

                Action action =  new Action(actionName,
                                            actionParameters,
                                            owner,
                                            cost,
                                            precondition,
                                            observes,
                                            aware,
                                            determines,
                                            announces,
                                            effects,
                                            domain
                                           );

                domain.addAction(action);
            }

            variableStack.pop();
        }
        return null;
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
        return fluent;
    }

    @Override public Formula visitFluentFormula(DeplParser.FluentFormulaContext ctx) {
        Fluent fluent = (Fluent) visit(ctx.fluent());
        if (constants.containsKey(fluent)) {
            return new Literal(constants.get(fluent));
        }
        if (!allFluents.contains(fluent)) {
            throw new RuntimeException("unknown fluent: " + fluent);
        }
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
