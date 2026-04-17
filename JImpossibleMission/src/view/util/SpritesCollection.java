package view.util;

import java.awt.image.BufferedImage;

/**
 * Main class that is used to save all the sprites for the game
 */
public class SpritesCollection {
	
	// PNG
	private static final String PNG = ".png";
	
	// Level related 
	private static final String LEVEL_DIRECTORY = "lv_sprites/";
	private static final String ROOM = LEVEL_DIRECTORY + "room_tiles" + PNG;
	private static final String IN_ELEVATOR = LEVEL_DIRECTORY + "in_elevator_tiles" + PNG;
	private static final String OUT_ELEVATOR = LEVEL_DIRECTORY + "out_elevator_tiles" + PNG;
	private static final String ELEVATOR_BACK = LEVEL_DIRECTORY + "elevator_back" + PNG;
	private static final String ELEVATOR_FRONT = LEVEL_DIRECTORY + "elevator_front" + PNG;
	private static final String ELEVATOR_LIMIT = LEVEL_DIRECTORY + "elevator_top" + PNG;
	
	// Entity related
	private static final String ENTITY_DIRECTORY = "entities_sprites/";
	private static final String PLAYER = ENTITY_DIRECTORY + "agent_spritesheet" + PNG;
	private static final String LIFT = ENTITY_DIRECTORY + "lift" + PNG;
	private static final String ROBOT = ENTITY_DIRECTORY + "robot_spritesheet" + PNG;
	private static final String ROBOT_ATTACK = ENTITY_DIRECTORY + "robot_attack" + PNG;
	private static final String BALL = ENTITY_DIRECTORY + "roboball" + PNG;
	private static final String TERMINAL = ENTITY_DIRECTORY + "terminal" + PNG;
	private static final String CONTROL_ROOM = ENTITY_DIRECTORY + "end" + PNG;
	private static final String FURNITURE_DIRECTORY = ENTITY_DIRECTORY + "objects/";
	
	// Extra stuff (UI elements)
	private static final String ROBOTSLEEP_NOTIFICATION = "robot_sleep" + PNG;
	private static final String LIFTRESET_NOTIFICATION = "elevator_reset" + PNG;
	private static final String PUZZLE_DIRECTORY = "puzzles/";
	private static final String HUD_ELEVATOR = "hud_with_map" + PNG;
	private static final String MAP_ELEVATORTILES = "maptiles_elevator" + PNG;
	private static final String MAP_ROOMTILE = "maptiles_room" + PNG;
	
	// Amount of furnitures
	private static final int N_FURNITURES = 22;
	private static final int N_PUZZLES = 8;

	// Instance of the class (Singleton)
	private static SpritesCollection instance;

	// Sprites
	private BufferedImage[][] playerAni;
	private BufferedImage[][] robotAni;
	private BufferedImage[][] robotAtk;
	private BufferedImage[][] outElevTiles;
	private BufferedImage[] furnSprites;
	private BufferedImage[] puzzlesSprites;
	private BufferedImage[] roomTiles;
	private BufferedImage[] inElevTiles;
	private BufferedImage[] mapElevTiles;
	private BufferedImage ballSprite;
	private BufferedImage liftSprite;
	private BufferedImage ctrlRoomSprite;
	private BufferedImage terminalSprite;
	private BufferedImage liftReset;
	private BufferedImage robotSleep;
	private BufferedImage hud;
	private BufferedImage mapRoomPart;
	private BufferedImage elevFront;
	private BufferedImage elevBack;
	private BufferedImage elevBorder;

	/**
	 * Private constructor for the Singleton Pattern, helping to load the sprites only once and then getting all directly
	 */
	private SpritesCollection() {
		loadSprites();
	}

	/**
	 * Used to get the main instance of this (Singleton)
	 * 
	 * @return SpritesCollection  The main instance of the SpritesCollection
	 */
	public static SpritesCollection getInstance() {
		if (instance == null) {
			instance = new SpritesCollection();
		}
		return instance;
	}
	
