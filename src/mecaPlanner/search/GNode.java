
package mecaPlanner.search;

import mecaPlanner.state.EpistemicState;
import mecaPlanner.actions.Action;
import mecaPlanner.models.Model;
import mecaPlanner.Domain;
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
    protected String agent;
    protected Map<String, Model> models;
    protected Domain domain;

    public GNode(EpistemicState estate, GeneralFormula goal, int time, GNode parent, Map<String, Model> models, Domain domain) {
        this.domain = domain;
        this.estate = estate;
        this.goal = goal;
        this.time = time;
        this.agent = domain.agentAtDepth(time);
        this.parent = parent;
        this.models = models;
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

    public GNode transition(Action action, Domain domain) {
        Action.UpdatedStateAndModels transitionResult = action.transition(estate, models);
        if (domain.isSystemAgent(time+1)) {
            return new OrNode(transitionResult.getState(), goal, time+1, this, transitionResult.getModels(), domain);
        }
        else {
            return new AndNode(transitionResult.getState(), goal, time+1, this, transitionResult.getModels(), domain);
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


