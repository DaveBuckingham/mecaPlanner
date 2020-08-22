grammar Depl;

fragment LOWER: [a-z];
fragment UPPER: [A-Z];
fragment LETTER: LOWER | UPPER;
fragment ANYCHAR: LETTER | DIGIT | '_';
fragment DIGIT:  [0-9];

fragment OP_EQ          : '==';
fragment OP_NE          : '!=';
fragment OP_LT          : '<';
fragment OP_LTE         : '<=';
fragment OP_GE          : '>';
fragment OP_GTE         : '>=';

LOWER_NAME     : LOWER ANYCHAR*;
UPPER_NAME     : UPPER ANYCHAR*;
CLASS          : (ANYCHAR*'.')* UPPER LETTER*;
VARIABLE       : '?' LOWER_NAME;
INTEGER        : DIGIT+;
ASSIGN         : '<-'|'=';

OP_INEQUALITY  : OP_NE | OP_LT | OP_LTE | OP_GE | OP_GTE;

% KEYWORD_OBJECT : 'Object' ;
% KEYWORD_TIME   : 'time' | 'timestep' ;
KEYWORD_TRUE   : 'true' ;
KEYWORD_FALSE  : 'false' ;
 
LINECOMMENT: '//' .*? '\r'? '\n' -> skip;
COMMENT: '/*' .*? '*/' -> skip;
WS: [ \n\t\r]+ -> skip;



init :
    typesSection
    objectsSection
    agentsSection
    passiveSection?
    constantsSection?
    fluentsSection
    initiallySection
    goalsSection
    actionsSection
;


// TYPE DEFINITIONS

typesSection : 'types' '{' (typeDefinition ',')* typeDefinition? '}' ;

typeDefinition : type '-' type ;


// OBJECT DEFINITIONS

objectsSection : 'objects' '{' (objectDefinition ',')* objectDefinition? '}' ;

objectDefinition : LOWER_NAME '-' type ;

% type : UPPER_NAME | KEYWORD_OBJECT ;
type : UPPER_NAME ;


// AGENT DEFINITIONS

agentsSection : 'agents' '{' (agentDef ',')* agentDef? '}' ;

agentDef : systemAgent | environmentAgent ;

systemAgent : LOWER_NAME ;

environmentAgent : LOWER_NAME '{' CLASS '}' ;


// PASSIVE AGENT DEFINITIONS

passiveSection : 'passive' '{' (passiveDef ',')* passiveDef? '}' ;

passiveDef : LOWER_NAME ;


// PREDICATES

predicateDefinition : LOWER_NAME parameterList ;

parameterList : '(' (parameter ',')* parameter? ')' ;

parameter : LOWER_NAME | VARIABLE - type ;

assignment : predicateDefinition ASSIGN (KEYWORD_FALSE | KEYWORD_TRUE | INTEGER | LOWER_NAME);



// CONSTANTS

constantsSection : 'constants' '{' (assignment ',')* assignment? '}' ;



// FLUENTS DEFINITIONS

fluentsSection : 'fluents' '{' (fluentDefinition ',')* fluentDefinition? '}' ;

fluentDefinition : predicateDefinition '-' type ;



// INITIAL STATE DEFINITION

initiallySection : 'initially' (startStateDef | '{' (startStateDef ',')* startStateDef ','? '}') ;

startStateDef : initiallyDef | kripkeModel ;

initiallyDef : '{' (beliefFormula ',')* beliefFormula? '}' ;

kripkeModel : '{' (kripkeFormula ',')* kripkeFormula? '}' ;

kripkeFormula
    : LOWER_NAME ASSIGN '{' (atom ',')* atom? '}'                                                     # kripkeWorld
    | relationType NAME ASSIGN '{' ('('fromWorld','toWorld')'',')* ('('fromWorld','toWorld')')? '}'   # kripkeRelation
    ;
relationType : 'B_' | 'K_' ;
fromWorld : LOWER_NAME;
toWorld : LOWER_NAME;




assignment : predicateDefinition ASSIGN (KEYWORD_FALSE | KEYWORD_TRUE | INTEGER | LOWER_NAME);

// FORMULAE

inequality
    : INTEGER OP_INEQUALITY INTEGER
    | predicate OP_INEQUALITY INTEGER
    | INTEGER OP_INEQUALITY predicate
    | predicate OP_INEQUALITY predicate
    ;

fluentFormula 
    : predicate                                                  # fluentAtom
    | '(' fluentFormula ')'                                      # fluentParens
    | '~' fluentFormula                                          # fluentNot
    | fluentFormula '&' fluentFormula ('&' fluentFormula)*       # fluentAnd
    | fluentFormula '|' fluentFormula ('|' fluentFormula)*       # fluentOr
    | fluentFormula '==' fluentFormula ('==' fluentFormula)*     # fluentEquals
    | KEYWORD_TRUE                                               # fluentTrue
    | KEYWORD_FALSE                                              # fluentFalse
    | INTEGER                                                    # fluentInteger
    | LOWER_NAME                                                 # fluentObject
    | inequality                                                 # fluentInequality
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



// GOALS DEFINITION

goalsSection : 'goals' '{' (goal ',')* goal? '}' ;

goal : beliefFormula ;


// ACTION DEFINITIONS

actionsSection : 'actions' '{' (actionDefinition ','?)* '}' ;

actionDefinition : NAME parameterList '{' (actionField ','?)* '}' ;

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
causesifActionField
    : 'causesif' variableList? '{' literal ',' fluentFormula '}'
    | 'causes' variableList? '{' literal 'if' fluentFormula '}'
    ;
determinesActionField   : 'determines' variableList? '{' fluentFormula '}' ;
announcesActionField    : 'announces' variableList? '{' beliefFormula '}' ;

