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

// KEYWORD_OBJECT         : 'Object' ;
// KEYWORD_TIME           : 'time' | 'timestep' ;
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

systemAgent : OBJECT ;

environmentAgent : OBJECT '{' CLASS '}' ;


// PASSIVE AGENT DEFINITIONS

passiveSection : 'passive' '{' (passiveDef ',')* passiveDef? '}' ;

passiveDef : OBJECT ;




// FLUENTS DEFINITIONS

expandableFluent : LOWER_NAME '[' ((OBJECT|TYPE) ',')* (OBJECT|TYPE)? ']' ;
fluentDef : expandableFluent '-' TYPE ;
fluentsSection : 'fluents' '{' (fluentDef ',')* fluentDef? '}' ;




// CONSTANTS

constantAssignment : expandableFluent ASSIGN value;
constantsSection : 'constants' '{' (constantAssignment ',')* constantAssignment? '}' ;





// INITIAL STATE DEFINITION

initiallySection : 'initially' (startStateDef | '{' (startStateDef ',')* startStateDef ','? '}') ;

startStateDef : initiallyDef | kripkeModel ;

initiallyDef : '{' (beliefFormula ',')* beliefFormula? '}' ;

kripkeModel : '{' (kripkeFormula ',')* kripkeFormula? '}' ;

kripkeFormula
    : LOWER_NAME ASSIGN '{' (expandableFluent ',')* expandableFluent? '}'
    | relationType OBJECT ASSIGN '{' ('('fromWorld','toWorld')'',')* ('('fromWorld','toWorld')')? '}'
    ;
relationType : 'B_' | 'K_' ;
fromWorld : LOWER_NAME;
toWorld : LOWER_NAME;


// FORMULAE

groundable : OBJECT | VARIABLE ;
fluent : LOWER_NAME '[' (groundable ',')* groundable? ']' ;
value : KEYWORD_FALSE | KEYWORD_TRUE | INTEGER | groundable;
atom : fluent | value ;

// IF JUST AN ATOM, MUST BE TRUE, FALSE, OR PREDICATE
// IF COMPARE, ATOMS MUST BE OR REFERENCE SAME PREDICATE TYPE.
// >,>=,<,<= ARE ONLY VALID FOR INTEGERS

fluentFormula 
    : atom                                                       # fluentAtom
    | atom COMPARE atom                                          # fluentCompare
    | '(' fluentFormula ')'                                      # fluentParens
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
    | 'B_' groundable '(' beliefFormula ')'                      # beliefBelieves
    ;



// GOALS DEFINITION

goalsSection : 'goals' '{' (goal ',')* goal? '}' ;

goal : beliefFormula ;






// ACTION DEFINITIONS

actionsSection : 'actions' '{' (actionDefinition ','?)* '}' ;

actionDefinition : LOWER_NAME parameterList? '{' (actionField ','?)* '}' ;
parameterList : '(' (parameter ',')* parameter? ')' ;
parameter : VARIABLE '-' TYPE ;
onticAssignment : expandableFluent ASSIGN value ;

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

ownerActionField        : 'owner' '{' groundable '}' ;
costActionField         : 'cost'  '{' INTEGER '}' ;
preconditionActionField : 'precolndition' parameterList? '{' fluentFormula '}' ;
observesActionField     : 'observes'      parameterList? '{' groundable ('if' fluentFormula)? '}' ;
awareActionField        : 'aware'         parameterList? '{' groundable ('if' fluentFormula)? '}' ;
causesActionField       : 'causes'        parameterList? '{' fluent ASSIGN atom ('if' fluentFormula)? '}' ;
determinesActionField   : 'determines'    parameterList? '{' fluentFormula '}' ;
announcesActionField    : 'announces'     parameterList? '{' beliefFormula '}' ;

