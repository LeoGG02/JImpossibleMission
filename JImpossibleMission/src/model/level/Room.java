package model.level;

import java.util.Objects;

import model.entity.Player;

/**
 * Level that contains the passes to win the game and enemies to avoid.
 */
public class Room extends Level {
	
	private static final int RSLEEP_TIMER = 15;

	// 0 = TOP LEFT | 1 = TOP RIGHT | 2 = BOTTOM LEFT | 3 = BOTTOM RIGHT
	private boolean[] entrances;
	// Timer used when the robots are asleep. Once the timer ran out the robots wakes up again
	private int remainingRobotSleep;

	/**
	 * Room constructor initializing all the datas from the file given
	 * 
	 * @param fileMap  File of the level used to create it's lvData and entities
	 */
	public Room(String fileMap) {
		super(fileMap);
		setEntrances();
	}
	
	/**
	 * Read entrances that are present from the level data
	 */
	private void setEntrances() {
		int topL = lvData[1][0];
		int topR = lvData[1][LV_WIDTH - 1];
		int bottomL = lvData[LV_HEIGHT - 2][0];
		int bottomR = lvData[LV_HEIGHT - 2][LV_WIDTH - 1];
		entrances = new boolean[] { topL == 4, topR == 4, bottomL == 4, bottomR == 4 };
	}

	/**
	 * Override. Updates all the entities of the room
	 * 
	 * @param player  Used to read Player datas
	 */
	@Override
	protected void update(Player player) {
		entities.update(player);
	}

	/**
	 * Terminal RESET LIFT
	 */
	public void resetLifts() {
		entities.getLiftsData().ifPresent(l -> l.resetAllLifts());
	}

	/**
	 * Terminal DISABLE ROBOTS
	 */
	public void disableRobots() {
		entities.getEnemiesData().ifPresent(e -> {
			e.sleepAll();
			remainingRobotSleep = RSLEEP_TIMER;
		});
		
	}

	/**
	 * Reduces the time for the robot sleep timer
	 * 
	 * @param time  Amount of time that reduces from the timer
	 */
	public void reduceRobotSleep(int time) {
		remainingRobotSleep -= time;
		// If reaches 0 wakes all the enemies
		if (remainingRobotSleep <= 0)
			entities.getEnemiesData().ifPresent(enemies -> enemies.wakeAll());
	}
	
	/*
	 * Getters
	 */
	public int getRemainingRobotSleep() {
		return remainingRobotSleep;
	}

	public boolean hasTopLEntrance() {
		return entrances[0];
	}

	public boolean hasTopREntrance() {
		return entrances[1];
	}

	public boolean hasBottomLEntrance() {
		return entrances[2];
	}

	public boolean hasBottomREntrance() {
		return entrances[3];
	}

	public boolean[] getEntrances() {
		return entrances;
	}

	/**
	 * equals with also entities after super
	 */
	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj) && obj instanceof Room) {
			Room other = (Room) obj;
			return entities.equals(other.getEntities());
		}
		return false;
	}

	/**
	 * hashCode of also entities after super
	 */
	@Override
	public int hashCode() {
		return Objects.hash(entities);
	}

}
