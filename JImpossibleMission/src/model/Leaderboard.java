package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import model.util.DataManager;

public class Leaderboard implements Serializable {

	/**
	 * Serial UID Generated
	 */
	private static final long serialVersionUID = 1642558689053848308L;
	
	// File name of the leaderboard file
	public static final String FILE_NAME = "leaderboard.dat";
	// Maximum amount of records that leaderboard can have
	public static final int MAX_CAPACITY = 15;
	// List of records
	private List<RecordSave> leaderboard;
	// Last inserted save to check if is the highscore
	private RecordSave lastInsert;

	/**
	 * Leaderboard constructor creates the leaderboard list
	 */
	public Leaderboard() {
		leaderboard = new ArrayList<RecordSave>();
	}

	/**
	 * Insert the new Record by giving the ID (first 5 letter of the current profile player in that moment)
	 * and points (accumulated during the game).
	 * If the list reaches the maximum amount, it removes the record with the lowest points
	 * 
	 * @param ID      Name that can relate to the points
	 * @param points  Amount of points gotten from the Play
	 */
	public void insertPoint(String ID, int points) {
		lastInsert = new RecordSave(ID, points);
		leaderboard.add(lastInsert);
		leaderboard.sort(Comparator.reverseOrder());
		
		if (leaderboard.size() > MAX_CAPACITY)
			leaderboard.remove(leaderboard.size()-1);
	}

	/**
	 * Checks if has a new highscore by checking if the last inserted record is on top
	 */
	public boolean hasNewHighScore() {
		if (leaderboard.size() > 1)
			return lastInsert.equals(leaderboard.get(0));
		return false;
	}

	/*
	 * Getter
	 */
	public List<RecordSave> getLeaderboardList() {
		return leaderboard;
	}

	/**
	 * Reset the leaderboard list
	 */
	public void reset() {
		leaderboard.clear();
	}

	/**
	 * Saves the leaderboard file
	 */
	public void save() {
		DataManager.save(this, FILE_NAME);
	}
	
}
