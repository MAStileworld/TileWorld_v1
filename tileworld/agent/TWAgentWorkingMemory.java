package tileworld.agent;

import sim.engine.Schedule;
import sim.field.grid.ObjectGrid2D;
import sim.util.Bag;
import sim.util.IntBag;

/**
 * TWAgentMemory
 * 
 * @author michaellees
 * 
 *         Created: Apr 15, 2010 Copyright michaellees 2010
 * 
 *         Description:
 * 
 *         This class represents the memory of the TileWorld agents. It stores
 *         all objects which is has observed for a given period of time. You may
 *         want to develop an entirely new memory system by extending this one.
 * 
 *         The memory is supposed to have a probabilistic decay, whereby an element is
 *         removed from memory with a probability proportional to the length of
 *         time the element has been in memory. The maximum length of time which
 *         the agent can remember is specified as MAX_TIME. Any memories beyond
 *         this are automatically removed.
 */
public class TWAgentWorkingMemory {

	private TWAgentWorkingMemorySingleton sharedMemory;
	private TWAgent agent;

	public TWAgentWorkingMemory(TWAgent moi, Schedule schedule, int x, int y) {
		this.agent = moi;
		this.sharedMemory = TWAgentWorkingMemorySingleton.getInstance(this.agent.getEnvironment());
		this.sharedMemory.addAgent(this.agent);
	}

	public TWAgentWorkingMemorySingleton getSharedMemory() {
		return this.sharedMemory;
	}

	public ObjectGrid2D getMemoryGrid() {
		return this.sharedMemory.getMemoryGrid();
	}

	/**
	 * Called at each time step, updates the memory map of the agent.
	 * Note that some objects may disappear or be moved, in which case part of
	 * sensed may contain null objects
	 *
	 * Also note that currently the agent has no sense of moving objects, so
	 * an agent may remember the same object at two locations simultaneously.
	 * 
	 * Other agents in the grid are sensed and passed to this function. But it
	 * is currently not used for anything. Do remember that an agent sense itself
	 * too.
	 *
	 * @param sensedObjects bag containing the sensed objects
	 * @param objectXCoords bag containing x coordinates of objects
	 * @param objectYCoords bag containing y coordinates of object
	 * @param sensedAgents bag containing the sensed agents
	 * @param agentXCoords bag containing x coordinates of agents
	 * @param agentYCoords bag containing y coordinates of agents
	 */
	public void updateMemory(Bag sensedObjects, IntBag objectXCoords, IntBag objectYCoords, Bag sensedAgents, IntBag agentXCoords, IntBag agentYCoords) {
		this.sharedMemory.updateMemory(this.agent, sensedObjects, objectXCoords, objectYCoords, sensedAgents, agentXCoords, agentYCoords);
	}
}
