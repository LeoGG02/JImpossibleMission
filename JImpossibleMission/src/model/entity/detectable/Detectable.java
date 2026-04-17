package model.entity.detectable;

import java.awt.geom.Rectangle2D;

import model.entity.Entity;

/**
 * Detectable is a type of Entity that other than having the normal hitbox, it also has the dtcbox (detectbox),
 * used to check if certain Entity is within it's dtcbox.
 */
public abstract class Detectable extends Entity {

	//The dtcbox is similar to the hitbox the difference is that it helps track if other entities is in range of interaction
	protected Rectangle2D.Float dtcbox;

	/**
	 * Detectable constructor that set the placement and the size
	 * 
	 * @param x  Position x where the detectable spawns into
	 * @param y  Position y where the detectable spawns into
	 * @param w  Width of the hitbox
	 * @param h  Height of the hitbox
	 */
	public Detectable(float x, float y, int w, int h) {
		super(x, y, w, h);
	}

	/**
	 * Initialize the dtcbox
	 * 
	 * @param x  Position x where to place the dtcbox
	 * @param y  Position y where to place the dtcbox
	 * @param w  Width of the dtcbox
	 * @param h  Height of the dtcbox
	 */
	protected void initDetectBox(float x, float y, float w, float h) {
		dtcbox = new Rectangle2D.Float(x, y, w, h);
	}

	/**
	 * Checks if the entity given is in the detectable range
	 * 
	 * @param e         Entity to check if is near
	 * 
	 * @return boolean  Result of the check if the Entity's hitbox is within the dtcbox or not
	 */
	protected boolean isNear(Entity e) {
		return dtcbox.intersects(e.getHitbox());
	}

	/**
	 * Getter of the dtcbox
	 * 
	 * @return Rectangle2D.Float  Rectangle that represents dtcbox of the detectable
	 */
	public Rectangle2D.Float getDetectbox() {
		return dtcbox;
	}

}