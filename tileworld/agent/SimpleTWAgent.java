/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.agent;

import tileworld.environment.*;
import tileworld.exceptions.CellBlockedException;
import tileworld.planners.*;

/**
 * TWContextBuilder
 *
 * @author michaellees
 * Created: Feb 6, 2011
 *
 * Copyright michaellees Expression year is undefined on line 16, column 24 in Templates/Classes/Class.java.
 *
 *
 * Description:
 *
 */
public class SimpleTWAgent extends TWAgent{
	private String name;
    private TWPathGenerator pathGenerator;
    private TWPlanner twPlanner;
    private TWAgentWorkingMemorySingleton sharedMemory;
    public SimpleTWAgent(String name, int xpos, int ypos, TWEnvironment env, double fuelLevel) {
        super(xpos,ypos,env,fuelLevel);
        this.name = name;
        this.twPlanner = new DefaultTWPlanner(env, this);
        this.sharedMemory = TWAgentWorkingMemorySingleton.getInstance(env);
    }

    protected TWThought think() {

//        getMemory().getClosestObjectInSensorRange(Tile.class);
        System.out.println("Simple Score: " + this.score);
        Object object = memory.getMemoryGrid().get(x, y);

        if(object instanceof TWTile && carriedTiles.size() < 3){
            return new TWThought(TWAction.PICKUP, TWDirection.Z);
        }else if(object instanceof TWHole && this.hasTile()){
            return new TWThought(TWAction.PUTDOWN, TWDirection.Z);
        }else if (object instanceof TWFuelStation) {
            return new TWThought(TWAction.REFUEL, TWDirection.Z);
        }else {
            TWPath path = twPlanner.generatePlan();
            if (path == null){
                return new TWThought(TWAction.MOVE, getRandomDirection());
            }else {
                TWPathStep pathStep = path.popNext();
                return new TWThought(TWAction.MOVE, pathStep.getDirection());
            }
        }
    }

    @Override
    protected void act(TWThought thought) {

        //You can do:
        //move(thought.getDirection())
        //pickUpTile(Tile)
        //putTileInHole(Hole)
        //refuel()
        Object object = memory.getMemoryGrid().get(x, y);
        switch (thought.getAction()){

            case MOVE:
                try {
                    this.move(thought.getDirection());
                } catch (CellBlockedException ex) {
                    // Cell is blocked, replan?
                }
                break;
            case PICKUP:
                pickUpTile((TWTile) object);
                this.getMemory().getMemoryGrid().set(x, y, null);
                break;
            case PUTDOWN:
                putTileInHole((TWHole) object);
                this.getMemory().getMemoryGrid().set(x, y, null);
                break;
            case REFUEL:
                refuel();
                break;
        }

    }


    private TWDirection getRandomDirection(){

        TWDirection randomDir = TWDirection.values()[this.getEnvironment().random.nextInt(5)];

        if(this.getX()>=this.getEnvironment().getxDimension() ){
            randomDir = TWDirection.W;
        }else if(this.getX()<=1 ){
            randomDir = TWDirection.E;
        }else if(this.getY()<=1 ){
            randomDir = TWDirection.S;
        }else if(this.getY()>=this.getEnvironment().getxDimension() ){
            randomDir = TWDirection.N;
        }

       return randomDir;

    }

    @Override
    public String getName() {
        return name;
    }
}
