package depl;

import mecaPlanner.models.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import mecaPlanner.*;
import mecaPlanner.formulae.*;
import mecaPlanner.actions.*;
import mecaPlanner.state.Initialize;
import mecaPlanner.state.EpistemicState;

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
    private EpistemicState startState;
    private Map<String, Model> startingModels;
    private Set<GeneralFormula> goals;

    // USED FOR PARSE-TIME CHECKS, DON'T GO IN DOMAIN
    private Integer agentIndex;
    private Set<String> allObjects;  // to check for undefined objects
    private Set<FluentAtom> constants;
    private Map<String, TypeNode> typeDefs;  // key is type name
    private Stack<Map<String, String>> variableStack;
    private Set<BeliefFormula> initiallyStatements;


    public DeplToProblem() {
        super();

    }


    // HELPER FUNCTIONS


    enum ActionType {
        ONTIC,
        SENSING,
        ANNOUNCEMENT
    }

    // RECURSIVELY CHECK THAT EVERY ATOM IN THE FORMULA HAS BEEN DEFINED
    private void checkAtoms(GeneralFormula f) {
        for (FluentAtom a : f.getAllAtoms()) {
            if (!domain.getAllAtoms().contains(a)) {
                System.out.println("Undefined atom: \"" + a + "\". Defined atoms are: " + domain.getAllAtoms());
                System.exit(1);
            }
        }
    }


    // READ A "PARAMETER" WHICH COULD BE AN OBJECT NAME OR A VARIABLE
    // IF IT IS AN OBJECT, MAKE SURE IT WAS DEFINED 
    // IF IT IS A VARIABLE, USE THE VARIABLE STACK TO RESOLVE IT

    private String resolveVariable(DeplParser.ParameterContext ctx) {
        if (ctx.NAME() != null) {
            String name = ctx.NAME().getText();
            if (!allObjects.contains(name)) {
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



    // READ A VARIABLE LIST AND GET A LIST OF MAPS
    // EACH LIST ELEMENT MAPS FROM EACH VARIABLE NAME TO ONE GROUND OBJECT
    // THE LIST GIVES THE CARTESIAN PRODUCT OF EACH POSSIBLE VARIABLE GROUNDING

    private List<Map<String, String>> getVariableMaps(DeplParser.VariableListContext ctx) {
        List<Map<String, String>> maps = new ArrayList<Map<String,String>>();
        if (ctx == null) {
            maps.add(new HashMap<String,String>());
            return maps;
        }

        List<String> varNames = new ArrayList<String>();
        List<List<String>> varGroundings = new ArrayList<List<String>>();
        for (DeplParser.VariableDefinitionContext varDefCtx : ctx.variableDefinition()) {
            varNames.add(varDefCtx.VARIABLE().getText());
            varGroundings.add(typeDefs.get(varDefCtx.NAME().getText()).groundings);
        }

        for (List<String> grounding : cartesianProduct(varGroundings)) {
            Map<String,String> groundingMap = new HashMap<String,String>();
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


    // RECURSIVELY RESOLVE CONSTANTS IN A FORMULA
    // WILL EVENTUALLY RETURN TRUE, FALSE, OR A FF WITHOUT CONSTANTS
    // WE CAN USE THIS TO FILTER OUT AT PARSET TIME ANY 
    // ACTIONS, ACTION EFFECTS, OR OBSERVATION EFFECTS
    // THAT ARE CONSTANTLY FALSE.
    FluentFormula removeConstants(FluentFormula ff) {
        if (ff instanceof FluentFormulaTrue || ff instanceof FluentFormulaFalse) {
            return ff;
        }
        if (ff instanceof FluentAtom) {
            FluentAtom fa = (FluentAtom) ff;
            if (domain.getAllAtoms().contains(fa)) {
                return fa;
            }
            if (domain.getConstants().contains(fa)) {
                return new FluentFormulaTrue();
            }
            return new FluentFormulaFalse();
        }
        if (ff instanceof FluentFormulaNot) {
            FluentFormulaNot fn = (FluentFormulaNot) ff;
            if (fn.getFormula() instanceof FluentFormulaTrue) {
                return new FluentFormulaFalse();
            }
            if (fn.getFormula() instanceof FluentFormulaFalse) {
                return new FluentFormulaTrue();
            }
            return new FluentFormulaNot(removeConstants(fn.getFormula()));
        }
        if (ff instanceof FluentFormulaAnd) {
            FluentFormulaAnd fa = (FluentFormulaAnd) ff;
            List<FluentFormula> newFormulae = new ArrayList<>();
            for (FluentFormula oldFormula : fa.getFormulae()) {
                FluentFormula newFormula = removeConstants(oldFormula);
                if (newFormula instanceof FluentFormulaFalse) {
                    return new FluentFormulaFalse();
                }
                if (!(newFormula instanceof FluentFormulaTrue)) {
                    newFormulae.add(newFormula);
                }
            }
            if (newFormulae.isEmpty()) {
                return new FluentFormulaTrue();
            }
            return new FluentFormulaAnd(newFormulae);
        }
        if (ff instanceof FluentFormulaOr) {
            FluentFormulaOr fo = (FluentFormulaOr) ff;
            List<FluentFormula> newFormulae = new ArrayList<>();
            for (FluentFormula oldFormula : fo.getFormulae()) {
                FluentFormula newFormula = removeConstants(oldFormula);
                if (newFormula instanceof FluentFormulaTrue) {
                    return new FluentFormulaTrue();
                }
                if (!(newFormula instanceof FluentFormulaFalse)) {
                    newFormulae.add(newFormula);
                }
            }
            if (newFormulae.isEmpty()) {
                return new FluentFormulaFalse();
            }
            return new FluentFormulaOr(newFormulae);
        }
        throw new RuntimeException("unknown formula type");
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

    //public void buildDomain (ParseTree tree) {
    //    Domain.clear();
    //    visit(tree);
    //}


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
        this.startState = null;
        this.startingModels = new HashMap<>();
        this.goals = new HashSet<>();

        this.agentIndex = 0;
        this.allObjects = new HashSet<String>();
        this.constants = new HashSet<FluentAtom>();
        this.typeDefs = new HashMap<String, TypeNode>();
        this.variableStack = new Stack<Map<String, String>>();
        this.initiallyStatements = new HashSet<>();;


        visit(tree);

        if (systemAgentIndex == null) {
            throw new RuntimeException("system agent not defined");
        }
        if (startState == null) {
            throw new RuntimeException("start state agent not defined");
        }
        if (goals.isEmpty()) {
            throw new RuntimeException("no goals defined");
        }
        // ADD OTHER CHECKS

        return new Problem(domain, systemAgentIndex, startState, startingModels, goals);
    }




    // TOP-LEVEL OF THE GRAMMAR

    @Override public Void visitInit(DeplParser.InitContext ctx) {
        visit(ctx.typesSection());
        visit(ctx.objectsSection());
        visit(ctx.agentsSection());
        visit(ctx.atomsSection());
        visit(ctx.constantsSection());
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
        return null;
    }

    @Override public Void visitAgentDef(DeplParser.AgentDefContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override public Void visitSystemAgent(DeplParser.SystemAgentContext ctx) {
        String name = ctx.NAME().getText();
        if (this.systemAgentIndex != null) {
            throw new RuntimeException("cannot define multiple system agents");
        }
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


    @Override public Void visitPassiveAgent(DeplParser.PassiveAgentContext ctx) {
        String name = ctx.NAME().getText();
        domain.addPassive(name);
        return null;
    }



    // PREDICATES

    @Override public Void visitAtomsSection(DeplParser.AtomsSectionContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override public Void visitAtomDefinition(DeplParser.AtomDefinitionContext ctx) {

        String atomName = ctx.NAME().getText();
        if (ctx.parameterList() == null) {
            domain.addAtom(new FluentAtom(atomName));
        }
        for (Map<String,String> variableMap : getVariableMaps(ctx.variableList())) {
            variableStack.push(variableMap);
            List<String> groundParameters = new ArrayList<String>();
            if (ctx.parameterList() != null) {
                for (DeplParser.ParameterContext parameterCtx : ctx.parameterList().parameter()) {
                    groundParameters.add(resolveVariable(parameterCtx));
                }
            }
            domain.addAtom(new FluentAtom(atomName, groundParameters));
            variableStack.pop();
        }
        return null;
    }


    // CONSTANTS

    @Override public Void visitConstantsSection(DeplParser.ConstantsSectionContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override public Void visitConstantDefinition(DeplParser.ConstantDefinitionContext ctx) {

        String constantName = ctx.NAME().getText();

        for (FluentAtom atom : domain.getAllAtoms()) {
            if (constantName.equals(atom.getName())) {
                throw new RuntimeException("invalid constant name already used for atom: " + constantName);
            }
        }

        if (ctx.parameterList() == null) {
            domain.addConstant(new FluentAtom(constantName));
        }
        for (Map<String,String> variableMap : getVariableMaps(ctx.variableList())) {
            variableStack.push(variableMap);
            List<String> groundParameters = new ArrayList<String>();
            if (ctx.parameterList() != null) {
                for (DeplParser.ParameterContext parameterCtx : ctx.parameterList().parameter()) {
                    groundParameters.add(resolveVariable(parameterCtx));
                }
            }
            domain.addConstant(new FluentAtom(constantName, groundParameters));
            variableStack.pop();
        }
        return null;
    }



    // INITIALLY

    @Override public Void visitInitiallySection(DeplParser.InitiallySectionContext ctx) {
        visitChildren(ctx);
        try {
            startState = Initialize.constructState(initiallyStatements, domain, true);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    @Override public Void visitInitiallyStatement(DeplParser.InitiallyStatementContext ctx) {
        BeliefFormula statement = (BeliefFormula) visit(ctx.beliefFormula());
        checkAtoms(statement);
        initiallyStatements.add(statement);
        return null;
    }



    // GOALS
    @Override public Void visitGoal(DeplParser.GoalContext ctx) {
        GeneralFormula goal = (GeneralFormula) visit(ctx.generalFormula());
        checkAtoms(goal);
        goals.add(goal);
        return null;
    }


    // ACTIONS
    @Override public Void visitActionDefinition(DeplParser.ActionDefinitionContext ctx) {
        for (Map<String,String> variableMap : getVariableMaps(ctx.variableList())) {
            variableStack.push(variableMap);
            Action action = buildAction(ctx);
            if (!(action.getPrecondition() instanceof FluentFormulaFalse)) {
                // SHOULD ALSO checkAtoms() FOR EFFECT CONDITIONS AND OBSERVES CONDITIONS
                checkAtoms(action.getPrecondition());
                domain.addAction(action.getActor(), action);
            }
            variableStack.pop();
        }
        return null;
    }

    private Action buildAction(DeplParser.ActionDefinitionContext ctx) {
        String actionName = ctx.NAME().getText();

        List<String> actionParameters = new ArrayList<String>();
        for (DeplParser.ParameterContext parameterCtx : ctx.parameterList().parameter()) {
            actionParameters.add(resolveVariable(parameterCtx));
        }

        // MANDATORY
        String owner = null;
        DeplToProblem.ActionType actionType = null;

        // OPTIONAL
        int cost = 1;
        List<FluentFormula> preconditionList = new ArrayList<FluentFormula>();
        Map<String, List<FluentFormula>> observesLists = new HashMap<>();
        Map<String, List<FluentFormula>> awareLists = new HashMap<>();
        for (String a : domain.getAgents()) {
            observesLists.put(a, new ArrayList<FluentFormula>());
            awareLists.put(a, new ArrayList<FluentFormula>());
        }

        // ACTION-TYPE-SPECIFIC
        Map<FluentLiteral, FluentFormula> effects = new HashMap<FluentLiteral, FluentFormula>();
        //Set<FluentLiteral> effects = new HashSet<>();
        FluentFormula announces = null;
        Set<FluentFormula> determines = new HashSet<FluentFormula>();

        for (DeplParser.ActionFieldContext fieldCtx : ctx.actionField()) {

            if (fieldCtx.ownerActionField() != null) {
                owner = resolveVariable(fieldCtx.ownerActionField().parameter());
                if (!domain.isNonPassiveAgent(owner)) {
                    throw new RuntimeException("action " +
                                               actionName +
                                               " owner " +
                                               owner +
                                               " not a declared system or environment agent.");
                }
            }

            else if (fieldCtx.costActionField() != null) {
                cost = Integer.parseInt(fieldCtx.costActionField().INTEGER().getText());
            }

            else if (fieldCtx.preconditionActionField() != null) {
                for (Map<String,String> variableMap : getVariableMaps(fieldCtx.preconditionActionField().variableList())) {
                    variableStack.push(variableMap);
                    preconditionList.add((FluentFormula) visit(fieldCtx.preconditionActionField().fluentFormula()));
                    variableStack.pop();
                }
            }

            else if (fieldCtx.observesActionField() != null) {
                for (Map<String,String> variableMap : getVariableMaps(fieldCtx.observesActionField().variableList())) {
                    variableStack.push(variableMap);
                    String agentName = resolveVariable(fieldCtx.observesActionField().parameter());
                    observesLists.get(agentName).add(new FluentFormulaTrue());
                    variableStack.pop();
                }
            }

            else if (fieldCtx.observesifActionField() != null) {
                for (Map<String,String> variableMap : getVariableMaps(fieldCtx.observesifActionField().variableList())) {
                    variableStack.push(variableMap);
                    String agentName = resolveVariable(fieldCtx.observesifActionField().parameter());
                    FluentFormula condition = (FluentFormula) visit(fieldCtx.observesifActionField().fluentFormula());
                    condition = removeConstants(condition);
                    if (!(condition instanceof FluentFormulaFalse)) {
                        observesLists.get(agentName).add(condition);
                    }
                    variableStack.pop();
                }
            }

            else if (fieldCtx.awareActionField() != null) {
                for (Map<String,String> variableMap : getVariableMaps(fieldCtx.awareActionField().variableList())) {
                    variableStack.push(variableMap);
                    String agentName = resolveVariable(fieldCtx.awareActionField().parameter());
                    awareLists.get(agentName).add(new FluentFormulaTrue());
                    variableStack.pop();
                }
            }

            else if (fieldCtx.awareifActionField() != null) {
                for (Map<String,String> variableMap : getVariableMaps(fieldCtx.awareifActionField().variableList())) {
                    variableStack.push(variableMap);
                    String agentName = resolveVariable(fieldCtx.awareifActionField().parameter());
                    FluentFormula condition = (FluentFormula) visit(fieldCtx.awareifActionField().fluentFormula());
                    awareLists.get(agentName).add(condition);
                    variableStack.pop();
                }
            }

            else if (fieldCtx.causesActionField() != null) {
                if (actionType != null && actionType != DeplToProblem.ActionType.ONTIC) {
                    throw new RuntimeException("action " + actionName + " has illegal action-type-specifying fields.");
                }
                actionType = DeplToProblem.ActionType.ONTIC;
                for (Map<String,String> variableMap : getVariableMaps(fieldCtx.causesActionField().variableList())) {
                    variableStack.push(variableMap);
                    FluentLiteral effect = (FluentLiteral) visit(fieldCtx.causesActionField().literal());
                    FluentFormula condition = new FluentFormulaTrue();
                    effects.put(effect, condition);
                    variableStack.pop();
                }
            }

            else if (fieldCtx.causesifActionField() != null) {
                if (actionType != null && actionType != DeplToProblem.ActionType.ONTIC) {
                    throw new RuntimeException("action " + actionName + "has illegal action-type-specifying fields.");
                }
                actionType = DeplToProblem.ActionType.ONTIC;
                for (Map<String,String> variableMap : getVariableMaps(fieldCtx.causesifActionField().variableList())) {
                    variableStack.push(variableMap);
                    FluentLiteral effect = (FluentLiteral) visit(fieldCtx.causesifActionField().literal());
                    FluentFormula condition = (FluentFormula) visit(fieldCtx.causesifActionField().fluentFormula());
                    condition = removeConstants(condition);
                    if (!(condition instanceof FluentFormulaFalse)) {
                        effects.put(effect, condition);
                    }
                    variableStack.pop();
                }
            }

            else if (fieldCtx.determinesActionField() != null) {
                if (actionType != null && actionType != DeplToProblem.ActionType.SENSING) {
                    throw new RuntimeException("action " + actionName + "has illegal action-type-specifying fields.");
                }
                actionType = DeplToProblem.ActionType.SENSING;

                for (Map<String,String> variableMap : getVariableMaps(fieldCtx.determinesActionField().variableList())) {
                    variableStack.push(variableMap);
                    determines.add((FluentFormula) visit(fieldCtx.determinesActionField().fluentFormula()));
                    variableStack.pop();
                }
            }

            else if (fieldCtx.announcesActionField() != null) {
                if (actionType != null) {
                    throw new RuntimeException("action " + actionName + "has illegal action-type-specifying fields.");
                }
                actionType = DeplToProblem.ActionType.ANNOUNCEMENT;
                announces = (FluentFormula) visit(fieldCtx.announcesActionField().fluentFormula());

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

        precondition = removeConstants(precondition);

        if (owner == null) {
            throw new RuntimeException("illegal action definition, no owner: " + actionName);
        }

        if (actionType == null) {
            actionType = DeplToProblem.ActionType.ONTIC;  // default
        }

        Map<String, FluentFormula> observes = new HashMap<>();
        Map<String, FluentFormula> aware = new HashMap<>();
        for (String a : domain.getAgents()) {
            observes.put(a, removeConstants(new FluentFormulaOr(observesLists.get(a))));
            aware.put(a, removeConstants(new FluentFormulaOr(awareLists.get(a))));
        }

        if (actionType == DeplToProblem.ActionType.ONTIC) {
            return new OnticAction(actionName,
                                   actionParameters,
                                   owner,
                                   cost,
                                   precondition,
                                   observes,
                                   aware,
                                   effects,
                                   domain
                                  );
        }

        else if (actionType == DeplToProblem.ActionType.SENSING) {
            return new SensingAction(actionName,
                                   actionParameters,
                                   owner,
                                   cost,
                                   precondition,
                                   observes,
                                   aware,
                                   determines,
                                   domain
                                  );
        }

        else if (actionType == DeplToProblem.ActionType.ANNOUNCEMENT) {
            return new AnnouncementAction(actionName,
                                   actionParameters,
                                   owner,
                                   cost,
                                   precondition,
                                   observes,
                                   aware,
                                   announces,
                                   domain
                                  );
        }

        else {
            throw new RuntimeException("invalid action type");
        }
    }





    // FLUENT FORMULAE

    @Override public FluentAtom visitAtom(DeplParser.AtomContext ctx) {
        String atomName = ctx.NAME().getText();
        List<String> parameters = new ArrayList<String>();
        if (ctx.parameterList() != null) {
            for (DeplParser.ParameterContext parameterCtx : ctx.parameterList().parameter()) {
                parameters.add(resolveVariable(parameterCtx));
            }
        }
        return new FluentAtom(atomName, parameters);
    }

    @Override public FluentAtom visitConstant(DeplParser.ConstantContext ctx) {
        String atomName = ctx.NAME().getText();
        List<String> parameters = new ArrayList<String>();
        if (ctx.parameterList() != null) {
            for (DeplParser.ParameterContext parameterCtx : ctx.parameterList().parameter()) {
                parameters.add(resolveVariable(parameterCtx));
            }
        }
        FluentAtom constant = new FluentAtom(atomName, parameters);
        return new FluentAtom(atomName, parameters);
    }

    @Override public FluentFormula visitFluentAtom(DeplParser.FluentAtomContext ctx) {
        return (FluentAtom) visit(ctx.atom());
    }

    @Override public FluentFormula visitFluentParens(DeplParser.FluentParensContext ctx) {
        return (FluentFormula) visit(ctx.fluentFormula());
    }

    @Override public FluentFormula visitFluentNot(DeplParser.FluentNotContext ctx) {
        FluentFormula inner = (FluentFormula) visit(ctx.fluentFormula());
        return (new FluentFormulaNot(inner));
    }

    @Override public FluentFormula visitFluentAnd(DeplParser.FluentAndContext ctx) {
        List<FluentFormula> subFormulae = new ArrayList<>();
        for (DeplParser.FluentFormulaContext subFormula : ctx.fluentFormula()) {
            subFormulae.add((FluentFormula) visit(subFormula));
        }
        return (new FluentFormulaAnd(subFormulae));
    }

    @Override public FluentFormula visitFluentOr(DeplParser.FluentOrContext ctx) {
        List<FluentFormula> subFormulae = new ArrayList<>();
        for (DeplParser.FluentFormulaContext subFormula : ctx.fluentFormula()) {
            subFormulae.add((FluentFormula) visit(subFormula));
        }
        return (new FluentFormulaOr(subFormulae));
    }

    @Override public FluentFormula visitFluentTrue(DeplParser.FluentTrueContext ctx) {
        return (new FluentFormulaTrue());
    }

    @Override public FluentFormula visitFluentFalse(DeplParser.FluentFalseContext ctx) {
        return (new FluentFormulaFalse());
    }



    // LITERALS
    @Override public FluentLiteral visitLiteralTrue(DeplParser.LiteralTrueContext ctx) {
        return new FluentLiteral(true, (FluentAtom) visit(ctx.atom()));
    }

    @Override public FluentLiteral visitLiteralFalse(DeplParser.LiteralFalseContext ctx) {
        return new FluentLiteral(false, (FluentAtom) visit(ctx.atom()));
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

    // TIME FORMULAE

    @Override public TimeFormula visitTimeFormula(DeplParser.TimeFormulaContext ctx) {
        return new TimeFormula((TimeFormula.Inequality) visit(ctx.inequality()),
                                Integer.parseInt(ctx.INTEGER().getText()));
    }

    @Override public TimeFormula.Inequality visitInequalityEq(DeplParser.InequalityEqContext ctx) {
        return TimeFormula.Inequality.EQ;
    }

    @Override public TimeFormula.Inequality visitInequalityNe(DeplParser.InequalityNeContext ctx) {
        return TimeFormula.Inequality.NE;
    }

    @Override public TimeFormula.Inequality visitInequalityLt(DeplParser.InequalityLtContext ctx) {
        return TimeFormula.Inequality.LT;
    }

    @Override public TimeFormula.Inequality visitInequalityLte(DeplParser.InequalityLteContext ctx) {
        return TimeFormula.Inequality.LTE;
    }

    @Override public TimeFormula.Inequality visitInequalityGt(DeplParser.InequalityGtContext ctx) {
        return TimeFormula.Inequality.GT;
    }

    @Override public TimeFormula.Inequality visitInequalityGte(DeplParser.InequalityGteContext ctx) {
        return TimeFormula.Inequality.GTE;
    }





    // GENERAL FORMULAE

    @Override public GeneralFormula visitGeneralBelief(DeplParser.GeneralBeliefContext ctx) {
        return (BeliefFormula) visit(ctx.beliefFormula());
    }

    @Override public GeneralFormula visitGeneralTime(DeplParser.GeneralTimeContext ctx) {
        return (TimeFormula) visit(ctx.timeFormula());
    }

    @Override public GeneralFormula visitGeneralNot(DeplParser.GeneralNotContext ctx) {
        GeneralFormula inner = (GeneralFormula) visit(ctx.generalFormula());
        return (new GeneralFormulaNot(inner));
    }

    @Override public GeneralFormula visitGeneralAnd(DeplParser.GeneralAndContext ctx) {
        List<GeneralFormula> subFormulae = new ArrayList<>();
        for (DeplParser.GeneralFormulaContext subFormula : ctx.generalFormula()) {
            subFormulae.add((GeneralFormula) visit(subFormula));
        }
        return (new GeneralFormulaAnd(subFormulae));
    }

    @Override public GeneralFormula visitGeneralOr(DeplParser.GeneralOrContext ctx) {
        List<GeneralFormula> subFormulae = new ArrayList<>();
        for (DeplParser.GeneralFormulaContext subFormula : ctx.generalFormula()) {
            subFormulae.add((GeneralFormula) visit(subFormula));
        }
        return (new GeneralFormulaOr(subFormulae));
    }






}
