package mecaPlanner.search;

import mecaPlanner.state.NDState;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.World;
import mecaPlanner.actions.Action;
import mecaPlanner.models.Model;
import mecaPlanner.formulae.GeneralFormula;
import mecaPlanner.Solution;
import mecaPlanner.Domain;
import mecaPlanner.Problem;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;





public class Search {

    Domain domain;
    Problem problem;


    public Search() {
    }


    public Solution findSolution(Problem problem) {

        this.problem = problem;
        this.domain = problem.getDomain();

        if (problem.getSystemAgentIndex() == null) {
            throw new RuntimeException("system agent not defined");
        }
        if (problem.getStartStates().isEmpty()) {
            throw new RuntimeException("no start state defined");
        }
        if (problem.getGoals().isEmpty()) {
            throw new RuntimeException("no goals defined");
        }
        // ADD OTHER CHECKS




        Set<EpistemicState> startStates = problem.getStartStates();

        int systemAgentIndex = problem.getSystemAgentIndex();

        int numAgents = domain.getNonPassiveAgents().size();

        GeneralFormula goal = problem.getGoal();
        int time = 0;

        Set<OrNode> allStartOrNodes = new HashSet<>();
        if (time == systemAgentIndex) {
            for (EpistemicState eState : startStates) {
                allStartOrNodes.add(new OrNode(eState, goal, 0, null, problem.getStartingModels(), systemAgentIndex, domain));
            }
        }
        else {
            for (EpistemicState eState : startStates) {
                AndNode startAndNode = new AndNode(eState, goal, 0, null, problem.getStartingModels(), systemAgentIndex, domain);
                Set<OrNode> startOrNodes = startAndNode.descend();
                if (startOrNodes == null) {
                    return null;
                }
                allStartOrNodes.addAll(startOrNodes);
            }
        }

        time = systemAgentIndex;

        Map<Perspective, Set<OrNode>> perspectives = new HashMap<>();
        for (OrNode ground : allStartOrNodes ){
            Perspective perspective = new Perspective(ground.getState(), ground.getAgent());
            if (!perspectives.containsKey(perspective)) {
                perspectives.put(perspective, new HashSet<OrNode>());
            }
            perspectives.get(perspective).add(ground);
        }
            
        Set<PNode> startPNodes = new HashSet<>();
        for (Map.Entry<Perspective, Set<OrNode>> entry : perspectives.entrySet()) {
            startPNodes.add(new PNode(entry.getKey(), entry.getValue(), time, 0, domain));
        }

        int maxDepth = 0;
        Solution solution = null;
        while (solution == null) {
            System.out.println(maxDepth);
            solution = searchToDepth(startPNodes, maxDepth);
            maxDepth += 1;
        }
        return solution;
    }

    

    public Solution searchToDepth(Set<PNode> startPNodes, int maxDepth) {

        for (PNode startPNode : startPNodes) {
            if (!startPNode.evaluate(maxDepth)) {
                return null;
            }
        }
        return pnodesToSolution(startPNodes);
    }

    private Solution pnodesToSolution(Set<PNode> pnodes) {
        Solution s = new Solution(problem);
        for (PNode p : pnodes) {
            s.addAction(p.getPerspective(), p.getAction(), pnodesToSolution(p.getSuccessors()));
        }
        return s;
    }
}


