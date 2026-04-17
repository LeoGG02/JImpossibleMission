package model.entity.enemy;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import model.entity.Player;
import model.entity.detectable.LiftManager;
import model.entity.enemy.entities.*;

/**
 * Main manager used for the enemies to update their position and help destroying/kill entities on their range.
 */
public class EnemyManager {

	// Robots
	private List<Robot> robots;
	// Ball
	private Optional<Ball> ball;
	private int[][] lvData;
	// Enemy state ON or OFF
	private boolean enemiesActive;

	/**
	 * EnemyManager constructor that collects the enemies and set lvData
	 * 
	 * @param robots  All the robots
	 * @param ball    Ball enemy if present
	 * @param lvData  Used to help the enemies traverse the level
	 */
	public EnemyManager(List<Robot> robots, Optional<Ball> ball, int[][] lvData) {
		this.robots = robots;
		this.ball = ball;
		this.lvData = lvData;
		enemiesActive = true;
	}

	/**
	 * Update the robots movement and checks if the Player is in the their atkbox so they can kill
	 * 
	 * @param player  Reads the player action and be killed
	 * @param lfData  Reads the lfData too for traverse
	 */
	public void update(Player player, Optional<LiftManager> lfData) {
		// If the robots are active update, else disables all active robots
		if (enemiesActive) {
			// Robot update
			robots.forEach(r -> {
				r.update(lvData, player);
				// If is in contact with the player it kills him
				if (r.inContact(player))
					player.kill();
				// If is in contact with the ball it destroys it
				ball.ifPresent(ball -> {
					if (r.inContact(ball))
						ball.destruction();
				});
			});
			// Ball update
			ball.ifPresent(ball -> {
				ball.update(player, lvData, lfData);
				// If is in contact with the player and not destroyed it kills him
				if (ball.inContact(player) && !ball.isDestroyed())
					player.kill();
			});
		}
	}

	/**
	 * Disable all enemies
	 */
	public void sleepAll() {
		enemiesActive = false;
	}

	/**
	 * Enable all enemies
	 */
	public void wakeAll() {
		enemiesActive = true;
	}

	/**
	 * Reset all the enemies in their original state and position
	 */
	public void resetAllEnemies() {
		robots.forEach(r -> r.reset());
		ball.ifPresent(b -> b.reset());
		enemiesActive = true;
	}

	/*
	 * Getters
	 */
	public List<Robot> getRobots() {
		return robots;
	}

	public Optional<Ball> getBall() {
		return ball;
	}
	
	public boolean areActive() {
		return enemiesActive;
	}

	/**
	 * equals with Robot list and Ball optional
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EnemyManager) {
			EnemyManager other = (EnemyManager)obj;
			return robots.equals(other.getRobots()) && ball.equals(other.getBall());
		}
		return false;
	}

	/**
	 * hashCode of Robot list and Ball optional
	 */
	@Override
	public int hashCode() {
		return Objects.hash(robots, ball);
	}
}
