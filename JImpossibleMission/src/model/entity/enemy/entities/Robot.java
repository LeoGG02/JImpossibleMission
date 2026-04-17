package model.entity.enemy.entities;

import java.util.Random;

import model.entity.Player;
import model.entity.enemy.Enemy;
import model.util.MapReader;

/**
 * Robot is an Enemy that represents a simple grounded robot that can have different behavior upon the creation
 * (depending also if it has the range attack).
 * The behavior that Robot can have is Roamer, Chaser, Patroller and Sentry.
 */
public class Robot extends Enemy {
	
	private static final Random r = new Random();

	// Width and height of the robot
	private static final int ROBOT_HITBOX_WIDTH = 42;
	private static final int ROBOT_HITBOX_HEIGHT = 57;
	// Atkbox datas
	private static final int AT_OFFSET = 25;
	private static final int AT_WIDTH = ROBOT_HITBOX_WIDTH + (AT_OFFSET * 2);
	private static final int AT_HEIGHT = 30;
	private static final int AT_RANGE = 120;
	// Robot datas
	private static final int DETECTION_RANGE = 250;
	private static final int FINAL_TICK = 48;
	private static final float WALK_SPEED = 1.5f;
	// Robots has 4 different behaviors that also depends if has the range attack
	private final RobotStrategy[] strategies = {new Roamer(), new Chaser(), new Patroller(), new Sentry()};
	private RobotStrategy currentStrategy;
	// RIGHT = true LEFT = false
	private boolean direction, originalDirection;
	// Robot States
	public enum RobotAction {
		IDLE, ROAM, TURN, ATTACK;
	}
	private RobotAction rAction;
	// First update is to put the robot on the ground upon creation 
	private boolean firstUpdate;
	// Check if the robot can do the range attack
	private boolean range;

	// Tick used during an action + current speed & original speed
	private int actionTick, currentSpeed, originalSpeed;

	/**
	 * Robot constructor that sets it's hitbox and atkbox
	 * 
	 * @param x  Position x to be place in
	 * @param y  Position y to be place in
	 */
	public Robot(float x, float y) {
		super(x, y, ROBOT_HITBOX_WIDTH, ROBOT_HITBOX_HEIGHT);
		initHitBox(x, y, ROBOT_HITBOX_WIDTH, ROBOT_HITBOX_HEIGHT);
		initAttackBox(x, y, AT_WIDTH, AT_HEIGHT, AT_OFFSET, 0);
		initRobot();
	}

	/**
	 * Initialize Robot's datas
	 */
	private void initRobot() {
		// Random selection of the Robot's behavior
		currentStrategy = strategies[r.nextInt(strategies.length)];
		// Random direction on creation
		direction = originalDirection = r.nextBoolean();
		// Always true upon creation
		firstUpdate = true;
		// Robot has a random speed on creation between 1 or 2
		currentSpeed = originalSpeed = r.nextInt(1, 3);
		// Robot has 1/5 probability to have the range attacks
		range = r.nextInt(5) < 1;
		// All Robots starts in IDLE state
		rAction = RobotAction.IDLE;
	}

	/**
	 * Updates both the position and the atkbox
	 * 
	 * @param lvData  Used to navigate through the level
	 * @param player  To read his position in case this robot can detects him
	 */
	public void update(int[][] lvData, Player player) {
		updatePos(lvData, player);
		updateAtk();
	}

	/**
	 * Update the position of the robot
	 * 
	 * @param lvData  Used to navigate through the level
	 * @param p       To read his position in case this robot can detects him
	 */
	private void updatePos(int[][] lvData, Player player) {
		// If firstUpdate is true that means that this robot is just been created and have to place it on the ground.
		// Afterwards it stops being used until the level get resetted where the robot have to be place again
		if (firstUpdate) {
			if (!MapReader.isOnTheFloor(hitbox, lvData))
				hitbox.y = MapReader.hitTheFloor(hitbox);
			firstUpdate = false;
		}
		// Execute the current strategy action
		currentStrategy.execute(lvData, player);
	}

	/**
	 * Move command, returns a boolean (true if can move, false if can't)
	 * 
	 * @param lvData    Used to navigate through the level
	 * 
	 * @return boolean  Returns the result of the movement, if is true that means the Robot moved, if not then gives false
	 */
	private boolean move(int[][] lvData) {
		// xSpeed used to change x
		float xSpeed = WALK_SPEED * (direction ? 1 : -1) * currentSpeed;
		
		if (MapReader.isPathFree(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvData) 
				&& MapReader.checkFloor(hitbox, xSpeed, lvData, direction)) {
			hitbox.x += xSpeed;
			return true;
		}
		return false;
	}

