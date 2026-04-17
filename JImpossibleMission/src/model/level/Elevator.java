package model.level;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import model.entity.Player;

/**
 * Level used as transition between Rooms acting like an Elevator that can access different floors for different rooms.
 */
public class Elevator extends Level{

	// Default elevator file for lvData
	private static final String ELEV_FILE = "elevator_data/elevator_room.txt";
	// Position of the entrances for the rooms (used to open or close the passage)
	private static final int LXELEVATOR_BORDER= 17;
	private static final int RXELEVATOR_BORDER= 22;
	// Ceiling and floor limit
	private static final int TYELEVATOR_BORDER= 5;
	private static final int BYELEVATOR_BORDER= 10;
	// Array of the rooms each floor
	private Room[][] rooms;
	// Values of the Elevator (Zone of the elevator, floor/points, current position and movement)
	private int zone, startPoint, endPoint, currentPoint, currentFloor, yPos, move;
	private List<Integer> floors;

	/**
	 * Elevator constructor that set which zone is it and read the map
	 * 
	 * @param zone  Represents which column of the map is in
	 * @param map   Map of the game used to initialize the datas of this Elevator
	 */
	public Elevator(int zone, Room[][] map) {
		super(ELEV_FILE);
		this.zone = zone;
		initDatas(map);
		updateLvData();
	}

	/**
	 * Initialize the data by reading the map, from there it reads the rooms from it's zone together with entrances
	 * (Top or Bottom)
	 */
	private void initDatas(Room[][] map) {
		rooms = new Room[map.length*2][2];
		floors =  new ArrayList<Integer>();
		for (int y=0; y<map.length; y++) {
			int roomY = y * 2;
			// Get the access in the left and in the right {TOP, BOTTOM}
			Room roomL = map[y][zone];
			Room roomR = map[y][zone + 1];
			boolean[] accessL = new boolean[] {Optional.ofNullable(roomL).map(r -> r.hasTopREntrance()).orElse(false),
					Optional.ofNullable(roomL).map(r -> r.hasBottomREntrance()).orElse(false)};
			boolean[] accessR = new boolean[] {Optional.ofNullable(roomR).map(r -> r.hasTopLEntrance()).orElse(false),
					Optional.ofNullable(roomR).map(r -> r.hasBottomLEntrance()).orElse(false)};
			// If is the first column sets the first floor on 0
			if (zone == 0 && roomY == 0)
				floors.add(roomY);
			// (Optional) Sets the first floor is in the same point that there's a room, even if there no entrances
			if (floors.isEmpty() && (accessL[0] || accessL[1] || accessR[0] || accessR[1]))
				floors.add(roomY);
			// Collects the floors with the rooms while also setting this Elevator Level to the rooms
			for (int i=0; i<2; i++) {
				int currentY = roomY+i;
				if (accessL[i] || accessR[i]) {
					if (accessL[i]) {
						rooms[currentY][0] = roomL;
						roomL.setRightRoom(this);
					}
					if (accessR[i]) {
						rooms[currentY][1] = roomR;
						roomR.setLeftRoom(this);
					}
					if (!floors.contains(currentY))
						floors.add(currentY);
				}
			}
		}
		// (Optional) Sets the last floor in the same point that there's a room, even if there no entrances
		if (floors.get(floors.size()-1) % 2 == 0)
			floors.add(floors.get(floors.size()-1) + 1);
		// Setting the datas
		startPoint = floors.get(0);
		endPoint = floors.get(floors.size()-1);
		currentFloor = 0;
		currentPoint = startPoint;
		yPos = 0;
		move = 0;
	}

	/**
	 * Override. Updates the elevator position in case it's moving
	 * 
	 * @param player  Used to set him in moving lift state when the Elevator is moving
	 */
	@Override
	protected void update(Player player) {
		if (move != 0) {
			if (yPos == (currentPoint - startPoint) * 576) {
				player.setLiftMoving(false);
				move = 0;
			}
			else
				yPos += move;
		}
	}

