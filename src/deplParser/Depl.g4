grammar Depl;

fragment LOWER          : [a-z];
fragment UPPER          : [A-Z];
fragment LETTER         : LOWER | UPPER;
fragment ANYCHAR        : LETTER | DIGIT | '_';
fragment DIGIT          :  [0-9];

OP_AND                  : '&'|'&&';
OP_OR                   : '|'|'||';
OP_NOT                  : '!'|'~' ;

KEYWORD_TRUE            : 'true'|'True' ;
KEYWORD_FALSE           : 'false'|'False' ;
KEYWORD_TIME            : 'timestep'|'Timestep' ;

INTEGER                 : DIGIT+;
ASSIGN                  : '<-';

VARIABLE                : '?' LOWER_NAME;

LOWER_NAME              : LOWER ANYCHAR*;
UPPER_NAME              : UPPER ANYCHAR*;

STAR                    : '*';

LINECOMMENT             : '//' .*? '\r'? '\n' -> skip;
COMMENT                 : '/*' .*? '*/' -> skip;
WS                      : [ \n\t\r]+ -> skip;

objectName              : LOWER_NAME ;
objectType              : UPPER_NAME ;


init :
    typesSection
    objectsSection
    agentsSection
    passiveSection?
    fluentsSection
    constantsSection?
    initiallySection
    postSection?
    goalsSection
    actionsSection
;


// TYPE DEFINITIONS

typesSection : 'types' '{' (typeDefinition ',')* typeDefinition? '}' ;
typeDefinition : objectType '-' objectType ;


// OBJECTNAME DEFINITIONS

objectsSection : 'objects' '{' (objectDefinition ',')* objectDefinition? '}' ;
objectDefinition : objectName '-' objectType ;


// AGENT DEFINITIONS

agentsSection : 'agents' '{' (agentDef ',')* agentDef? '}' ;
agentDef : objectName ('{' UPPER_NAME '}')? ;


// OPTIONAL PASSIVE AGENT DEFINITIONS

passiveSection : 'passive' '{' (passiveDef ',')* passiveDef? '}' ;
passiveDef : objectName ;


// FLUENTS DEFINITIONS

groundableObject : objectName | VARIABLE ;
expandableObject : objectName | objectType;
expandableFluent : LOWER_NAME '(' (expandableObject ',')* expandableObject? ')' ;
fluentsSection   : 'fluents' '{' (expandableFluent ',')* expandableFluent? '}' ;


// CONSTANTS

constant : OP_NOT? expandableFluent ;
constantsSection : 'constants' '{' (constant ',')* constant? '}' ;


// FORMULAE

fluent 
    : LOWER_NAME '(' (groundableObject ',')* groundableObject? ')'
    | '(' fluent ')'
    ;

localFormula
    : fluent                                                            # localFluent
    | KEYWORD_TRUE                                                      # localLiteralTrue
    | KEYWORD_FALSE                                                     # localLiteralFalse
    | '(' localFormula ')'                                              # localParens
    | localFormula OP_OR  localFormula (OP_OR localFormula)*            # localOr
    | localFormula OP_AND localFormula (OP_AND localFormula)*           # localAnd
    | OP_NOT localFormula                                               # localNot
    ;

beliefFormula 
    : localFormula                                               # beliefLocalFormula
    | '(' beliefFormula ')'                                      # beliefParens
    | beliefFormula OP_OR beliefFormula (OP_OR beliefFormula)*   # beliefOr
    | beliefFormula OP_AND beliefFormula                         # beliefAnd
    | OP_NOT beliefFormula                                       # beliefNot
    | 'B' '[' groundableObject ']'  beliefFormula                # beliefBelieves
    | 'P' '[' groundableObject ']'  beliefFormula                # beliefPossibly
    | 'K' '[' groundableObject ']'  beliefFormula                # beliefKnows
    | 'C' '(' beliefFormula ')'                                  # beliefCommon
    ;

inequality
    : '=='                # inequalityEq
    | '='                 # inequalityEq2
    | '!='                # inequalityNe
    | '~='                # inequalityNe2
    | '<'                 # inequalityLt
    | '<='                # inequalityLte
    | '>'                 # inequalityGt
    | '>='                # inequalityGte
    ;

temporalConstraint : KEYWORD_TIME inequality INTEGER;

timeFormula
    : beliefFormula                                              # timeBelief
    | temporalConstraint                                         # timeConstraint
    | OP_NOT timeFormula                                         # timeNot
    | timeFormula OP_AND timeFormula (OP_AND timeFormula)*       # timeAnd
    | timeFormula OP_OR timeFormula (OP_OR timeFormula)*         # timeOr
    ;


// INITIAL STATE DEFINITION

initiallySection : 'initially' '{' (startStateDef ',')* startStateDef ','? '}' ;

startStateDef : initiallyDef | kripkeModel ;
//startStateDef : '{' initiallyDef '}' ;
//startStateDef : '{' kripkeModel '}' ;

initiallyDef : beliefFormula ;

kripkeModel : '{' (kripkeWorld ','?)+ (kripkeRelation ','?)+ '}' ;

kripkeWorld : STAR? LOWER_NAME ASSIGN '{' (fluent ',')* fluent? '}' ;

kripkeRelation : relationType '[' objectName ']' ASSIGN
                 '{' ('('fromWorld','toWorld')')* '}' ;

relationType : 'B' | 'K' ;
fromWorld : LOWER_NAME;
toWorld : LOWER_NAME;


// OPTIONAL POST STATE

postSection : 'post' startStateDef ;


// GOALS DEFINITION

goalsSection : 'goals' '{' (goal ',')* goal? '}' ;
goal : timeFormula ;


// ACTION DEFINITIONS

actionsSection : 'actions' '{' (actionDefinition ','?)* '}' ;
actionDefinition : LOWER_NAME variableDefList '{' (actionField ','?)* '}' ;
variableDefList : ('(' (variableDef ',')* variableDef? ')')? ;
variableDef : VARIABLE '-' objectType ;

actionField
    : ownerActionField
    | costActionField
    | preconditionActionField
    | observesActionField
    | awareActionField
    | causesActionField
    | determinesActionField
    | announcesActionField
    ;

ownerActionField        : 'owner' '{' groundableObject '}' ;
costActionField         : 'cost'  '{' INTEGER '}' ;
preconditionActionField : 'precondition' variableDefList '{' localFormula '}' ;
observesActionField     : 'observes'     variableDefList '{' groundableObject ('if' localFormula)? '}' ;
awareActionField        : 'aware'        variableDefList '{' groundableObject ('if' localFormula)? '}' ;
determinesActionField   : 'determines'   variableDefList '{' localFormula '}' ;
announcesActionField    : 'announces'    variableDefList '{' beliefFormula '}' ;
causesActionField       : 'causes'       variableDefList '{' OP_NOT? fluent ('if' localFormula)? '}' ;

