/**
 * The basic behavior for a Human is to react when a Zombie comes within its local neighborhood by running away from the area with the most Zombies. 
 * Additionally, Humans have a certain amount of energy that is expended in running away. If this energy is 0 or less then a Human is unable to run
 */
package jzombies;

import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;


/**
 * @author d_desi
 *
 */
public class Human {
	
	private ContinuousSpace < Object > space ;
	private Grid < Object > grid ;
	private int energy , startingEnergy ;
	
	public Human(ContinuousSpace<Object> space, Grid<Object> grid, int energy){
		this.space = space ;
		this.grid = grid ;
		this.energy = startingEnergy = energy ;
	}
	
	/*	Unlike the Zombie code we are not going to schedule the run() method for execution.
		Rather we are going to setup a watcher that will trigger this run() method whenever a
		Zombie moves into a Human’s neighborhood. We do this using the @Watch annotation.
		The @Watch annotation requires the class name of the class to watch, as well as a field
		within that class. The watcher will trigger whenever this field has been accessed. We can
		also define a query on the @Watch to further specify when the watcher will trigger	*/
	
	@Watch (watcheeClassName = "jzombies.Zombie",
			watcheeFieldNames = "moved",
			query = "within_moore 1",
			whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	/*This Watch will watch for any changes to a “moved” variable in the Zombies class. 
	 * What this means is whenever any Zombie moves and their moved variable is updated,
	 * then this Watch will be checked for each Human. If the query returns true for that particular Human then run
	 * will be called immediately on that Human*/

	public void run () {
		// get the grid location of this Human
		GridPoint pt = grid . getLocation ( this );
		// use the GridCellNgh class to create GridCells for
		// the surrounding neighborhood.
		GridCellNgh < Zombie > nghCreator = new GridCellNgh < Zombie >( grid , pt ,	Zombie . class , 1 , 1);
		// import repast.simphony.query.space.grid.GridCell
		//I add "java.util.List" instead of just "List"
		java.util.List<GridCell<Zombie>> gridCells = nghCreator.getNeighborhood(true);
		SimUtilities . shuffle ( gridCells , RandomHelper . getUniform ());
			
		GridPoint pointWithLeastZombies = null ;
		int minCount = Integer . MAX_VALUE ;
		for ( GridCell < Zombie > cell : gridCells ) {
			if ( cell . size () < minCount ) {
				pointWithLeastZombies = cell . getPoint ();
				minCount = cell . size ();
			}
		}
		
		if ( energy > 0) {
			moveTowards ( pointWithLeastZombies );
		} else {
			energy = startingEnergy ;
		}
	}

	private void moveTowards(GridPoint pt) {
		// only move if we are not already in this grid location
		if (! pt . equals ( grid . getLocation ( this ))) {
			NdPoint myPoint = space . getLocation ( this );
			NdPoint otherPoint = new NdPoint ( pt . getX () , pt . getY ());
			
			double angle = SpatialMath.calcAngleFor2DMovement ( space , myPoint ,otherPoint );
			
			space.moveByVector ( this , 2 , angle , 0);
			myPoint = space.getLocation ( this );
			grid.moveTo( this , ( int ) myPoint . getX () , ( int ) myPoint . getY ());
			energy --;
		}
		
	}

}
