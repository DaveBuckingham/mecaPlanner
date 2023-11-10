
# The MECA Planner

The Multiagent Epistemic Cooperation-Agnostic Planner is an automated task
planner. It is intended for social task environments where the
possibly-conflicting and nested beliefs and knowledge of multiple agents are of
importance.

## Example

**TODO

## Build

You can build the MECA Planner with

    $ make

Intermediate generated files will be put into the `/build` directory. 
The planner planner will be compiled into `mecaPlanner.jar`|, which
will be put into the top level directory. This will also generate two
executable scripts, `meca` and `mecad`.

The `meca` executable takes a .depl planning problem file as its single
argument and runs the planner. The `mecad` script is like `meca` but
also turns on java assertions: most of the planner's run-time error checking is
in assertions.

## Demo

    \begin{lstlisting}
    $ ./meca problems/example.depl
    \end{lstlisting}


## Output

As the planner performs iterated depth first search, it prints out the current
search depth limit. For the example problem, the planner searches to depth 1
before finding a solution.
The solution is a conditional plan.

**Need to add a description of the output format**


## The depl planning language

### Planning assumptions

A planning problem includes a single system agent and zero or more environment
agents.  We also admit so-called passive agents. Passive agents do not act, but
the planner will maintain representations of their mental states within
epistemic-doxastic states.  We assume discrete turn-taking.

Planning problems for the MECA Planner are specified in the doxastic/epistemic
planning language: **depl**. A single depl file describes both a planning domain
and a planning problem.
The grammar of depl is defined with the ANTLR meta-language in the file:
`src/deplParser/Depl.g4`.

We will use an example depl file, which is located at 
`problems/example.depl`.

This describes a planning domain involving two agents: a robot, whose behavior
will be determined by the planner, and a human, whose behavior is
estimated by a predictive model. There are two rooms and two hallways: each
hallway connects the two rooms. The first room contains a coin and both agents,
the second room contains a pizza. The robot does not know whether the coin lies
heads- or tails-up, although the human does. The robot knows about the pizza in
the second room, but the human is unaware of the pizza.

The robot's goal is to know how the coin lies without the human knowing that the
robot knows this.

The robot can determine
how the coin lies by looking at it, but if the human is in the same room, she
will observe this and know that the robot knows. The human is hungry, and would
leave for the second room if she know about the pizza (according to the
predictive model).

A successful plan is for the robot to announce to the human that there is pizza
in the other room, wait for the human to leave by either of the halls, and then
observe the coin. Here is the example depl file describing this problem:

Consider the example depl at
`/problems/example.depl`.


### Structure and syntax

depl is formally with ANTLR meta-language in `/src/deplParser/Depl.g4`.

A depl file contains the following sections, which must occur in the correct
order, and are required unless specified as optional:

- types
- objects
- agents
- passive (optional)
- fluents
- constants (optional)
- initially
- goals
- actions

Each section begins with the section name, followed by
`{`, then the section contents, and then'}'.

Whitespace, newlines, and C-style comments are ignored,

It tries to be generous. For example, `&` means the same thing as `&&` ("and"), while


#### Types

The Types section defines a type hierarchy relating all objects in the planning
problem, and consists
of a comma-separated list of
type definitions. A type definition takes the form
`Subtype - Supertype`.
The type `Object' is built-in, and
every type must be a subtype of
`Object'.  A type name begins with an uppercase letter, followed
by any number of upper- or lower-case letters, integers, and underscores.

The depl parser will use the type hierarchy defined in this section for two 
purposes. First, it will provide type checking of objects as a guard against errors.
Second, it will automatically expand some
statements that contain types into a set of statements instead containing
objects of those types, providing a convenient short-hand. 
The type hierarchy is a parse-time entity: no type information will be
available to the planner.


#### Objects

