package view.util.play;

import java.util.Optional;
import java.util.Random;

import model.Play;
import model.entity.Player;
import model.entity.enemy.EnemyManager;
import model.entity.enemy.entities.Robot.RobotAction;
import model.level.Elevator;
import model.level.Level;
import view.util.AudioManager;

/**
 * Class used to play audio of the Play
 */
public class AudioPlay {
	
	private static final Random r = new Random();
	
	// Used to read the landing of the Player
	private static boolean landing = false;

	/**
	 * Plays the Level's audios (Depending if is an Elevator or a Room)
	 * 
	 * @param p  Used to just read the information from it (The level datas)
	 */
	public static void playLevelSounds(Play p) {
		model.level.LevelManager levels = p.getLevels();
		// In Room Level
		if (levels.currentInRoom())
			playRoomSounds(levels.getCurrentLv().getEntities().getEnemiesData());
		// In Elevator Level
		else
			playElevSounds((Elevator)levels.getCurrentLv());
	}

	/**
	 * Plays the Player's audios
	 * 
	 * @param p  Used to just read the infomation from it (player's action and status)
	 */
	public static void playPlayerSounds(Play p) {
		Player player = p.getPlayer();
		// Play player's death sounds if is dead and doesn't go any further
		if (player.isDead()) {
			AudioPlay.playPlayerDeath(player);
			return;
		}
		// Plays the player sounds
		AudioPlay.playPlayerSteps(player);
	}
	
	/**
	 * Each time the player dies plays one of the two death sound effect (SHOCK or SCREAM)
	 * 
	 * @param player  Used to just read where the player died
	 */
	private static void playPlayerDeath(Player player) {
		// Stop all sounds
		if (!AudioManager.getInstance().isSingularSoundRunning(AudioManager.SCREAM) &&
				!AudioManager.getInstance().isSingularSoundRunning(AudioManager.SHOCK)) {
			resetSounds();
			System.out.println("SOUND RESETTED");
		}
		// If the player died under the level plays the SCREAM sounds, if not then SHOCK
		if (player.getHitbox().y >= Level.LV_REAL_HEIGHT + 10)
			AudioManager.getInstance().playSingularSound(AudioManager.SCREAM);
		else
			AudioManager.getInstance().playSingularSound(AudioManager.SHOCK);
	}

	/**
	 * Plays sounds effect coming from the player (steps and landings)
	 * 
	 * @param player  Used to just read the player actions
	 */
	private static void playPlayerSteps(Player player) {
		boolean inAir = player.isRolling() || player.isInAir();
		// Run sounds
		if (player.getAction() == Player.PlayerActions.RUN)
			if (!inAir && (player.getActionTick() == 24 || player.getActionTick() == 66))
				AudioManager.getInstance().playSound(player.isRight() ? AudioManager.STEPR : AudioManager.STEPL);
		// Jump sounds
		if (inAir && !landing)
			landing = true;
		else if (!inAir && landing) {
			AudioManager.getInstance().playSound(player.isRight() ? AudioManager.JUMPR : AudioManager.JUMPL);
			landing = false;
		}
		// In case he dies in mid air
		if (player.isDead() && landing)
			landing = false;
	}

	/**
	 * Plays all sound effect coming from the room
	 * 
	 * @param enemies  Used to read the enemies information to play the sounds depending to their actions (can be null)
	 */
	private static void playRoomSounds(Optional<EnemyManager> enemies) {
		// If the EnemyManager is not present doesn't go further
		if(enemies.isEmpty())
			return;
		
		// Checks if there's any robots if is then plays the robot's ambience sound
		if (enemies.map(e -> e.areActive() && !e.getRobots().isEmpty()).orElse(false))
			AudioManager.getInstance().playSingularSound(AudioManager.ROBOT);
		else if (AudioManager.getInstance().isSingularSoundRunning(AudioManager.ROBOT))
			AudioManager.getInstance().stopSingularSound(AudioManager.ROBOT);
		// Sound effect when the robot turns
		enemies.ifPresent(e -> e.getRobots().stream()
				.filter(r -> r.getRobotAction() == RobotAction.TURN && e.areActive() && r.getActionTick() == 0)
				.forEach(r -> AudioManager.getInstance().playSound(AudioManager.R_TURN)));
		// Sound effect when the robot do the zap attack (depending the current speed of the robot it plays the short or the long zap)
		enemies.ifPresent(e -> e.getRobots().stream()
				.filter(r -> e.areActive() && r.getRobotAction() == RobotAction.ATTACK && r.getActionTick() == 0)
				.forEach(r -> playZap(r.getCurrentSpeed())));
	}
	
	/**
	 * Plays the zap sound effect between long and short variation
	 * 
	 * @param speed  Speed of the robot to choose which audio to play
	 */
	private static void playZap (int speed) {
		int start = speed == 2 ? 3 : 1;
		AudioManager.getInstance().playSound(AudioManager.ZAP + r.nextInt(start, 3 * speed));
	}

	/**
	 * Play Elevator sounds when it moves
	 * 
	 * @param elev  Used to just read if is moving or not
	 */
	private static void playElevSounds(Elevator elev) {
		// Stops the robot sound from the Room level
		if (AudioManager.getInstance().isSingularSoundRunning(AudioManager.ROBOT))
			AudioManager.getInstance().stopSingularSound(AudioManager.ROBOT);
		// Set the sound in case the Elevator moves or not
		if (elev.isMoving())
			AudioManager.getInstance().playSingularSound(AudioManager.ELEV_START);
		else if (AudioManager.getInstance().isSingularSoundRunning(AudioManager.ELEV_START)) {
			AudioManager.getInstance().stopSingularSound(AudioManager.ELEV_START);
			AudioManager.getInstance().playSingularSound(AudioManager.ELEV_STOP);
		}
	}

	/**
	 * Resets all the sounds, disabling the landing state to put back in the initial state
	 */
	public static void resetSounds() {
		AudioManager.getInstance().stopAllSingularsSounds();
		landing = false;
	}

}
