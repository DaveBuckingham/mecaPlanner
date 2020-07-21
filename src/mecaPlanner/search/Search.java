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


    GeneralFormula goal;


    public Search() {
    }






    public Set<Solution> findSolution(EpistemicState startState, GeneralFormula goal) {
        Set<EpistemicState> startStateSet = new HashSet<>();
        startStateSet.add(startState);
        return(findSolution(startStateSet, goal));
    }

    public Set<Solution> findSolution(Set<EpistemicState> startStates, GeneralFormula goal) {
        int maxDepth = 0;
        Set<Solution> solutions = null;
        while (solutions == null) {
            System.out.println(maxDepth);
            solutions = searchToDepth(startStates, goal, maxDepth);
            maxDepth += 1;
        }
        return solutions;
    }

    

    public Set<Solution> searchToDepth(Set<EpistemicState> startStates, GeneralFormula goal, int maxDepth) {

        // NEED TO MOVE ALL THIS INITIALIZATION STUFF TO FINDSOLUTION, AS IN PSEUDOCODE

        this.goal = goal;
        String startAgent = Domain.agentAtDepth(0);

        Set<OrNode> allStartOrNodes = new HashSet<>();
        if (Domain.isSystemAgent(startAgent)) {
            for (EpistemicState eState : startStates) {
                allStartOrNodes.add(new OrNode(eState, goal, 0, null, Domain.getStartingModels(), maxDepth));
            }
        }
        else {
            for (EpistemicState eState : startStates) {
                AndNode startAndNode = new AndNode(eState, goal, 0, null, Domain.getStartingModels(), maxDepth);
                Set<OrNode> startOrNodes = startAndNode.descend();
                if (startOrNodes == null) {
                    return null;
                }
                allStartOrNodes.addAll(startOrNodes);
            }
        }

        int time = 0;
        while (Domain.isEnvironmentAgent(time)) {
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
            startPNodes.add(new PNode(entry.getKey(), entry.getValue(), time));
        }

        // one solution per possible start state
        Set<Solution> solutions = new HashSet<>();
        for (PNode startPNode : startPNodes) {
            if (!startPNode.evaluate()) {
                return null;
            }
            solutions.add(pnodeToSolution(startPNode));
        }

        return solutions;
    }

    private Solution pnodeToSolution(PNode pnode) {
        Solution s = new Solution(pnode.getPerspective(), pnode.getAction(), pnode.getTime());
        for (PNode successor : pnode.getSuccessors()) {
            s.addChild(pnodeToSolution(successor));
        }
        return s;
    }
}


