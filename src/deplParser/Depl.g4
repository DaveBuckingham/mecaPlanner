grammar Depl;

fragment LOWER          : [a-z];
fragment UPPER          : [A-Z];
fragment LETTER         : LOWER | UPPER;
fragment ANYCHAR        : LETTER | DIGIT | '_';
fragment DIGIT          :  [0-9];

fragment OP_LT          : '<';
fragment OP_LTE         : '<=';
fragment OP_GE          : '>';
fragment OP_GTE         : '>=';
COMPARE                 : OP_LT | OP_LTE | OP_GE | OP_GTE;
OP_EQ                   : '==';
OP_AND                  : '&'|'&&';
OP_OR                   : '|'|'||';
OP_NOT                  : '!'|'~' ;

KEYWORD_TRUE            : 'true' ;
KEYWORD_FALSE           : 'false' ;
KEYWORD_TIME            : 'Timestep' ;

INTEGER                 : DIGIT+;
ASSIGN                  : '<-';

VARIABLE                : '?' LOWER_NAME;

LOWER_NAME              : LOWER ANYCHAR*;
UPPER_NAME              : UPPER ANYCHAR*;

 
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
    constantsSection?
    fluentsSection
    initiallySection
    postSection?
    goalsSection
    actionsSection
;


// TYPE DEFINITIONS

typesSection : 'types' '{' (typeDefinition ',')* typeDefinition? '}' ;

typeDefinition : objectType '-' objectType ;


// objectName DEFINITIONS

objectsSection : 'objects' '{' (objectDefinition ',')* objectDefinition? '}' ;

objectDefinition : objectName '-' objectType ;



// AGENT DEFINITIONS

agentsSection : 'agents' '{' (agentDef ',')* agentDef? '}' ;

agentDef : objectName ('{' UPPER_NAME '}')? ;



// PASSIVE AGENT DEFINITIONS

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


localFormula  // EVALUATE TO BOOLEAN
    : fluent                                                            # localFluent
    | KEYWORD_TRUE                                                      # localLiteralTrue
    | KEYWORD_FALSE                                                     # localLiteralFalse
    | OP_NOT localFormula                                               # localNot
    | '(' localFormula ')'                                              # localParens
    | localFormula OP_AND localFormula (OP_AND localFormula)*           # localAnd
    | localFormula OP_OR  localFormula (OP_OR localFormula)*            # localOr
    ;

beliefFormula 
    : localFormula                                               # beliefLocalFormula
    | '(' beliefFormula ')'                                      # beliefParens
    | OP_NOT beliefFormula                                       # beliefNot
    | beliefFormula OP_AND beliefFormula                         # beliefAnd
    | beliefFormula OP_OR beliefFormula (OP_OR beliefFormula)*   # beliefOr
    | 'C' '(' beliefFormula ')'                                  # beliefCommon
    | 'B' '[' groundableObject ']' '(' beliefFormula ')'         # beliefBelieves
    | 'P' '[' groundableObject ']' '(' beliefFormula ')'         # beliefPossibly
    ;

inequality
    : '=='                # inequalityEq
    | '!='                # inequalityNe
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

initiallySection : 'initially' (startStateDef | '{' (startStateDef ',')* startStateDef ','? '}') ;

startStateDef : '{' initiallyDef '}' | '{' kripkeModel '}' ;
//startStateDef : '{' initiallyDef '}' ;
//startStateDef : '{' kripkeModel '}' ;

initiallyDef : (beliefFormula ',')* beliefFormula? ;

kripkeModel : (kripkeWorld ','?)+ (kripkeRelation ','?)+ ;

kripkeWorld : LOWER_NAME ASSIGN '{' (fluent ',')* fluent? '}' ;

kripkeRelation : relationType '[' objectName ']' ASSIGN
                 '{' ('('fromWorld','toWorld')'',')* ('('fromWorld','toWorld')')? '}' ;

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

