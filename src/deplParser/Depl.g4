grammar Depl;

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

LINECOMMENT: '//' .*? '\r'? '\n' -> skip;
COMMENT: '/*' .*? '*/' -> skip;
WS: [ \n\t\r]+ -> skip;


init :
    typesSection
    objectsSection
    agentsSection
    atomsSection
    constantsSection
    initiallySection
    goalsSection
    actionsSection
;


// TYPE DEFINITIONS

typesSection : 'types' '{' (typeDefinition ',')* typeDefinition? '}' ;

typeDefinition : NAME '-' type ;


// OBJECT DEFINITIONS

objectsSection : 'objects' '{' (objectDefinition ',')* objectDefinition? '}' ;

objectDefinition : NAME '-' type ;

type : NAME | OBJECT ;


// AGENT DEFINITIONS

agentsSection : 'agents' '{' (agentDef ',')* agentDef? '}' ;

agentDef : systemAgent | passiveAgent | environmentAgent ;

systemAgent : NAME ;

passiveAgent : NAME '{' '}' ;

environmentAgent : NAME '{' CLASS '}' ;



// PREDICATES DEFINITIONS

atomsSection : 'atoms' '{' (atomDefinition ',')* atomDefinition? '}' ;

atomDefinition : NAME variableList? parameterList? ;

variableList : '[' (variableDefinition ',')* variableDefinition? ']' ;

parameterList : '(' (parameter ',')* parameter? ')' ;

variableDefinition : VARIABLE '-' NAME ;

parameter : NAME | VARIABLE ;


// CONSTANTS DEFINITIONS

constantsSection : 'constants' '{' (constantDefinition ',')* constantDefinition? '}' ;

constantDefinition : NAME variableList? parameterList? ;


// INITIAL STATE DEFINITION

initiallySection : 'initially' '{' (initiallyStatement ',')* initiallyStatement? '}' ;

initiallyStatement : beliefFormula ;

atom : NAME parameterList? | '(' NAME parameterList? ')';

constant : NAME parameterList? | '(' NAME parameterList? ')';


// FORMULAE

fluentFormula 
    : atom                                                       # fluentAtom
    | '(' fluentFormula ')'                                      # fluentParens
    | '~' fluentFormula                                          # fluentNot
    | fluentFormula '&' fluentFormula ('&' fluentFormula)*       # fluentAnd
    | fluentFormula '|' fluentFormula ('|' fluentFormula)*       # fluentOr
    | 'true'                                                     # fluentTrue
    | 'True'                                                     # fluentTrue
    | 'T'                                                        # fluentTrue
    | 'false'                                                    # fluentFalse
    | 'False'                                                    # fluentFalse
    | 'F'                                                        # fluentFalse
    ;

beliefFormula 
    : fluentFormula                                              # beliefFluent
    | '(' beliefFormula ')'                                      # beliefParens
    | '~' beliefFormula                                          # beliefNot
    | beliefFormula '&' beliefFormula ('&' beliefFormula)*       # beliefAnd
    | beliefFormula '|' beliefFormula ('|' beliefFormula)*       # beliefOr
    | 'C' '(' beliefFormula ')'                                  # beliefCommon
    | 'B_' parameter '(' beliefFormula ')'                       # beliefBelieves
    ;

inequality
    : '=='                # inequalityEq
    | '!='                # inequalityNe
    | '<'                 # inequalityLt
    | '<='                # inequalityLte
    | '>'                 # inequalityGt
    | '>='                # inequalityGte
    ;

timeFormula : ('t' | 'time' | 'timestep')  inequality INTEGER;

generalFormula
    : beliefFormula                                              # generalBelief
    | timeFormula                                                # generalTime
    | '~' generalFormula                                         # generalNot
    | generalFormula '&' generalFormula ('&' generalFormula)*    # generalAnd
    | generalFormula '|' generalFormula ('|' generalFormula)*    # generalOr
    ;

literal
    : atom                                  # literalTrue
    | '~' atom                              # literalFalse
    ;


// GOALS DEFINITION

goalsSection : 'goals' '{' (goal ',')* goal? '}' ;

goal : generalFormula ;


// ACTION DEFINITIONS

actionsSection : 'actions' '{' (actionDefinition ','?)* '}' ;

actionDefinition : NAME variableList? parameterList '{' (actionField ','?)* '}' ;

actionField
    : ownerActionField
    | costActionField
    | preconditionActionField
    | observesActionField
    | observesifActionField
    | awareActionField
    | awareifActionField
    | causesActionField
    | causesifActionField
    | determinesActionField
    | announcesActionField
    ;

ownerActionField        : 'owner' '{' parameter '}' ;
costActionField         : 'cost' '{' INTEGER '}' ;
preconditionActionField : 'precondition' variableList? '{' fluentFormula '}' ;
observesActionField     : 'observes' variableList? '{' parameter '}' ;
observesifActionField   : 'observesif' variableList? '{' parameter ',' fluentFormula '}' ;
awareActionField        : 'aware' variableList? '{' parameter '}' ;
awareifActionField      : 'awareif' variableList? '{' parameter ',' fluentFormula '}' ;

causesActionField       : 'causes' variableList? '{' literal '}' ;
causesifActionField     : 'causesif' variableList? '{' literal ',' fluentFormula '}' ;
determinesActionField   : 'determines' variableList? '{' fluentFormula '}' ;
announcesActionField    : 'announces' '{' fluentFormula '}' ;

