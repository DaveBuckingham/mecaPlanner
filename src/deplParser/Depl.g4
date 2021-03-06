grammar Depl;

fragment LOWER          : [a-z];
fragment UPPER          : [A-Z];
fragment LETTER         : LOWER | UPPER;
fragment ANYCHAR        : LETTER | DIGIT | '_';
fragment DIGIT          : [0-9];

OP_AND                  : '&'|'&&';
OP_OR                   : '|'|'||';
OP_IMPLIES              : '->';
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


// THE MAIN STRUCTURE OF A DEPL FILE

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
    | OP_NOT localFormula                                               # localNot
    | localFormula OP_AND localFormula (OP_AND localFormula)*           # localAnd
    | localFormula OP_OR  localFormula (OP_OR localFormula)*            # localOr
    | localFormula OP_IMPLIES localFormula                              # localImplies
    ;

beliefFormula 
    : localFormula                                               # beliefLocalFormula
    | '(' beliefFormula ')'                                      # beliefParens
    | OP_NOT beliefFormula                                       # beliefNot
    | 'B' '[' groundableObject ']' '(' beliefFormula ')'         # beliefBelieves
    | 'P' '[' groundableObject ']' '(' beliefFormula ')'         # beliefPossibly
    | 'K' '[' groundableObject ']' '(' beliefFormula ')'         # beliefKnows
    | 'C' '(' beliefFormula ')'                                  # beliefCommon
    | beliefFormula OP_AND beliefFormula                         # beliefAnd
    | beliefFormula OP_OR beliefFormula (OP_OR beliefFormula)*   # beliefOr
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

timeConstraint : KEYWORD_TIME inequality INTEGER;


// INITIAL STATE DEFINITION

initiallySection : 'initially' ( startStateDef | '{' (startStateDef ','?)* '}' ) ;

startStateDef : '{' (initiallyDef | kripkeModel) '}' ;

initiallyDef : (beliefFormula ',')* beliefFormula ;

kripkeModel : (kripkeWorld ','?)+ (kripkeRelation ','?)+ ;

kripkeWorld : STAR? LOWER_NAME ASSIGN '{' (fluent ',')* fluent? '}' ;

kripkeRelation : relationType '[' objectName ']' ASSIGN
                 '{' ( '(' fromWorld ',' toWorld ')' ','? )* '}' ;

relationType : 'B' | 'K' ;
fromWorld : LOWER_NAME;
toWorld : LOWER_NAME;


// OPTIONAL POST STATE

postSection : 'post' startStateDef ;


// GOALS DEFINITION

goalsSection : 'goals' '{' (goal ',')* goal? '}' ;
goal : beliefFormula | timeConstraint ;


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
observesActionField     : 'observes'     variableDefList '{' groundableObject ('if' condition)? '}' ;
awareActionField        : 'aware'        variableDefList '{' groundableObject ('if' condition)? '}' ;
determinesActionField   : 'determines'   variableDefList '{' localFormula ('if' condition)? '}' ;
announcesActionField    : 'announces'    variableDefList '{' beliefFormula ('if' condition)? '}' ;
causesActionField       : 'causes'       variableDefList '{' OP_NOT? fluent ('if' condition)? '}' ;

condition : localFormula ;