	/**
	 * Loads all the image of the game, ready to be used
	 */
	private void loadSprites() {
		playerAni = LoadImage.loadGroupImage(PLAYER, 4, 14, 35, 41);
		robotAni = LoadImage.loadGroupImage(ROBOT, 2, 4, 14, 23);
		robotAtk = LoadImage.loadGroupImage(ROBOT_ATTACK, 2, 4, 48, 10);
		roomTiles = LoadImage.loadGroupImage(ROOM, 1, 5, 8, 8)[0];
		inElevTiles = LoadImage.loadGroupImage(IN_ELEVATOR, 1, 7, 64, 64)[0];
		outElevTiles = LoadImage.loadGroupImage(OUT_ELEVATOR, 2, 2, 128, 64);
		mapElevTiles = LoadImage.loadGroupImage(MAP_ELEVATORTILES, 1, 4, 4, 4)[0];
		elevFront = LoadImage.getImage(ELEVATOR_FRONT);
		elevBack = LoadImage.getImage(ELEVATOR_BACK);
		elevBorder = LoadImage.getImage(ELEVATOR_LIMIT);
		mapRoomPart = LoadImage.getImage(MAP_ROOMTILE);
		ballSprite = LoadImage.getImage(BALL);
		liftSprite = LoadImage.getImage(LIFT);
		terminalSprite = LoadImage.getImage(TERMINAL);
		ctrlRoomSprite = LoadImage.getImage(CONTROL_ROOM);
		liftReset = LoadImage.getImage(LIFTRESET_NOTIFICATION);
		robotSleep = LoadImage.getImage(ROBOTSLEEP_NOTIFICATION);
		hud = LoadImage.getImage(HUD_ELEVATOR);
		furnSprites = new BufferedImage[N_FURNITURES];
		puzzlesSprites = new BufferedImage[N_PUZZLES];
		for (int i = 0; i < Math.max(N_FURNITURES, N_PUZZLES); i++) {
			if (i < N_FURNITURES)
				furnSprites[i] = LoadImage.getImage(FURNITURE_DIRECTORY + i + PNG);
			if (i < N_PUZZLES)
				puzzlesSprites[i] = LoadImage.getImage(PUZZLE_DIRECTORY + i + PNG);
		}
	}

	/*
	 * Getters
	 */
	public BufferedImage getPlayerSprite(int type, int index) {
		return playerAni[type][index];
	}

	public BufferedImage getRobotSprite(int type, int index) {
		return robotAni[type][index];
	}

	public BufferedImage getRobotAtkSprite(int type, int index) {
		return robotAtk[type][index];
	}

	public BufferedImage getOutElevTileSprite(int type, int side) {
		return outElevTiles[type][side];
	}

	public BufferedImage getInElevTileSprite(int type) {
		return inElevTiles[type];
	}

	public BufferedImage getFurnSprite(int type) {
		return furnSprites[type];
	}

	public BufferedImage getPuzzleSprite(int type) {
		return puzzlesSprites[type];
	}

	public BufferedImage getRoomTileSprite(int type) {
		return roomTiles[type];
	}

	public BufferedImage getMapElevTileSprite(int type) {
		return mapElevTiles[type];
	}

	public BufferedImage getMapRoomSprite() {
		return mapRoomPart;
	}

	public BufferedImage getBallSprite() {
		return ballSprite;
	}

	public BufferedImage getLiftSprite() {
		return liftSprite;
	}

	public BufferedImage getControlRoomSprite() {
		return ctrlRoomSprite;
	}

	public BufferedImage getTerminalSprite() {
		return terminalSprite;
	}

	public BufferedImage getLiftResetSprite() {
		return liftReset;
	}

	public BufferedImage getRobotSleepSprite() {
		return robotSleep;
	}

	public BufferedImage getHUDSprite() {
		return hud;
	}

	public BufferedImage getElevFrontSprite() {
		return elevFront;
	}

	public BufferedImage getElevBackSprite() {
		return elevBack;
	}

	public BufferedImage getElevBorderSprite() {
		return elevBorder;
	}

}
