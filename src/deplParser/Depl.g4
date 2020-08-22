grammar Depl;

fragment LOWER          : [a-z];
fragment UPPER          : [A-Z];
fragment LETTER         : LOWER | UPPER;
fragment ANYCHAR        : LETTER | DIGIT | '_';
fragment DIGIT          :  [0-9];

fragment OP_EQ          : '==';
fragment OP_NE          : '!=';
fragment OP_LT          : '<';
fragment OP_LTE         : '<=';
fragment OP_GE          : '>';
fragment OP_GTE         : '>=';
COMPARE_OPERATION       : OP_EQ | OP_NE | OP_LT | OP_LTE | OP_GE | OP_GTE;

LOWER_NAME              : LOWER ANYCHAR*;
UPPER_NAME              : UPPER ANYCHAR*;
INTEGER                 : DIGIT+;
ASSIGN                  : '<-'|'=';

% KEYWORD_OBJECT         : 'Object' ;
% KEYWORD_TIME           : 'time' | 'timestep' ;
KEYWORD_TRUE            : 'true' ;
KEYWORD_FALSE           : 'false' ;

TYPE                    : UPPER_NAME ;
OBJECT                  : LOWER_NAME ;
VARIABLE                : '?' LOWER_NAME;
CLASS                   : (ANYCHAR*'.')* UPPER LETTER*;
 
LINECOMMENT             : '//' .*? '\r'? '\n' -> skip;
COMMENT                 : '/*' .*? '*/' -> skip;
WS                      : [ \n\t\r]+ -> skip;



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

typeDefinition : TYPE '-' TYPE ;


// OBJECT DEFINITIONS

objectsSection : 'objects' '{' (objectDefinition ',')* objectDefinition? '}' ;

objectDefinition : OBJECT '-' TYPE ;



// AGENT DEFINITIONS

agentsSection : 'agents' '{' (agentDef ',')* agentDef? '}' ;

agentDef : systemAgent | environmentAgent ;

systemAgent : LOWER_NAME ;

environmentAgent : LOWER_NAME '{' CLASS '}' ;


// PASSIVE AGENT DEFINITIONS

passiveSection : 'passive' '{' (passiveDef ',')* passiveDef? '}' ;

passiveDef : LOWER_NAME ;


// PREDICATES


predicate : LOWER_NAME parameterList ;
parameterList : '(' (parameter ',')* parameter? ')' ;
parameter : OBJECT | VARIABLE | TYPE ;

assignment : predicate ASSIGN (KEYWORD_FALSE | KEYWORD_TRUE | INTEGER | OBJECT);




// CONSTANTS

constantsSection : 'constants' '{' (assignment ',')* assignment? '}' ;



// FLUENTS DEFINITIONS

fluentsSection : 'fluents' '{' (fluentDefinition ',')* fluentDefinition? '}' ;

fluentDefinition : predicate '-' TYPE ;



// INITIAL STATE DEFINITION

initiallySection : 'initially' (startStateDef | '{' (startStateDef ',')* startStateDef ','? '}') ;

startStateDef : initiallyDef | kripkeModel ;

initiallyDef : '{' (beliefFormula ',')* beliefFormula? '}' ;

kripkeModel : '{' (kripkeFormula ',')* kripkeFormula? '}' ;

kripkeFormula
    : assignment
    | relationType OBJECT ASSIGN '{' ('('fromWorld','toWorld')'',')* ('('fromWorld','toWorld')')? '}'
    ;
relationType : 'B_' | 'K_' ;
fromWorld : LOWER_NAME;
toWorld : LOWER_NAME;


// FORMULAE

atom : predicate | KEYWORD_FALSE | KEYWORD_TRUE | INTEGER | OBJECT;

atomicFormula                                                                          % predicate(s) must be:
    : INTEGER   INEQUALITY INTEGER                               # atomicInequality
    | predicate INEQUALITY INTEGER                               # atomicInequality    % Integer
    | INTEGER   INEQUALITY predicate                             # atomicInequality    % Integer
    | predicate INEQUALITY predicate                             # atomicInequality    % Integers
    | atom '==' atom                                             # atomicEqual         % of the same type
    | predicate                                                  # atomicPredicate     % Boolean
    | KEYWORD_FALSE                                              # atomicFalse
    | KEYWORD_TRUE                                               # atomicTrue
    ;

fluentFormula 
    : predicateFormula                                           # fluentPredicate
    | '(' fluentFormula ')'                                      # fluentParens
    | '~' fluentFormula                                          # fluentNot
    | fluentFormula '&' fluentFormula ('&' fluentFormula)*       # fluentAnd
    | fluentFormula '|' fluentFormula ('|' fluentFormula)*       # fluentOr
    | fluentFormula '==' fluentFormula ('==' fluentFormula)*     # fluentEquals
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

