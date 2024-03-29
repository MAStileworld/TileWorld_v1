/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tileworld.planners;

import sim.util.Int2D;
import tileworld.agent.SimpleTWAgent;
import tileworld.agent.TWAgent;
import tileworld.environment.*;

/**
 * DefaultTWPlanner
 *
 * @author michaellees
 * Created: Apr 22, 2010
 *
 * Copyright michaellees 2010
 *
 * Here is the skeleton for your planner. Below are some points you may want to
 * consider.
 *
 * Description: This is a simple implementation of a Tileworld planner. A plan
 * consists of a series of directions for the agent to follow. Plans are made,
 * but then the environment changes, so new plans may be needed
 *
 * As an example, your planner could have 4 distinct behaviors:
 *
 * 1. Generate a random walk to locate a Tile (this is triggered when there is
 * no Tile observed in the agents memory
 *
 * 2. Generate a plan to a specified Tile (one which is nearby preferably,
 * nearby is defined by threshold - @see TWEntity)
 *
 * 3. Generate a random walk to locate a Hole (this is triggered when the agent
 * has (is carrying) a tile but doesn't have a hole in memory)
 *
 * 4. Generate a plan to a specified hole (triggered when agent has a tile,
 * looks for a hole in memory which is nearby)
 *
 * The default path generator might use an implementation of A* for each of the behaviors
 *
 */
public class DefaultTWPlanner implements TWPlanner {
    private TWEnvironment env;
    private TWAgent agent;
    private static final int maxDistance = Integer.MAX_VALUE;
    private TWPathGenerator pathGenerator;

    public DefaultTWPlanner(TWEnvironment env, SimpleTWAgent agent){
        this.env = env;
        this.agent = agent;
        pathGenerator = new AstarPathGenerator(env, agent, maxDistance);
    }


    public TWPath generatePlan() {
        TWPath path = null;
        int agentX = agent.getX();
        int agentY = agent.getY();
        TWEntity tile = agent.getMemory().getSharedMemory().getClosestObject(agent, TWTile.class);
        TWEntity hole = agent.getMemory().getSharedMemory().getClosestObject(agent, TWHole.class);

        if (tile != null && agent.carriedNumber() <3){
            path = pathGenerator.findPath(agentX, agentY, tile.getX(), tile.getY());
        }else if(hole != null && agent.hasTile()){
            path = pathGenerator.findPath(agentX, agentY, hole.getX(), hole.getY());
        }

        return path;
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasPlan() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void voidPlan() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Int2D getCurrentGoal() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TWDirection execute() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

