grammar Depl;

LOWER_NAME     : LOWER ANYCHAR*;
UPPER_NAME     : UPPER ANYCHAR*;
CLASS          : (ANYCHAR*'.')* UPPER LETTER*;
VARIABLE       : '?' LOWER_NAME;
INTEGER        : DIGIT+;

KEYWORD_OBJECT : 'Object' ;
KEYWORD_TRUE   : 'true' ;
KEYWORD_FALSE  : 'false' ;

fragment LOWER: [a-z];
fragment UPPER: [A-Z];
fragment LETTER: LOWER | UPPER;
fragment ANYCHAR: LETTER | DIGIT | '_';
fragment DIGIT:  [0-9];

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

type : UPPER_NAME | KEYWORD_OBJECT ;


// AGENT DEFINITIONS

agentsSection : 'agents' '{' (agentDef ',')* agentDef? '}' ;

agentDef : systemAgent | environmentAgent ;

systemAgent : LOWER_NAME ;

environmentAgent : LOWER_NAME '{' CLASS '}' ;


// PASSIVE AGENT DEFINITIONS

passiveSection : 'passive' '{' (passiveDef ',')* passiveDef? '}' ;

passiveDef : LOWER_NAME ;


// CONSTANTS DEFINITIONS

constantsSection : 'constants' '{' (constantAssignment ',')* constantAssignment? '}' ;

constantAssignment : predicateDefinition ('<-'|'=') | (KEYWORD_FALSE | KEYWORD_TRUE | INTEGER | LOWER_NAME);

predicateDefinition : LOWER_NAME parameterList ;



// FLUENTS DEFINITIONS

fluentsSection : 'fluents' '{' (fluentDefinition ',')* fluentDefinition? '}' ;

fluentDefinition : predicateDefinition '-' type ;

parameterList : '(' (parameter ',')* parameter? ')' ;

parameter : LOWER_NAME | VARIABLE - type ;


// INITIAL STATE DEFINITION

initiallySection : 'initially' (startStateDef | '{' (startStateDef ',')* startStateDef ','? '}') ;

startStateDef : initiallyDef | kripkeModel ;

initiallyDef : '{' (beliefFormula ',')* beliefFormula? '}' ;

kripkeModel : '{' (kripkeFormula ',')* kripkeFormula? '}' ;


kripkeFormula
    : NAME '=' '{' (atom ',')* atom? '}'                                                               # kripkeWorld
    | relationType NAME '=' '{' ('('fromWorld','toWorld')'',')* ('('fromWorld','toWorld')')? '}'   # kripkeRelation
    ;

relationType : 'B_' | 'K_' ;
fromWorld : NAME;
toWorld : NAME;



atom : NAME parameterList? | '(' NAME parameterList? ')';


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




// GOALS DEFINITION

goalsSection : 'goals' '{' (goal ',')* goal? '}' ;

goal : beliefFormula ;


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
causesifActionField
    : 'causesif' variableList? '{' literal ',' fluentFormula '}'
    | 'causes' variableList? '{' literal 'if' fluentFormula '}'
    ;
determinesActionField   : 'determines' variableList? '{' fluentFormula '}' ;
announcesActionField    : 'announces' variableList? '{' beliefFormula '}' ;

