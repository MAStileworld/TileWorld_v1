package tileworld.agent;

import sim.engine.Schedule;
import sim.field.grid.ObjectGrid2D;
import sim.util.Bag;
import sim.util.Int2D;
import sim.util.IntBag;
import tileworld.Parameters;
import tileworld.environment.*;

import java.util.ArrayList;
import java.util.List;


public class TWAgentWorkingMemorySingleton {
    /**
     * Access to Scedule (TWEnvironment) so that we can retrieve the current timestep of the simulation.
     */
    private Schedule schedule;

    private final static int MAX_TIME = 10;
    private final static float MEM_DECAY = 0.5f;

    private static volatile TWAgentWorkingMemorySingleton mInstance;

    private TWEnvironment mEnv;
    private ObjectGrid2D memoryGrid;

    /*
     * This was originally a queue ordered by the time at which the fact was observed.
     * However, when updating the memory a queue is very slow.
     * Here we trade off memory (in that we maintain a complete image of the map)
     * for speed of update. Updating the memory is a lot more straightforward.
     */
    private TWAgentPercept[][] objects;
    private List<TWAgent> agents = new ArrayList<>();
    /**
     * Number of items recorded in memory, currently doesn't decrease as memory
     * is not degraded - nothing is ever removed!
     */
    private int memorySize;



    static private List<Int2D> spiral = new NeighbourSpiral(Parameters.defaultSensorRange * 4).spiral();


    private TWAgentWorkingMemorySingleton(TWEnvironment env) {
        this.mEnv = env;
        this.objects = new TWAgentPercept[mEnv.getxDimension()][mEnv.getyDimension()];
        this.schedule = env.schedule;
        this.memoryGrid = new ObjectGrid2D(mEnv.getxDimension(), mEnv.getyDimension());

    }

    public static TWAgentWorkingMemorySingleton getInstance(TWEnvironment env) {
        if (mInstance == null) {
            synchronized (TWAgentWorkingMemorySingleton.class) {
                if (mInstance == null) {
                    mInstance = new TWAgentWorkingMemorySingleton(env);
                }
            }
        }
        return mInstance;
    }

