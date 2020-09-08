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
OP_NE                   : '~='|'!=';
OP_AND                  : '&'|'&&';
OP_OR                   : '|'|'||';
OP_NOT                  : '~'|'!' ;

KEYWORD_TRUE            : 'true' ;
KEYWORD_FALSE           : 'false' ;
KEYWORD_BOOLEAN         : 'Boolean' ;
KEYWORD_INTEGER         : 'Integer' ;

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

groundableObject : objectName | fluent | VARIABLE ;
expandableObject : objectName | objectType;

expandableFluent : LOWER_NAME '(' (expandableObject ',')* expandableObject? ')' ;
fluentType       : KEYWORD_BOOLEAN | KEYWORD_INTEGER | objectType ;
fluentDef        :  expandableFluent '-' fluentType ;
fluentsSection   : 'fluents' '{' (fluentDef ',')* fluentDef? '}' ;



// CONSTANTS

valueAssignment : expandableFluent ASSIGN value;
constantsSection : 'constants' '{' (valueAssignment ',')* valueAssignment? '}' ;


// FORMULAE

fluent : LOWER_NAME '(' (groundableObject ',')* groundableObject? ')' ;
fluentFormula : integerFormula | booleanFormula | groundableObject ;

value 
    : KEYWORD_FALSE           # valueFalse
    | KEYWORD_TRUE            # valueTrue
    | INTEGER                 # valueInteger
    | objectName              # valueObject
    ;

integerFormula  // EVALUATE TO INTEGER
    : fluent                                                     # integerFluent
    | INTEGER                                                    # integerLiteral
    | '(' integerFormula ')'                                     # integerParens
    | integerFormula '+' integerFormula                          # integerAdd
    | integerFormula '-' integerFormula                          # integerSubtract
    | integerFormula '*' integerFormula                          # integerMultiply
    | integerFormula '/' integerFormula                          # integerDivide
    | integerFormula '%' integerFormula                          # integerModulo
    ;

booleanFormula  // EVALUATE TO BOOLEAN
    : fluent                                                            # booleanFluent
    | KEYWORD_TRUE                                                      # booleanLiteralTrue
    | KEYWORD_FALSE                                                     # booleanLiteralFalse
    | '(' booleanFormula ')'                                            # booleanParens
    | integerFormula COMPARE integerFormula                             # booleanCompareIntegers
    | integerFormula (OP_EQ|OP_NE) integerFormula                       # booleanEqualIntegers
    | booleanFormula (OP_EQ|OP_NE) booleanFormula                       # booleanEqualBooleans
    | groundableObject (OP_EQ|OP_NE) groundableObject                   # booleanEqualObjects
    | OP_NOT booleanFormula                                          # booleanNot
    | booleanFormula OP_AND booleanFormula (OP_AND booleanFormula)*     # booleanAnd
    | booleanFormula OP_OR  booleanFormula (OP_OR booleanFormula)*      # booleanOr
    ;

beliefFormula 
    : booleanFormula                                             # beliefBooleanFormula
    | '(' beliefFormula ')'                                      # beliefParens
    | OP_NOT beliefFormula                                    # beliefNot
    | beliefFormula (OP_EQ|OP_NE) beliefFormula                  # beliefEqualBeliefs
    | beliefFormula OP_AND beliefFormula (OP_AND beliefFormula)* # beliefAnd
    | beliefFormula OP_OR beliefFormula (OP_OR beliefFormula)*   # beliefOr
    | 'C' '(' beliefFormula ')'                                  # beliefCommon
    | 'B' '[' groundableObject ']' '(' beliefFormula ')'                # beliefBelieves
    ;


// INITIAL STATE DEFINITION

initiallySection : 'initially' (startStateDef | '{' (startStateDef ',')* startStateDef ','? '}') ;

//startStateDef : initiallyDef | kripkeModel ;
startStateDef : '{' kripkeModel '}' ;

//initiallyDef : '{' (beliefFormula ',')* beliefFormula? '}' ;

kripkeModel : (kripkeWorld ','?)+ (kripkeRelation ','?)+ ;

kripkeWorld : LOWER_NAME ASSIGN '{' ((valueAssignment|fluent) ',')* (valueAssignment|fluent)? '}' ;

kripkeRelation : relationType '[' objectName ']' ASSIGN
                 '{' ('('fromWorld','toWorld')'',')* ('('fromWorld','toWorld')')? '}' ;

relationType : 'B' | 'K' ;
fromWorld : LOWER_NAME;
toWorld : LOWER_NAME;



// GOALS DEFINITION

goalsSection : 'goals' '{' (goal ',')* goal? '}' ;
goal : beliefFormula ;



// ACTION DEFINITIONS

actionsSection : 'actions' '{' (actionDefinition ','?)* '}' ;

actionDefinition : LOWER_NAME variableDefList '{' (actionField ','?)* '}' ;
variableDefList : ('(' (variableDef ',')* variableDef? ')')? ;
variableDef : VARIABLE '-' objectType ;

formulaAssignment 
    : fluent ASSIGN (beliefFormula | integerFormula | groundableObject)
    | fluent
    | OP_NOT ( '('fluent')' | fluent )
    ;


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
preconditionActionField : 'precondition' variableDefList '{' booleanFormula '}' ;
observesActionField     : 'observes'     variableDefList '{' groundableObject ('if' booleanFormula)? '}' ;
awareActionField        : 'aware'        variableDefList '{' groundableObject ('if' booleanFormula)? '}' ;
determinesActionField   : 'determines'   variableDefList '{' booleanFormula '}' ;
announcesActionField    : 'announces'    variableDefList '{' beliefFormula '}' ;
causesActionField       : 'causes'       variableDefList '{' formulaAssignment ('if' booleanFormula)? '}' ;

