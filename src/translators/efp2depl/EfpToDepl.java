package efp2depl;

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


public class EfpToDepl extends EfpBaseVisitor {


    private Map<String, List<String>> actions = new HashMap<>();

    public EfpToDepl() {
        super();
    }



    @Override public Void visitInit(EfpParser.InitContext ctx) {
        System.out.println("types{}");


        List<String> agents = (List<String>) visit(ctx.declareAgents());

        System.out.println("objects{");
        System.out.println("    " + "god - object,");
        for (String a : agents) {
            System.out.println("    " + a + " - object,");
        }
        System.out.println("}");

        System.out.println("agents{");
        System.out.println("    " + "god,");
        for (String a : agents) {
            System.out.println("    " + a + "{},");
        }
        System.out.println("}");

        System.out.println("predicates{");
        visit(ctx.declareFluents());
        System.out.println("}");

        System.out.println("constants{}");

        System.out.println("initially{");
        for (EfpParser.DefineInitiallyContext defineInitiallyCtx : ctx.defineInitially()) {
            visit(defineInitiallyCtx);
        }
        System.out.println("}");

        System.out.println("goals{");
        for (EfpParser.GoalContext goalCtx : ctx.goal()) {
            System.out.print("    ");
            visit(goalCtx);
            System.out.println(",");
        }
        System.out.println("}");

        for (EfpParser.DeclareActionsContext declareActionsCtx : ctx.declareActions()) {
            visit(declareActionsCtx);
        }

        for (EfpParser.DefineActionContext defineActionCtx : ctx.defineAction()) {
            visit(defineActionCtx);
        }

        System.out.println("actions{");
            for (Map.Entry<String,List<String>> entry : actions.entrySet()) {
                System.out.println("    " + entry.getKey() + "()" +  "{");
                System.out.println("        " + "owner{god}");
                for (String def : entry.getValue()) {
                    System.out.println("        " + def + ",");
                }
                System.out.println("    " + "}");
            }
        System.out.println("}");

        return null;
    }

    @Override public List<String> visitDeclareAgents(EfpParser.DeclareAgentsContext ctx) {
        List<String> agents = new ArrayList<>();
        for (EfpParser.AgentContext agentCtx : ctx.agent()) {
            agents.add((String) visit(agentCtx));
        }
        return agents;
    }

    @Override public String visitAgent(EfpParser.AgentContext ctx) {
        return ctx.getText();
    }

    @Override public Void visitDeclareFluents(EfpParser.DeclareFluentsContext ctx) {
        for (EfpParser.FluentContext fluentCtx : ctx.fluent()) {
            System.out.print("    ");
            System.out.print((String) visit(fluentCtx));
            System.out.println(",");
        }
        return null;
    }

    @Override public Void visitDefineInitially(EfpParser.DefineInitiallyContext ctx) {
        for (EfpParser.BeliefFormulaContext formulaCtx : ctx.beliefFormula()) {
            String formula = (String) visit(formulaCtx);
            if (!(formula.startsWith("~") || formula.startsWith("(~"))) {
                System.out.print("    ");
                System.out.print((String) visit(formulaCtx));
                System.out.println(",");
            }
        }
        return null;
    }

    @Override public String visitFluent(EfpParser.FluentContext ctx) {
        return ctx.getText();
    }

    @Override public String visitBeliefLiteral(EfpParser.BeliefLiteralContext ctx) {
        return (String) visit(ctx.literal());
    }

    @Override public String visitBeliefNot(EfpParser.BeliefNotContext ctx) {
        return ("~" + (String) visit(ctx.beliefFormula()));
    }

    @Override public String visitBeliefOr(EfpParser.BeliefOrContext ctx) {
        return ((String) visit(ctx.beliefFormula(0)) + " | " + (String) visit(ctx.beliefFormula(1)));
    }

    @Override public String visitBeliefCommon(EfpParser.BeliefCommonContext ctx) {
        return ("C(" + (String) visit(ctx.beliefFormula()) + ")");
    }

