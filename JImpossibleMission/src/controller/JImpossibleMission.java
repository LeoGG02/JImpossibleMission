package controller;

import model.Game;
import view.Window;

/**
 * JIMPOSSIBLE MISSION
 * 
 * @author Leo Clen Galutera Gadian (2087727)
 */
public class JImpossibleMission {
	
	/**
	 * main to start the game JImpossibleMission
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		new Controller(new Game(), new Window());
	}

}
