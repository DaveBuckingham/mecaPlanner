package depl;

import mecaPlanner.models.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import mecaPlanner.*;
import mecaPlanner.formulae.atomic.*;
import mecaPlanner.formulae.fluent.*;
import mecaPlanner.formulae.belief.*;
import mecaPlanner.state.*;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
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

    // GO IN PROBLEM
    private Domain domain;
    private Integer systemAgentIndex;
    private Set<EpistemicState> startStates;
    private Map<String, Model> startingModels;
    private Set<BeliefFormula> goals;

    // USED FOR PARSE-TIME CHECKS, DON'T GO IN DOMAIN
    private Map<Fluent,String> fluents;              // fluents and their fluent types
    private Integer agentIndex;
    private Set<String> allObjects;          // to check for undefined objects
    private Map<Fluent,Value> constants;
    private Map<String, TypeNode> typeDefs;  // key is object type name
    private Stack<Map<String, String>> variableStack;


    public DeplToProblem() {
        super();
    }


    // HELPER FUNCTIONS


    // READ A PARAMETER LIST AND GET A LIST OF MAPS
    // EACH LIST ELEMENT MAPS FROM EACH VARIABLE NAME TO ONE GROUND OBJECT
    // THE LIST GIVES THE CARTESIAN PRODUCT OF EACH POSSIBLE VARIABLE GROUNDING

    private List<LinkedHashMap<String, String>> getVariableMaps(DeplParser.VariableDefListContext ctx) {
        List<Map<String, String>> maps = new ArrayList<LinkedHashMap<String,String>>();

        List<String> varNames = new ArrayList<String>();
        List<List<String>> varGroundings = new ArrayList<List<String>>();
        for (DeplParser.ParameterContext variableDefCtx : ctx.variableDef()) {
            varNames.add(variableDefCtx.VARIABLE().getText());
            varGroundings.add(typeDefs.get(varDefCtx.OBJECT_TYPE().getText()).groundings);
        }

        for (List<String> grounding : cartesianProduct(variableGroundings)) {
            Map<String,String> groundingMap = new LinkedHashMap<String,String>();
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
            maps.put(new LinkedHashMap<String,String>());
        }

        return maps;
    }


    private isFalse(FluentFormula ff) {
        return (ff instanceof FluentFormulaAtom && !((FluentFormulaAtom) ff).isFalse()) {
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
        public String parent;
        public List<String> groundings;
        public TypeNode(String parent) {
            this.parent = parent;
            this.groundings = new ArrayList<String>();
        }
    }


    // FOR STORING AN ASSIGNMENT TO A FLUENTS OR CONSTANTS
    // MULTIPLE REFERENCES IN CASE ITS EXPANDABLE
    private class Assignment {
        private Set<Fluent> references;
        private Value value;
        public TypeNode(Set<Fluent> references, Value value) {
            this.references = references;
            this.value = valeu;
        }
        public Set<Fluent> getReferences() {
            return references;
        }
        public Value getValue() {
            return value;
        }
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
        ParseTree tree           = parser.init();

        this.domain = new Domain();
        this.systemAgentIndex = null;
        this.startStates = new HashSet<>();
        this.startingModels = new HashMap<>();
        this.goals = new HashSet<>();

        this.agentIndex = 0;
        this.allObjects = new HashSet<String>();
        this.constants = new HashSet<FluentAtom>();
        this.constraints = new HashSet<FluentAtom>();
        this.typeDefs = new HashMap<String, TypeNode>();
        this.variableStack = new Stack<Map<String, String>>();


        visit(tree);

        return new Problem(domain, systemAgentIndex, startStates, startingModels, goals);
    }




    // TOP-LEVEL OF THE GRAMMAR

    @Override public Void visitInit(DeplParser.InitContext ctx) {
        visit(ctx.typesSection());
        visit(ctx.objectsSection());
        visit(ctx.agentsSection());
        if (ctx.passiveSection() != null) {visit(ctx.passiveSection());}
        visit(ctx.atomsSection());
        if (ctx.constantsSection() != null) {visit(ctx.constantsSection());}
        if (ctx.constraintsSection() != null) {visit(ctx.constraintsSection());}
        visit(ctx.initiallySection());
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
        String subtype = ctx.NAME().getText();
        String supertype = ctx.type().getText();
        this.typeDefs.put(subtype, new TypeNode(supertype));
        return null;
    }



    // OBJECTS

    @Override public Void visitObjectsSection(DeplParser.ObjectsSectionContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override public Void visitObjectDefinition(DeplParser.ObjectDefinitionContext ctx) {
        String objectName = ctx.NAME().getText();
        String objectType = ctx.type().getText();
        while (!objectType.equalsIgnoreCase("object")) {
            if (!typeDefs.containsKey(objectType)) {
                throw new RuntimeException("object type or supertype " + objectType + " not defined");
            }
            typeDefs.get(objectType).groundings.add(objectName);
            objectType = typeDefs.get(objectType).parent;
        }
        allObjects.add(objectName);
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
        return null;
    }

    @Override public Void visitSystemAgent(DeplParser.SystemAgentContext ctx) {
        String agent = ctx.NAME().getText();
        if (this.systemAgentIndex != null) {
            throw new RuntimeException("cannot define multiple system agents");
        }
        domain.addAgent(agent);
        this.systemAgentIndex = this.agentIndex;
        this.agentIndex += 1;
        return null;
    }

    @Override public Void visitEnvironmentAgent(DeplParser.EnvironmentAgentContext ctx) {
        String agent = ctx.NAME().getText();

        Model model = null;
        String modelClassName = "mecaPlanner.models." + ctx.CLASS().getText();
        try {
            Constructor constructor = Class.forName(modelClassName).getConstructor(String.class, Domain.class);
            model = (Model) constructor.newInstance(agent, domain);
        }
        catch(ClassNotFoundException ex) {
            System.out.println(ex.toString());
            System.exit(1);
        }
        catch(NoSuchMethodException ex) {
            System.out.println(ex.toString());
            System.exit(1);
        }
        catch(InstantiationException ex) {
            System.out.println(ex.toString());
            System.exit(1);
        }
        catch(IllegalAccessException ex) {
            System.out.println(ex.toString());
            System.exit(1);
        }
        catch(InvocationTargetException ex) {
            System.out.println(ex.toString());
            System.out.println("Exception while instantiating " + modelClassName);
            System.out.println("The cause was: " + ex.getCause());
            System.exit(1);
        }

        startingModels.put(agent, model);
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
        String name = ctx.NAME().getText();
        domain.addPassive(name);
        return null;
    }



    // FLUENTS

    @Override public Void visitFluentsSection(DeplParser.FluentsSectionContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override public Set<Fluent> visitExpandableFluent(DeplParser.ExpandableFluentContext ctx) {
        String name = ctx.LOWER_NAME().getText();
        if (ctx.variableList() == null) {
            atoms.add(new FluentAtom(name));
            return atoms;
        }
        List<List<String>> expandedParameters = new ArrayList<List<String>>();
        for (DeplParser.ExpandableObjectContext parameterCtx : ctx.expandableObject()) {
            expandedPareters.add(visit parameterCtx);
        }
        Set<Fluent> expandedFluents = new HashSet<>();
        for (List<String> expansion : cartesianProduct(expandedParameters)) {
            expandedFluents.add(new Fluent(expansion));
        }
        return expandedFluents;
    }

    //HERE
    @Override public Void visitFluentDef(DeplParser.FluentDefContext ctx) {
        String type = ctx.FLUENT_TYPE.getText();
        for (Fluent fluent : visit(ctx.expandableFluent()) {
            fluents.add(fluent, type);
        }
    }



//              domain.addAtoms(atoms);
//          Set<FluentAtom> atoms = new HashSet<>();;
//          String name = ctx.NAME().getText();
//          if (ctx.variableList() == null) {
//              atoms.add(new FluentAtom(name));
//              return atoms;
//          }
//          for (Map<String,String> variableMap : getVariableMaps(ctx.variableList())) {
//              variableStack.push(variableMap);
//              List<String> groundParameters = new ArrayList<String>();
//              for (DeplParser.ParameterContext parameterCtx : ctx.parameterList().parameter()) {
//                  groundParameters.add(resolveVariable(parameterCtx));
//              }
//              atoms.add(new FluentAtom(name, groundParameters));
//              variableStack.pop();
//          }
//          return atoms;
//      }


    // CONSTANTS

    @Override public Void visitConstantsSection(DeplParser.ConstantsSectionContext ctx) {
        for (DeplParser.AssignmentContext assignmentCtx : ctx.assignment()) {
            Assignment assignment = (Assignment) visit(assignmentCtx);
            for (Fluent constant : assignment.getReferences()) {
                constants.put(constant, assignment.getValue());
            }
        }
        return null;
    }

    @Override public Assignment visitAssignment(DeplParser.AssignmentContext ctx) {
        Value value = visit(ctx.value());
        Set<Fluent> fluents = new hashSet<>();
        for (Fluent fluent : (Fluent) visit(ctx.expandableFluent()) {
            fluent.add(fluent, value);
        }
        return new Assignment(fluents, value);
    }








    // INITIALLY

    @Override public Void visitInitiallySection(DeplParser.InitiallySectionContext ctx) {
        visitChildren(ctx);
        return null;
    }

    // @Override public Void visitInitiallyDef(DeplParser.InitiallyDefContext ctx) {
    //     Set<BeliefFormula> initiallyStatements = new HashSet<>();
    //     for (DeplParser.BeliefFormulaContext statementContext : ctx.beliefFormula()) {
    //         BeliefFormula statement = (BeliefFormula) visit(statementContext);
    //         initiallyStatements.add(statement);
    //     }
    //     try {
    //         startStates.add(Initialize.constructState(initiallyStatements, domain, true));
    //     }
    //     catch (Exception ex) {
    //         ex.printStackTrace();
    //         System.exit(1);
    //     }
    //     return null;
    // }


    @Override public Void visitKripkeModel(DeplParser.KripkeModelContext ctx) {
        Map<String, World> worlds = new HashMap<>();
        World designatedWorld = null;
        Map<String, Relation> beliefRelation = new HashMap<>();
        Map<String, Relation> knowledgeRelation = new HashMap<>();
        for (DeplParser.KripkeFormulaContext statementContext : ctx.kripkeFormula()) {
            if (statementContext instanceof DeplParser.KripkeWorldContext) {
                World world = (World) visit(statementContext);
                worlds.put(world.getName(), world);
                if (designatedWorld == null) {
                    designatedWorld = world;
                }
            }
            else if (statementContext instanceof DeplParser.KripkeRelationContext) {
                DeplParser.KripkeRelationContext relationContext = (DeplParser.KripkeRelationContext) statementContext;
                String agent = relationContext.NAME().getText();
                Relation relation = new Relation();
                List<String> fromWorlds = new ArrayList<>();
                List<String> toWorlds = new ArrayList<>();
                for (DeplParser.FromWorldContext fromWorld : relationContext.fromWorld()) {
                    String fromWorldName = fromWorld.getText();
                    if (!worlds.containsKey(fromWorldName)) {
                        throw new RuntimeException("unknown world: " + fromWorldName);
                    }
                    fromWorlds.add(fromWorldName);
                }
                for (DeplParser.ToWorldContext toWorld : relationContext.toWorld()) {
                    String toWorldName = toWorld.getText();
                    if (!worlds.containsKey(toWorldName)) {
                        throw new RuntimeException("unknown world: " + toWorldName);
                    }
                    toWorlds.add(toWorldName);
                }
                assert(fromWorlds.size() == toWorlds.size());
                for (int i = 0; i < fromWorlds.size(); i++) {
                    relation.connect(worlds.get(fromWorlds.get(i)), worlds.get(toWorlds.get(i)));
                }
                String relationType = relationContext.relationType().getText();
                if (relationType.equals("B_")) {
                    beliefRelation.put(agent, relation);
                }
                else if (relationType.equals("K_")) {
                    knowledgeRelation.put(agent, relation);
                }
                else {
                    throw new RuntimeException("unknow relation specifier: " + relationType);
                }
            }
            else {
                throw new RuntimeException("unknow KripkeFormulaContext");
            }
        }
        assert(designatedWorld != null);
        KripkeStructure kripke = new KripkeStructure(new HashSet<World>(worlds.values()), beliefRelation, knowledgeRelation);
        startStates.add(new EpistemicState(kripke, designatedWorld));
        return null;
    }


    @Override public World visitKripkeWorld(DeplParser.KripkeWorldContext ctx) {
        String worldName = ctx.LOWER_NAME().getText();
        Map<Fluent, Value> fluentAssignments = new HashMap<>();
        for (DeplParser.AssignmentContext assignmentCtx : ctx.assignment()) {
            Assignment assignment = (Assignment) visit(assignmentCrx);
            for (Fluent fluent : assignment.getReferences()) {
                fluentAssignments.put(fluent, assignment.getValue());
            }
        }
        return new World(worldName, fluentAssignments);
    }




    // GOALS
    @Override public Void visitGoal(DeplParser.GoalContext ctx) {
        BeliefFormula goal = (BeliefFormula) visit(ctx.beliefFormula());
        goals.add(goal);
        return null;
    }


    // ACTIONS
    @Override public Void visitActionDefinition(DeplParser.ActionDefinitionContext ctx) {
        String actionName = ctx.LOWER_NAME().getText();
        for (LinkedHashMap<String,String> variableMap : getVariableMaps(ctx.variableList())) {
            variableStack.push(variableMap);

            List<String> actionParameters = new ArrayList<String>(variableMap.values());

            String owner = null;

            int cost = 1;
            List<FluentFormula> preconditionList = new ArrayList<FluentFormula>();
            Map<String, List<FluentFormula>> observesLists = new HashMap<>();
            Map<String, List<FluentFormula>> awareLists = new HashMap<>();
            for (String a : domain.getAgents()) {
                observesLists.put(a, new ArrayList<FluentFormula>());
                awareLists.put(a, new ArrayList<FluentFormula>());
            }

            Set<FluentFormula> determines = new HashSet<>();
            Set<BeliefFormula> announces = new HashSet<>();
            Map<FluentLiteral, FluentFormula> effects = new HashMap<FluentLiteral, FluentFormula>();

            for (DeplParser.ActionFieldContext fieldCtx : ctx.actionField()) {

                if (fieldCtx.ownerActionField() != null) {
                    owner = ((ObjectValue)fieldCtx.ownerActionField().groundableObject()).get();
                    if (!domain.isNonPassiveAgent(owner)) {
                        throw new RuntimeException("action " + actionName + " owner " + owner +
                                                   " not a declared system or environment agent.");
                    }
                }

                else if (fieldCtx.costActionField() != null) {
                    cost = Integer.parseInt(fieldCtx.costActionField().INTEGER().getText());
                }


                else if (fieldCtx.preconditionActionField() != null) {
                    DeplParser.PreconditionActionFieldContext preCtx = visit(fieldCtx.preconditionActionField());
                    for (Map<String,String> variableMap : getVariableMaps(preCtx().variableDefList())) {
                        variableStack.push(variableMap);
                        preconditionList.add((FluentFormula) visit(preCtx().fluentFormula()));
                        variableStack.pop();
                    }
                }

                else if (fieldCtx.observesActionField() != null) {
                    DeplParser.ObservesActitonFieldContext obsCtx = visit(fieldCtx.observesActionField());
                    for (Map<String,String> variableMap : getVariableMaps(obsCtx.variableDefList())) {
                        variableStack.push(variableMap);
                        String agentName = ((ObjectValue) visit(obsCtx.groundableObject())).get();
                        FluentFormula condition;
                        if (obsCtx.fluentFormula() == null) {
                            condition = new BoleanValue(true);
                        }
                        else {
                            condition = (FluentFormula) visit(obsCtx.fluentFormula());
                        }
                        if (!isFalse(condition)) {
                            observesLists.get(agentName).add(condition);
                        }
                        variableStack.pop();
                    }
                }


                else if (fieldCtx.awareActionField() != null) {
                    DeplParser.AwareActionFieldContext awaCtx = visit(fieldCtx.awareActionField());
                    for (Map<String,String> variableMap : getVariableMaps(awaCtx.variableDefList())) {
                        variableStack.push(variableMap);
                        String agentName = ((ObjectValue) visit(awaCtx.groundableObject())).get();
                        FluentFormula condition;
                        if (awaCtx.fluentFormula() == null) {
                            condition = new BoleanValue(true);
                        }
                        else {
                            condition = (FluentFormula) visit(awaCtx.fluentFormula());
                        }
                        if (!isFalse(condition)) {
                            awareLists.get(agentName).add(condition);
                        }
                        variableStack.pop();
                    }
                }

                else if (fieldCtx.determinesActionField() != null) {
                    DeplParser.AwareActionFieldContext detCtx = visit(fieldCtx.determinesActionField());
                    for (Map<String,String> variableMap : getVariableMaps(detCtx.variableDefList())) {
                        variableStack.push(variableMap);
                        determines.add((FluentFormula) visit(detCtx.fluentFormula()));
                        variableStack.pop();
                    }
                }

                else if (fieldCtx.announcesActionField() != null) {
                    DeplParser.AwareActionFieldContext annCtx = visit(fieldCtx.announcementActionField());
                    for (Map<String,String> variableMap : getVariableMaps(annCtx.variableDefList())) {
                        variableStack.push(variableMap);
                        announces.add((BeliefFormula) visit(annCtx.beliefFormula()));
                        variableStack.pop();
                    }
                }


                else if (fieldCtx.causesActionField() != null) {
                    DeplParser.CausesActionFieldContext effCtx = visit(fieldCtx.causesActionField());
                    for (Map<String,String> variableMap : getVariableMaps(effCtx.variableDefList())) {
                        variableStack.push(variableMap);
                        FluentLiteral effect = (FluentLiteral) visit(fieldCtx.causesifActionField().literal());
                        // ASSIGNMENT
                        //FluentFormula condition = (FluentFormula) visit(fieldCtx.causesifActionField().fluentFormula());
                        condition = condition.simplify();
                        if (!(condition instanceof FluentFormulaFalse)) {
                            effects.put(effect, condition);
                        }
                        FluentFormula condition;
                        if (effCtx.fluentFormula() == null) {
                            condition = new BoleanValue(true);
                        }
                        else {
                            condition = (FluentFormula) visit(effCtx.fluentFormula());
                        }
                        if (!isFalse(condition)) {
                            awareLists.get(agentName).add(condition);
                        }

                        variableStack.pop();
                    }
                }

            else {
                throw new RuntimeException("invalid action field, somehow a syntax error didn't get caught?");
            }

        }

        FluentFormula precondition;
        if (preconditionList.isEmpty()) {
            precondition = new FluentFormulaTrue();
        }
        else if (preconditionList.size() == 1) {
            precondition = preconditionList.get(0);
        }
        else {
            precondition = new FluentFormulaAnd(preconditionList);
        }

        precondition = precondition.simplify();

        if (isFalse(ff)) {
            return null;
        }




        if (owner == null) {
            throw new RuntimeException("illegal action definition, no owner: " + actionName);
        }

        Map<String, FluentFormula> observes = new HashMap<>();
        Map<String, FluentFormula> aware = new HashMap<>();
        for (String a : domain.getAgents()) {
            observes.put(a, (new FluentFormulaOr(observesLists.get(a))).simplify());
            aware.put(a, (new FluentFormulaOr(awareLists.get(a))).simplify());
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

        actions.add(action);
        variableStack.pop();
        return null;
    }







    //  ATOMIC

    @Override public Value visitValue(DeplParser.ValueContext ctx) {
        if (ctx.KEYWORD_FALSE() != null) {
            return new BooleanValue(false);
        }
        if (ctx.KEYWORD_TRUE() != null) {
            return new BooleanValue(true);
        }
        if (ctx.INTEGER() != null) {
            return new IntegerValue(Integer.parseInt(ctx.INTEGER().getText()));
        }
        if (ctx.groundable() != null) {
            return visit(ctx.groundable());
        }
        throw new RuntimeException("invalid value");
    }

    @Override public ObjectValue visitGroundable(DeplParser.GroundableContext ctx) {
        if (ctx.OBJECT() != null) {
            String name = ctx.OBJECT().getText();
            if (!allObjects.contains(name)) {
                throw new RuntimeException("undefined object: " + name);
            }
            return new ObjectValue(name);
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
            return new ObjectValue(grounding);
        }
    }


    @Override public Fluent visitFluent(DeplParser.FluentContext ctx) {
        String fluentName = ctx.NAME().getText();
        List<String> parameters = new ArrayList<String>();
        if (ctx.parameterList() != null) {
            for (DeplParser.ParameterContext parameterCtx : ctx.parameterList().parameter()) {
                parameters.add(resolveVariable(parameterCtx));
            }
        }
        return new Fluent(fluentName, parameters);
    }

    @Override public Atom visitAtom(DeplParser.AtomContext ctx) {
        if (ctx.fluent() != null) {
            return visit(ctx.fluent());
        }
        assert (ctx.value() != null);
        return visit(ctx.value());
    }


    @Override public FluentFormula visitFluentAtom(DeplParser.FluentAtomContext ctx) {
        return (FluentFormula) visit(ctx.atom());
    }

    @Override public FluentFormula visitFluentParens(DeplParser.FluentParensContext ctx) {
        return (FluentFormula) visit(ctx.fluentFormula());
    }

    @Override public FluentFormula visitFluentCompare(DeplParser.FluentCompareContext ctx) {
        if (ctx.fluentFormula().size == 2) {
            DeplParser.FluentFormulaContext leftCtx = visit(ctx.fluentFormula().get(0));
            DeplParser.FluentFormulaContext rightCtx = visit(ctx.fluentFormula().get(1));
        }
        else if (ctx.integerFormula().size == 2) {
        }
        else {
            throw new RuntimeException("illegal comparison: " + ctx.getText());
        }
    }

    @Override public FluentFormula visitFluentNot(DeplParser.FluentNotContext ctx) {
        FluentFormula inner = (FluentFormula) visit(ctx.fluentFormula());
        return (FluentFormulaNot.make(inner));
    }

    @Override public FluentFormula visitFluentAnd(DeplParser.FluentAndContext ctx) {
        List<FluentFormula> subFormulae = new ArrayList<>();
        for (DeplParser.FluentFormulaContext subFormula : ctx.fluentFormula()) {
            subFormulae.add((FluentFormula) visit(subFormula));
        }
        return (FluentFormulaAnd.make(subFormulae));
    }

    @Override public FluentFormula visitFluentOr(DeplParser.FluentOrContext ctx) {
        List<FluentFormula> subFormulae = new ArrayList<>();
        for (DeplParser.FluentFormulaContext subFormula : ctx.fluentFormula()) {
            subFormulae.add((FluentFormula) visit(subFormula));
        }
        return (FluentFormulaOr.make(subFormulae));
    }




    // BELIEF FORMULAE

    @Override public BeliefFormula visitBeliefFluent(DeplParser.BeliefFluentContext ctx) {
        return (FluentFormula) visit(ctx.fluentFormula());
    }

    @Override public BeliefFormula visitBeliefParens(DeplParser.BeliefParensContext ctx) {
        return (BeliefFormula) visit(ctx.beliefFormula());
    }

    @Override public BeliefFormula visitBeliefNot(DeplParser.BeliefNotContext ctx) {
        BeliefFormula inner = (BeliefFormula) visit(ctx.beliefFormula());
        return (new BeliefFormulaNot(inner));
    }

    @Override public BeliefFormula visitBeliefAnd(DeplParser.BeliefAndContext ctx) {
        List<BeliefFormula> subFormulae = new ArrayList<>();
        for (DeplParser.BeliefFormulaContext subFormula : ctx.beliefFormula()) {
            subFormulae.add((BeliefFormula) visit(subFormula));
        }
        return (new BeliefFormulaAnd(subFormulae));
    }

    @Override public BeliefFormula visitBeliefOr(DeplParser.BeliefOrContext ctx) {
        List<BeliefFormula> subFormulae = new ArrayList<>();
        for (DeplParser.BeliefFormulaContext subFormula : ctx.beliefFormula()) {
            subFormulae.add((BeliefFormula) visit(subFormula));
        }
        return (new BeliefFormulaOr(subFormulae));
    }

    @Override public BeliefFormula visitBeliefCommon(DeplParser.BeliefCommonContext ctx) {
        BeliefFormula inner = (BeliefFormula) visit(ctx.beliefFormula());
        return new BeliefFormulaCommon(inner);
    }

    @Override public BeliefFormula visitBeliefBelieves(DeplParser.BeliefBelievesContext ctx) {
        BeliefFormula inner = (BeliefFormula) visit(ctx.beliefFormula());
        String agentName = resolveVariable(ctx.parameter());
        if (!domain.isAgent(agentName)) {
            throw new RuntimeException("unknown agent grounding '" + agentName + "' in formula: " + ctx.getText());
        }
        return new BeliefFormulaBelieves(agentName, inner);
    }








}