	/**
	 * The Elevator itself doesn't really move but simply use yPos acting as timer to keep the player in inMovingLift
	 * state while also updating the level data to give the illusion that the passage is blocked.
	 * If the room on that side is null then puts a wall, if not opens the passage.
	 */
	private void updateLvData() {
		if (rooms[currentPoint][0] != null)
			putAWall(4, LXELEVATOR_BORDER);
		else
			putAWall(0, LXELEVATOR_BORDER);
		if (rooms[currentPoint][1] != null)
			putAWall(4, RXELEVATOR_BORDER);
		else
			putAWall(0, RXELEVATOR_BORDER);
	}

	/**
	 * Puts a wall or emptiness depending by the in put wall (0 for solid and 4 for free)
	 */
	private void putAWall(int wall, int xPos) {
		int currentY = TYELEVATOR_BORDER;
		while (currentY < BYELEVATOR_BORDER) {
			lvData[currentY][xPos] = wall;
			currentY++;
		}
	}

	/**
	 * Start making the elevator moving
	 * 
	 * @param floor     Which floor have to move on
	 * 
	 * @return boolean  Returns a boolean that tells if you successfully make the Elevator move or not
	 */
	protected boolean moveFloor (int floor) {
		if (floor >= 0 && floor < floors.size()) {
			if (currentFloor > floor)
				move = -9;
			else if (currentFloor < floor)
				move = 9;
			changeFloor(floor);
			return true;
		}
		return false;
	}

	/**
	 * Changes the current floor and point that the Elevator is currently on
	 * 
	 * @param floor  Change the datas into this floor
	 */
	private void changeFloor(int floor) {
		currentFloor = floor;
		currentPoint = floors.get(currentFloor);
		updateLvData();
	}

	/**
	 * Sets to the same floor of the Room that the Player exits from
	 * 
	 * @param roomToFind  The Room that have to find to set into
	 * @param side        Which side of the Elevator have to search (0 = LEFT, 1 = RIGHT)
	 */
	protected void searchRoom(Level roomToFind, int side) {
		for (int i=0; i<floors.size(); i++) {
			int floorCheck = floors.get(i);
			Level roomCheck = rooms[floorCheck][side];
			if (roomCheck != null && roomCheck.equals(roomToFind)) {
				yPos = (floorCheck - startPoint) * 576;
				changeFloor(i);
			}
		}
	}

	/*
	 * Getters
	 */
	public int getZone() {
		return zone;
	}
	
	public int getYPos() {
		return yPos;
	}
	
	public int getStartPoint() {
		return startPoint;
	}
	
	public int getEndPoint() {
		return endPoint;
	}
	
	public int getCurrentFloor() {
		return currentFloor;
	}
	
	public int getCurrentPoint() {
		return currentPoint;
	}
	
	public Level[] getRoomsInFloor(int searchFloor) {
		return rooms[searchFloor];
	}
	
	public Level[][] getRooms() {
		return rooms;
	}
	
	public List<Integer> getFloors() {
		return floors;
	}
	
	@Override
	public Level getLeftRoom() {
		return rooms[currentPoint][0];
	}
	
	@Override
	public Level getRightRoom() {
		return rooms[currentPoint][1];
	}

	/**
	 * Checks if the Elevator is moving
	 * 
	 * @return boolean  Result of the state of movement of the Elevator
	 */
	public boolean isMoving() {
		return move != 0;
	}

	/**
	 * equals with also checks rooms and floors after super
	 */
	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj) && obj instanceof Elevator) {
			Elevator other = (Elevator) obj;
			return rooms.equals(other.getRooms()) && floors.equals(other.getFloors());
		}
		return false;
	}

	/**
	 * hashCode of also checks rooms and floors after super
	 */
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), rooms, floors);
	}
	
}
