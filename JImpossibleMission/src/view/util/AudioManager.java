package view.util;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Main manager used to save and control audios.
 */
public class AudioManager {

	private static final Random r = new Random();
	
	// Audio directory + .wav
	public static final String AUDIO_DIRECTORY = "/audios/";
	public static final String WAV = ".wav";
	
	// Audio file names
	public static final String VILLAIN_INTRO = "anothervisitor";
	public static final String BEEP = "beep";
	public static final String SHOCK = "dieByZap";
	public static final String ROBOT = "droid";
	public static final String R_TURN = "droidTurn";
	public static final String ELEV_START = "elevatorStart";
	public static final String ELEV_STOP = "elevatorStop";
	public static final String SCREAM = "falling";
	public static final String VILLAIN_VICTORY = "hahaha";
	public static final String JUMPL = "jumpLeft";
	public static final String JUMPR = "jumpRight";
	public static final String MISSION_ACCOMPLISHED = "missionAccomplished";
	public static final String VILLAIN_DEFEAT = "nonono";
	public static final String STEPL = "stepLeft";
	public static final String STEPR = "stepRight";
	public static final String TRACK = "track";
	public static final String ZAP = "zap";
	
	// Array of audio that plays in sigular time
	private static final String[] SINGULAR_AUDIONAMES = {VILLAIN_INTRO, VILLAIN_VICTORY, VILLAIN_DEFEAT, ROBOT,
			ELEV_START, ELEV_STOP, SCREAM, SHOCK, MISSION_ACCOMPLISHED};

	private static AudioManager instance;
	private Clip track;
	private Map<String, Clip> singularAudios;
	private boolean runningTrack = true, runningSounds = true;

	/**
	 * Private constructor for Singleton with getIstance so can load the singular audios only once
	 */
	private AudioManager() {
		loadAudios();
	}

	/**
	 * Used to get the main instance of this (Singleton)
	 * 
	 * @return AudioManager  The main instance of the AudioManager
	 */
	public static AudioManager getInstance() {
		if (instance == null)
			instance = new AudioManager();
		return instance;
	}

	/**
	 * Loads all the singular audios in a Map<Name of the audio, Clip audio associated>
	 */
	private void loadAudios() {
		track = getClip(TRACK);
		singularAudios = List.of(SINGULAR_AUDIONAMES).stream()
				.map(name -> Map.entry(name, getClip(name)))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	}

	/**
	 * Gets the clip from the filename
	 * 
	 * @param fileName  Name of the audio file
	 * @return Clip     Clip audio gotten from the file name
	 */
	public Clip getClip(String fileName) {
		
		try (AudioInputStream audioIn = AudioSystem
				.getAudioInputStream(AudioManager.class.getResource(AUDIO_DIRECTORY + fileName + WAV))) {
			Clip clip = AudioSystem.getClip();
			clip.open(audioIn);
			return clip;
		}
		catch (Exception e){
			System.err.println("Error during the load of the audio " + fileName);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Plays the singular audio given if isn't already running
	 * 
	 * @param audioName  Name of the audio to play
	 */
	public void playSingularSound (String audioName) {
		if (!singularAudios.get(audioName).isRunning() && runningSounds) {
			singularAudios.get(audioName).setMicrosecondPosition(0);
			singularAudios.get(audioName).start();
			System.out.println("Audio "+audioName+" playing...");
		}
	}
	
	/**
	 * Stops the singular audio given if is running
	 * 
	 * @param audioName  Name of the audio to stop
	 */
	public void stopSingularSound (String audioName) {
		if (singularAudios.get(audioName).isRunning()) {
			singularAudios.get(audioName).stop();
		}
	}
	
	/**
	 * Stops all the singular sounds in the collection
	 */
	public void stopAllSingularsSounds() {
		singularAudios.entrySet().stream()
		.filter(e -> e.getValue().isRunning())
		.forEach(e -> e.getValue().stop());
	}

	/**
	 * Play the sounds directly, but without a real control of it
	 * 
	 * @param fileName  Name of the audio file to play
	 */
	public void playSound (String fileName) {
		if (!runningSounds)
			return;
		
		try (AudioInputStream audioIn = AudioSystem.getAudioInputStream(AudioManager.class.getResource(AUDIO_DIRECTORY + fileName + WAV))) {
			Clip clip = AudioSystem.getClip();
			clip.open(audioIn);
			clip.start();
			System.out.println("Audio "+fileName+" playing...");
		}
		catch (Exception e) {
			System.err.println("Error during the load of the audio " + fileName);
			e.printStackTrace();
		}
	}

	/**
	 * Plays a button sound
	 */
	public void playButtonSound() {
		if (runningSounds)
			playSound(BEEP + r.nextInt(1, 6));
	}

	/**
	 * Start the main track in the menu
	 */
	public void startTrack() {
		if (!track.isRunning())
			track.loop(Clip.LOOP_CONTINUOUSLY);
	}

	/**
	 * Stop the main track in the menu
	 */
	public void stopTrack() {
		if (track.isRunning()) {
			track.stop();
			track.setMicrosecondPosition(0);
		}
	}

	/*
	 * Getters/Setters
	 */
	public Clip getTrack() {
		return track;
	}
	
	/**
	 * Checks if the audio given is running
	 * 
	 * @param audioName  Name of the audio to check
	 * 
	 * @return boolean   Result of the sound if is running or not
	 */
	public boolean isSingularSoundRunning (String audioName) {
		return singularAudios.get(audioName).isRunning();
	}
	
	public boolean isTrackEnabled() {
		return runningTrack;
	}
	
	public void setRunningTrack(boolean runningTrack) {
		this.runningTrack = runningTrack;
	}
	
	public boolean isSoundsEnabled() {
		return runningSounds;
	}

	/**
	 * Enable/Disable sounds, if switch to ON gives a random audio to test
	 * 
	 * @param runningSounds  Boolean used to set the sound running or not
	 */
	public void setRunningSound(boolean runningSounds) {
		this.runningSounds = runningSounds;
		if (runningSounds)
			playSingularSound(SINGULAR_AUDIONAMES[r.nextInt(SINGULAR_AUDIONAMES.length)]);
		else
			stopAllSingularsSounds();
	}
}
