package model.entity.detectable;

import model.Play;

/**
 * Detectable entity that allows an additional interactions with the Player.
 */
public abstract class Interactable extends Detectable {

	/**
	 * Interactable constructor, detectable entity that can be interacted by the player
	 * 
	 * @param x  Position x where the interactable spawns into
	 * @param y  Position y where the interactable spawns into
	 * @param w  Width of the hitbox
	 * @param h  Height of the hitbox
	 */
	public Interactable(float x, float y, int w, int h) {
		super(x, y, w, h);
	}
	
	/**
	 * Interact command
	 * 
	 * @param p       Mostly used to change it's state
	 */
	protected abstract void interact(Play p);

}
