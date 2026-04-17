package view;

import java.util.Observable;

/**
 * Class used to communicate with Window to switch Panel
 */
@SuppressWarnings("deprecation")
public class Navigator extends Observable {

	/**
	 * Types of screen to show
	 */
	public enum ScreenStates {
		MENU, PROFILE, PLAY, RESULT, SETTINGS, TERMINAL;
	}
	
	/**
	 * Calling this method, helps change the screen helping to "navigate" between them
	 * 
	 * @param screen  The panel to navigate into by sending to the Observable to switch to it
	 */
	public void navigate(ScreenStates screen) {
		setChanged();
		notifyObservers(screen);
	}
}
