package model;

import java.io.Serializable;

import model.util.DataManager;

/**
 * Basically the profile of the user that can be saved as a file and contains the name, the avatar (file image), plays,
 * victories, loses and levels accumulated for each 5000 points. It also has a boolean to set this profile if
 * is choosen before closing the game.
 */
public class Profile implements Serializable {

	/**
	 * Serial UID Generated
	 */
	private static final long serialVersionUID = 1994363032657393878L;
	// Default datas
	public static final String DEFAULT_NICKNAME = "Agent 4125";
	public static final String DEFAULT_AVATAR = "agent.png";
	// Amount of points needed to level up each level
	private static final int CHANGE_LV_POINTS_REQUIRED = 5000;
	// Components
	private String nickname, avatar, fileName;
	private int plays, wins, loses, levels, sumPoints;
	// Choosen is used to see which was the last profile was using
	private boolean choosen;

	/**
	 * Profile constructor that sets the nickname, avatar and get the file name of it
	 * 
	 * @param nickname  Name used for the profile
	 * @param avatar    Name of the avatar used for the profile
	 * @param fileName  Name of the file used for saving
	 */
	public Profile(String nickname, String avatar, String fileName) {
		this.nickname = nickname;
		this.avatar = avatar;
		this.fileName = fileName;
		plays = 0;
		loses = 0;
		wins = 0;
		levels = 0;
		sumPoints = 0;
		choosen = false;
		save();
	}

	/*
	 * Getters/Setters
	 */
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	
	public String getFileName() {
		return fileName;
	}

	public int getPlays() {
		return plays;
	}

	public void increasePlays(int plays) {
		this.plays = plays;
		loses = plays - wins;
	}

	public int getWins() {
		return wins;
	}

	public void increaseWins(int wins) {
		this.wins = wins;
	}

	public int getLoses() {
		return loses;
	}

	public int getLevels() {
		return levels;
	}

	/**
	 * Adds points and depending how many points the profile accumulated, it levels up (levels each 5000 points)
	 * 
	 * @param points  The amount of points to be added with the rest
	 */
	public void addPoints(int points) {
		sumPoints += points;
		levels = sumPoints / CHANGE_LV_POINTS_REQUIRED;
	}
	
	public int getPoints() {
		return sumPoints;
	}
	
	public int getRequiredPoints() {
		return (levels + 1) * CHANGE_LV_POINTS_REQUIRED;
	}

	/**
	 * Used to put back to this profile when the game is open if is chosen
	 */
	public boolean isChosen() {
		return choosen;
	}
	
	public void setChosen(boolean choosen) {
		this.choosen = choosen;
		save();
	}

	/**
	 * Resets the current profile to the default one
	 */
	public void reset() {
		nickname = DEFAULT_NICKNAME;
		avatar = DEFAULT_AVATAR;
		plays = 0;
		wins = 0;
		loses = 0;
		levels = 0;
		sumPoints = 0;
		choosen = false;
		save();
	}

	/**
	 * Saves the current modification of the profile to the data file
	 */
	public void save() {
		DataManager.save(this, fileName);
	}

	/**
	 * Shows it's nickname
	 * 
	 * @return String  Example: nickname = "Agent" -> "Agent"
	 */
	@Override
	public String toString() {
		return nickname;
	}

}
