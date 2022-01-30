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

ORDER                   : '>';

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

formula
    : fluent                                                            # fluentFormula
    | KEYWORD_TRUE                                                      # trueFormula
    | KEYWORD_FALSE                                                     # falseFormula
    | '(' formula ')'                                                   # parensFormula
    | OP_NOT formula                                                    # notFormula
    | formula OP_AND formula (OP_AND formula)*                          # andFormula
    | formula OP_OR  formula (OP_OR formula)*                           # orFormula
    | formula OP_IMPLIES formula                                        # impliesFormula
    | 'K' '[' groundableObject ']' '(' formula ')'                      # knowsFormula
    | 'S' '[' groundableObject ']' '(' formula ')'                      # safeFormula
    | 'B' '[' groundableObject ']' '(' formula ')'                      # believesformula
    | 'K\'' '[' groundableObject ']' '(' formula ')'                    # dualKnowsFormula
    | 'S\'' '[' groundableObject ']' '(' formula ')'                    # dualSafeFormula
    | 'B\'' '[' groundableObject ']' '(' formula ')'                    # dualBelievesFormula
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

startStateDef : '{' (initiallyDef | model) '}' ;

initiallyDef : (formula ',')* formula ;

model : (world ','?)+ (relation ','?)+ ;

world : STAR? LOWER_NAME ASSIGN '{' (fluent ',')* fluent? '}' ;

relation : agent=objectName ASSIGN '{' ( '(' from=LOWER_NAME ',' to=LOWER_NAME ')' ','? )* '}' ;



// OPTIONAL POST STATE

postSection : 'post' startStateDef ;


// GOALS DEFINITION

goalsSection : 'goals' '{' (goal ',')* goal? '}' ;
goal : formula | timeConstraint ;


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
preconditionActionField : 'precondition' variableDefList '{' formula '}' ;
observesActionField     : 'observes'     variableDefList '{' groundableObject ('if' condition=formula)? '}' ;
awareActionField        : 'aware'        variableDefList '{' groundableObject ('if' condition=formula)? '}' ;
determinesActionField   : 'determines'   variableDefList '{' formula ('if' condition=formula)? '}' ;
announcesActionField    : 'announces'    variableDefList '{' formula ('if' condition=formula)? '}' ;
causesActionField       : 'causes'       variableDefList '{' OP_NOT? fluent ('if' condition=formula)? '}' ;


