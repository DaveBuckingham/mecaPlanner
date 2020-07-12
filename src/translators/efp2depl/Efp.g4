grammar Efp;

NAME: LOWER ANYCHAR*;
CLASS: UPPER LETTER*;
VARIABLE: '?' NAME;
INTEGER: DIGIT+;
OBJECT : 'object';

fragment LOWER: [a-z];
fragment UPPER: [A-Z];
fragment LETTER: LOWER | UPPER;
fragment ANYCHAR: LETTER | DIGIT | '_';
fragment DIGIT:  [0-9];
fragment DECIMAL: '.' DIGIT+;

LINECOMMENT: '%' .*? '\r'? '\n' -> skip;
WS: [ \n\t\r]+ -> skip;

init :
    declareFluents
    declareActions*
    declareAgents
    defineAction*
    defineInitially*
    goal*
;

declareFluents : 'fluent' (fluent ',')* fluent ';' ;

declareActions : 'action' (action ',')* action ';' ;

declareAgents : 'agent' (agent ',')* agent ';' ;

defineAction
    : 'executable' action 'if' (beliefFormula ',')* beliefFormula ';'   # actionDefExecutable
    | action 'causes' (literal ',')* literal ';'                        # actionDefCauses
    | action 'causes' literal 'if' literal ';'                          # actionDefCausesIf
    | agent 'observes' action ';'                                       # actionDefObserves
    | agent 'observes' action 'if' literal ';'                          # actionDefObservesIf
    | agent 'aware_of' action ';'                                       # actionDefAware
    | action 'announces' fluent ';'                                     # actionDefAnnounces
    | action 'determines' fluent ';'                                    # actionDefDetermines
    ;

defineInitially : 'initially' (beliefFormula ',')* beliefFormula ';' ;

goal : 'goal' beliefFormula ';' ;

fluent : NAME;
action : NAME;
agent : NAME;
agentList : '[' (agent ',')* agent ']' ;


beliefFormula 
    : literal                                  # beliefLiteral
    | '-' beliefFormula                        # beliefNot
    | beliefFormula '|' beliefFormula          # beliefOr
    | 'C' '(' agentList ',' beliefFormula ')'  # beliefCommon
    | 'B' '(' agent ',' beliefFormula ')'      # beliefBelieves
    | '(' beliefFormula ')'                    # beliefParens
    ;

literal
    : fluent                                # literalTrue
    | '-' fluent                            # literalFalse
    ;

