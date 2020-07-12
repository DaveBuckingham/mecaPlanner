package epddl2depl;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import mecaPlanner.*;

import java.io.IOException;

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


public class EpddlToDepl extends EpddlBaseVisitor {


    private Map<String, List<String>> actions = new HashMap<>();

    private String type3DeckAgent = null;
    private String type3DeckAtom = null;

    private String type4DeckAgent = null;
    private String type4DeckAtom = null;

    private clearDeck() {
        type3DeckAgent = null;
        type3DeckAtom = null;
        type4DeckAgent = null;
        type4DeckAtom = null;
    }



    public EpddlToDepl() {
        super();
    }



    @Override public Void visitInit(EpddlParser.InitContext ctx) {
        visit(ctx.domain());
        return null;
    }

    @Override public Void visitDomain(EpddlParser.DomainContext ctx) {

        List<String> agents = (List<String>) visit(ctx.agentsSection());
        Map<String, List<String>> objectDeclarations = (Map<String, List<String>>) visit(ctx.objectsSection());

        System.out.println("types{");
        System.out.println("    actor - object,");
        for (String type : objectDeclarations.keySet()) {
            System.out.println("    " + type + " - object,");
        }
        System.out.println("}");

        System.out.println("objects{");
        for (String a : agents) {
            System.out.println("    " + a + " - actor,");
        }
        for (String type : objectDeclarations.keySet()) {
            for (String obj : objectDeclarations.get(type)) {
                System.out.println("    " + obj + " - " + type + ",");
            }
        }
        System.out.println("}");

        System.out.println("agents{");
        for (String a : agents) {
            System.out.println("    " + a + ",");
        }
        System.out.println("}");


        System.out.println("predicates{");
        visit(ctx.predicatesSection());
        System.out.println("}");

        System.out.println("constants{}");

        System.out.println("initially{");
        visit(ctx.defineInitially());
        System.out.println("}");

        //System.out.println("goals{");
        //for (EpddlParser.GoalContext goalCtx : ctx.goal()) {
        //    System.out.print("    ");
        //    visit(goalCtx);
        //    System.out.println(",");
        //}
        //System.out.println("}");

        //for (EpddlParser.DeclareActionsContext declareActionsCtx : ctx.declareActions()) {
        //    visit(declareActionsCtx);
        //}

        //for (EpddlParser.DefineActionContext defineActionCtx : ctx.defineAction()) {
        //    visit(defineActionCtx);
        //}

        //System.out.println("actions{");
        //    for (Map.Entry<String,List<String>> entry : actions.entrySet()) {
        //        System.out.println("    " + entry.getKey() + "()" +  "{");
        //        System.out.println("        " + "owner{god}");
        //        for (String def : entry.getValue()) {
        //            System.out.println("        " + def + ",");
        //        }
        //        System.out.println("    " + "}");
        //    }
        //System.out.println("}");

        return null;
    }

    @Override public Map<String, List<String>> visitObjectsSection(EpddlParser.ObjectsSectionContext ctx) {
        Map<String, List<String>> objectsByType = new HashMap<>();
        for (EpddlParser.TypedObjectsContext objectsCtx : ctx.typedObjects()) {
            List<String> objectsDef = (List<String>) visit(objectsCtx);
            if (objectsDef.size() < 2) {
                throw new RuntimeException("object def should have length at least 2, one for object and one for type");
            }
            String type = objectsDef.get(0);
            List<String> objs = new ArrayList<>();
            for (int i = 1; i < objectsDef.size(); i++) {
                objs.add(objectsDef.get(i));
            }
            objectsByType.put(type, objs);
        }
        return objectsByType;
    }

    @Override public List<String> visitTypedObjects(EpddlParser.TypedObjectsContext ctx) {
        List<String> objectList = new ArrayList<>();
        objectList.add(ctx.objectType().getText());
        for (EpddlParser.ObjectDefContext objCtx : ctx.objectDef()) {
            objectList.add(objCtx.getText());
        }
        return objectList;
    }

    @Override public List<String> visitAgentsSection(EpddlParser.AgentsSectionContext ctx) {
        List<String> agents = new ArrayList<>();
        for (TerminalNode agent : ctx.NAME()) {
            agents.add(agent.getText());
        }
        return agents;
    }


