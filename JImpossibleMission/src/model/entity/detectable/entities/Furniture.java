package model.entity.detectable.entities;

import model.Play;
import model.entity.detectable.Interactable;

/**
 * Furnitures is an Interactable used to search the pass inside, waiting for the percentage gets to 0 before getting
 * the item inside (action helped by the manager) and deactivate.
 * Depending of the type they all has their own name, width, height, offsetY(in case if this type is too big)
 * and percentage of the searching. 
 */
public class Furniture extends Interactable {

	/**
	 * Types of Furniture (name, width, height, offsetY, check percent)
	 */
	public enum FurnitureTypes {
		
		TUB("Tub", 144, 96, 0, 25f),
		BED("Bed", 144, 96, 0, 50f),
		BIGSOFA("Big Sofa", 144, 96, 0, 50f),
		CAN("Can", 24, 96, 0, 25f),
		CANDY("Candy", 96, 96, 0, 50f),
		COMPUTER("Computer", 48, 96, 0, 100f),
		DATAUNIT("Data Unit", 72, 96, 0, 50f),
		DESK("Desk", 168, 96, 0, 100f),
		DRAWER("Drawer", 72, 96, 0, 75f),
		FIREPLACE("Fireplace", 216, 96, 0, 100f),
		FRIDGE("Fridge", 72, 96, 0, 75f),
		JUKEBOX("Jukebox", 48, 96, 0, 25f),
		KITCHEN("Kitchen", 144, 96, 0, 100f),
		LAMP("Lamp", 48, 96, 0, 25f),
		LIBRARY("Library", 72, 108, 12, 100f),
		MAINFRAME("Mainframe", 120, 96, 0, 25f),
		PRINTER("Printer", 72, 96, 0, 50f),
		SINK("Sink", 96, 96, 0, 75f),
		SOFA("Sofa", 72, 96, 0, 50f),
		SPEAKER("Speaker", 48, 96, 0, 25f),
		STEREO("Stereo", 48, 96, 0, 25f),
		TOILETTE("Toilette", 72, 96, 0, 25f);

		private String name;
		private int w, h, offsetY;
		private float percent;
		
		/**
		 * Constructor of the furniture type to get it's datas
		 * 
		 * @param name     Name of the furniture
		 * @param w        Width of the furniture
		 * @param h        Height of the furniture
		 * @param offsetY  In case the furniture is too big has a offsetY that adjust the y position
		 * @param percent  Percentual for the searching before giving the item inside
		 */
		FurnitureTypes(String name, int w, int h, int offsetY, float percent) {
			this.name = name;
			this.w = w;
			this.h = h;
			this.offsetY = offsetY;
			this.percent = percent;
		}

		/*
		 * Getters
		 */
		public String getName() {
			return name;
		}

		public int getWidth() {
			return w;
		}

		public int getHeight() {
			return h;
		}

		public int getOffsetY() {
			return offsetY;
		}

		public float getPercent() {
			return percent;
		}

	}
	private FurnitureTypes furniture;
	// Components
	private int type, item;
	private float currentPercent;
	// Furniture states
	private boolean active, interaction;

	/**
	 * Particular part of this constructor is when it does super (since you can't initialize the datas before the super)
	 * it calls directly from the enum.
	 * 
	 * @param x     Position x to place the furniture
	 * @param y     Position y to place the furniture
	 * @param type  Type of furniture and depending which type has different width, height and percentual
	 */
	public Furniture(float x, float y, int type) {
		super(x, y - FurnitureTypes.values()[type].getOffsetY(),
				FurnitureTypes.values()[type].getWidth(),
				FurnitureTypes.values()[type].getHeight());
		this.type = type;
		initHitBox(x, this.y, width, height);
		initDetectBox(x, this.y, width, height);
		initFurniture(type);
	}

	/**
	 * Initialize the furnitures datas
	 * 
	 * @param type  From the type of the furniture initialize the datas
	 */
	private void initFurniture(int type) {
		furniture = FurnitureTypes.values()[type];
		currentPercent = furniture.getPercent();
		item = 0;
		active = true;
	}

	/**
	 * Updates the percent, reducing it until it reaches 0 only if the furniture is currently getting interacted
	 */
	public void updatePercent() {
		if (currentPercent > 0 && interaction)
			currentPercent -= 0.2;
	}
	
	/**
	 * Set the interaction to true if the player is interacting it
	 * 
	 * @param p  Is not used in this case
	 */
	@Override
	protected void interact(Play p) {
		interaction = true;
	}

	/*
	 * Getters/Setters
	 */
	public int getItem() {
		return item;
	}
	
	public void setItem(int item) {
		this.item = item;
	}

	public FurnitureTypes getFurniture() {
		return furniture;
	}

	public int getType() {
		return type;
	}

	public float getCurrentPercent() {
		return currentPercent;
	}
	
	public boolean isInteracting() {
		return interaction;
	}
	
	public void setInteraction(boolean interaction) {
		this.interaction = interaction;
	}

	public boolean isActive() {
		return active;
	}

	public void deactivate() {
		active = false;
	}

	/**
	 * Gives the name of the furniture
	 * 
	 * @return String  Example: type = TUB -> "Tub"
	 */
	@Override
	public String toString() {
		return furniture.getName();
	}

}
