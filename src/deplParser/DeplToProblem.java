package depl;

import mecaPlanner.models.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import mecaPlanner.*;
import mecaPlanner.formulae.timeFormulae.*;
import mecaPlanner.formulae.beliefFormulae.*;
import mecaPlanner.formulae.localFormulae.*;
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
    private Set<TimeFormula> goals;

    // USED FOR PARSE-TIME CHECKS, DON'T GO IN DOMAIN
    private Set<Fluent> allFluents;
    private Integer agentIndex;
    private Map<String, String> allObjects;     // from object name to object type

    private Map<Fluent, Boolean> constants;

    private Map<String, TypeNode> typeDefs;  // key is object type, TypeNode.getGroundings() gives objects
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
            public void syntaxError (final Recognizer <?,?> recognizer, Object sym, int line, int pos, String msg, RecognitionException e)
            {
                throw new AssertionError ("depl syntax error. Line:" + line + ", position:" + pos + ". " + msg);
            }
        });


        ParseTree tree           = parser.init();

        this.domain = new Domain();
        this.systemAgentIndex = null;
        this.startStates = new HashSet<>();
        this.startingModels = new HashMap<>();
        this.goals = new HashSet<>();

        this.agentIndex = 0;
        this.allObjects = new HashMap<>();

        allFluents = new HashSet<>();
        this.constants = new HashMap<>();
        this.variableStack = new Stack<Map<String, String>>();

        this.typeDefs = new HashMap<String, TypeNode>();
        this.typeDefs.put("Object", new TypeNode(null));



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
            NDState ndState;
            if (stateCtx.kripkeModel() != null) {
                ndState = (NDState) visit(stateCtx.kripkeModel());
            }
            else {
                throw new RuntimeException("state construction not supported");
                //ndState = (NDState) visit(stateCtx.initiallyDef());
            }
            startStates.addAll(ndState.getEpistemicStates());
        }
        return null;
    }

    @Override public Void visitPostSection(DeplParser.PostSectionContext ctx) {
        //EpistemicState e = (EpistemicState) visit(ctx.startStateDef().kripkeModel());
        //domain.setPostState(e);
        return null;
    }

    @Override public EpistemicState visitInitiallyDef(DeplParser.InitiallyDefContext ctx) {
        Set<BeliefFormula> initiallyStatements = new HashSet<>();
        for (DeplParser.BeliefFormulaContext statementContext : ctx.beliefFormula()) {
            BeliefFormula statement = (BeliefFormula) visit(statementContext);
            initiallyStatements.add(statement);
        }
        return Construct.constructState(initiallyStatements, domain);
    }


    @Override public NDState visitKripkeModel(DeplParser.KripkeModelContext ctx) {
        Map<String,World> worlds = new HashMap<>();
        Set<World> designatedWorlds = new HashSet<>();;
        Map<String, Relation> beliefRelations = new HashMap<>();
        Map<String, Relation> knowledgeRelations = new HashMap<>();
        for (DeplParser.KripkeWorldContext worldCtx : ctx.kripkeWorld()) {
            World world = (World) visit(worldCtx);
            worlds.put(world.getName(),world);
            if (worldCtx.STAR() != null) {
                designatedWorlds.add(world);
            }
        }

        if (designatedWorlds.isEmpty()) {
            throw new RuntimeException("an initial state has no designaged worlds");
        }


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
        return new NDState(kripke, designatedWorlds);
    }


    @Override public World visitKripkeWorld(DeplParser.KripkeWorldContext ctx) {
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
        TimeFormula goal = (TimeFormula) visit(ctx.timeFormula());
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
            List<LocalFormula> preconditionList = new ArrayList<>();
            Map<String, List<LocalFormula>> observesLists = new HashMap<>();
            Map<String, List<LocalFormula>> awareLists = new HashMap<>();
            for (String a : domain.getAgents()) {
                observesLists.put(a, new ArrayList<LocalFormula>());
                awareLists.put(a, new ArrayList<LocalFormula>());
            }

            Set<LocalFormula> determines = new HashSet<>();
            Set<BeliefFormula> announces = new HashSet<>();
            Map<Assignment, LocalFormula> effects = new HashMap<>();

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
                        preconditionList.add((LocalFormula) visit(preCtx.localFormula()));
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
                        LocalFormula condition;
                        if (obsCtx.localFormula() == null) {
                            condition = new Literal(true);
                        }
                        else {
                            condition = (LocalFormula) visit(obsCtx.localFormula());
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
                        LocalFormula condition;
                        if (awaCtx.localFormula() == null) {
                            condition = new Literal(true);
                        }
                        else {
                            condition = (LocalFormula) visit(awaCtx.localFormula());
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
                        determines.add((LocalFormula) visit(detCtx.localFormula()));
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
                        LocalFormula condition;
                        if (effCtx.localFormula() == null)  {
                            condition = new Literal(true);
                        }
                        else {
                            condition = (LocalFormula) visit(effCtx.localFormula());
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

            LocalFormula precondition = LocalAndFormula.make(preconditionList);

            if (owner == null) {
                throw new RuntimeException("illegal action definition, no owner: " + actionName);
            }


            Map<String, LocalFormula> observes = new HashMap<>();
            Map<String, LocalFormula> aware = new HashMap<>();
            for (String a : domain.getAgents()) {
                observes.put(a, (LocalOrFormula.make(observesLists.get(a))));
                aware.put(a, (LocalOrFormula.make(awareLists.get(a))));
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






    //  ATOMIC

//    @Override public BooleanFormula visitValueFalse(DeplParser.ValueFalseContext ctx) {
//        return new BooleanAtom(false);
//    }
//    @Override public BooleanFormula visitValueTrue(DeplParser.ValueTrueContext ctx) {
//        return new BooleanAtom(true);
//    }
//    @Override public IntegerFormula visitValueInteger(DeplParser.ValueIntegerContext ctx) {
//        return new IntegerAtom(Integer.parseInt(ctx.INTEGER().getText()));
//    }
//    @Override public ObjectAtom visitValueObject(DeplParser.ValueObjectContext ctx) {
//        return new ObjectAtom(ctx.objectName().getText());
//    }


    @Override public String visitGroundableObject(DeplParser.GroundableObjectContext ctx) {
        if (ctx.objectName() != null) {
            String name = ctx.objectName().getText();
            if (!allObjects.containsKey(name)) {
                throw new RuntimeException("undefined object: " + name);
            }
            return name;
        }

//        if (ctx.fluent() != null) {
//
//            Fluent objectFluent = (Fluent) visit(ctx.fluent());
//            if (objectConstants.containsKey(objectFluent)) {
//                return objectConstants.get(objectFluent);
//            }
//            if (!allObjectFluents.containsKey(objectFluent)) {
//                throw new RuntimeException("unknown object fluent: " + objectFluent);
//            }
//            return new ObjectAtom(objectFluent);
//        }

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


    @Override public LocalFormula visitLocalFluent(DeplParser.LocalFluentContext ctx) {
        Fluent fluent = (Fluent) visit(ctx.fluent());
        if (constants.containsKey(fluent)) {
            return new Literal(constants.get(fluent));
        }
        if (!allFluents.contains(fluent)) {
            throw new RuntimeException("unknown fluent: " + fluent);
        }
        return fluent;
    }

    @Override public LocalFormula visitLocalLiteralTrue(DeplParser.LocalLiteralTrueContext ctx) {
        return new Literal(true);
    }

    @Override public LocalFormula visitLocalLiteralFalse(DeplParser.LocalLiteralFalseContext ctx) {
        return new Literal(false);
    }

    @Override public LocalFormula visitLocalParens(DeplParser.LocalParensContext ctx) {
        return (LocalFormula) visit(ctx.localFormula());
    }

    @Override public LocalFormula visitLocalNot(DeplParser.LocalNotContext ctx) {
        LocalFormula inner = (LocalFormula) visit(ctx.localFormula());
        return (LocalNotFormula.make(inner));
    }

    @Override public LocalFormula visitLocalAnd(DeplParser.LocalAndContext ctx) {
        List<LocalFormula> subFormulae = new ArrayList<>();
        for (DeplParser.LocalFormulaContext subFormula : ctx.localFormula()) {
            subFormulae.add((LocalFormula) visit(subFormula));
        }
        return (LocalAndFormula.make(subFormulae));
    }

    @Override public LocalFormula visitLocalOr(DeplParser.LocalOrContext ctx) {
        List<LocalFormula> subFormulae = new ArrayList<>();
        for (DeplParser.LocalFormulaContext subFormula : ctx.localFormula()) {
            subFormulae.add((LocalFormula) visit(subFormula));
        }
        return (LocalOrFormula.make(subFormulae));
    }




    // BELIEF FORMULAE

    @Override public BeliefFormula visitBeliefLocalFormula(DeplParser.BeliefLocalFormulaContext ctx) {
        return (LocalFormula) visit(ctx.localFormula());
    }

    @Override public BeliefFormula visitBeliefParens(DeplParser.BeliefParensContext ctx) {
        return (BeliefFormula) visit(ctx.beliefFormula());
    }

    @Override public BeliefFormula visitBeliefNot(DeplParser.BeliefNotContext ctx) {
        BeliefFormula inner = (BeliefFormula) visit(ctx.beliefFormula());
        return (BeliefNotFormula.make(inner));
    }

    @Override public BeliefFormula visitBeliefAnd(DeplParser.BeliefAndContext ctx) {
        BeliefFormula left = (BeliefFormula) visit(ctx.beliefFormula().get(0));
        BeliefFormula right = (BeliefFormula) visit(ctx.beliefFormula().get(1));
        return (BeliefAndFormula.make(left, right));
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
        String agentName = (String) visit(ctx.groundableObject());
        if (!domain.isAgent(agentName)) {
            throw new RuntimeException("unknown agent grounding '" + agentName + "' in formula: " + ctx.getText());
        }
        return new BeliefBelievesFormula(agentName, inner);
    }

    @Override public BeliefFormula visitBeliefPossibly(DeplParser.BeliefPossiblyContext ctx) {
        BeliefFormula inner = (BeliefFormula) visit(ctx.beliefFormula());
        String agentName = (String) visit(ctx.groundableObject());
        if (!domain.isAgent(agentName)) {
            throw new RuntimeException("unknown agent grounding '" + agentName + "' in formula: " + ctx.getText());
        }
        return BeliefNotFormula.make(new BeliefBelievesFormula(agentName, BeliefNotFormula.make(inner)));
    }



    // TIME FORMULAE

    @Override public TimeFormula visitTimeBelief(DeplParser.TimeBeliefContext ctx) {
        return (BeliefFormula) visit(ctx.beliefFormula());
    }

    @Override public TimeFormula visitTimeConstraint(DeplParser.TimeConstraintContext ctx) {
        return (TimeFormula) visit(ctx.temporalConstraint());
    }

    @Override public TimeFormula visitTimeNot(DeplParser.TimeNotContext ctx) {
        TimeFormula inner = (TimeFormula) visit(ctx.timeFormula());
        return (new TimeNotFormula(inner));
    }

    @Override public TimeFormula visitTimeAnd(DeplParser.TimeAndContext ctx) {
        List<TimeFormula> subFormulae = new ArrayList<>();
        for (DeplParser.TimeFormulaContext subFormula : ctx.timeFormula()) {
            subFormulae.add((TimeFormula) visit(subFormula));
        }
        return (TimeAndFormula.make(subFormulae));
    }

    @Override public TimeFormula visitTimeOr(DeplParser.TimeOrContext ctx) {
        List<TimeFormula> subFormulae = new ArrayList<>();
        for (DeplParser.TimeFormulaContext subFormula : ctx.timeFormula()) {
            subFormulae.add((TimeFormula) visit(subFormula));
        }
        return (TimeOrFormula.make(subFormulae));
    }



    @Override public TimeFormulaConstraint visitTemporalConstraint(DeplParser.TemporalConstraintContext ctx) {
        return new TimeFormulaConstraint((TimeFormulaConstraint.Inequality) visit(ctx.inequality()),
                                  Integer.parseInt(ctx.INTEGER().getText()));
    }

    @Override public TimeFormulaConstraint.Inequality visitInequalityEq(DeplParser.InequalityEqContext ctx) {
        return TimeFormulaConstraint.Inequality.EQ;
    }

    @Override public TimeFormulaConstraint.Inequality visitInequalityNe(DeplParser.InequalityNeContext ctx) {
        return TimeFormulaConstraint.Inequality.NE;
    }

    @Override public TimeFormulaConstraint.Inequality visitInequalityLt(DeplParser.InequalityLtContext ctx) {
        return TimeFormulaConstraint.Inequality.LT;
    }

    @Override public TimeFormulaConstraint.Inequality visitInequalityLte(DeplParser.InequalityLteContext ctx) {
        return TimeFormulaConstraint.Inequality.LTE;
    }

    @Override public TimeFormulaConstraint.Inequality visitInequalityGt(DeplParser.InequalityGtContext ctx) {
        return TimeFormulaConstraint.Inequality.GT;
    }

    @Override public TimeFormulaConstraint.Inequality visitInequalityGte(DeplParser.InequalityGteContext ctx) {
        return TimeFormulaConstraint.Inequality.GTE;
    }










}
