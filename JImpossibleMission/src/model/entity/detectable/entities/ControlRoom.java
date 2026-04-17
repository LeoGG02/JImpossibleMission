package model.entity.detectable.entities;

import model.Play;
import model.entity.detectable.Interactable;

/**
 * ControlRoom is basically an Interactable that represents an entrance that the Player need have a full pass
 * to get access into and winning the game.
 */
public class ControlRoom extends Interactable {

	// Width and height of the Control Room entrance, hitbox and dtcbox share the same data
	private static final int CR_WIDTH = 144;
	private static final int CR_HEIGHT = 120;

	/**
	 * ControlRoom constructor (The entity used as finish line) that sets the hitbox and dtcbox
	 * 
	 * @param x  The x position to place in
	 * @param y  The y position to place in
	 */
	public ControlRoom(float x, float y) {
		super(x, y, CR_WIDTH, CR_HEIGHT);
		initHitBox(x, y, CR_WIDTH, CR_HEIGHT);
		initDetectBox(x, y, CR_WIDTH, CR_HEIGHT);
	}

	/**
	 * Checks if the player has the full password to get access to the control room, if is then sets the game in WIN state
	 * 
	 * @param p  If the player has the pass sets the WIN state if being interacted
	 */
	@Override
	protected void interact(Play p) {
		if (p.getPlayer().hasAccess())
			p.setPlayState(Play.PlayState.WIN);
	}
}
