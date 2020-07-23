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




    public Search(Domain domain) {
        this.domain = domain;
    }



    public Set<Solution> findSolution() {

        // THE SEARCH ALGORITHM WORKS WITH MULTIPLE START STATES
        // RIGHT NOW OUR DOMAIN REPRESENTATION ASSUMES A SINGLE START STATE
        // IF THERE ARE MULTIPLE START STATES, WE MAY GET MULTIPLE SOLUTIONS
        // CURRENTLY, THE PLANNER CLASS ASSUMES WE WILL GET A SINGLE SOLUTION
        Set<EpistemicState> startStates = new HashSet<>();
        startStates.add(domain.getStartState());

        GeneralFormula goal = domain.getGoal();
        int time = 0;

        Set<OrNode> allStartOrNodes = new HashSet<>();
        if (domain.isSystemAgent(time)) {
            for (EpistemicState eState : startStates) {
                allStartOrNodes.add(new OrNode(eState, goal, 0, null, domain.getStartingModels(), domain));
            }
        }
        else {
            for (EpistemicState eState : startStates) {
                AndNode startAndNode = new AndNode(eState, goal, 0, null, domain.getStartingModels(), domain);
                Set<OrNode> startOrNodes = startAndNode.descend();
                if (startOrNodes == null) {
                    return null;
                }
                allStartOrNodes.addAll(startOrNodes);
            }
        }

        while (domain.isEnvironmentAgent(time)) {
            time++;
        }

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
        Set<Solution> solutions = null;
        while (solutions == null) {
            System.out.println(maxDepth);
            solutions = searchToDepth(startPNodes, maxDepth);
            maxDepth += 1;
        }
        return solutions;
    }

    

    public Set<Solution> searchToDepth(Set<PNode> startPNodes, int maxDepth) {

        // NEED TO MOVE ALL THIS INITIALIZATION STUFF TO FINDSOLUTION, AS IN PSEUDOCODE
        // one solution per possible start state
        Set<Solution> solutions = new HashSet<>();
        for (PNode startPNode : startPNodes) {
            if (!startPNode.evaluate(maxDepth)) {
                return null;
            }
            solutions.add(pnodeToSolution(startPNode));
        }

        return solutions;
    }

    private Solution pnodeToSolution(PNode pnode) {
        Solution s = new Solution(pnode.getPerspective(), pnode.getAction(), pnode.getTime(), domain);
        for (PNode successor : pnode.getSuccessors()) {
            s.addChild(pnodeToSolution(successor));
        }
        return s;
    }
}


