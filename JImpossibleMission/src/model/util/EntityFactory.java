package model.util;

import model.entity.Entity;
import model.entity.detectable.entities.*;
import model.entity.enemy.entities.*;

/**
 * Factory used for the creation of the entities
 */
public class EntityFactory {
	
	/**
	 * Types of entity
	 */
	public enum EntityTypes {
		ROBOT, BALL, LIFT, TERMINAL, FURNITURE, CONTROL_ROOM;
	}
	
	/**
	 * Gets 4 integers that acts like important datas for the creation of the Entity
	 * 
	 * @throws IllegalArgumentException  Used in case the type isn't one of the entities to create
	 * 
	 * @param type                       Is used to identify which type of Entity that have to create
	 * @param x                          Position x used to place the entity
	 * @param y                          Position y used to place the entity
	 * @param specialData                Is used upon creation of certain entities
	 * (LIFT to place a lift in the position and FURNITURE to tell which type to get)
	 * 
	 * @return Entity                    Gives the entity requested (can be null)
	 */
	public static Entity createEntity(int type, int x, int y, int specialData) {
		// If the type doesn't falls to any type of entity, it throws an IllegalArgumentException to prevent going ahead
		if (type > 5 || type < 0)
			throw new IllegalArgumentException("This entity type doesn't exists.");
		
		Entity result = null;
		
		switch(EntityTypes.values()[type]) {
		case ROBOT -> result = new Robot(x, y);
		case BALL -> result = new Ball(x, y);
		// If the special data is higher than -1 (0 and beyond) that means it is the currentFloor data that Lift class needs
		case LIFT -> result = specialData > -1 ? new Lift(x, y, specialData) : result;
		// The special data is used to see which type of furniture is it
		case FURNITURE -> result = new Furniture(x, y, specialData);
		case TERMINAL -> result = new Terminal(x, y);
		case CONTROL_ROOM -> result = new ControlRoom(x, y);
		}
		
		return result;
	}
	
}
