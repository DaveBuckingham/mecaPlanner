grammar Epddl;

NAME: LOWERDIGIT ANYCHAR*;
VARIABLE: '?' NAME;
INTEGER: DIGIT+;

fragment LOWERDIGIT: [a-z0-9];
fragment LOWER: [a-z];
fragment UPPER: [A-Z];
fragment LETTER: LOWER | UPPER;
fragment ANYCHAR: LETTER | DIGIT | '_';
fragment DIGIT:  [0-9];
fragment DECIMAL: '.' DIGIT+;

LINECOMMENT: '%' .*? '\r'? '\n' -> skip;
WS: [ \n\t\r]+ -> skip;

nameOrVar : NAME | VARIABLE ;

init : domain;

domain :
    '(define' '(domain' NAME ')'
    objectsSection
    agentsSection
    predicatesSection
    defineAction*
    defineInitially
    defineConstraint
    defineGoal
    ')'
;

objectsSection : '(:objects' typedObjects* ')' ;

typedObjects : objectDef objectDef* '-' objectType ;
objectDef : NAME ;
objectType : NAME ;


agentsSection : '(:agents' NAME* ')' ;


predicatesSection : '(:predicates' predicateDef* ')' ;

predicateDef : '(' NAME parameter* ')' ;

parameter : VARIABLE '-' NAME ;

defineAction : defineOnticAction | defineCommunicationAction | defineSensingAction ;

defineOnticAction : '(:action' NAME
    ':category (ontic)'
    actionParameters
    actionPrecondition
    actionEffects
    ')'
;

defineCommunicationAction : '(:action' NAME
    ':category (communication)'
    actionParameters
    actionPrecondition
    actionEffects
    ')'
;

defineSensingAction : '(:action' NAME
    ':category (sensing)'
    actionParameters
    actionPrecondition
    actionObserves
    ')'
;

actionParameters : ':parameters' '(' parameter* ')' ;

actionPrecondition : ':precondition' '(' formula ')' ;

actionEffects : ':effect' '(' effect* ')' ;

effect : '<' '{' '(' formula ')' '}' '{' '(' formula ')' '}' '>' ;

actionObserves : ':observe_pos' '(' formula ')'
                 ':observe_neg' '(' formula ')' ;

formula 
    : NAME nameOrVar*                          # formulaAtom
    | 'not' '(' formula ')'                    # formulaNot
    | 'or' ('(' formula ')')*                  # formulaOr
    | 'and' ('(' formula ')')*                 # formulaAnd
    | 'K_' nameOrVar '(' formula ')'           # formulaKnows
    | 'DK_' nameOrVar '(' formula ')'          # formulaDoesntKnow
    | 'True'                                   # formulaTrue
    ;

defineInitially : '(:init' '(' formula ')' ')';

defineConstraint : '(:constraint' '(' formula ')' ')' ;

defineGoal : '(:goal' '(' formula ')' ')' ;