    @Override public Void visitPredicatesSection(EpddlParser.PredicatesSectionContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override public Void visitPredicateDef(EpddlParser.PredicateDefContext ctx) {
        String predicateName = ctx.NAME().getText();
        System.out.print("    " + predicateName + "[");
        List<String> parameterNames = new ArrayList<>();
        List<EpddlParser.ParameterContext> parameters = ctx.parameter();
        for (int i = 0; i < parameters.size(); i++) {
            EpddlParser.ParameterContext parameter = parameters.get(i);
            String parameterName = parameter.VARIABLE().getText();
            parameterNames.add(parameterName);
            String parameterType = parameter.NAME().getText();
            System.out.print(parameterName + " - " + parameterType);
            if (i < parameters.size() - 1) {
                System.out.print(", ");
            }
        }
        System.out.print("](");
        for (int i = 0; i < parameterNames.size(); i++) {
            System.out.print(parameterNames.get(i));
            if (i < parameterNames.size() - 1) {
                System.out.print(",");
            }
        }
        System.out.print(")\n");
        return null;
    }

    @Override public Void visitDefineInitially(EpddlParser.DefineInitiallyContext ctx) {
        EpddlParser.FormulaContext formulaCtx = ctx.formula();
        if (formulaCtx instanceof EpddlParser.FormulaAndContext) {
            EpddlParser.FormulaAndContext formulaAndCtx = (EpddlParser.FormulaAndContext) formulaCtx;
            for (EpddlParser.FormulaContext innerCtx : formulaAndCtx.formula()) {
                String innerString = formulaToInitFormula(innerCtx);
                if (!innerString.empty()) {
                    System.out.println("    " + innerString + ",");
                }
            }
        }
        else {
            System.out.println("    " + formulaToInitFormula(formulaCtx) + ",");
        }
        return null;
    }

    private String formulaToInitFormula(EpddlParser.FormulaContext ctx) {
        StringBuilder str = new StringBuilder();
        if (ctx instanceof EpddlParser.FormulaAtomContext) {
            str.append(visit(ctx));
        }
        else if (ctx instanceof EpddlParser.FormulaNotContext) {
            EpddlParser.FormulaContext innerCtx + ((EpddlParser.FormulaNotContext) ctx).formula();
            if (innerCtx instanceof EpddlParser.FormulaAtomContext) {
                return "";
            }
            elseif (innerCtx instanceof EpddlParser.FormulaKnowsContext) {
                //str.append(
            }
            else {
                throw new RuntimeException("invalid init: " + ctx.getText());
            }
        }
        else if (ctx instanceof EpddlParser.FormulaKnowsContext) {
            str.append("unimplemented (K)");
        }
        else if (ctx instanceof EpddlParser.FormulaDoesntKnowContext) {
            str.append("unimplemented (~K)");
        }
        else {
            throw new RuntimeException("invalid init: " + ctx.getText());
        }
        return str.toString();
    }


    @Override public String visitFormulaAtom(EpddlParser.FormulaAtomContext ctx) {
        StringBuilder str = new StringBuilder();
        str.append(ctx.NAME().getText());
        List<EpddlParser.NameOrVarContext> parameters = ctx.nameOrVar();
        if (parameters.size() > 0) {
            str.append("(");
            for (EpddlParser.NameOrVarContext parameter : ctx.nameOrVar()) {
                str.append(parameter.getText());
                str.append(",");
            }
            str.deleteCharAt(str.length() - 1);
            str.append(")");
        }
        return str.toString();
    }

    @Override public String visitFormulaNot(EpddlParser.FormulaNotContext ctx) {
        return ("~" + (String) visit(ctx.formula()));
    }

    @Override public String visitFormulaOr(EpddlParser.FormulaOrContext ctx) {
        StringBuilder str = new StringBuilder();
        List<EpddlParser.FormulaContext> subFormulae = ctx.formula();
        if (subFormulae.size() < 2) {
            throw new RuntimeException("or needs at least 2 subformulae");
        }
        str.append("(");
        for (EpddlParser.FormulaContext subFormula : subFormulae) {
            str.append((String) visit(subFormula));
            str.append(" | ");
        }
        str.deleteCharAt(str.length() - 1);
        str.deleteCharAt(str.length() - 1);
        str.deleteCharAt(str.length() - 1);
        str.append(")");
        return str.toString();
    }

    //@Override public String visitFormulaCommon(EpddlParser.FormulaCommonContext ctx) {
    //    return ("C(" + (String) visit(ctx.beliefFormula()) + ")");
    //}

    //@Override public String visitFormulaBelieves(EpddlParser.FormulaBelievesContext ctx) {
    //    StringBuilder str = new StringBuilder();
    //    str.append("B_");
    //    str.append((String) visit(ctx.agent()));
    //    str.append("(");
    //    str.append((String) visit(ctx.beliefFormula()));
    //    str.append(")");
    //    return str.toString();
    //}

    //@Override public String visitBeliefParens(EpddlParser.BeliefParensContext ctx) {
    //    return ("(" + (String) visit(ctx.beliefFormula()) + ")");
    //}

    //@Override public String visitLiteralTrue(EpddlParser.LiteralTrueContext ctx) {
    //    return ((String) visit(ctx.fluent()));
    //}

    //@Override public String visitLiteralFalse(EpddlParser.LiteralFalseContext ctx) {
    //    return ("~" + (String) visit(ctx.fluent()));
    //}

    //@Override public Void visitGoal(EpddlParser.GoalContext ctx) {
    //    System.out.print((String) visit(ctx.beliefFormula()));
    //    return null;
    //}

    //@Override public Void visitDeclareActions(EpddlParser.DeclareActionsContext ctx) {
    //    for (EpddlParser.ActionContext actionCtx : ctx.action()) {
    //        actions.put(actionCtx.getText(), new ArrayList<String>());
    //    }
    //    return null;
    //}

    //@Override public Void visitActionDefExecutable(EpddlParser.ActionDefExecutableContext ctx) {
    //    String actionName = ctx.action().getText();
    //    if (!actions.containsKey(actionName)) {
    //        System.out.println("WARNING: creating undeclared action: " + actionName);
    //        actions.put(actionName, new ArrayList<String>());
    //    }
    //    for (EpddlParser.BeliefFormulaContext formulaCtx : ctx.beliefFormula()) {
    //        actions.get(actionName).add("precondition{" + (String) visit(formulaCtx) + "}");
    //    }
    //    return null;
    //}

    //@Override public Void visitActionDefCauses(EpddlParser.ActionDefCausesContext ctx) {
    //    String actionName = ctx.action().getText();
    //    for (EpddlParser.LiteralContext literalCtx : ctx.literal()) {
    //        actions.get(actionName).add("causes{" + (String) visit(literalCtx) + "}");
    //    }
    //    return null;
    //}

    //@Override public Void visitActionDefCausesIf(EpddlParser.ActionDefCausesIfContext ctx) {
    //    String actionName = ctx.action().getText();
    //    String effect = (String) visit(ctx.literal(0));
    //    String condition = (String) visit(ctx.literal(1));
    //    actions.get(actionName).add("causesif{" + effect + ", " + condition + "}");
    //    return null;
    //}

    //@Override public Void visitActionDefObserves(EpddlParser.ActionDefObservesContext ctx) {
    //    String actionName = ctx.action().getText();
    //    actions.get(actionName).add("observes{" + (String) visit(ctx.agent()) + "}");
    //    return null;
    //}

    //@Override public Void visitActionDefObservesIf(EpddlParser.ActionDefObservesIfContext ctx) {
    //    String actionName = ctx.action().getText();
    //    String agent = (String) visit(ctx.agent());
    //    String condition = (String) visit(ctx.literal());
    //    actions.get(actionName).add("observesif{" + agent + ", " + condition + "}");
    //    return null;
    //}

    //@Override public Void visitActionDefAware(EpddlParser.ActionDefAwareContext ctx) {
    //    String actionName = ctx.action().getText();
    //    actions.get(actionName).add("aware{" + (String) visit(ctx.agent()) + "}");
    //    return null;
    //}

    //@Override public Void visitActionDefAnnounces(EpddlParser.ActionDefAnnouncesContext ctx) {
    //    String actionName = ctx.action().getText();
    //    actions.get(actionName).add("announces{" + (String) visit(ctx.fluent()) + "}");
    //    return null;
    //}

    //@Override public Void visitActionDefDetermines(EpddlParser.ActionDefDeterminesContext ctx) {
    //    String actionName = ctx.action().getText();
    //    actions.get(actionName).add("determines{" + (String) visit(ctx.fluent()) + "}");
    //    return null;
    //}



    public static void main(String args[]) {
        if (args.length < 1) {
            System.out.println("must provide input file name");
            return;
        }

        CharStream input = null;
        try {
            input = CharStreams.fromFileName(args[0]);
        }
        catch (IOException e) {
            System.out.println("failed to read input file: " + e.getMessage());
            System.exit(1);
        }

        EpddlLexer lexer           = new EpddlLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        EpddlParser parser         = new EpddlParser(tokens);
        ParseTree tree           = parser.init();

        EpddlToDepl visitor = new EpddlToDepl();
        visitor.visit(tree);

    }



}