    public void addAgent(TWAgent agent) {
        agents.add(agent);
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

    public void updateMemory(TWAgent currentAgent, Bag sensedObjects, IntBag objectXCoords, IntBag objectYCoords, Bag sensedAgents, IntBag agentXCoords, IntBag agentYCoords) {
        assert (sensedObjects.size() == objectXCoords.size() && sensedObjects.size() == objectYCoords.size());
        int agentX = currentAgent.getX();
        int agentY = currentAgent.getY();
        int sensorRange = Parameters.defaultSensorRange;
        for (int i = agentX - sensorRange; i <= agentX + sensorRange; i++) {
            for (int j = agentY - sensorRange; j <= agentY + sensorRange; j++) {
                if(currentAgent.getEnvironment().isInBounds(i, j)) {
                    boolean isFound = false;
                    for (Object obj : sensedObjects.objs) {
                        if (obj instanceof TWEntity && ((TWEntity) obj).getX() == i && ((TWEntity) obj).getY() == j) {
                            isFound = true;
                            TWEntity o = (TWEntity) obj;

                            TWAgentPercept previousPercept = objects[o.getX()][o.getY()];
                            if (previousPercept == null) {
                                addObject(o);
                            } else {
                                TWEntity previous = previousPercept.getO();
                                if (previous == null || !previous.getClass().isInstance(o)) {
                                    if (previous == null) {
                                        addObject(o);
                                    }else {
                                        replaceObject(o);
                                    }

                                } else {
                                    double observedTime = previousPercept.getT();
                                    if (schedule.getTime() - observedTime >= Parameters.lifeTime) {
                                        replaceObject(o);
                                    } else {
                                        // do nothing
                                    }
                                }
                            }
                            break;
                        }
                    }
                    if (!isFound) {
                        removeObject(i, j);
                    } else {
                        // do nothing
                    }
                } else {
                    continue;
                }
            }
        }
    }

    public void replaceObject(TWEntity entity){
        objects[entity.getX()][entity.getX()] = new TWAgentPercept(entity, schedule.getTime());
        memoryGrid.set(entity.getX(), entity.getY(), entity);
    }

    public void addObject(TWEntity entity){
        replaceObject(entity);
        memorySize++;
    }

    public void removeObject(int x, int y) {
        if (objects[x][y] != null) {
            memorySize--;
        }
        objects[x][y] = null;
        memoryGrid.set(x, y, null);
    }

    public void removeObject(TWEntity o){
        removeObject(o.getX(), o.getY());
    }

    /**
     * Finds a nearby tile we have seen less than threshold timesteps ago
     *
     * @see TWAgentWorkingMemorySingleton#getNearbyObject(int, int, double, java.lang.Class)
     */
    public TWTile getNearbyTile(int x, int y, double threshold) {
        return (TWTile) this.getNearbyObject(x, y, threshold, TWTile.class);
    }

    /**
     * Finds a nearby hole we have seen less than threshold timesteps ago
     *
     * @see TWAgentWorkingMemorySingleton#getNearbyObject(int, int, double, java.lang.Class)
     */
    public TWHole getNearbyHole(int x, int y, double threshold) {
        return (TWHole) this.getNearbyObject(x, y, threshold, TWHole.class);
    }


    /**
     * Returns the number of items currently in memory
     */
    public int getMemorySize() {
        return memorySize;
    }



    /**
     * Returns the nearest object that has been remembered recently where recently
     * is defined by a number of timesteps (threshold)
     *
     * If no Object is in memory which has been observed in the last threshold
     * timesteps it returns the most recently observed object. If there are no objects in
     * memory the method returns null. Note that specifying a threshold of one
     * will always return the most recently observed object. Specifying a threshold
     * of MAX_VALUE will always return the nearest remembered object.
     *
     * Also note that it is likely that nearby objects are also the most recently observed
     *
     *
     * @param sx coordinate from which to check for objects
     * @param sy coordinate from which to check for objects
     * @param threshold how recently we want to have seen the object
     * @param type the class of object we're looking for (Must inherit from TWObject, specifically tile or hole)
     * @return
     */
    private TWObject getNearbyObject(int sx, int sy, double threshold, Class<?> type) {

        //If we cannot find an object which we have seen recently, then we want
        //the one with maxTimestamp
        double maxTimestamp = 0;
        TWObject o = null;
        double time = 0;
        TWObject ret = null;
        int x, y;
        for (Int2D offset : spiral) {
            x = offset.x + sx;
            y = offset.y + sy;

            if (mEnv.isInBounds(x, y) && objects[x][y] != null) {
                o = (TWObject) objects[x][y].getO();//get mem object
                if (type.isInstance(o)) {//if it's not the type we're looking for do nothing

                    time = objects[x][y].getT();//get time of memory

                    if (schedule.getTime() - time <= threshold) {
                        //if we found one satisfying time, then return
                        return o;
                    } else if (time > maxTimestamp) {
                        //otherwise record the timestamp and the item in case
                        //it's the most recent one we see
                        ret = o;
                        maxTimestamp = time;
                    }
                }
            }
        }

        //this will either be null or the object of Class type which we have
        //seen most recently but longer ago than now-threshold.
        return ret;
    }

    public TWEntity getClosestObject(TWAgent agent, Class<?> clazz) {
        double minDis = mEnv.getxDimension() + mEnv.getyDimension() - 2;
        TWEntity closestEntity = null;
        for (int i = 0; i < mEnv.getxDimension(); i++) {
            for (int j = 0; j < mEnv.getyDimension(); j++) {
                Object obj = this.memoryGrid.get(i, j);
                if (clazz.isInstance(obj) && ((TWEntity) obj).getDistanceTo(agent.getX(), agent.getY()) <= minDis) {
                    minDis = ((TWEntity) obj).getDistanceTo(agent.getX(), agent.getY());
                    closestEntity = (TWEntity) obj;
                }
            }
        }
        return closestEntity;
    }

    /**
     * Is the cell blocked according to our memory?
     *
     * @param tx x position of cell
     * @param ty y position of cell
     * @return true if the cell is blocked in our memory
     */
    public boolean isCellBlocked(int tx, int ty) {

        //no memory at all, so assume not blocked
        if (objects[tx][ty] == null) {
            return false;
        }

        TWEntity e = (TWEntity) objects[tx][ty].getO();
        //is it an obstacle?
        return (e instanceof TWObstacle);
    }

    public ObjectGrid2D getMemoryGrid() {
        return this.memoryGrid;
    }
}