The Objects section defines the objects in the planning problem.
The content of the objects section consists of a comma-separated list of
object definitions. An object definition takes the form `object - Type`. Each type must
either be `Object`, or be defined in the Types section.  An
object name begins with a lowercase letter, followed by any number of upper-
or lower-case letters, integers, and underscores.
Objects will be used as arguments to predicates to build a set of atomic
Boolean fluents (propositions).


#### Agents

The MECA planner uses three types of agents. A single
*system* agent and any number of *environment* agents are discussed in
this section.  The third type, *passive* agents, are discussed in the next
section. 

An agent must be an object that has been defined in the
Objects section. An agent can have any type, but it is generally
convenient to create a type for objects that will be agents, for example 
as we do with the `actor` type in the example.

The Agents section consists of a comma-separated list of agent
definitions. There must be at least one agent definition. There must be exactly
one *system* agent definition, the rest must be *environment* agent
definitions. A system agent definition consists of only an agent name.
An environment
agent definition takes the form `name{Model}`, where `name`
is an agent name and `Model` is the name of a model class. The model
class name must begin with an uppercase letter, followed by any number of
lower- and upper-case characters, integers, and underscores. The model name must
be the same as the name of a java class that extends
`mecaPlanner.models.Model`, and the class should be defined in a
`.java` file `src/mecaPlanner/models/`.

This example defines a system agent, `robot1`, and a single environment
agent, `human1`.
The order in which agents are defined determines the order in which they will
act. thus `robot1` will act first, followed by
`human1`, and then `robot1` again, etc. The planner will query
`ExampleModel` to determine the predicted actions of `human1`, and
will attempt to construct a plan that specifies the actions of `robot1`.

The depl parser and the MECA planner will use the information defined in this
section for three
purposes. First, agents will be associated with actions. Second, the model
assigned to each environment agent will be queried to predict that agent's
actions. Third, the epistemic and doxastic state and action
systems will maintain representations of agents' mental states.



#### Passive

This optional section defines *passive* agents, whose beliefs and knowledge are
modeled by the planner (as with system and environment agents), but
who do not act (unlike system and environment agents).
The passive section consists of a comma-separated list of passive agent
definitions. A passive agent definition takes the same form as a system
agent definition.



#### Fluents

This section contains a comma-delimited list of fluent definitions. A fluent
definition takes the form `name(p1,...,pn)`, where `name` is the
predicate, which begins with a lowercase letter, followed by any number of
upper- and lower-case letters, integers, and underscores, and each element of
`(p1,...,pn)` is an argument. An argument is
*either* an object name *or* a type name. If all arguments
of a fluent definition are object names, a single fluent is defined. If any
arguments are type names, the fluent definition is automatically expanded,
substituting all objects that are of the specified type(s), in all combinations, to
construct multiple fluents.



### Constants

This optional section defines constant (either true or false) atoms, and
consists of a comma-delimited list of constant
definitions, where a constant definition takes either the form
`name(p1,...,pn)` (true), 
or
`!name(p1,...,pn)` (false).
Automatic type-expansion is allowed as with fluent definitions.

If a constant is defined multiple times, its previously-defined values will be
overridden. Thus, we could use type-expansion to construct
a large number of false constants, and then
override some of them to be true. As an example, separate from our
running pizza-robot example (which does not use constants), consider a domain
having many rooms, some (but not most) of which are connected to each other.
A constants section specifying these constraints might look like this:

    constants{
        !connected(Room, Room),
        connected(room1,room2),
        connected(room1,room3),
        connected(room3,room4),
        connected(room4,room5),
    }

Similarly to the type hierarchy, constant definitions are a parse-time entity.
The planner does not have access to them.  Wherever a defined constant is found
within a depl file, it is replaced withe a *true* literal or a *false* literal.

