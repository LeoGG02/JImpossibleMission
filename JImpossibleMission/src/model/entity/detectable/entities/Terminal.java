package model.entity.detectable.entities;

import model.Play;
import model.entity.detectable.Interactable;

/**
 * Terminal is an Interactable that used to interupt the Play temporarily to get access to it's screen
 */
public class Terminal extends Interactable {

	// Width and height of the Terminal, hitbox and dtcbox share the same data
	private static final int TERMINAL_WIDTH = 72;
	private static final int TERMINAL_HEIGHT = 72;

	/**
	 * Terminal constructor that sets the hitbox and detectionbox
	 * 
	 * @param x  Position x to be place in
	 * @param y  Position y to be place in
	 */
	public Terminal(float x, float y) {
		super(x, y, TERMINAL_WIDTH, TERMINAL_HEIGHT);
		initHitBox(x, y, TERMINAL_WIDTH, TERMINAL_HEIGHT);
		initDetectBox(x, y, TERMINAL_WIDTH, TERMINAL_HEIGHT);
	}

	/**
	 * Once interacted with a Terminal sets the game in INTERRUPTED state
	 * 
	 * @param p  Used to set to INTERRUPTED state if being interacted
	 */
	@Override
	protected void interact(Play p) {
		p.setPlayState(Play.PlayState.INTERRUPTED);
	}
}
