package model.entity.enemy.entities;

import java.util.Optional;

import model.entity.Player;
import model.entity.detectable.LiftManager;
import model.entity.enemy.Enemy;
import model.level.Level;
import model.util.MapReader;

/**
 * Ball is an Enemy that represents a simple floating ball that continuously chase the Player.
 */
public class Ball extends Enemy {

	// Ball hitbox
	private static final int BALL_HITBOX_WIDTH = 66;
	private static final int BALL_HITBOX_HEIGHT = 47;
	// Ball atkbox
	private static final int AT_OFFSET = 18;
	private static final int AT_WIDTH = BALL_HITBOX_WIDTH + (AT_OFFSET * 2);
	private static final int AT_HEIGHT = BALL_HITBOX_HEIGHT + (AT_OFFSET * 2);
	// Ball speed
	private static final float ENEMY_SPEED = 0.75f;
	// Ball status check
	private boolean destroyed;

	/**
	 * Ball constructor that sets it's hitbox and atkbox
	 * 
	 * @param x  Position x to be place in
	 * @param y  Position y to be place in
	 */
	public Ball(float x, float y) {
		super(x, y, BALL_HITBOX_WIDTH, BALL_HITBOX_HEIGHT);
		initHitBox(x, y, BALL_HITBOX_WIDTH, BALL_HITBOX_HEIGHT);
		initAttackBox(x, y, AT_WIDTH, AT_HEIGHT, AT_OFFSET, AT_OFFSET);
		destroyed = false;
	}

	/**
	 * Updates the position and the atkbox if isn't destroyed
	 * 
	 * @param player  Used to read the player position for the chase
	 * @param lvData  Used to read the level collision
	 * @param lfData  Used to read the lifts collision in the level
	 */
	public void update(Player player, int[][] lvData, Optional<LiftManager> lfData) {
		if (!destroyed) {
			updatePos(player, lvData, lfData);
			updateAtk();
		}
	}

	/**
	 * Update the position of the ball, always chasing the Player
	 * 
	 * @param player  Used to read the player position for the chase
	 * @param lvData  Used to read the level collision
	 * @param lfData  Used to read the lifts collision in the level
	 */
	private void updatePos(Player player, int[][] lvData, Optional<LiftManager> lfData) {
		// ySpeed used for the y position
		float ySpeed = 0;
		// Under it
		if (player.getHitbox().getCenterY() > hitbox.getCenterY())
			ySpeed += ENEMY_SPEED;
		// Above it
		else if (player.getHitbox().getCenterY() < hitbox.getCenterY())
			ySpeed -= ENEMY_SPEED;
		// If can move goes ahead
		if (MapReader.directionalIsPathFree(hitbox, ySpeed, false, lvData, lfData))
			hitbox.y += ySpeed;
		// xSpeed used for the x position
		float xSpeed = 0;
		// On it's right
		if (player.getHitbox().getCenterX() > hitbox.getCenterX())
			xSpeed += ENEMY_SPEED;
		// On it's left
		else if (player.getHitbox().getCenterX() < hitbox.getCenterX())
			xSpeed -= ENEMY_SPEED;
		// If can move goes ahead
		if (MapReader.directionalIsPathFree(hitbox, xSpeed, true, lvData, lfData))
			hitbox.x += xSpeed;
		// If goes under the level it teleport above
		if (hitbox.getMinY() > Level.LV_REAL_HEIGHT - 1) {
			hitbox.y = -72;
			hitbox.x = Math.max(72, Math.min(hitbox.x, Level.LV_REAL_HEIGHT - 72));
		}
	}

	/**
	 * Update to keep the atkbox attached to the hitbox
	 */
	@Override
	protected void updateAtk() {
		atkbox.x = hitbox.x - atkOffsetW;
		atkbox.y = hitbox.y - atkOffsetH;
	}

	/**
	 * Destroys the ball
	 */
	public void destruction() {
		destroyed = true;
	}

	/**
	 * Destruction check
	 * 
	 * @return boolean  Result of the state of the ball
	 */
	public boolean isDestroyed() {
		return destroyed;
	}

	/**
	 * Put the ball in the original state
	 */
	public void reset() {
		hitbox.x = x;
		hitbox.y = y;
		updateAtk();
		destroyed = false;
	}

}
