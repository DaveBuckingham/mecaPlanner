
package mecaPlanner.search;

import mecaPlanner.state.State;
import mecaPlanner.state.Action;
import mecaPlanner.agents.Agent;
import mecaPlanner.formulae.Formula;
import mecaPlanner.formulae.TimeConstraint;

import mecaPlanner.Domain;

import mecaPlanner.Log;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

// A GROUND NODE. COULD BE EITHER AN 'AND' OR AN 'OR'.


public abstract class GNode  {
    protected State estate;
    protected Formula goal;
    protected List<TimeConstraint> timeConstraints;
    protected Integer time;
    protected GNode parent;
    protected Set<GNode> successors;
    protected Domain domain;
    protected int maxDepth;

    protected String agent;

    private int agentIndex;
    private int numAgents;

    


    public GNode(State estate,
                 Formula goal,
                 List<TimeConstraint> timeConstraints,
                 Integer time,
                 GNode parent,
                 Domain domain,
                 int maxDepth
                ) {
        this.estate = estate;
        this.goal = goal;
        this.timeConstraints = timeConstraints;
        this.time = time;
        this.parent = parent;
        this.successors = new HashSet<GNode>();
        this.domain = domain;
        this.maxDepth = maxDepth;

        this.numAgents = domain.getTurnOrder().size();
        this.agentIndex = this.time % this.numAgents;
        this.agent = domain.agentAtTime(agentIndex);

        //Log.debug("t=" + time.toString());
        //Log.debug("ag=" + agent);

    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public Set<GNode> getSuccessors() {
        return successors;
    }

    public State getState() {
        return estate;
    }

    public GNode getParent() {
        return parent;
    }

    public Integer getTime() {
        return time;
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
            if ((this.agentIndex == ancestor.getAgentIndex()) && estate.bisimilar(ancestor.getState())) {
                return true;
            }
            ancestor = ancestor.getParent();
        }
        return false;
    }


    public GNode transition(Action action) {
        State newState = action.transition(estate);
        int nextTime = time+1;
        if (domain.isSystemAgentIndex(nextTime)) {
            return new OrNode(newState,
                              goal,
                              timeConstraints,
                              nextTime,
                              this,
                              domain,
                              maxDepth);
        }
        else {
            return new AndNode(newState,
                               goal,
                               timeConstraints,
                               nextTime,
                               this,
                               domain,
                               maxDepth);
        }
    }
 

    public abstract OrLayer descend();

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


