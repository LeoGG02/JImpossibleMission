package model.entity;

import java.awt.geom.Rectangle2D;

/**
 * Class used by all the entities which contains their original position (x, y) and their hitbox
 */
public abstract class Entity {

	// Original position and size
	protected float x, y;
	protected int width, height;
	// The hitbox is a rectangle that represents the entity, helps with the collisions and with the position
	protected Rectangle2D.Float hitbox;

	/**
	 * Entity constructor that set the placement and the size
	 * 
	 * @param x  Position x where the entity spawns into
	 * @param y  Position y where the entity spawns into
	 * @param w  Width of the hitbox
	 * @param h  Height of the hitbox
	 */
	public Entity(float x, float y, int w, int h) {
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
	}

	/**
	 * Initialize the hitbox of the entity
	 * 
	 * @param x  Position x where to place the hitbox
	 * @param y  Position y where to place the hitbox
	 * @param w  Width of the hitbox
	 * @param h  Height of the hitbox
	 */
	protected void initHitBox(float x, float y, float w, float h) {
		hitbox = new Rectangle2D.Float(x, y, w, h);
	}

	/**
	 * Getters the hitbox
	 * 
	 * @return Rectangle2D.Float  Rectangle that represents hitbox of the entity
	 */
	public Rectangle2D.Float getHitbox() {
		return hitbox;
	}

	/*
	 * Getters of the original position
	 */
	public float getOriginalX() {
		return x;
	}
	
	protected void setOriginalX(float x) {
		this.x = x;
	}
	
	public float getOriginalY() {
		return y;
	}
	
	protected void setOriginalY(float y) {
		this.y = y;
	}
}
