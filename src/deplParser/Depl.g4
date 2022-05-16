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
    startStateSection?
    initiallySection?
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

startStateSection : 'start' '{' ((model|stateDef) ','?)* '}' ;

model : '(' (world ','?)+ (relation ','?)+ ')' ;
world : STAR? LOWER_NAME ASSIGN? '{' (fluent ',')* fluent? '}' ;
relation : agent ASSIGN? '{' ( relate ','? )* '}' ;
relate : '(' from=LOWER_NAME ',' to=LOWER_NAME ')'
       | from=LOWER_NAME'-'to=LOWER_NAME
       ;


stateDef : '(' (fluent ','?)* (stateAssertion ','?)* ')';
stateAssertion : doubts='?' '[' (agent ',')* agent ']' '(' fluent ')'
               | believes='B' '[' (agent ',')* agent ']' '(' OP_NOT? fluent ')'
               | knows='K' '[' (agent ',')* agent ']' '(' formula ')'
               ;


// INITIAL FORMULAE

initiallySection : 'initially' '{' (formula ',')* formula? '}' ;




// GOALS DEFINITION

goalsSection : 'goals' '{' (goal ',')* goal? '}' ;
goal : formula | timeConstraint ;


// ACTION DEFINITIONS

variableDefList : ('<' ((variableDef|variableInequality) ',')* (variableDef|variableInequality)? '>')? ;
variableDef : VARIABLE '-' objectType ;
variableInequality : lhs=VARIABLE '!=' rhs=VARIABLE ;


actionsSection : 'actions' '{' (eventModelDef | actionDef ','?)* '}' ;

actionDef : actionScope=variableDefList LOWER_NAME '('
    'owner' owner=groundableObject ','
    ('cost' cost=INTEGER ',')?
    (preconditionScope=variableDefList 'precondition' precondition=formula ',')?
    ((observesDef|awareDef) ',')*
    ((determinesDef|announcesDef|causesDef) ',')* (determinesDef|announcesDef|causesDef)?
')' ;

observesDef : variableDefList 'observes' groundableObject ('if' condition=formula)?;
awareDef    : variableDefList 'aware' groundableObject ('if' condition=formula)?;

determinesDef : variableDefList  'determines'   determined=formula ('if' condition=formula)?;
announcesDef  : variableDefList  'announces'    announced=formula ('if' condition=formula)?;
causesDef     : variableDefList  'causes'       literal (OP_AND literal)* ('if' condition=formula)?;


literal : OP_NOT? fluent;



eventModelDef : LOWER_NAME '(' '{' (event ','?)+ '}' ',' (eventRelation ',')* eventRelation? ')' ;
event         : STAR? LOWER_NAME '(' formula ',' '{' (assignment ',')* assignment? '}' ')'
              | STAR? LOWER_NAME '(' formula ',' deletes=atoms ',' adds=atoms ')'
              ;
assignment    : fluent ASSIGN formula ;
atoms         : '{' (fluent ',')* fluent? '}' ;
eventRelation : agent '{' (edge ',')* edge? '}' ;
edge          : '(' from=LOWER_NAME ',' to=LOWER_NAME (',' formula)? ')'
              | from=LOWER_NAME'-'(formula'-')?to=LOWER_NAME
              ;



