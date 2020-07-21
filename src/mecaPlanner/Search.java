package mecaPlanner;

import mecaPlanner.state.NDState;
import mecaPlanner.state.EpistemicState;
import mecaPlanner.state.KripkeStructure;
import mecaPlanner.state.World;
import mecaPlanner.actions.Action;
import mecaPlanner.models.Model;
import mecaPlanner.formulae.GeneralFormula;
import mecaPlanner.Perspective;

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



    private class PNode  {
        private Action successfulAction;
        private Perspective perspective;
        private Set<OrNode> grounds;
        private Set<PNode> successors;
        private int time;

        public PNode(Perspective perspective, Set<OrNode> grounds, int time) {
            successfulAction = null;
            this.perspective = perspective;
            this.grounds = grounds;
            this.time = time;
        }

        public Perspective getPerspective() {
            return perspective;
        }

        public Action getAction() {
            return successfulAction;
        }

        public Set<PNode> getSuccessors() {
            return successors;
        }

        public int getTime() {
            return time;
        }

        private int nextSystemAgentTime() {
            int i = time + 1;
            while (Domain.isEnvironmentAgent(i)) {
                i += 1;
            }
            return i;
        }

        private Set<Action> getPossibleActions() {
            Set<Action> possibleActions = new HashSet<>();
            for (Action action : Domain.getAgentActions(time)) {
                boolean safe = true;
                for (OrNode ground : grounds) {
                    if (!action.executable(ground.getState())) {
                        safe = false;
                        break;
                    }
                }
                if (safe) {
                    possibleActions.add(action);
                }
            }
            return possibleActions;
        }

        public boolean evaluate() {
            for (Action action : getPossibleActions()) {
                successors = pTransition(action);
                if (successors == null) {
                    continue;
                }
                boolean failedSuccessor = false;
                for (PNode successor : successors) {
                    if (!successor.evaluate()) {
                        failedSuccessor = true;
                        break;
                    }
                }
                if (!failedSuccessor) {
                    successfulAction = action;
                    return true;
                }
            }
            return false;
        }

        // get the set of successor perspective nodes
        private Set<PNode> pTransition(Action action) {
            Map<Perspective, Set<OrNode>> successorPerspectives = new HashMap<>();
            for (OrNode ground : grounds ){
                //Set<OrNode> gSuccessors = ground.orTransition(action);

                Set<OrNode> gSuccessors = ground.transition(action).descend();

                if (gSuccessors == null) {
                    return null;
                }
                for (OrNode gSuccessor : gSuccessors) {
                    Perspective successorPerspective = new Perspective(gSuccessor.getState(), gSuccessor.getAgent());
                    if (!successorPerspectives.containsKey(successorPerspective)) {
                        successorPerspectives.put(successorPerspective, new HashSet<OrNode>());
                    }
                    successorPerspectives.get(successorPerspective).add(gSuccessor);
                }
            }
            
            Set<PNode> successorNodes = new HashSet<>();
            for (Map.Entry<Perspective, Set<OrNode>> entry : successorPerspectives.entrySet()) {
                successorNodes.add(new PNode(entry.getKey(), entry.getValue(), nextSystemAgentTime()));
            }
            return successorNodes;
        }
    }


    private abstract class GNode  {
        protected EpistemicState estate;
        protected int time;
        protected int maxDepth;
        protected GNode parent;
        protected Set<GNode> successors;
        protected String agent;
        protected Map<String, Model> models;

        public GNode(EpistemicState estate, int time, GNode parent, Map<String, Model> models, int maxDepth) {
            this.estate = estate;
            this.time = time;
            this.agent = Domain.agentAtDepth(time);
            this.parent = parent;
            this.models = models;
            this.maxDepth = maxDepth;
            this.successors = new HashSet<GNode>();
        }

        public Set<GNode> getSuccessors() {
            return successors;
        }

        public EpistemicState getState() {
            return estate;
        }

        public String getAgent() {
            return agent;
        }

        public GNode getParent() {
            return parent;
        }

        public Map<String, Model> getModels() {
            return models;
        }

        public boolean isGoal() {
            return goal.holds(estate, time);
        }

        public boolean isCycle() {
            GNode ancestor = this.parent;
            while (ancestor != null) {
                if (agent == ancestor.getAgent() && estate.equivalent(ancestor.getState())) {
                    return true;
                }
                ancestor = ancestor.getParent();
            }
            return false;
        }

        public GNode transition(Action action) {
            Action.UpdatedStateAndModels transitionResult = action.transition(estate, models);
            if (Domain.isSystemAgent(time+1)) {
                return new OrNode(transitionResult.getState(), time+1, this, transitionResult.getModels(), maxDepth);
            }
            else {
                return new AndNode(transitionResult.getState(), time+1, this, transitionResult.getModels(), maxDepth);
            }
        }

        public abstract Set<OrNode> descend();

        private String treeToString(int time) {
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < time; i++) {
                str.append("  ");
            }
            str.append(toString());
            for (GNode child : successors) {
                str.append(child.treeToString(time+1));
            }
            return str.toString();
        }

        public String treeToString() {
            return treeToString(0);
        }
    }


    private class AndNode extends GNode {

        public AndNode(EpistemicState estate, int time, GNode parent, Map<String, Model> models, int maxDepth) {
            super(estate, time, parent, models, maxDepth);
            assert (Domain.isEnvironmentAgent(agent));
        }

        protected Set<Action> getPossibleActions() {
            Set<Action> possibleActions = new HashSet<Action>();
            Set<Action> prediction = models.get(agent).getPrediction(estate.getBeliefPerspective(agent), agent);

            if (prediction == null) {
                throw new RuntimeException("Model returned null, indicating model failure.");
            }

            Log.debug(agent + " prediction:");
            for (Action action : prediction) {
                Log.debug("  " + action.getSignature());
                if (action.executable(estate) && action.necessarilyExecutable(estate.getBeliefPerspective(agent))) {
                    possibleActions.add(action);
                }
            }

            if (possibleActions.isEmpty()) {
                Log.warning("Model for " + agent + "predicted no necessarily executable action.");
            }

            return possibleActions;
        }

        // descending through layers of and-nodes, stops when we reach an or-node layer
        public Set<OrNode> descend() {

            Set<OrNode> allOrSuccessors = new HashSet<>();
            if (isGoal()) {
                return allOrSuccessors;
            }
            if (isCycle() || time >= maxDepth) {
                return null;
            }
            for (Action action : getPossibleActions()) {
                GNode successor = transition(action);
                Set<OrNode> orSuccessors = successor.descend();
                if (orSuccessors == null) {
                    return null;
                }
                allOrSuccessors.addAll(orSuccessors);
            }
            return allOrSuccessors;
        }
    }

    private class OrNode extends GNode {

        public OrNode(EpistemicState estate, int time, GNode parent, Map<String, Model> models, int maxDepth) {
            super(estate, time, parent, models, maxDepth);
        }

        // bottom out a recursive descent through and nodes
        public Set<OrNode> descend() {
            if (isCycle() || time == maxDepth) {
                return null;
            }
            Set<OrNode> s = new HashSet<OrNode>();
            if (!isGoal()) {
                s.add(this);
            }
            return s;
        }

        // follow the action, recursively descending through any and nodes, and return the resulting or node set
        //public Set<OrNode> orTransition(Action action) {
        //    GNode successor = transition(action);
        //    Set<OrNode> nextOrLayer = successor.descend();
        //    if (nextOrLayer == null) {
        //        return null;
        //    }
        //    return nextOrLayer;
        //}
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
                allStartOrNodes.add(new OrNode(eState, 0, null, Domain.getStartingModels(), maxDepth));
            }
        }
        else {
            for (EpistemicState eState : startStates) {
                AndNode startAndNode = new AndNode(eState, 0, null, Domain.getStartingModels(), maxDepth);
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