    @Override public String visitBeliefBelieves(EfpParser.BeliefBelievesContext ctx) {
        StringBuilder str = new StringBuilder();
        str.append("B_");
        str.append((String) visit(ctx.agent()));
        str.append("(");
        str.append((String) visit(ctx.beliefFormula()));
        str.append(")");
        return str.toString();
    }

    @Override public String visitBeliefParens(EfpParser.BeliefParensContext ctx) {
        return ("(" + (String) visit(ctx.beliefFormula()) + ")");
    }

    @Override public String visitLiteralTrue(EfpParser.LiteralTrueContext ctx) {
        return ((String) visit(ctx.fluent()));
    }

    @Override public String visitLiteralFalse(EfpParser.LiteralFalseContext ctx) {
        return ("~" + (String) visit(ctx.fluent()));
    }

    @Override public Void visitGoal(EfpParser.GoalContext ctx) {
        System.out.print((String) visit(ctx.beliefFormula()));
        return null;
    }

    @Override public Void visitDeclareActions(EfpParser.DeclareActionsContext ctx) {
        for (EfpParser.ActionContext actionCtx : ctx.action()) {
            actions.put(actionCtx.getText(), new ArrayList<String>());
        }
        return null;
    }

    @Override public Void visitActionDefExecutable(EfpParser.ActionDefExecutableContext ctx) {
        String actionName = ctx.action().getText();
        if (!actions.containsKey(actionName)) {
            System.err.println("WARNING: creating undeclared action: " + actionName);
            actions.put(actionName, new ArrayList<String>());
        }
        for (EfpParser.BeliefFormulaContext formulaCtx : ctx.beliefFormula()) {
            actions.get(actionName).add("precondition{" + (String) visit(formulaCtx) + "}");
        }
        return null;
    }

    @Override public Void visitActionDefCauses(EfpParser.ActionDefCausesContext ctx) {
        String actionName = ctx.action().getText();
        for (EfpParser.LiteralContext literalCtx : ctx.literal()) {
            actions.get(actionName).add("causes{" + (String) visit(literalCtx) + "}");
        }
        return null;
    }

    @Override public Void visitActionDefCausesIf(EfpParser.ActionDefCausesIfContext ctx) {
        String actionName = ctx.action().getText();
        String effect = (String) visit(ctx.literal(0));
        String condition = (String) visit(ctx.literal(1));
        actions.get(actionName).add("causesif{" + effect + ", " + condition + "}");
        return null;
    }

    @Override public Void visitActionDefObserves(EfpParser.ActionDefObservesContext ctx) {
        String actionName = ctx.action().getText();
        actions.get(actionName).add("observes{" + (String) visit(ctx.agent()) + "}");
        return null;
    }

    @Override public Void visitActionDefObservesIf(EfpParser.ActionDefObservesIfContext ctx) {
        String actionName = ctx.action().getText();
        String agent = (String) visit(ctx.agent());
        String condition = (String) visit(ctx.literal());
        actions.get(actionName).add("observesif{" + agent + ", " + condition + "}");
        return null;
    }

    @Override public Void visitActionDefAware(EfpParser.ActionDefAwareContext ctx) {
        String actionName = ctx.action().getText();
        actions.get(actionName).add("aware{" + (String) visit(ctx.agent()) + "}");
        return null;
    }

    @Override public Void visitActionDefAnnounces(EfpParser.ActionDefAnnouncesContext ctx) {
        String actionName = ctx.action().getText();
        actions.get(actionName).add("announces{" + (String) visit(ctx.fluent()) + "}");
        return null;
    }

    @Override public Void visitActionDefDetermines(EfpParser.ActionDefDeterminesContext ctx) {
        String actionName = ctx.action().getText();
        actions.get(actionName).add("determines{" + (String) visit(ctx.fluent()) + "}");
        return null;
    }



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

        EfpLexer lexer           = new EfpLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        EfpParser parser         = new EfpParser(tokens);
        ParseTree tree           = parser.init();

        EfpToDepl visitor = new EfpToDepl();
        visitor.visit(tree);

    }



}
