package mecaPlanner.models;

import mecaPlanner.state.*;
import mecaPlanner.search.Perspective;
import mecaPlanner.formulae.localFormulae.Fluent;
import mecaPlanner.Action;
import mecaPlanner.Log;
import mecaPlanner.Domain;
import mecaPlanner.Solution;

import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;


public class RecursiveModel extends Model {

    Solution plan;

    public RecursiveModel(String agent, Domain domain) {
        super(agent, domain);
        plan = null;
    }

    public Set<Action> getPrediction(EpistemicState eState) {
        Perspective perspective = new Perspective(eState, agent);
        Set<Action> prediction = new HashSet<>();

        if (plan == null || !plan.hasPerspective(perspective)) {

            Domain localDomain = new Domain();

            for (Action a : domain.getAllActions()) {
                localDomain.addAction(a);
            }

            int currentAgent = domain.getNonPassiveAgents().indexOf(agent);
            int lastAgent = domain.getNonPassiveAgents().size() + currentAgent;
            while (currentAgent <= lastAgent) {
                localDomain.addAgent(domain.agentAtDepth(currentAgent));
            }
            int systemAgentIndex = 0;

            for (String p : domain.getPassiveAgents()) {
                localDomain.addPassive(p);
            }

            Set<EpistemicState> startStates = eState.getBeliefPerspective(agent).getEpistemicStates();

            //Problem problem = new Problem(localDomain, systemAgentIndex, startStates, 
            //Map<String,Model> 
        }
        assert (plan != null);
        assert (plan.hasPerspective(perspective));
        Set<Action> prediction = new HashSet<>();
        prediction.add(plan.getAction(perspective));
        return prediction;

//    public Problem(Domain domain,
//                   int systemAgentIndex,
//                   Set<EpistemicState> startStates,
//                   Map<String,Model> startingModels,
//                   Set<TimeFormula> goals
 




//            Set<EpistemicState> robotPerspectiveStates =
//                currentState.getBeliefPerspective(currentAgent).getEpistemicStates();
//
//            Search search = new Search();
//            Problem newProblem = new Problem(problem.getDomain(),
//                                             sIndex,
//                                             robotPerspectiveStates,
//                                             models,
//                                             problem.getGoals()
//                                            );
//            plan = search.findSolution(newProblem);
//            if (plan == null) {
//                System.out.println("NO SOLUTION FOUND, ABORTING.");
//                System.exit(0);
//            }
//            if (!plan.hasPerspective(robotView)) {
//                throw new RuntimeException("got a bad plan.");
//            }
//            System.out.println("SOLUTION FOUND.");
//        }
//
//                // THE PLAN GIVES US THE ROBOT'S ACTION
//                action = plan.getAction(robotView);
//
//                // THE PLAN IS A TREE, PROCEDE ACCORDING TO THE STATE
//                plan = plan.getChild(robotView);
        //Set<Action> allActions = getSafeActions(ndState);
 


    }


}
