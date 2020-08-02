
package mecaPlanner.search;

import mecaPlanner.state.EpistemicState;
import mecaPlanner.actions.Action;
import mecaPlanner.models.Model;
import mecaPlanner.formulae.GeneralFormula;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;


public abstract class GNode  {
    protected EpistemicState estate;
    protected GeneralFormula goal;
    protected int time;
    protected GNode parent;
    protected Set<GNode> successors;
    protected Map<String, Model> models;

    private int systemAgentIndex;
    private int numAgents;

    public GNode(EpistemicState estate,
                 GeneralFormula goal,
                 int time,
                 GNode parent,
                 Map<String, Model> models,
                 int systemAgentIndex,
                 int numAgents
                ) {
        this.estate = estate;
        this.goal = goal;
        this.time = time;
        this.parent = parent;
        this.models = models;
        this.successors = new HashSet<GNode>();

        this.systemAgentIndex = systemAgentIndex;
        this.numAgents = numAgents;
    }

    public Set<GNode> getSuccessors() {
        return successors;
    }

    public EpistemicState getState() {
        return estate;
    }

    public GNode getParent() {
        return parent;
    }

    public int getTime() {
        return time;
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
            if ((time % numAgents == ancestor.gettime() % numAgents) && estate.equivalent(ancestor.getState())) {
                return true;
            }
            ancestor = ancestor.getParent();
        }
        return false;
    }

    public GNode transition(Action action) {
        Action.UpdatedStateAndModels transitionResult = action.transition(estate, models);
        int nextTime = time + 1;
        if (nextTime == systemAgentIndex) {
            return new OrNode(transitionResult.getState(), goal, nextTime, this, transitionResult.getModels());
        }
        else {
            return new AndNode(transitionResult.getState(), goal, nextTime, this, transitionResult.getModels());
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


