package model.level;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

import model.entity.EntityManager;
import model.entity.Player;
import model.util.MapReader;

/**
 * Level is a place that is loaded by a txt file (getting the lvData and giving to the EntityManager)
 * where Player can traverse and Entity can stay (managed by EntityManager).
 * Other than that, it also save levels in the left and right of it (used by the manager for the level transition).
 */
public abstract class Level {

	// Width and height in tiles
	public static final int LV_WIDTH = 40;
	public static final int LV_HEIGHT = 25;
	// Width and height in real size (Tile x 24)
	public static final int LV_REAL_WIDTH = MapReader.TILE_SIZE * LV_WIDTH;
	public static final int LV_REAL_HEIGHT = MapReader.TILE_SIZE * LV_HEIGHT;
	// Name of the file txt of this room
	protected String fileMap;
	// Room in the left and in the right
	protected Level roomL, roomR;
	protected EntityManager entities;
	// Data of the level that tells which tile is solid or not
	protected int[][] lvData;

	/**
	 * Level constructor that other than getting the data it also initialize the EntityManager
	 * getting the same file name in entity data folder
	 * 
	 * @param fileMap  Name of the file used to create the level
	 */
	public Level(String fileMap) {
		this.fileMap = fileMap;
		loadLvData();
		entities = new EntityManager(fileMap, lvData);
	}

	/**
	 * Load the level data as a matrix with int values
	 * 
	 * @throws Exception  If any exception got thrown simply stops sending also the message
	 */
	private void loadLvData() {
		lvData = new int[LV_HEIGHT][LV_WIDTH];
		try (BufferedReader br = new BufferedReader(new InputStreamReader(Level.class.getResourceAsStream("/rooms_data/" + fileMap)))) {
			for (int y = 0; y < LV_HEIGHT; y++) {
				// Reads the line and it splits it
				String[] num = br.readLine().split(" ");
				// For each elements it transforms from String to int and put it in lvData in x and y position
				for (int x = 0; x < LV_WIDTH; x++)
					lvData[y][x] = Integer.parseInt(num[x]);
			}
		}
		catch (Exception e) {
			System.err.println("Error! The map didn't loaded properly.");
			e.printStackTrace();
		}

	}

	/**
	 * Abstract update method used: entities (Room) and floor position (Elevator)
	 * 
	 * @param p  Used to get data from it and use it for their methods
	 */
	protected abstract void update(Player player);

	/**
	 * Resetting the level basically resets all the entities inside
	 */
	public void reset() {
		entities.reset();
	}

	/*
	 * Getters/Setters
	 */
	public String getFileMap() {
		return fileMap;
	}

	public int[][] getLvData() {
		return lvData;
	}

	public EntityManager getEntities() {
		return entities;
	}

	public String getName() {
		return fileMap.substring(0, fileMap.length() - 4).toUpperCase();
	}
	
	public Level getRightRoom() {
		return roomR;
	}
	
	protected void setRightRoom(Level roomR) {
		this.roomR = roomR;
	}
	
	public Level getLeftRoom() {
		return roomL;
	}
	
	protected void setLeftRoom(Level roomL) {
		this.roomL = roomL;
	}

	/**
	 * Simply gives the name of the file that used to create the Level
	 * 
	 * @return String  Example: "room1.txt"
	 */
	@Override
	public String toString() {
		return fileMap;
	}

	/**
	 * equals with file map and level data
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Level) {
			Level objLv = (Level)obj;
			return fileMap.equals(objLv.getFileMap()) && lvData.equals(objLv.getLvData());
		}
		return false;
	}

	/**
	 * hashCode of file map and level data
	 */
	@Override
	public int hashCode() {
		return Objects.hash(fileMap, lvData);
	}
}
