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

agent :  objectName;

groundableObject : objectName | VARIABLE ;
groundableAgent :  groundableObject;
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
    | 'K' '[' groundableAgent ']' '(' formula ')'                       # knowsFormula
    | 'S' '[' groundableAgent ']' '(' formula ')'                       # safeFormula
    | 'B' '[' groundableAgent ']' '(' formula ')'                       # believesFormula
    | 'K\'' '[' groundableAgent ']' '(' formula ')'                     # knowsDualFormula
    | 'S\'' '[' groundableAgent ']' '(' formula ')'                     # safeDualFormula
    | 'B\'' '[' groundableAgent ']' '(' formula ')'                     # believesDualFormula
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

relation : agent ASSIGN '{' ( relate ','? )* '}' ;

relate : '(' from=LOWER_NAME ',' to=LOWER_NAME ')' ;



// OPTIONAL POST STATE

postSection : 'post' startStateDef ;


// GOALS DEFINITION

goalsSection : 'goals' '{' (goal ',')* goal? '}' ;
goal : formula | timeConstraint ;


// ACTION DEFINITIONS


actionsSection : 'actions' ( eventDef | '{' (eventDef ','?)* '}' ) ;
eventDef : '{' (actionDefinition | eventModelDef) '}' ;

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
determinesActionField   : 'determines'   variableDefList '{' determined=formula ('if' condition=formula)? '}' ;
announcesActionField    : 'announces'    variableDefList '{' announced=formula ('if' condition=formula)? '}' ;
causesActionField       : 'causes'       variableDefList '{' OP_NOT? fluent ('if' condition=formula)? '}' ;



//action_name( { *e1({p},{q},{r,s}), e2({},{p},{}), eventName3({},{},{q}) }, agent1{(e1,e2),(e3,e1)}, agent2{(e1,e1)} ):

eventModelDef : LOWER_NAME '(' '{' (event ','?)+ '}' ',' (eventRelation ',')* eventRelation? ')' ;
atoms         : '{' (fluent ',')* fluent? '}' ;
event         : STAR? LOWER_NAME '(' formula ',' atoms ',' atoms ')' ;
eventRelation : agent '{' (edge ',')* edge? '}' ;
edge          : '(' from=LOWER_NAME ',' to=LOWER_NAME (',' formula)? ')' ;







