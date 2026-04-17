package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Optional;

import model.Play.PlayState;
import model.util.DataManager;

/**
 * Main game model used to manage the profiles (up to 8), the leaderboard and starting/ending/restarting the Play.
 * Also, as Observable, notifies every Observer associated to a certain change.
 */
@SuppressWarnings("deprecation")
public class Game extends Observable {

	// Max amount of profiles
	public static final int MAX_AMOUNT_PROFILES = 8;
	// Amount of games and wins
	private int games, wins;
	
	// Profiles and Leaderboard
	private List<Profile> profiles;
	private Profile currentProfile;
	private Leaderboard leaderboard;
	
	// Current game
	private Play play;

	/**
	 * Game constructor that starts loading the profiles, leaderboard and initialize the stats
	 */
	public Game() {
		loadProfiles();
		loadLeaderboard();
		initStats();
	}

	/**
	 * Load all the profiles (Total of 8)
	 */
	private void loadProfiles() {
		// Loading of the profiles
		profiles = new ArrayList<Profile>();
		for(int i = 1; i <= MAX_AMOUNT_PROFILES; i++) {
			String fileName = "profile"+i+".dat";
			Profile loadedProfile = (Profile)DataManager.load(fileName);
			if (loadedProfile == null)
				profiles.add(new Profile(Profile.DEFAULT_NICKNAME, Profile.DEFAULT_AVATAR, fileName));
			else
				profiles.add(loadedProfile);
		}
		
		// Set the current profile after loading them all
		currentProfile = profiles.stream()
				.filter(p -> p.isChosen())
				.findFirst().orElse(profiles.get(0));
	}

	/**
	 * Load the current Leaderboard
	 */
	private void loadLeaderboard() {
		leaderboard = (Leaderboard)DataManager.load(Leaderboard.FILE_NAME);
		// If the file isn't present it creates a new one
		if (leaderboard == null) {
			System.out.println("File not found. Creating e new one.");
			leaderboard = new Leaderboard();
			saveLeaderboard();
		}
	}

	/**
	 * Gets the stats from the current profile
	 */
	private void initStats() {
		games = currentProfile.getPlays();
		wins = currentProfile.getWins();
	}

	/**
	 * Update the current game
	 */
	public void update() {
		Optional.ofNullable(play).ifPresent(p -> p.update());
		notifyAllObservers(null);
	}

	/**
	 * Start a game with the given Play
	 * 
	 * @param p  New Play
	 */
	public void start(Play p) {
		play = p;
		notifyAllObservers(p);
	}

	/**
	 * Ends the current game updating the amount of plays, win and lose and if has a result (WIN or LOST) updates the leaderboard
	 */
	public void end() {
		play.stop();
		// Gets the PlayState and finished (won or lost) adds the points to the current profile and updates the leaderboard
		PlayState currentPlaystate = play.getPlayState();
		updatePlays(currentPlaystate);
		if (currentPlaystate != PlayState.FORCEDSTOP) {
			currentProfile.addPoints(play.getScores().getTotalPoints());
			updateLeaderboard(play);
		}
		System.out.println("Play ended");
		notifyAllObservers(currentPlaystate);
	}

	/**
	 * Restart the game, stopping the previous one if was in progress and starting a new one
	 * 
	 * @param p  New Play
	 */
	public void restart(Play p) {
		Optional.ofNullable(play).ifPresent(pl -> {
			pl.stop();
			updatePlays(pl.getPlayState());
		});
		start(p);
	}

	/**
	 * Update the amount of game, win and loses of the current profile
	 * 
	 * @param currentPlaystate  The state that Play ended with used to see if the player won or lost
	 */
	private void updatePlays(PlayState currentPlaystate) {
		if (currentPlaystate == PlayState.WIN)
			currentProfile.increaseWins(++wins);
		currentProfile.increasePlays(++games);
		currentProfile.save();
	}

	/**
	 * Update the leaderboard by getting the name of the profile at the time (First 5 characters) with the total points made in game
	 * 
	 * @param p  Play used to read the total points
	 */
	private void updateLeaderboard(Play p) {
		String profileName = currentProfile.getNickname();
		leaderboard.insertPoint(currentProfile.getNickname().substring(0, Math.min(5, profileName.length())).toUpperCase(),
				p.getScores().getTotalPoints());
		saveLeaderboard();
	}

	/**
	 * Switch the current profile
	 * 
	 * @param index  Choose the profile in the given index of the list
	 */
	public void changeProfile(int index) {
		currentProfile.setChosen(false);
		currentProfile = profiles.get(index);
		currentProfile.setChosen(true);
		updateStats();
	}

	/**
	 * Resets the stats of the current profile
	 */
	public void resetProfile() {
		currentProfile.reset();
		updateStats();
	}

	/**
	 * Resets the current leaderboard
	 */
	public void resetLeaderboard() {
		leaderboard.reset();
		saveLeaderboard();
	}

	/**
	 * Updates the stats getting from the current profile
	 */
	private void updateStats() {
		games = currentProfile.getPlays();
		wins = currentProfile.getWins();
		notifyAllObservers(currentProfile);
	}

	/**
	 * Saves the leaderboard file
	 */
	private void saveLeaderboard() {
		leaderboard.save();
		notifyAllObservers(leaderboard);
	}

	/**
	 * Play check
	 */
	public boolean isPlaying() {
		return Optional.ofNullable(play).map(p -> p.inProgress()).orElse(false);
	}

	/*
	 * Getters
	 */
	public Play getPlay() {
		return play;
	}

	public Profile getCurrentProfile() {
		return currentProfile;
	}
	
	public List<Profile> getProfiles() {
		return profiles;
	}
	
	public Leaderboard getLeaderboard() {
		return leaderboard;
	}
	
	public List<RecordSave> getLeaderboardList() {
		return leaderboard.getLeaderboardList();
	}
	
	/**
	 * To notify all Observers connected with this class.
	 */
	private void notifyAllObservers(Object arg) {
		setChanged();
		notifyObservers(arg);
	}
	
}
