
package mecaPlanner.search;

import mecaPlanner.state.EpistemicState;
import mecaPlanner.Action;
import mecaPlanner.models.Model;
import mecaPlanner.formulae.Formula;
import mecaPlanner.formulae.TimeConstraint;

import mecaPlanner.Domain;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;


public abstract class GNode  {
    protected EpistemicState estate;
    protected Formula goal;
    protected Set<TimeConstraint> timeConstraints;
    protected Integer time;
    protected GNode parent;
    protected Set<GNode> successors;
    protected Map<String, Model> models;
    protected Domain domain;

    protected String agent;

    private int agentIndex;
    private int numAgents;

    

    private int systemAgentIndex;

    public GNode(EpistemicState estate,
                 Formula goal,
                 Set<TimeConstraint> timeConstraints,
                 Integer time,
                 GNode parent,
                 Map<String, Model> models,
                 int systemAgentIndex,
                 Domain domain
                ) {
        this.estate = estate;
        this.goal = goal;
        this.timeConstraints = timeConstraints;
        this.time = time;
        this.parent = parent;
        this.models = models;
        this.successors = new HashSet<GNode>();
        this.systemAgentIndex = systemAgentIndex;
        this.domain = domain;

        this.numAgents = domain.getNonPassiveAgents().size();
        this.agentIndex = this.time % this.numAgents;
        this.agent = domain.getNonPassiveAgents().get(agentIndex);

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

    public Integer getTime() {
        return time;
    }

    public Map<String, Model> getModels() {
        return models;
    }

    public boolean isGoal() {
        for (TimeConstraint c : timeConstraints) {
            if (!c.holdsAt(time)) {
                return false;
            }
        }
        return goal.evaluate(estate);
    }

    public int getAgentIndex() {
        return agentIndex;
    }

    public String getAgent() {
        return agent;
    }

    public boolean isCycle() {
        GNode ancestor = this.parent;
        while (ancestor != null) {
            if ((this.agentIndex == ancestor.getAgentIndex()) && estate.equivalent(ancestor.getState())) {
                return true;
            }
            ancestor = ancestor.getParent();
        }
        return false;
    }


    public GNode transition(Action action) {
        Action.UpdatedStateAndModels transitionResult = action.transition(estate, models);
        int nextTime = time+1;
        if (nextTime % numAgents == systemAgentIndex) {
            return new OrNode(transitionResult.getState(),
                              goal,
                              timeConstraints,
                              nextTime,
                              this,
                              transitionResult.getModels(),
                              systemAgentIndex,
                              domain);
        }
        else {
            return new AndNode(transitionResult.getState(),
                               goal,
                               timeConstraints,
                               nextTime,
                               this,
                               transitionResult.getModels(),
                               systemAgentIndex,
                               domain);
        }
    }
 

    public abstract GroundSuccessors descend();

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


