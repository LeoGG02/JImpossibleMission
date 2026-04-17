package model;

import model.entity.Player;
import model.level.LevelManager;
import model.level.Room;

/**
 * Play is basically the current match of the game, saving it's states, creating the entities and the levels needed and
 * updating it's content.
 */
public class Play {

	// Initial spawn when the game starts
	public static final float XSTARTING_SPAWN = 464;
	public static final float YSTARTING_SPAWN = 155;
	// Default time of wait when the player dies
	public static final int DEATH_WAIT = 3;
	
	/**
	 * States of the current play
	 */
	public enum PlayState {
		INPROGRESS, INTERRUPTED, FORCEDSTOP, LOST, WIN;
	}
	private PlayState playState;
	
	// Components
	private Game g;
	private ScoreManager scores;
	private Player player;
	private LevelManager levels;
	// Game time is 7200 which translates in 2 hours
	private int remainingTime = 7200, deathTime;

	/**
	 * Play constructor getting the Game class and initialize the rest of the components
	 * (ScoreManager, LevelManager, Player and sets the playState)
	 * 
	 * @param g  Used to notify when the play ended
	 */
	public Play(Game g) {
		this.g = g;
		initClasses();
	}

	/**
	 * Initialize the Player and the LevelManager
	 */
	private void initClasses() {
		scores = new ScoreManager(this);
		levels = new LevelManager(this);
		player = new Player(XSTARTING_SPAWN, YSTARTING_SPAWN, levels.getCurrentLv());
		playState = PlayState.INPROGRESS;
	}

	/**
	 * Updates the current game (updating the player, the entities and the level)
	 */
	protected void update() {
		if (playState != PlayState.INPROGRESS)
			return;

		// Player and Level (with it's entities) update
		player.update();
		levels.update();
		// Death check
		if (player.isDead()) {
			deathTime = DEATH_WAIT;
			// Interrupts the current Play and start the death timer
			setPlayState(PlayState.INTERRUPTED);
			return;
		}
	}

	/**
	 * Reset the level and the player after the player died
	 */
	private void reset() {
		levels.getCurrentLv().reset();
		player.reset();	
	}

	/**
	 * Terminal RESET LIFTS
	 */
	public boolean resetLifts() {
		if (player.getCurrentResets() > 0 && levels.currentInRoom()) {
			player.useReset();
			((Room)levels.getCurrentLv()).resetLifts();
			return true;
		}
		return false;
	}

	/**
	 * Terminal DISABLE ROBOTS
	 */
	public boolean disableRobots() {
		if (player.getCurrentSleep() > 0 && levels.currentInRoom()) {
			player.useSleep();
			((Room)levels.getCurrentLv()).disableRobots();
			return true;
		}
		return false;
	}

	/**
	 * Update the timer of the game
	 */
	public void updateTime() {
		if (playState == PlayState.INPROGRESS)
			reduceTime(1);
	}
	
	/**
	 * Update the timer when the player is dead
	 */
	public void updateDeathTime() {
		// If isn't INTERRUPTED doesn't go further
		if (playState != PlayState.INTERRUPTED)
			return;
		
		deathTime -= 1;
		
		// After the times is done checks the situation after reducing the time by 600 (10 minutes)
		if (deathTime <= 0) {
			reduceTime(600);
			if (playState != PlayState.INTERRUPTED)
				return;
			// If is 0 or under set the LOST state
			if (player.getLives() <= 0)
				setPlayState(PlayState.LOST);
			// If not returns on INPROGRESS
			else {
				reset();
				setPlayState(PlayState.INPROGRESS);
			}
		}
	}

	/**
	 * Reduce time of all timers by given amount
	 */
	private void reduceTime(int time) {
		remainingTime -= time;
		// If the robots are disabled updates the robot sleep timer
		if (levels.currentInRoom() && ((Room)levels.getCurrentLv()).getRemainingRobotSleep() > 0)
			((Room)levels.getCurrentLv()).reduceRobotSleep(time);
		// If the time arrives to 0 or under, set to LOST
		if (remainingTime <= 0)
			setPlayState(PlayState.LOST);
	}

	/*
	 * Getters
	 */
	public ScoreManager getScores() {
		return scores;
	}
	
	public Player getPlayer() {
		return player;
	}

	public LevelManager getLevels() {
		return levels;
	}

	public int getRemainingTime() {
		return remainingTime;
	}
	
	public int getDeathTime() {
		return deathTime;
	}
	
	public PlayState getPlayState() {
		return playState;
	}
	
	/**
	 * Set PlayState (in case is WIN, LOST or FORCESTOP it stops the game)
	 * 
	 * @param p  PlayState to be set
	 */
	public void setPlayState(PlayState p) {
		playState = p;
		System.out.println(playState.toString());
		if (isEnded())
			g.end();
	}

	public boolean inProgress() {
		return playState == PlayState.INPROGRESS || playState == PlayState.INTERRUPTED;
	}
	
	public boolean isEnded() {
		return playState == PlayState.WIN || playState == PlayState.LOST || playState == PlayState.FORCEDSTOP;
	}

	/**
	 * Simply sets on FORCEDSTOP if isn't already WIN or LOST
	 */
	protected void stop() {
		if (playState != PlayState.LOST && playState != PlayState.WIN) {
			playState = PlayState.FORCEDSTOP;
		}
	}
	
}
