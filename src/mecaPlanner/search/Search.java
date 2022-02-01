package mecaPlanner.search;

import mecaPlanner.state.State;
import mecaPlanner.state.Model;
import mecaPlanner.state.World;
import mecaPlanner.formulae.Formula;
import mecaPlanner.formulae.TimeConstraint;
import mecaPlanner.Solution;
import mecaPlanner.Domain;
import mecaPlanner.Problem;
import mecaPlanner.Action;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;


public class Search {

    private Domain domain;
    private Problem problem;
    private int leavesPerDepth;


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

        // SHOULD ADD OTHER CHECKS

        leavesPerDepth = 0;
        for (String agent : domain.getNonPassiveAgents()) {
            leavesPerDepth += domain.getActions(agent).size();
        }

        Set<State> startStates = problem.getStartStates();

        for (State eState : startStates) {
            eState.forceCheck();
        }

        int systemAgentIndex = problem.getSystemAgentIndex();

        int numAgents = domain.getNonPassiveAgents().size();

        Formula goal = problem.getGoal();
        Set<TimeConstraint> timeConstraints = problem.getTimeConstraints();
        int time = 0;

        Set<OrNode> allStartOrNodes = new HashSet<>();
        if (time == systemAgentIndex) {
            for (State eState : startStates) {
                allStartOrNodes.add(new OrNode(eState,
                                               goal, 
                                               timeConstraints,
                                               0, 
                                               null, 
                                               problem.getStartingModels(), 
                                               systemAgentIndex, 
                                               domain
                                              ));
            }
        }
        else {
            for (State eState : startStates) {
                AndNode startAndNode = new AndNode(eState,
                                                   goal, 
                                                   timeConstraints,
                                                   0, 
                                                   null, 
                                                   problem.getStartingModels(), 
                                                   systemAgentIndex, 
                                                   domain
                                                  );

                GroundSuccessors startOrNodesWithScore = startAndNode.descend();

                if (startOrNodesWithScore == null) {
                    return null;
                }


                allStartOrNodes.addAll(startOrNodesWithScore.getOrLayer());
            }
        }

        time = systemAgentIndex;

        int maxDepth = 0;
        Solution solution = null;
        while (solution == null) {
            System.out.print(maxDepth);
            System.out.print("\t");
            System.out.print(Math.round(Math.pow(leavesPerDepth, maxDepth)));
            System.out.print("\t");
            System.out.print("\n");
            solution = searchToDepth(allStartOrNodes, time,  maxDepth);
            maxDepth += 1;
        }
        return solution;
    }

    

    public Solution searchToDepth(Set<OrNode> allStartOrNodes, int time, int maxDepth) {


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
            startPNodes.add(new PNode(entry.getKey(), entry.getValue(), time, 0, maxDepth, domain));
        }

        for (PNode startPNode : startPNodes) {
            if (startPNode.expand() == Integer.MAX_VALUE) {
                return null;
            };
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


