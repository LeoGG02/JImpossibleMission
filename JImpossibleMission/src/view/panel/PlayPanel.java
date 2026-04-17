package view.panel;

import java.awt.Graphics;
import java.awt.Graphics2D;

import model.Play;
import view.Navigator;
import view.Navigator.ScreenStates;
import view.util.*;
import view.util.play.*;

/**
 * Panel to show the Play (rendering the sprites and playing the audios depending the situation of the Play)
 */
public class PlayPanel extends Panel {

	/**
	 * Default Serial UID
	 */
	private static final long serialVersionUID = 1L;
	// Used to read the information from it
	private Play p;
	// Phases of Play
	private boolean startPhase, victoryPhase;
	// Temp used to see hitbox and other debug stuff
	private boolean debug;

	/**
	 * PlayPanel constructor that initialize the phase state
	 * 
	 * @param n  Navigator used to give the command to change panel
	 */
	public PlayPanel(Navigator n) {
		super(n);
		initComponents();
	}

	/*
	 * Initialize components which is just the states of the game
	 */
	@Override
	protected void initComponents() {
		debug = false;
		startPhase = false;
		victoryPhase = false;
	}

	@Override
	protected void addComponents() {
		// Empty
	}

	/**
	 * With the DrawManager and the information of the Play draww the entities and the level
	 */
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		super.paintComponent(g2);
		//Color background
		g2.setColor(FactoryComponents.DEFAULT_COLOR_BACKGROUND);
		g2.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
		// Render the normal stuff of Play
		RenderPlay.drawLevel(p, g2);
		RenderPlay.drawAllEntities(p, g2);
		RenderPlay.drawOuterStuff(p, g2);
		// Render the HUD that shown only when the player is in the Elevator level
		if (!AudioManager.getInstance().isSingularSoundRunning(AudioManager.VILLAIN_INTRO) && p.getLevels().currentInElevator())
			RenderPlay.drawHud(p, g2);
		// Temp used for debugging
		if (debug)
			RenderPlay.showDebugStuff(p, g2);
	}

	/**
	 * Plays automatically sounds, thanks to AudioManager, depending on the situation in the game
	 */
	public void updateSounds() {
		// Checks if the sounds is ON
		if (!AudioManager.getInstance().isSoundsEnabled())
			return;
		// Starting phase is when the villain does the initial monologue
		if (startPhase)
			if (!AudioManager.getInstance().isSingularSoundRunning(AudioManager.VILLAIN_INTRO))
				startPhase = false;
			else
				return;
		// Victory phase is when the villain does the defeated monologue before switching to RESULT
		if (victoryPhase) {
			if (!AudioManager.getInstance().isSingularSoundRunning(AudioManager.VILLAIN_DEFEAT)) {
				n.navigate(ScreenStates.RESULT);
				AudioManager.getInstance().playSingularSound(AudioManager.MISSION_ACCOMPLISHED);
			}
			return;
		}
		// Plays the player sounds
		AudioPlay.playPlayerSounds(p);
		// If the player is dead doesn't play any other audios
		if (p.getPlayer().isDead())
			return;
		// Plays the sounds depending of the level
		AudioPlay.playLevelSounds(p);
	}

	/**
	 * If sounds are enable starts the game with the initial monologue from the villain
	 */
	private void startAudio() {
		if (AudioManager.getInstance().isSoundsEnabled())
			AudioManager.getInstance().playSingularSound(AudioManager.VILLAIN_INTRO);
		else
			startPhase = false;
	}

	/*
	 * Getters/Setters
	 */
	public boolean isStarting() {
		return startPhase;
	}

	/**
	 * Sets the Play to get information from and set the start phase
	 * 
	 * @param p  Play used to read the information to render the sprites and play the audios
	 */
	public void setPlay(Play p) {
		this.p = p;
		startPhase = true;
		victoryPhase = false;
		AudioPlay.resetSounds();
		startAudio();
	}

	/**
	 * Once finally reaching the control room with the complete passwords, starts the victory sequence.
	 * If the audio is disabled goes straight to RESULT
	 */
	public void setVictory() {
		if (AudioManager.getInstance().isSoundsEnabled()) {
			AudioManager.getInstance().stopAllSingularsSounds();
			AudioManager.getInstance().playSingularSound(AudioManager.VILLAIN_DEFEAT);
			victoryPhase = true;
		}
		else
			n.navigate(ScreenStates.RESULT);
	}

	/**
	 * Debug state
	 */
	public void debug() {
		debug = !debug;
	}

}