	/**
	 * Uses actionTick as some sort of timer that hold the Robot in place
	 * After reaching the half of FINAL_TICK changes direction 
	 */
	private void turn() {
		actionTick++;
		if (actionTick == (FINAL_TICK / 2) / currentSpeed)
			direction = !direction;
		else if (actionTick == FINAL_TICK / currentSpeed) {
			rAction = RobotAction.IDLE;
			actionTick = 0;
		}
	}

	/**
	 * Uses actionTick as some sort of timer that hold the Robot in place
	 * Returns true if still holding, once is done returns false
	 * 
	 * @return boolean  Returns the result if the robot is still holding it's place or not
	 */
	private boolean hold() {
		if (actionTick > FINAL_TICK * 5 / currentSpeed) {
			actionTick = 0;
			return false;
		}
		else
			actionTick ++;
		return true;
	}

	/**
	 * Update to keep the atkbox attached to the hitbox. It also increase the range if does the zap attack
	 */
	@Override
	protected void updateAtk() {
		atkbox.x = hitbox.x - atkOffsetW;
		atkbox.y = hitbox.y;
		atkbox.width = AT_WIDTH;
		if (rAction == RobotAction.ATTACK) {
			atkbox.width += AT_RANGE;
			if (!direction)
				atkbox.x -= AT_RANGE;
		}
			
	}

	/**
	 * Put the robot in the original state
	 */
	public void reset() {
		hitbox.x = x;
		hitbox.y = y;
		updateAtk();
		actionTick = 0;
		direction = originalDirection;
		currentSpeed = originalSpeed;
		firstUpdate = true;
		rAction = RobotAction.IDLE;
	}

	/*
	 * Getters
	 */
	public boolean isRight() {
		return direction;
	}

	public int getActionTick() {
		return actionTick;
	}

	public int getCurrentSpeed() {
		return currentSpeed;
	}
	
	public RobotAction getRobotAction() {
		return rAction;
	}

	/**
	 * Strategies for the robots behavior
	 */
	private interface RobotStrategy {
		/**
		 * Execute the action
		 * 
		 * @param lvData  Used to navigate through the level
		 * @param p       To read his position in case this robot can detects him
		 */
		public void execute(int[][] lvData, Player player);
	}

	/**
	 * Simply makes the robot roam around unaware of the Player,
	 * if has range each time it stops or turns does the range attack
	 */
	private class Roamer implements RobotStrategy {

		// Tells if is right after a turn
		private boolean afterTurn = false;
	
		/**
		 * Execute the action
		 * 
		 * @param lvData  Used to navigate through the level
		 * @param p       To read his position in case this robot can detects him
		 */
		@Override
		public void execute(int[][] lvData, Player player) {
			switch (rAction) {
			// If has range goes to ATTACK state if not goes to ROAM
			case IDLE -> {
				rAction = range ? RobotAction.ATTACK : RobotAction.ROAM;
				}
			// Moves until it stops, if has range goes to ATTACK state if not goes to TURN
			case ROAM -> {
				if (!move(lvData))
					rAction = range ? RobotAction.ATTACK : RobotAction.TURN;
				}
			// Turns and set the afterTurn
			case TURN -> {
				turn();
				afterTurn = true;
				}
			// Stays on position while attacking, once done if is after the turn goes to ROAM resetting the afterTurn too,
			// if not goes to TURN
			case ATTACK -> {
				if (!hold()) {
					rAction = afterTurn ? RobotAction.ROAM : RobotAction.TURN;
					afterTurn = afterTurn ? false : afterTurn;
				}
				}
			}
		}
		
	}

	/**
	 * Makes the robot continuously chase the Player, if has range, once the Player is in the same platform,
	 * it does the range attack
	 */
	private class Chaser implements RobotStrategy {

