package model.level;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import model.entity.detectable.InteractableManager;
import model.entity.detectable.entities.Furniture;
import model.Play;

/**
 * Main manager of the levels that saves them, create the map (with the distribution of the items)
 * and handles the transition and commands of the Player.
 */
public class LevelManager {
	
	private static final Random r = new Random();
	
	// Rows and Columns of the game map
	public static final int MAP_ROW = 6;
	public static final int MAP_COL = 4;
	// Amount of puzzles
	public static final int MAX_LIFT_PASS = 6;
	public static final int MAX_ROBOT_PASS = 6;
	public static final int MAX_PUZZLES_AMOUNT = 8;
	// Datas used for spawn change of the player when changing level
	private static final float XLEVEL_SPAWN = 932;
	private static final float YHIGHER_SPAWN = 59;
	private static final float YLOWER_SPAWN = 491;
	
	// Current level that the player is currently in
	private Level currentLv;
	// Name of the level files
	private List<String> lvDataFiles;
	// Lists of levels divided by Room and Elevator
	private List<Room> rooms;
	private List<Elevator> elevators;
	// Map of the game
	private Room[][] map;
	// Total of pieces that can be found
	private int lResetPass = MAX_LIFT_PASS, rSleepPass = MAX_ROBOT_PASS, puzzleValue = MAX_PUZZLES_AMOUNT;
	// Play used to get datas from
	private Play p;

	/**
	 * Loads the levels and initialize them together with the puzzles distribution and the map
	 * 
	 * @param p  Used to get the datas from (mostly the player)
	 */
	public LevelManager(Play p) {
		this.p = p;
		readLevels();
		initRooms();
		initPuzzles();
		initMap();
		initElevators();
		currentLv = elevators.get(0);
	}

