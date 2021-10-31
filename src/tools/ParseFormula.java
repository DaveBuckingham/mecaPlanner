package tools;

import mecaPlanner.formulae.beliefFormulae.BeliefFormula;
import mecaPlanner.Log;
import mecaPlanner.Domain;
import mecaPlanner.Problem;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import depl.*;

import java.util.Scanner;

import depl.DeplToProblem;


public class ParseFormula {

    public static void main(String args[]) {

        Log.setThreshold("warning");

        if (args.length != 1) {
            throw new RuntimeException("expected single depl file parameter.");
        }

        DeplToProblem visitor = new DeplToProblem();
        Scanner stdin         = new Scanner(System.in);

        // here we parse a depl file.
        // we need to do this because when the visitor is parsing a formula
        // it will want to do checks, e.g. making sure fluents in the formula have been defined
        visitor.buildProblem(args[0]);

        while(true) {
            String input             = stdin.nextLine();                     // get a line of text from the user
            CharStream inputStream   = CharStreams.fromString(input);        // antlr wants a CharStream
            DeplLexer lexer          = new DeplLexer(inputStream);           // instantiate a lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer);         // run the lexer, making stream of tokens
            DeplParser parser        = new DeplParser(tokens);               // instantiate a parser
            ParseTree tree           = parser.beliefFormula();               // run the parser, making a parse tree
            BeliefFormula f          = (BeliefFormula) visitor.visit(tree);  // visit the parse tree, building a formula
            System.out.println(f);
        }

    }
}


