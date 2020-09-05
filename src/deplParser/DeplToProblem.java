package depl;

import mecaPlanner.models.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import mecaPlanner.*;
import mecaPlanner.formulae.integerFormulae.*;
import mecaPlanner.formulae.booleanFormulae.*;
import mecaPlanner.formulae.beliefFormulae.*;
import mecaPlanner.formulae.Formula;
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

    // GO IN PROBLEM
    private Domain domain;
    private Integer systemAgentIndex;
    private Set<EpistemicState> startStates;
    private Map<String, Model> startingModels;
    private Set<BeliefFormula> goals;

    // USED FOR PARSE-TIME CHECKS, DON'T GO IN DOMAIN
    private Set<Fluent> allBooleanFluents;
    private Set<Fluent> allIntegerFluents;
    private Set<Fluent> allObjectFluents;
    private Integer agentIndex;
    private Set<String> allObjects;          // to check for undefined objects
    private Set<Assignment> constants;
    //private Map<Fluent,Integer> integerConstants;
    //private Map<Fluent,Boolean> booleanConstants;
    //private Map<Fluent,String> objectConstants;
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
        List<LinkedHashMap<String, String>> maps = new ArrayList<LinkedHashMap<String,String>>();

        List<String> varNames = new ArrayList<String>();
        List<List<String>> varGroundings = new ArrayList<List<String>>();
        for (DeplParser.VariableDefContext variableDefCtx : ctx.variableDef()) {
            varNames.add(variableDefCtx.VARIABLE().getText());
            varGroundings.add(typeDefs.get(variableDefCtx.objectType().getText()).groundings);
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
        public String parent;
        public List<String> groundings;
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


//    // FOR STORING AN ASSIGNMENT TO A FLUENTS OR CONSTANTS
//    // MULTIPLE REFERENCES IN CASE ITS EXPANDABLE
//    private class ValueAssignment {
//        private Set<Fluent> references;
//        private Value value;
//        public ValueAssignment(Set<Fluent> references, Value value) {
//            this.references = references;
//            this.value = valeu;
//        }
//        public Set<Fluent> getReferences() {
//            return references;
//        }
//        public Value getValue() {
//            return value;
//        }
//    }





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

        allBooleanFluents = new HashSet<>();
        allIntegerFluents = new HashSet<>();
        allObjectFluents = new HashSet<>();
        this.constants = new HashSet<Assignment>();
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
        visit(ctx.fluentsSection());
        if (ctx.constantsSection() != null) {visit(ctx.constantsSection());}
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
        String agent = ctx.objectName().getText();


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
                Model model = (Model) constructor.newInstance(agent, domain);
                startingModels.put(agent, model);
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
        domain.addPassive(name);
        return null;
    }



    // FLUENTS

    @Override public Void visitFluentsSection(DeplParser.FluentsSectionContext ctx) {
        visitChildren(ctx);
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
            List<ObjectAtom> atomizedExpansion = new ArrayList<>();
            for (String s :expansion) {
                atomizedExpansion.add(new ObjectAtom(s));
            }
            expandedFluents.add(new Fluent(name, atomizedExpansion));
        }
        return expandedFluents;
    }

    //HERE
    @Override public Void visitFluentDef(DeplParser.FluentDefContext ctx) {
        String type = ctx.fluentType().getText();
        for (Fluent fluent : (Set<Fluent>) visit(ctx.expandableFluent())) {
            if (allObjects.contains(fluent.getName())) {
                throw new RuntimeException("Fluent name already used for object: " + fluent.getName());
            }
            switch (type) {
                case "Boolean":
                    allBooleanFluents.add(fluent);
                    break;
                case "Integer":
                    allIntegerFluents.add(fluent);
                    break;
                //XXX
                case "Object":
                    allObjectFluents.add(fluent);
                    break;
                default : throw new RuntimeException("invalid fluent type: " + type);
            }
        }
        return null;
    }




    // CONSTANTS

    @Override public Void visitConstantsSection(DeplParser.ConstantsSectionContext ctx) {
        for (DeplParser.ValueAssignmentContext assignmentCtx : ctx.valueAssignment()) {
            Set<Assignment> assignments = (Set<Assignment>) visit(assignmentCtx);
            constants.addAll(assignments);
        }
        return null;
    }

    @Override public Set<Assignment> visitValueAssignment(DeplParser.ValueAssignmentContext ctx) {
        Formula value = (Formula) visit(ctx.value());
        Set<Assignment> assignments = new HashSet<>();
        for (Fluent fluent : (Set<Fluent>) visit(ctx.expandableFluent())) {
            assignments.add(new Assignment(fluent, value));
        }
        return assignments;
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
        Map<String,World> worlds = new HashMap<>();
        World designatedWorld = null;
        Map<String, Relation> beliefRelations = new HashMap<>();
        Map<String, Relation> knowledgeRelations = new HashMap<>();
        for (DeplParser.KripkeWorldContext worldCtx : ctx.kripkeWorld()) {
            World world = (World) visit(worldCtx);
            worlds.put(world.getName(),world);
            if (designatedWorld == null) {
                designatedWorld = world;
            }
        }
        assert(designatedWorld != null);


        for (DeplParser.KripkeRelationContext relationCtx : ctx.kripkeRelation()) {
            String relationType = relationCtx.relationType().getText();
            String agent = relationCtx.objectName().getText();

            Relation relation = new Relation();
            List<String> fromWorlds = new ArrayList<>();
            List<String> toWorlds = new ArrayList<>();
            for (DeplParser.FromWorldContext fromWorld : relationCtx.fromWorld()) {
                String fromWorldName = fromWorld.getText();
                if (!worlds.containsKey(fromWorldName)) {
                    throw new RuntimeException("unknown world: " + fromWorldName);
                }
                fromWorlds.add(fromWorldName);
            }
            for (DeplParser.ToWorldContext toWorld : relationCtx.toWorld()) {
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

            if (relationType.equals("B")) {
                beliefRelations.put(agent, relation);
            }
            else if (relationType.equals("K")) {
                knowledgeRelations.put(agent, relation);
            }
            else {
                throw new RuntimeException("unknow relation specifier: " + relationType);
            }
        }

        Set<World> worldSet = new HashSet<World>(worlds.values());
        KripkeStructure kripke = new KripkeStructure(worldSet, beliefRelations, knowledgeRelations);
        startStates.add(new EpistemicState(kripke, designatedWorld));
        return null;
    }

    @Override public World visitKripkeWorld(DeplParser.KripkeWorldContext ctx) {
        String worldName = ctx.LOWER_NAME().getText();
        Map<Fluent, Boolean> booleanFluentAssignments = new HashMap<>();
        Map<Fluent, Integer> integerFluentAssignments = new HashMap<>();
        Map<Fluent, String> objectFluentAssignments = new HashMap<>();
        for (DeplParser.ValueAssignmentContext assignCtx : ctx.valueAssignment()) {
            Set<Assignment> assignments = (Set<Assignment>) visit(assignCtx);
            for (Assignment assignment : assignments) {
                Fluent fluent = assignment.getReference();
                Formula value = assignment.getValue();
                if (value instanceof BooleanAtom && !((BooleanAtom)value).isFluent()) {
                    Boolean b = value.getBooleanValue();
                    booleanFluentAssignments.put(fluent, b);
                }
                else if (value instanceof IntegerAtom && !((IntegerAtom)value).isFluent()) {
                    Integer i = value.getIntegerValue();
                    integerFluentAssignments.put(fluent, i);
                }
                else if (value instanceof ObjectAtom && !((ObjectAtom)value).isFluent()) {
                    String o = value.getObjectValue();
                    objectFluentAssignments.put(fluent, o);
                }
                else {
                    throw new RuntimeException("invalid fluent assignment: " + assignCtx.getText());
                }
            }
        }
        for (Fluent booleanFluent : allBooleanFluents.keySet()) {
            if (!booleanFluentAssignments.contains(booleanFluent)) {
                Log.info("boolean fluent " + booleanFluent + " not set, assuming false.");
                booleanFluentAssignments.put(booleanFluent, false);
            }
        }
        for (Fluent integerFluent : allIntegerFluents.keySet()) {
            if (!integerFluentAssignments.contains(integerFluent)) {
                throw new RuntimeException("integer fluent " + integerFluent + " must be set.");
            }
        }

        for (Fluent objectFluent : allObjectFluents.keySet()) {
            if (!objectFluentAssignments.contains(objectFluent)) {
                throw new RuntimeException("object fluent " + objectFluent + " must be set.");
            }
        }

        World world = new World(worldName, booleanFluents, integerFluents, objectFluents);
        return world;
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
        for (LinkedHashMap<String,String> actionVariableMap : getVariableMaps(ctx.variableDefList())) {
            variableStack.push(actionVariableMap);

            List<String> actionParameters = new ArrayList<String>(actionVariableMap.values());

            String owner = null;

            int cost = 1;
            List<BooleanFormula> preconditionList = new ArrayList<BooleanFormula>();
            Map<String, List<BooleanFormula>> observesLists = new HashMap<>();
            Map<String, List<BooleanFormula>> awareLists = new HashMap<>();
            for (String a : domain.getAgents()) {
                observesLists.put(a, new ArrayList<BooleanFormula>());
                awareLists.put(a, new ArrayList<BooleanFormula>());
            }

            Set<BooleanFormula> determines = new HashSet<>();
            Set<BeliefFormula> announces = new HashSet<>();
            Map<BooleanFormula, Assignment> effects = new HashMap<>();

            for (DeplParser.ActionFieldContext fieldCtx : ctx.actionField()) {

                if (fieldCtx.ownerActionField() != null) {
                    ObjectAtom ownerObject = (ObjectAtom) visit(fieldCtx.ownerActionField().groundableObject());
                    owner = ownerObject.getObjectValue();
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
                        preconditionList.add((BooleanFormula) visit(preCtx.booleanFormula()));
                        variableStack.pop();
                    }
                }

                else if (fieldCtx.observesActionField() != null) {
                    DeplParser.ObservesActionFieldContext obsCtx = fieldCtx.observesActionField();
                    for (Map<String,String> variableMap : getVariableMaps(obsCtx.variableDefList())) {
                        variableStack.push(variableMap);
                        ObjectAtom agentObject = (ObjectAtom) visit(obsCtx.groundableObject());
                        String agentName = agentObject.getObjectValue();

                        BooleanFormula condition;
                        if (obsCtx.booleanFormula() == null) {
                            condition = new BooleanAtom(true);
                        }
                        else {
                            condition = (BooleanFormula) visit(obsCtx.booleanFormula());
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
                        ObjectAtom agentObject = (ObjectAtom) visit(awaCtx.groundableObject());
                        String agentName = agentObject.getObjectValue();
                        BooleanFormula condition;
                        if (awaCtx.booleanFormula() == null) {
                            condition = new BooleanAtom(true);
                        }
                        else {
                            condition = (BooleanFormula) visit(awaCtx.booleanFormula());
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
                        determines.add((BooleanFormula) visit(detCtx.booleanFormula()));
                        variableStack.pop();
                    }
                }

                else if (fieldCtx.announcesActionField() != null) {
                    DeplParser.AnnouncesActionFieldContext annCtx = fieldCtx.announcesActionField();
                    for (Map<String,String> variableMap : getVariableMaps(annCtx.variableDefList())) {
                        variableStack.push(variableMap);
                        announces.add((BeliefFormula) visit(annCtx.beliefFormula()));
                        variableStack.pop();
                    }
                }


                else if (fieldCtx.causesActionField() != null) {
                    DeplParser.CausesActionFieldContext effCtx = fieldCtx.causesActionField();
                    for (Map<String,String> variableMap : getVariableMaps(effCtx.variableDefList())) {
                        variableStack.push(variableMap);
                        Fluent reference = (Fluent) visit(effCtx.formulaAssignment().fluent());
                        Formula value;
                        if (effCtx.formulaAssignment().integerFormula() != null) {
                            value = (Formula) visit(effCtx.formulaAssignment().integerFormula());
                        }
                        else if (effCtx.formulaAssignment().beliefFormula() != null) {
                            value = (Formula) visit(effCtx.formulaAssignment().beliefFormula());
                        }
                        if (effCtx.formulaAssignment().groundableObject() != null) {
                            value = (Formula) visit(effCtx.formulaAssignment().groundableObject());
                        }
                        else {
                            throw new RuntimeException("effect parse error");
                        }
                        BooleanFormula condition = (BooleanFormula) visit(effCtx.booleanFormula());
                        if (!(condition.isFalse()));
                            effects.put(condition, new Assignment(reference, value));
                        }
                        variableStack.pop();
                }

                else {
                    throw new RuntimeException("invalid action field, somehow a syntax error didn't get caught?");
                }
            }

            BooleanFormula precondition;
            if (preconditionList.isEmpty()) {
                precondition = new BooleanAtom(true);
            }
            else if (preconditionList.size() == 1) {
                precondition = preconditionList.get(0);
            }
            else {
                precondition = BooleanAndFormula.make(preconditionList);
            }

            if (precondition.isFalse()) {
                return null;
            }


            if (owner == null) {
                throw new RuntimeException("illegal action definition, no owner: " + actionName);
            }

            Map<String, BooleanFormula> observes = new HashMap<>();
            Map<String, BooleanFormula> aware = new HashMap<>();
            for (String a : domain.getAgents()) {
                observes.put(a, (BooleanOrFormula.make(observesLists.get(a))));
                observes.put(a, (BooleanOrFormula.make(awareLists.get(a))));
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

            domain.addAction(action);
            variableStack.pop();
        }
        return null;
    }






    //  ATOMIC

    @Override public BooleanFormula visitValueFalse(DeplParser.ValueFalseContext ctx) {
        return new BooleanAtom(false);
    }
    @Override public BooleanFormula visitValueTrue(DeplParser.ValueTrueContext ctx) {
        return new BooleanAtom(true);
    }
    @Override public IntegerFormula visitValueInteger(DeplParser.ValueIntegerContext ctx) {
        return new IntegerAtom(Integer.parseInt(ctx.INTEGER().getText()));
    }
    @Override public ObjectAtom visitValueObject(DeplParser.ValueObjectContext ctx) {
        return new ObjectAtom(ctx.objectName().getText());
    }


    @Override public ObjectAtom visitGroundableObject(DeplParser.GroundableObjectContext ctx) {
        if (ctx.objectName() != null) {
            String name = ctx.objectName().getText();
            if (!allObjects.contains(name)) {
                throw new RuntimeException("undefined object: " + name);
            }
            return new ObjectAtom(name);
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
            return new ObjectAtom(grounding);
        }
    }


    @Override public Fluent visitFluent(DeplParser.FluentContext ctx) {
        String fluentName = ctx.LOWER_NAME().getText();
        List<ObjectAtom> parameters = new ArrayList<>();
        for (DeplParser.GroundableObjectContext objCtx : ctx.groundableObject()) {
            parameters.add((ObjectAtom) visit(objCtx));
        }
        return new Fluent(fluentName, parameters);
    }

    @Override public IntegerFormula visitIntegerFluent(DeplParser.IntegerFluentContext ctx) {
        Fluent integerFluent = (Fluent) visit(ctx.fluent());
        if (!allIntegerFluents.contains(integerFluent)) {
            throw new RuntimeException("unknown integer fluent: " + integerFluent);
        }
        return new IntegerAtom(integerFluent);
    }

    @Override public IntegerFormula visitIntegerLiteral(DeplParser.IntegerLiteralContext ctx) {
        return new IntegerAtom((Integer) visit(ctx.INTEGER()));
    }

    @Override public IntegerFormula visitIntegerParens(DeplParser.IntegerParensContext ctx) {
        return (IntegerFormula) visit(ctx.integerFormula());
    }

    @Override public IntegerFormula visitIntegerAdd(DeplParser.IntegerAddContext ctx) {
        DeplParser.IntegerFormulaContext lhs = ctx.integerFormula(0);
        DeplParser.IntegerFormulaContext rhs = ctx.integerFormula(1);
        return new IntegerAdd((IntegerFormula) visit(lhs), (IntegerFormula) visit(rhs));
    }


    // SUBTRACT, ETC...





    @Override public BooleanFormula visitBooleanFluent(DeplParser.BooleanFluentContext ctx) {
        Fluent booleanFluent = (Fluent) visit(ctx.fluent());
        if (!allBooleanFluents.contains(booleanFluent)) {
            throw new RuntimeException("unknown boolean fluent: " + booleanFluent);
        }
        return new BooleanAtom(booleanFluent);
    }

    @Override public BooleanFormula visitBooleanLiteralTrue(DeplParser.BooleanLiteralTrueContext ctx) {
        return new BooleanAtom(true);
    }

    @Override public BooleanFormula visitBooleanLiteralFalse(DeplParser.BooleanLiteralFalseContext ctx) {
        return new BooleanAtom(false);
    }

    @Override public BooleanFormula visitBooleanParens(DeplParser.BooleanParensContext ctx) {
        return (BooleanFormula) visit(ctx.booleanFormula());
    }

    @Override public BooleanFormula visitBooleanCompareIntegers(DeplParser.BooleanCompareIntegersContext ctx) {
        IntegerFormula lhs = (IntegerFormula) visit(ctx.integerFormula(0));
        IntegerFormula rhs = (IntegerFormula) visit(ctx.integerFormula(1));
        return CompareIntegers.make(ctx.COMPARE().getText(), lhs, rhs);
    }

    @Override public BooleanFormula visitBooleanEqualIntegers(DeplParser.BooleanEqualIntegersContext ctx) {
        IntegerFormula lhs = (IntegerFormula) visit(ctx.integerFormula(0));
        IntegerFormula rhs = (IntegerFormula) visit(ctx.integerFormula(1));
        if (ctx.OP_EQ() == null) {
            return CompareIntegers.make(CompareIntegers.Inequality.NE, lhs, rhs);
        }
        return CompareIntegers.make(CompareIntegers.Inequality.EQ, lhs, rhs);
    }

    @Override public BooleanFormula visitBooleanEqualBooleans(DeplParser.BooleanEqualBooleansContext ctx) {
        BooleanFormula lhs = (BooleanFormula) visit(ctx.booleanFormula(0));
        BooleanFormula rhs = (BooleanFormula) visit(ctx.booleanFormula(1));
        if (ctx.OP_EQ() == null) {
            return BooleanNotFormula.make(CompareBooleans.make(lhs, rhs));
        }
        return CompareBooleans.make(lhs, rhs);
    }
    @Override public BooleanFormula visitBooleanEqualObjects(DeplParser.BooleanEqualObjectsContext ctx) {
        ObjectAtom lhs = (ObjectAtom) visit(ctx.groundableObject(0));
        ObjectAtom rhs = (ObjectAtom) visit(ctx.groundableObject(1));
        if (ctx.OP_EQ() == null) {
            return BooleanNotFormula.make(CompareObjects.make(lhs, rhs));
        }
        return CompareObjects.make(lhs, rhs);
    }




    @Override public BooleanFormula visitBooleanNot(DeplParser.BooleanNotContext ctx) {
        BooleanFormula inner = (BooleanFormula) visit(ctx.booleanFormula());
        return (BooleanNotFormula.make(inner));
    }

    @Override public BooleanFormula visitBooleanAnd(DeplParser.BooleanAndContext ctx) {
        List<BooleanFormula> subFormulae = new ArrayList<>();
        for (DeplParser.BooleanFormulaContext subFormula : ctx.booleanFormula()) {
            subFormulae.add((BooleanFormula) visit(subFormula));
        }
        return (BooleanAndFormula.make(subFormulae));
    }

    @Override public BooleanFormula visitBooleanOr(DeplParser.BooleanOrContext ctx) {
        List<BooleanFormula> subFormulae = new ArrayList<>();
        for (DeplParser.BooleanFormulaContext subFormula : ctx.booleanFormula()) {
            subFormulae.add((BooleanFormula) visit(subFormula));
        }
        return (BooleanOrFormula.make(subFormulae));
    }




    // BELIEF FORMULAE

    @Override public BeliefFormula visitBeliefBooleanFormula(DeplParser.BeliefBooleanFormulaContext ctx) {
        return (BooleanFormula) visit(ctx.booleanFormula());
    }

    @Override public BeliefFormula visitBeliefParens(DeplParser.BeliefParensContext ctx) {
        return (BeliefFormula) visit(ctx.beliefFormula());
    }

    @Override public BeliefFormula visitBeliefNot(DeplParser.BeliefNotContext ctx) {
        BeliefFormula inner = (BeliefFormula) visit(ctx.beliefFormula());
        return (BeliefNotFormula.make(inner));
    }

    @Override public BeliefFormula visitBeliefEqualBeliefs(DeplParser.BeliefEqualBeliefsContext ctx) {
        BeliefFormula lhs = (BeliefFormula) visit(ctx.beliefFormula(0));
        BeliefFormula rhs = (BeliefFormula) visit(ctx.beliefFormula(1));
        if (ctx.OP_EQ() == null) {
            return BeliefNotFormula.make(CompareBeliefs.make(lhs, rhs));
        }
        return CompareBeliefs.make(lhs, rhs);

    }


    @Override public BeliefFormula visitBeliefAnd(DeplParser.BeliefAndContext ctx) {
        List<BeliefFormula> subFormulae = new ArrayList<>();
        for (DeplParser.BeliefFormulaContext subFormula : ctx.beliefFormula()) {
            subFormulae.add((BeliefFormula) visit(subFormula));
        }
        return (new BeliefAndFormula(subFormulae));
    }

    @Override public BeliefFormula visitBeliefOr(DeplParser.BeliefOrContext ctx) {
        List<BeliefFormula> subFormulae = new ArrayList<>();
        for (DeplParser.BeliefFormulaContext subFormula : ctx.beliefFormula()) {
            subFormulae.add((BeliefFormula) visit(subFormula));
        }
        return (BeliefOrFormula.make(subFormulae));
    }

    @Override public BeliefFormula visitBeliefCommon(DeplParser.BeliefCommonContext ctx) {
        BeliefFormula inner = (BeliefFormula) visit(ctx.beliefFormula());
        return new BeliefCommonFormula(inner);
    }

    @Override public BeliefFormula visitBeliefBelieves(DeplParser.BeliefBelievesContext ctx) {
        BeliefFormula inner = (BeliefFormula) visit(ctx.beliefFormula());
        ObjectAtom agentObject = (ObjectAtom) visit(ctx.groundableObject());
        String agentName = agentObject.getObjectValue();
        if (!domain.isAgent(agentName)) {
            throw new RuntimeException("unknown agent grounding '" + agentName + "' in formula: " + ctx.getText());
        }
        return new BeliefBelievesFormula(agentName, inner);
    }








}