	/**
	 * Reads all the existing .txt files in the "rooms_data" in "resources" directory and collects them into a list
	 */
	private void readLevels() {
		try {
			File folder = new File("resources/rooms_data");
			List<File> files = List.of(folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt")));
			lvDataFiles = files.stream()
					.filter(f -> f.isFile())
					.map(f -> f.getName())
					.collect(Collectors.toList());
		}
		catch (Exception e) {
			System.err.println("Error during the reading of the files!");
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the levels and put all in a list
	 */
	private void initRooms() {
		rooms = lvDataFiles.stream()
				.map(fileName -> new Room(fileName))
				.collect(Collectors.toList());
	}

	/**
	 * Initialize the elevators
	 */
	private void initElevators() {
		elevators = IntStream.range(0, map[0].length-1)
				.mapToObj(i -> new Elevator(i, map))
				.collect(Collectors.toList());
	}

	/**
	 * Initialize the map creating a matrix and distributing the rooms randomly
	 * 
	 * @throws IllegalArgumentException  Being throw in case there's not enough space in the map for rooms or stuck
	 * into the distribution
	 */
	private void initMap() {
		// If has too many rooms to load the map throws an exception
		if (rooms.size() >= MAP_ROW * MAP_COL)
			throw new IllegalArgumentException("There's not enough space for the rooms to load the map.");
		// Map as a matrix
		map = new Room[MAP_ROW][MAP_COL];
		// Shuffles the rooms
		Collections.shuffle(rooms);
		// Force stop used as count-down in case the while (on distribution of the rooms) doesn't stops
		// When it reaches 0 stops the while and throws the exception
		int forceStop = 100;
		// Force place is used to forcefully place the first 2 rooms with left and right side exits
		// on column 1 and 2 making sure the map doesn't get stuck in only one side
		int forcePlace = 2;
		
		for (Room room : rooms) {
			int y = r.nextInt(MAP_ROW);
			int minXPos = 0;
			int maxXPos = MAP_COL;
			// If has a left entrance the minimal x of the map increases to 1
			if (room.hasTopLEntrance() || room.hasBottomLEntrance())
				minXPos++;
			// If has a right entrance the maximum x of the map decrease by 1
			if (room.hasTopREntrance() || room.hasBottomREntrance())
				maxXPos--;
			// Search loop for a free space to place the room
			int x = minXPos == 1 && maxXPos == MAP_COL - 1 && forcePlace > 0 ? forcePlace : r.nextInt(minXPos, maxXPos);
			if (map[y][x] != null && forceStop > 0)
				while (map[y][x] != null && forceStop > 0) {
					x = minXPos == 1 && maxXPos == MAP_COL - 1 && forcePlace > 0 ? forcePlace : r.nextInt(minXPos, maxXPos);
					y = r.nextInt(MAP_ROW);
					forceStop--;
				}
			
			if (forceStop <= 0)
				throw new IllegalArgumentException("The distribution of rooms cycle was forcefully stopped for being stuck in a loop.");
			// Room placement
			map[y][x] = room;
			// Decreases forcePlay each time it places the room with two side exits in a cycle 'for' until it reaches 0
			if (minXPos == 1 && maxXPos == MAP_COL - 1 && forcePlace > 0)
				forcePlace--;
		}
	}

	/**
	 * Distribution of pass and puzzle pieces on random furnitures in random rooms
	 * 
	 * @throws IllefalArgumentException  In case there's no interactables to place or stuck into the distribution
	 */
	private void initPuzzles() {
		// If there's no interactables to all room throws an exception
		if (rooms.stream().allMatch(rm -> rm.getEntities().getInteractablesData().isEmpty()))
			throw new IllegalArgumentException("There's no interactables in the rooms!");
		
		// Creates a list of InteractableManager that has Furnitures
		List<InteractableManager> interactables = rooms.stream()
				// Filters the Room list getting only the ones with InteractableManager
				.filter(rm -> rm.getEntities().getInteractablesData().isPresent())
				// Transforms into Stream<InteractableManager>
				.flatMap(rm -> rm.getEntities().getInteractablesData().stream()
						// Filters all InteractableManagers to get only with Furnitures
						.filter(in -> !in.getFurnitures().isEmpty()))
				// Transforms into a list
				.collect(Collectors.toList());
		
		// Gets a random InteractableManager's Furniture list
		List<Furniture> furnitures = interactables.get(r.nextInt(interactables.size())).getFurnitures();
		// Gets a random Furniture
		Furniture checkFurn = furnitures.get(r.nextInt(furnitures.size()));
		
		int forceStop = 100;

		// Item -> 0 = Nothing, 1 = Lift Resets, 2 = Robot Sleep, <=3 = Puzzle pieces
		int currentItem;
		while((lResetPass > 0 || rSleepPass > 0 || puzzleValue > 0) && forceStop > 0) {
			// Lift Reset Pass
			if (lResetPass != 0) {
				currentItem = 1;
				lResetPass--;
			}
			// Robot Sleep Pass
			else if (rSleepPass != 0) {
				currentItem = 2;
				rSleepPass--;
			}
			// Puzzle Pieces
			else
				currentItem = 2 + puzzleValue--;
			// While used to search for an empty furniture
			while((checkFurn == null || checkFurn.getItem() != 0) && forceStop > 0) {
				// Gets a random InteractableManager's Furniture list
				furnitures = interactables.get(r.nextInt(interactables.size())).getFurnitures();
				// Gets a random Furniture
				checkFurn = furnitures.get(r.nextInt(furnitures.size()));
				forceStop--;
			}
			
			if (forceStop == 0)
				throw new IllegalArgumentException("The distribution of items cycle was forcefully stopped for being stuck in a loop.");
			// Puts the item to the furniture
			checkFurn.setItem(currentItem);
		}
	}

	/**
	 * Update the current level and check for the level transition
	 */
	public void update() {
		// Update of the current level (and it's entities)
		currentLv.update(p.getPlayer());
		// Level Transition
		// Left
		if (p.getPlayer().getHitbox().getMaxX() < 0)
			changeLv(currentLv.getLeftRoom(), 1);
		// Right
		if (p.getPlayer().getHitbox().getMinX() >= Level.LV_REAL_WIDTH)
			changeLv(currentLv.getRightRoom(), 0);
	}
	
	/**
	 * Changes the current Level
	 * 
	 * @param nextLv     The level that the player transition to from the current level
	 * @param direction  Integer used to position the player on the right or on the left (0 = RIGHT, 1 = LEFT)
	 */
	private void changeLv(Level nextLv, int direction) {
		// In case the next level is an Elevator the Elevator Level searches which floor is currently the player
		if (nextLv instanceof Elevator)
			((Elevator)nextLv).searchRoom(currentLv, direction);
		p.getPlayer().playerTransition(nextLv, (int)XLEVEL_SPAWN * direction, getYTransition(nextLv));
		currentLv = nextLv;
	}
	
	/**
	 * Gives the new y spawn by reading the current and the next Level
	 * 
	 * @param nextLv     The level that the player transition to from the current level
	 * 
	 * @return int       Gives the y used for the new spawn point when changing level
	 */
	private int getYTransition(Level nextLv) {
		if (nextLv instanceof Elevator)
			// Use the y of the starting spawn because the starting spawn is in an Elevator
			// and each Elevator level has the same y when you get into
			return (int)Play.YSTARTING_SPAWN;
		else if (currentInElevator() && ((Elevator)currentLv).getCurrentPoint() % 2 == 1)
			return (int)YLOWER_SPAWN;
		return (int)YHIGHER_SPAWN;
	}
	
	/**
	 * Move the elevator in the Elevator Level
	 * 
	 * @param move  Integer used to tell where to go, 1 = DOWN, -1 = UP
	 */
	public void moveElevator(int move) {
		if (!currentInElevator())
			return;
		
		Elevator elevator = (Elevator)currentLv;
		// If the player is in the center of the elevator gives the command to move
		if (p.getPlayer().getHitbox().getMinX() >= 432 && p.getPlayer().getHitbox().getMaxX() < 528 && !p.getPlayer().isInMovingLift())
			if (elevator.moveFloor(elevator.getCurrentFloor() + move))
				p.getPlayer().setLiftMoving(true);
	}

	/*
	 * Getters
	 */
	public Level getCurrentLv() {
		return currentLv;
	}

	public Level[][] getMap() {
		return map;
	}
	
	public List<Room> getRooms() {
		return rooms;
	}
	
	public List<Elevator> getElevators() {
		return elevators;
	}

	/**
	 * Checks if the current level is a Room
	 * 
	 * @return boolean  Result of the check
	 */
	public boolean currentInRoom() {
		return currentLv instanceof Room;
	}

	/**
	 * Checks if the current level is a Elevator
	 * 
	 * @return boolean  Result of the check
	 */
	public boolean currentInElevator() {
		return currentLv instanceof Elevator;
	}
}
