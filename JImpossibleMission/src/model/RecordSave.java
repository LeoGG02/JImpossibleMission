package model;

import java.io.Serializable;
import java.util.Objects;

/**
 * RecordSave is basically the record that is saved by the Leaderboard, it contains the ID (which is the nickname of the
 * current Profile, only the first 5 characters in caps) and the points accumulated through a match in Play.
 */
public class RecordSave implements Comparable<RecordSave>, Serializable {
	
	/**
	 * Serial UID Generated
	 */
	private static final long serialVersionUID = -3993074627791632454L;
	
	// Record saves the ID (first 5 letter of the current profile player) and points (accumulated during the game)
	private String id;
	private int points;

	/**
	 * RecordSave constructor setting the id and points
	 * 
	 * @param id      Name that can relate to the points
	 * @param points  Amount of points gotten during Play
	 */
	public RecordSave(String id, int points) {
		this.id = id;
		this.points = points;
	}

	/*
	 * Getters
	 */
	public String getID() {
		return id;
	}
	
	public int getPoints() {
		return points;
	}

	/**
	 * Gives the ID next to the points of the record
	 * 
	 * @return String  Example: id = "AGENT", points = 5340 -> "AGENT: 5340"
	 */
	@Override
	public String toString() {
		return id+": "+points;
	}

	/**
	 * 
	 * @param o     The other RecordSave to be compared to
	 * 
	 * @return int  Amount of int that difference to each other
	 */
	@Override
	public int compareTo(RecordSave o) {
		return Integer.compare(points, o.getPoints());
	}

	/**
	 * equals with ID and points
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RecordSave) {
			RecordSave other = (RecordSave)obj;
			return id.equals(other.getID()) && points == other.getPoints();
		}
		return super.equals(obj);
	}

	/**
	 * hashCode of ID and points
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id, points);
	}

}
