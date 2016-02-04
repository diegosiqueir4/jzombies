/**
 * The Zombies behavior is to wander around looking for Humans to infect. More specifically,
each iteration of the simulation, each Zombie will determine where the most Humans are
within its local area and move there. Once there it will attempt to infect a Human at that
location and turn it into a Zombie.
 */
package jzombies;

import java.util.ArrayList;
import java.util.List;
import repast.simphony.context.Context;
import repast.simphony.util.ContextUtils;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;
import repast.simphony.query.space.grid.GridCell;

/**
 * @author d_desi01
 *We will now create our Zombie and Human classes.
  (1) Right click on the jzombies folder under src
  (2) Select New then Class from the Menu (Fig. 6)
  (3) Type Zombie for the name
  (4) Optionally, click the generate comments box. This won’t comment the code for
  you, of course, but it does leave a placeholder
 */
public class Zombie {
	
	private ContinuousSpace < Object > space ; /* The Zombie will move about the ContinuousSpace and we will simply round the ContinuousSpace location
	 											to determine the corresponding Grid location*/
												
	private Grid < Object > grid ; /* The space and grid variables have Object as their template parameter. 
									This allows us to put anything in them and prevents Eclipse from giving us spurious warnings.*/
	private boolean moved;
	
	public Zombie (ContinuousSpace < Object > space , Grid < Object > grid ) {
		this.space = space;
		this.grid = grid;
	}
	
	/*  We want the step method to be called every iteration of the simulation. We can do this
		by adding an @ScheduledMethod annotation on it. Obviously, the method itself is part of a
		class, and thus what we are actually scheduling are invocations of this method on instances
		of this class. For example, if we have 10 Zombies, then we can schedule this method to be
		called on each of those Zombies. The annotation has a variety of parameters that are used
		to specify when and how often the method will be called, the number of object instances
		to invoke the method on, and so on. */
	@ScheduledMethod ( start = 1 , interval = 1)


	public void step(){
		//get the grid location of this Zombie
		GridPoint pt = grid.getLocation(this);
		
		//use the GridCellNgh class to create GridCells for the surrounding neighborhood
		GridCellNgh<Human> nghCreator = new GridCellNgh<Human>(grid, pt, Human.class, 1, 1);
		
		//import repast . simphony . query . space . grid . GridCell
		//I add "java.util.List" instead of just "List"
		java.util.List<GridCell<Human>> gridCells = nghCreator.getNeighborhood(true);
		
		//Using SimUtilities.shuffle, we shuffle the list of GridCells. Without the shuffle, the Zombies will always move in the same direction when all cells are equal.
		// We use the RandomHelper class to provide us with a random generator for the shuffle
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
		GridPoint pointWithMostHumans = null ;
		int maxCount = -1;
		//  A GridCell’s size is a measure of the number of objects it contains and thus the cell with the greatest size contains the most Humans.
		for ( GridCell <Human> cell : gridCells ) {
			if ( cell.size () > maxCount ) {
				pointWithMostHumans = cell.getPoint();
				maxCount = cell.size();
			}
		}	
		// see description of the method below
		moveTowards ( pointWithMostHumans );				
		//see description of the method below
		infect();

	}
	
	/*Now that we have discovered the location with the most Humans (i.e. pointWithMostHumans),
	we want to move the Zombie towards that location. We will first write this method then
	add the code to call it in the step() method.*/
	
	public void moveTowards(GridPoint pt){
		// only move if we are not already in this grid location
		if (!pt.equals ( grid . getLocation ( this ))) {
			NdPoint myPoint = space . getLocation (this);
			NdPoint otherPoint = new NdPoint ( pt.getX(), pt.getY());
			
			double angle = SpatialMath . calcAngleFor2DMovement ( space , myPoint , otherPoint );
			
			space . moveByVector ( this , 1 , angle , 0);
			myPoint = space . getLocation ( this );
			
			grid . moveTo ( this , ( int ) myPoint.getX() , ( int ) myPoint.getY());
				
			/*this flag is watched by the human class
			 * What this means is whenever any Zombie moves and their moved variable is updated,
			 * then this Watch will be checked for each Human. If the query returns true for that particular Human then run
			 * will be called immediately on that Human*/
			moved = true;		
		}
	}
			
	public void infect() {
		if (countHuman().size() > 0) {
			int index = RandomHelper.nextIntFromTo(0, countHuman().size() - 1);
			Object obj = countHuman().get(index);
			NdPoint spacePt = space.getLocation(obj);
			Context<Object> context = ContextUtils.getContext(obj);
			context.remove(obj);
			Zombie zombie = new Zombie(space, grid);
			context.add(zombie);
			
			GridPoint pt = grid.getLocation(this);
			space.moveTo(zombie, spacePt.getX(), spacePt.getY());
			grid.moveTo(zombie, pt.getX(), pt.getY());
			
			Network<Object> net = (Network<Object>)context.getProjection("infection network");
			net.addEdge(this, zombie);
		}
	}
	
	
	public java.util.List<Object> countHuman(){
		GridPoint pt = grid.getLocation(this);
		java.util.List<Object> humans = new ArrayList<Object>();
		for (Object obj : grid.getObjectsAt(pt.getX(), pt.getY())) {
			if (obj instanceof Human) {
				humans.add(obj);
			}
		}
		return humans;
	}
	
}
