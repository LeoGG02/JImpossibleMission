package model;

import model.Play.PlayState;

/**
 * ScoreManager is used mostly to get information of how many points it gain
 * (separated in different types: passes for the entities, passes for ControlRoom, time and total)
 */
public class ScoreManager {
	
	// Default amount of points for the most stuff
	public static final int PIECE_POINTS = 100;
	// Special amount of points for amount of puzzle completed
	public static final int PUZZLE_POINTS = 500;
	// Play used to read informations for points
	private Play p;

	/**
	 * Constructor of ScoreManager that set Play to read the points at the end of the game
	 * 
	 * @param p  Play used to read informations
	 */
	public ScoreManager(Play p) {
		this.p = p;
	}

	/*
	 * Getters
	 */
	public int getPuzzlePiecesFound() {
		return p.getPlayer().getPuzzlePieces();
	}
	
	public int getPuzzlePiecesPoints() {
		return PIECE_POINTS * getPuzzlePiecesFound();
	}
	
	public int getLiftPassFound() {
		return p.getPlayer().getResetsMaxFound();
	}
	
	public int getRobotPassFound() {
		return p.getPlayer().getSleepMaxFound();
	}
	
	public int getPassPoints() {
		return PIECE_POINTS * (getLiftPassFound() + getRobotPassFound());
	}
	
	public int getCompletedPuzzles() {
		return p.getPlayer().numPuzzleCompleted();
	}
	
	public int getPuzzlePoints() {
		return PUZZLE_POINTS * getCompletedPuzzles();
	}
	
	public int getSecPoints() {
		return Math.max(0, p.getRemainingTime());
	}
	
	public int getTotalPoints() {
		int extraPoints = p.getPlayState() == PlayState.WIN ? 1000 : 0;
		return getPuzzlePiecesPoints() + getPassPoints() + getPuzzlePoints() + getSecPoints() + extraPoints;
	}

}
