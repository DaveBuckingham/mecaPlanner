package mecaPlanner.search;

import mecaPlanner.state.*;
import mecaPlanner.formulae.Formula;
import mecaPlanner.formulae.TimeConstraint;
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

    private Domain domain;
    private Problem problem;
    //private int leavesPerDepth;


    public Search() {
    }

    public Solution findSolution(Problem problem) {

        this.problem = problem;
        this.domain = problem.getDomain();

        if (problem.getStartStates().isEmpty()) {
            throw new RuntimeException("no start state defined");
        }
        if (problem.getGoals().isEmpty()) {
            throw new RuntimeException("no goals defined");
        }

        // SHOULD ADD OTHER CHECKS

        //leavesPerDepth = 0;
        //for (String agent : domain.getNonPassiveAgents()) {
        //    leavesPerDepth += domain.getActions(agent).size();
        //}

        Set<PointedPlausibilityState> startStates = problem.getStartStates();

        for (PointedPlausibilityState state : startStates) {
            state.checkRelations();
        }

        //int systemAgentIndex = problem.getSystemAgentIndex();


        Formula goal = problem.getGoal();
        List<TimeConstraint> timeConstraints = problem.getTimeConstraints();
        int time = 0;
        while (!domain.isSystemAgentIndex(time)) {
            time++;
        }

        int maxDepth = 0;
        Solution solution = null;
        OrLayer startingOrLayer = null;
        while (solution == null) {
            System.out.print(maxDepth);
            System.out.print("\n");



            if (domain.isSystemAgentIndex(time)) {
                Set<OrNode> allStartOrNodes = new HashSet<>();
                for (PointedPlausibilityState eState : startStates) {
                    allStartOrNodes.add(new OrNode(eState,
                                                   goal, 
                                                   timeConstraints,
                                                   time, 
                                                   null, 
                                                   domain,
                                                   maxDepth
                                                  ));
                }
                startingOrLayer = new OrLayer(time, allStartOrNodes,maxDepth,domain);
                assert(startingOrLayer != null);
            }
            else {
                startingOrLayer = new OrLayer(maxDepth,domain);
                for (PointedPlausibilityState eState : startStates) {
                    AndNode startAndNode = new AndNode(eState,
                                                       goal, 
                                                       timeConstraints,
                                                       time, 
                                                       null, 
                                                       domain,
                                                       maxDepth
                                                      );

                    OrLayer nextOrLayer = startAndNode.descend();

                    if (nextOrLayer == null) {
                        return null;
                    }

                    startingOrLayer.merge(nextOrLayer);
                }
            }

            solution = searchToDepth(startingOrLayer);
            maxDepth += 1;
        }
        return solution;
    }

    

    public Solution searchToDepth(OrLayer allStartOrNodes) {

        PerspectiveSuccessors pNodesWithScore = allStartOrNodes.lift();
        Set<PNode> startPNodes = pNodesWithScore.getPLayer();

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


