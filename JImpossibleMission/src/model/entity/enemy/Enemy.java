package model.entity.enemy;

import java.awt.geom.Rectangle2D;

import model.entity.Entity;

/**
 * Enemy is a type of Entity that other than having the normal hitbox, it has also the atkbox (attackbox),
 * used to check if certain Entity is within it's atkbox and to destroy/kill it (actions helped by the manager).
 */
public abstract class Enemy extends Entity {

	// The atkbox is similar to the hitbox the difference is that it helps track the enemy's attack's range
	protected Rectangle2D.Float atkbox;
	// Offset used to adjust the atkbox
	protected float atkOffsetW, atkOffsetH;

	/**
	 * Enemy constructor that set the placement and the size
	 * 
	 * @param x  Position x where the enemy spawns into
	 * @param y  Position y where the enemy spawns into
	 * @param w  Width of the hitbox
	 * @param h  Height of the hitbox
	 */
	public Enemy(float x, float y, int w, int h) {
		super(x, y, w, h);
	}

	/**
	 * Initialize the atkbox
	 * 
	 * @param x           Position x where to place the atkbox
	 * @param y           Position y where to place the atkbox
	 * @param w           Width of the atkbox
	 * @param h           Height of the atkbox
	 * @param atkOffsetW  Offsets used to adjust the position x of the atkbox
	 * @param atkOffsetH  Offsets used to adjust the position y of the atkbox
	 */
	protected void initAttackBox(float x, float y, float w, float h, float atkOffsetW, float atkOffsetH) {
		this.atkOffsetW = atkOffsetW;
		this.atkOffsetH = atkOffsetH;
		atkbox = new Rectangle2D.Float(x, y, w, h);
	}

	/**
	 * Together on updating the position, the enemy also have to update their atkbox
	 */
	protected abstract void updateAtk();

	/**
	 * Checks if the entity given is in the attack range
	 * 
	 * @param e         Entity to check if is in contact on the attack range of the enemy
	 * 
	 * @return boolean  Result of the check if the Entity's hitbox is within the atkbox or not
	 */
	protected boolean inContact(Entity e) {
		return atkbox.intersects(e.getHitbox());
	}

	/**
	 * Getter of the atkbox
	 * 
	 * @return Rectangle2D.Float  Rectangle that represents atkbox of the enemy
	 */
	public Rectangle2D.Float getAttackbox() {
		return atkbox;
	}

}
