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
COMPARE                 : OP_EQ | OP_NE | OP_LT | OP_LTE | OP_GE | OP_GTE;

LOWER_NAME              : LOWER ANYCHAR*;
UPPER_NAME              : UPPER ANYCHAR*;
INTEGER                 : DIGIT+;
ASSIGN                  : '<-'|'=';

// KEYWORD_TIME           : 'time' | 'timestep' ;
KEYWORD_TRUE            : 'true' ;
KEYWORD_FALSE           : 'false' ;
KEYWORD_BOOLEAN         : 'Boolean' ;
KEYWORD_INTEGER         : 'Integer' ;
KEYWORD_OBJECT          : 'Object' ;

FLUENT_TYPE             : KEYWORD_BOOLEAN | KEYWORD_INTEGER | KEYWORD_OBJECT ;
OBJECT_TYPE             : UPPER_NAME ;
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

typeDefinition : OBJECT_TYPE '-' OBJECT_TYPE ;


// OBJECT DEFINITIONS

objectsSection : 'objects' '{' (objectDefinition ',')* objectDefinition? '}' ;

objectDefinition : OBJECT '-' OBJECT_TYPE ;



// AGENT DEFINITIONS

agentsSection : 'agents' '{' (agentDef ',')* agentDef? '}' ;

agentDef : systemAgent | environmentAgent ;

systemAgent : OBJECT ;

environmentAgent : OBJECT '{' CLASS '}' ;


// PASSIVE AGENT DEFINITIONS

passiveSection : 'passive' '{' (passiveDef ',')* passiveDef? '}' ;

passiveDef : OBJECT ;




// FLUENTS DEFINITIONS

groundableObject : OBJECT | VARIABLE ;
expandableObject : OBJECT | OBJECT_TYPE

expandableFluent : LOWER_NAME '[' (expandableObject ',')* expandableObject? ']' ;
fluentDef : expandableFluent '-' FLUENT_TYPE ;
fluentsSection : 'fluents' '{' (fluentDef ',')* fluentDef? '}' ;



// CONSTANTS

valueAssignment : expandableFluent ASSIGN value;
constantsSection : 'constants' '{' (valueAssignment ',')* valueAssignment? '}' ;


// FORMULAE

fluent : LOWER_NAME '[' (groundableObject ',')* groundableObject? ']' ;
value : KEYWORD_FALSE | KEYWORD_TRUE | INTEGER | groundableObject;
atom : fluent | value ;

integerFormula  // EVALUATE TO INTEGER
    : atom                                                       # integerAtom
    | '(' integerFormula ')'                                     # integerParens
    | integerFormula '+' integerFormula                          # integerAdd
    | integerFormula '-' integerFormula                          # integerSubtract
    | integerFormula '*' integerFormula                          # integerMultiply
    | integerFormula '/' integerFormula                          # integerDivide
    | integerFormula '%' integerFormula                          # integerModulo
    ;

fluentFormula  // EVALUATE TO BOOLEAN
    : atom                                                       # fluentAtom
    | '(' fluentFormula ')'                                      # fluentParens
    | (fluentFormula|integerFormula) COMPARE (fluentFormula|integerFormula) # fluentCompare
    | '~' fluentFormula                                          # fluentNot
    | fluentFormula '&' fluentFormula ('&' fluentFormula)*       # fluentAnd
    | fluentFormula '|' fluentFormula ('|' fluentFormula)*       # fluentOr
    ;

beliefFormula 
    : fluentFormula                                              # beliefFluent
    | '(' beliefFormula ')'                                      # beliefParens
    | '~' beliefFormula                                          # beliefNot
    | beliefFormula '&' beliefFormula ('&' beliefFormula)*       # beliefAnd
    | beliefFormula '|' beliefFormula ('|' beliefFormula)*       # beliefOr
    | 'C' '(' beliefFormula ')'                                  # beliefCommon
    | 'B_' groundableObject '(' beliefFormula ')'                      # beliefBelieves
    ;


// INITIAL STATE DEFINITION

initiallySection : 'initially' (startStateDef | '{' (startStateDef ',')* startStateDef ','? '}') ;

//startStateDef : initiallyDef | kripkeModel ;
startStateDef : kripkeModel ;

//initiallyDef : '{' (beliefFormula ',')* beliefFormula? '}' ;

kripkeModel : '{' (kripkeFormula ',')* kripkeFormula? '}' ;

kripkeFormula
    : LOWER_NAME ASSIGN '{' (valueAssignment ',')* valueAssignment? '}'
    | relationType OBJECT ASSIGN '{' ('('fromWorld','toWorld')'',')* ('('fromWorld','toWorld')')? '}'
    ;
relationType : 'B_' | 'K_' ;
fromWorld : LOWER_NAME;
toWorld : LOWER_NAME;



// GOALS DEFINITION

goalsSection : 'goals' '{' (goal ',')* goal? '}' ;
goal : beliefFormula ;



// ACTION DEFINITIONS

actionsSection : 'actions' '{' (actionDefinition ','?)* '}' ;

actionDefinition : LOWER_NAME variableDefList '{' (actionField ','?)* '}' ;
variableDefList : '(' (variableDef ',')* variableDef? ')' ;
variableDef : VARIABLE '-' OBJECT_TYPE ;

formulaAssignment : groundableFluent ASSIGN fluentFormula | integerFormula;

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
preconditionActionField : 'precondition' variableDefList '{' fluentFormula '}' ;
observesActionField     : 'observes'     variableDefList '{' groundableObject ('if' fluentFormula)? '}' ;
awareActionField        : 'aware'        variableDefList '{' groundableObject ('if' fluentFormula)? '}' ;
determinesActionField   : 'determines'   variableDefList '{' fluentFormula '}' ;
announcesActionField    : 'announces'    variableDefList '{' beliefFormula '}' ;
causesActionField       : 'causes'       variableDefList '{' formulaAssignment ('if' fluentFormula)? '}' ;

