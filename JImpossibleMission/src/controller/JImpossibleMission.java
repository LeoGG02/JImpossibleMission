package controller;

import model.Game;
import view.Window;

/**
 * JIMPOSSIBLE MISSION
 * 
 * @author Leo Clen Galutera Gadian (2087727)
 */
public class JImpossibleMission {

	System.setProperty("sun.java2d.uiScale", "1");
	/**
	 * main to start the game JImpossibleMission
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Force scale
		System.setProperty("sun.java2d.uiScale", "1");
		new Controller(new Game(), new Window());
	}

}