Constants can be used to simplify and clarify some definitions,
especially action definitions. For example
we can efficiently define a *move* action that transitions
between any two rooms `?from` and `?to` (see the
Actions section below) only if the rooms are they are 'connected', specifying as a
precondition that `connected(?from,?to) | connected(?to,?from)`.  If this
were done using fluents instead of constants,
the parser would generate (and give to the planner) an
action for every pair of rooms. As the planner searched for a plan, it
would repeatedly consider each of these move actions, only to discover that the
preconditions are never satisfied for the vast majority of them. If
the `connected` constraints are defined as constants, the parser determines that
the preconditions for most of the possible *move* actions are constantly
false, and only generates (and passes to the planner) actions for movement
between connected rooms.



\subsection{initially}

<!---

This section defines the start state or the set of start states.
The state representations used by the planner are presented only in outline
here.

A state definition has two parts.
If there are multiple start states, they represent
plan-time uncertainty of the of the planner about the start state: the planner
will search for a conformant plan that can achieve its goals from any of
the start states.
First, worlds and their valuations are
defined. Then, per-agent knowledge and belief relations are defined over worlds.

Each world definition takes the form
\verb|w <- {f1,...,fn}|
where \verb|w| is a world name
and each of \verb|f1| \ldots \verb|fn| is a fluent 
that is true in that world. Unlisted fluents are false.
Designated worlds are prerpended by a star (\verb|*|).
There must be at least one designated world. 
If there are mutliple designated worlds, the state willl be replicated to
construct multiple pointed states, one taking each of those worlds as its
designated world. This is equivalent to defining multiple start states that
differ only in the selection of the designated world.

A comma-separated list of belief and knowledge relations for each agent follow the world definitions.
A belief relation takes the form
\verb|B[i] <- {e1,...,en}|
where \verb|i| is an agent
and each of \verb|e1| \ldots \verb|en| is a pair of worlds, taking the
form \verb|(w1,w2)| where \verb|w1| and \verb|w2| are worlds.
A knowledge relation takes the same form except replacing \verb|B| with \verb|K|.

The Belief relations must be serial, transitive, and Euclidean (the \kdff{}
properties). The Knowledge relations must be reflexive, transitive, and
symmetric (the \sfive{} properties). The Belief and Knowledge relations must also
satisfy the \kbone{} property ($B_i \subseteq K_i$), and the \kbtwo{} property
($(u,v) \in K_i, (v,w) \in B_i \rightarrow (u,w) \in B_i$) for every agent
$i$.

-->


#### Goals


The Goals section specifies the goals the planner tries to achieve.
This section contains a comma-delimited list of goal formulae. The parser and's
these together to construct a single goal formula. A goal `G` takes the form:

    G := f | (G) | M[a]G | G&G | G|G | !G | Timestep E i
    M := B | K | P | C
    E := == | != | < | <= | > | >=

where `f`is a fluent or a constant, `a` is an agent, 
and `i` is an integer.

`B[a]G` means that agent \verb|a| believes that formula \verb|G| is true.
`P[a]G` provides syntactic sugar for the dual, i.e. is equivalent to `!B[i]!g`.
`K[a]G` means that agent \verb|a| knows \verb|G|.
`C[a]G` means that all agents believe \verb|G| 
(there is not infinite iteration of common knowledge).


#### Actions

The Actions section defines actions available to system and environment
agents. An action definition takes the from 
`name(p1,...,pn){f1,...,fm}`.
Each of `p1,...,pn` is a parameter, taking the form `?n - T`, where
`?n` is a variable name starting with `?` and `T` is a type.
The action definition will be expanded into one or more ground actions by
assigning each action parameter to each object having its type.

Each of `f1,...,fn` is an action definition. An action definition
takes the form `name(p1,...,pn){f1,...,fm}`.

 
### Agent models

Our example environment agent model is ExampleModel, which is located at
`src/mecaplanner/models/ExampleModel.depl`

**todo**


## Testing a planning problem

The `test` program gives an interactive interface to  a planning domain.
You can display the start state, apply actions, and observe subsequent
states.

Build `test` with:
`$ make test`