		/**
		 * Execute the action
		 * 
		 * @param lvData  Used to navigate through the level
		 * @param p       To read his position in case this robot can detects him
		 */
		@Override
		public void execute(int[][] lvData, Player player) {
			// Checks for player detection and the distance between them
			boolean playerDetectionFloor = player.getHitbox().getCenterY() > hitbox.getMinY() &&
					player.getHitbox().getCenterY() < hitbox.getMaxY();
			int playerDistance = (int)((player.getHitbox().getCenterX() - hitbox.getCenterX()) * (direction? 1 : -1));
			
			switch (rAction) {
			// Directly goes to ROAM
			case IDLE -> rAction = RobotAction.ROAM;
			// Chases the player by reading the player position, if the distance is negative it TURN,
			// if has range and is close enough goes to ATTACK
			case ROAM -> {
				move(lvData);
				if (range && playerDetectionFloor && playerDistance < DETECTION_RANGE)
					rAction = RobotAction.ATTACK;
				if (playerDistance < 0)
					rAction = RobotAction.TURN;
				}
			// Simply turns
			case TURN -> turn();
			// After staying for attack goes to IDLE
			case ATTACK -> {
				if (!hold())
					rAction = RobotAction.IDLE;
				}
			}
		}
		
	}

	/**
	 * Makes the robot patrol, stop every now and then and chases once it detects the Player in front of it,
	 * if has range it does the range attack each time it stops or when the player is in front of it
	 */
	private class Patroller implements RobotStrategy {

		/**
		 * Execute the action
		 * 
		 * @param lvData  Used to navigate through the level
		 * @param p       To read his position in case this robot can detects him
		 */
		@Override
		public void execute(int[][] lvData, Player player) {
			// Checks for player detection on the same platform and the distance between them
			boolean playerDetectionFloor = player.getHitbox().getCenterY() > hitbox.getMinY() &&
					player.getHitbox().getCenterY() < hitbox.getMaxY();
			int playerDistance = (int)((player.getHitbox().getCenterX() - hitbox.getCenterX()) * (direction? 1 : -1));
			boolean playerOnDirection = playerDistance > 0;
			boolean playerDetect = playerDetectionFloor && playerOnDirection;
			
			switch (rAction) {
			// After is done holding to it's place it goes to ROAM, if detects the player in front of it
			// changes to ATTACK or ROAM depending if the Robot has range
			case IDLE -> {
				if (!hold()) {
					rAction = RobotAction.ROAM;
					if (playerDetect) {
						actionTick = 0;
						rAction = range ? rAction = RobotAction.ATTACK : RobotAction.ROAM;
					}
				}
				}
			// Roams just for a bit until goes to idle, unless it detects the player (going ATTACK if has range and being close enough)
			// switching the speed too by 2, if doesn't detects reset the speed. If can't move further if detects the player keeps on
			// ROAM or goes to ATTACK (depending if has range), if not goes to TURN
			case ROAM -> {
				if (actionTick > FINAL_TICK / currentSpeed) {
					rAction = RobotAction.IDLE;
					actionTick = 0;
				}
				else {
					if (playerDetect) {
						currentSpeed = currentSpeed != 2 ? 2 : currentSpeed;
						if (range && playerDistance < DETECTION_RANGE) {
							actionTick = 0;
							rAction = RobotAction.ATTACK;
						}
					}
					else {
						actionTick++;
						currentSpeed = currentSpeed != originalSpeed ? originalSpeed : currentSpeed;
					}
					
					if (!move(lvData)) {
						actionTick = 0;
						rAction = playerDetect ? (range ? RobotAction.ATTACK : RobotAction.ROAM) : RobotAction.TURN;
					}
				}
				}
			// Simply turns
			case TURN -> turn();
			// After staying for attack goes to ROAM
			case ATTACK -> {
				if (!hold())
					rAction = RobotAction.ROAM;
				}
			}
		}
		
	}

	/**
	 * Makes the robot stationary basically acting like wall hazard,
	 * if has range will continuously turn and range attack without moving
	 */
	private class Sentry implements RobotStrategy {

		/**
		 * Execute the action
		 * 
		 * @param lvData  Used to navigate through the level
		 * @param p       To read his position in case this robot can detects him
		 */
		@Override
		public void execute(int[][] lvData, Player p) {
			switch (rAction) {
			// If has range goes to ATTACK, if not it stays stationary
			case IDLE -> {
				if (range)
					rAction = RobotAction.ATTACK;
				}
			// Since a stationary strategy doen't have a ROAM actions
			case ROAM -> {}
			// Simply turns
			case TURN -> turn();
			// After staying for attack goes to TURN
			case ATTACK -> {
				if (!hold())
					rAction = RobotAction.TURN;
				}
			}
		}
		
	}
}
