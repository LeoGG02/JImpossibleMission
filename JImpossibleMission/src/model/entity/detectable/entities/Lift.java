package model.entity.detectable.entities;

import java.awt.geom.Rectangle2D;

import model.entity.Entity;
import model.entity.detectable.Detectable;

/**
 * Entity that represents a Lift platform for the Player to stand on and control.
 */
public class Lift extends Detectable {

	// Lift hitbox
	private static final int LIFT_WIDTH = 72;
	private static final int LIFT_HEIGHT = 24;
	// Lift dtcbox
	private static final int DT_OFFSET = 4;
	private static final int DT_HEIGHT = LIFT_HEIGHT + (DT_OFFSET * 2);
	// Speed of the lift
	private static final float DEFAULT_SPEED = 4f;
	// Current floor & original position upon creation
	private int currentFloor, originalFloor;
	// Destination + movement speed
	private float destination, move;

	/**
	 * Lift contructor that set the position and the current floor is in
	 * 
	 * @param x             Position x where the entity spawns into
	 * @param y             Position y where the entity spawns into
	 * @param currentFloor  Current floor that is in
	 */
	public Lift(float x, float y, int currentFloor) {
		super(x, y, LIFT_WIDTH, LIFT_HEIGHT);
		initHitBox(x, y, LIFT_WIDTH, LIFT_HEIGHT);
		initDetectBox(x, y - DT_OFFSET, LIFT_WIDTH, DT_HEIGHT);
		this.currentFloor = originalFloor = currentFloor;
		move = 0;
	}
	
	/**
	 * Update if the lift is moving
	 */
	public void update() {
		if (move != 0) {
			updatePos();
			updateDtcbox();
		}
	}

	/**
	 * Update the position of the lift if has an active action
	 */
	private void updatePos() {
		hitbox.y += move;
		// If reaches the destination place y and sets move back to 0
		if ((move < 0 && hitbox.y <= destination) || (move > 0 && hitbox.y >= destination)) {
			hitbox.y = destination;
			move = 0;
		}
	}

	/**
	 * Update to keep the dtcbox attached to the hitbox
	 */
	private void updateDtcbox() {
		dtcbox.x = hitbox.x;
		dtcbox.y = hitbox.y - DT_OFFSET;
	}

	/**
	 * Allows to give the command to go up or down
	 * 
	 * @param command         Command that can be UP(-1) or DOWN(1)
	 * @param newDestination  Destination where the Lift have to reach
	 */
	public void changeStatus(int command, float newDestination) {
		destination = newDestination;
		currentFloor = currentFloor + command;
		move = DEFAULT_SPEED * command;
	}

	/**
	 * Checks if the entity given is on the way of a moving platform
	 * 
	 * @param e  Entity to check
	 */
	public boolean isInTheWay(Entity e) {
		return e.getHitbox().intersects(new Rectangle2D.Float(hitbox.x, hitbox.y + move, hitbox.width, hitbox.height));
	}
	
	/**
	 * Given an entity, it pushes on the direction is going
	 * 
	 * @param e  Entity to push
	 */
	public void push(Entity e) {
		e.getHitbox().y += move;
	}

	/**
	 * Checks the position inserted in input is within the lift hitbox
	 * 
	 * @param x  Position x to check
	 * @param y  Position y to check
	 */
	public boolean isIn(float x, float y) {
		return hitbox.contains(x, y);
	}

	/*
	 * Getters
	 */
	public float getStatus() {
		return move;
	}

	public int getCurrentFloor() {
		return currentFloor;
	}

	/**
	 * Put the lift in the original state
	 */
	public void reset() {
		hitbox.x = x;
		hitbox.y = y;
		updateDtcbox();
		currentFloor = originalFloor;
	}

}
