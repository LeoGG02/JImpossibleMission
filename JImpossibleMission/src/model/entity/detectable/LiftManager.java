package model.entity.detectable;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import model.entity.Player;
import model.entity.detectable.entities.Lift;
import model.entity.enemy.entities.Ball;
import model.util.MapReader;

/**
 * Main manager for the lifts and their floors, helping with their collisions, updates their positions
 * and command given by the Player.
 */
public class LiftManager {
	
	// List of lifts
	private List<Lift> lifts;
	// Map that has x as key and list of y as value acting like different floors of the lifts in x position
	private Map<Integer, List<Integer>> floors;
	// Last lift that detected the player near it
	private Lift currentLift;

	/**
	 * LiftManager constructor that collects the lifts and their floors
	 * 
	 * @param lifts   All the lifts
	 * @param floors  Stopping points for the lifts
	 */
	public LiftManager(List<Lift> lifts, Map<Integer, List<Integer>> floors) {
		this.lifts = lifts;
		this.floors = floors;
		currentLift = null;
	}

	/**
	 * Updates the lifts if they're moving, pushing the Player and the Ball if is in the way
	 * 
	 * @param player  Reads the player action, pushes if the lift is moving and sets the lifting moving state
	 * @param ball    In case if the ball is in the way and be pushed
	 */
	public void update(Player player, Optional<Ball> ball) {
		lifts.stream()
		.filter(l -> l.getStatus() != 0)
		.forEach(l -> {
			if (l.isNear(player))
				l.push(player);
			ball.ifPresent(b -> {
				if (l.isInTheWay(b))
					l.push(b);
			});
			l.update();
		});
		// Once all the lifts aren't moving disable the inMovingLift status from the player
		if (lifts.stream().allMatch(l -> l.getStatus() == 0) && player.isInMovingLift())
			player.setLiftMoving(false);
	}

	/**
	 * Reset all lifts in their original position
	 */
	public void resetAllLifts() {
		lifts.forEach(l -> {
			l.changeStatus(0, 0);
			l.reset();
		});
	}

	/**
	 * Checks if the given position is within any lift
	 * 
	 * @param x         Position x to check
	 * @param y         Position y to check
	 * 
	 * @return boolean  Result of the check if in the position is within the lift
	 */
	public boolean isLift(float x, float y) {
		return lifts.stream()
				.anyMatch(l -> l.isIn(x, y));
	}

	/**
	 * Check if the hitbox is on top of the lift
	 * 
	 * @param hitbox    Hitbox of the entity used to see it's position
	 * @param wTiles    Amount of tiles that the width of the entity occupy
	 * 
	 * @return boolean  Result of the check if is right on top or not
	 */
	public boolean isOnTheLift(Rectangle2D.Float hitbox, int wTiles) {
		return liftCheckH((float)hitbox.getMinX(), (float)hitbox.getMaxY() + 1, wTiles);
	}

	/**
	 * Horizontal lift check
	 * 
	 * @param x         Position x to check
	 * @param y         Position y to check
	 * @param wTiles    Amount of tiles that the width of the entity occupy (used to check like a for)
	 * 
	 * @return boolean  Result of the check if there's a lift all the way horizontally
	 */
	public boolean liftCheckH(float x, float y, int wTiles) {
		return lifts.stream()
				.anyMatch(l -> IntStream.range(0, wTiles)
						.anyMatch(i -> l.isIn(x + (i * MapReader.TILE_SIZE), y)));
	}

	/**
	 * Vertical lift check
	 * 
	 * @param x         Position x to check
	 * @param y         Position y to check
	 * @param hTiles    Amount of tiles that the height of the entity occupy (used to check like a for)
	 * 
	 * @return boolean  Result of the check if there's a lift all the way vertically
	 */
	public boolean liftCheckV(float x, float y, int hTiles) {
		return lifts.stream()
				.anyMatch(l -> IntStream.range(0, hTiles)
						.anyMatch(i -> l.isIn(x, y + (i * MapReader.TILE_SIZE))));
	}

	/**
	 * Check if you can move in this position
	 * 
	 * @param x         Position x to check
	 * @param y         Position y to check
	 * @param w         Width of the entity
	 * @param h         Height of the entity
	 * @param wTiles    Amount of tiles that the width of the entity occupy (used to check like a for)
	 * @param hTiles    Amount of tiles that the height of the entity occupy (used to check like a for)
	 * 
	 * @return boolean  Result of the check if there's no lift in the way
	 */
	public boolean noLiftInTheWay(float x, float y, float w, float h, int wTiles, int hTiles) {
		return !liftCheckH(x, y, wTiles) && !liftCheckH(x, y + h, wTiles) && !liftCheckV(x, y, hTiles) && !liftCheckV(x + w, y, hTiles);
	}

	/**
	 * Check if the player is fully on the lift setting also the current lift the Player is on
	 * 
	 * @param player    Used to see if the player is fully on the lift
	 * 
	 * @return boolean  Result of the check if the player is fully on the lift
	 */
	public boolean isFullyOn(Player player) {
		Optional<Lift> lift = lifts.stream()
				.filter(l -> l.isIn((float)player.getHitbox().getMinX(), (float)player.getHitbox().getMaxY() + 1) &&
						l.isIn((float)player.getHitbox().getMaxX(), (float)player.getHitbox().getMaxY() + 1))
				.findFirst();
		
		if (lift.isPresent()) {
			currentLift = lift.get();
			return true;
		}
		return false;
	}

	/**
	 * Checks if the highest lift isn't in the first floor
	 * 
	 * @return boolean  Tells if there's a floor that you can go up to or not
	 */
	private boolean checkUp() {
		return lifts.stream()
				.filter(l -> (int)l.getHitbox().x == currentLift.getHitbox().x)
				.allMatch(l -> l.getCurrentFloor() > 0);
	}

	/**
	 * Checks if the lowest lift isn't in the last floor
	 * 
	 * @return boolean  Tells if there's a floor that you can go down to or not
	 */
	private boolean checkDown() {
		return lifts.stream()
				.filter(l -> (int)l.getHitbox().x == currentLift.getHitbox().x)
				.allMatch(l -> l.getCurrentFloor() < floors.get((int) l.getHitbox().x).size() - 1);
	}

	/**
	 * Given a command, applies on all the lifts of the same x
	 * 
	 * @param action  Tells where to go (-1 = UP or 1 = DOWN)
	 * @param player  Set to the lifting moving state
	 */
	public void commandLift(int action, Player player) {
		// If the player isn't fully on doesn't execute the command
		if (!isFullyOn(player))
			return;
		// Set the move and if isn't neither -1 (UP) or 1 (DOWN) doesn't go further
		int move;
		if ( (action == -1 && checkUp()) || (action == 1 && checkDown()))
			move = action;
		else
			return;
		// Sets the player in lift moving state and make all the lifts in x position to move
		player.setLiftMoving(true);
		lifts.stream()
		.filter(l -> (int)l.getHitbox().x == currentLift.getHitbox().x && l.getStatus() == 0)
		.forEach(l -> {
			float destination = floors.get((int)l.getHitbox().x).get(l.getCurrentFloor() + move);
			l.changeStatus(action, destination);
		});
	}

	/*
	 * Getters
	 */
	public List<Lift> getLifts() {
		return lifts;
	}

	public Map<Integer, List<Integer>> getFloors() {
		return floors;
	}

	/*
	 * equals with Lift list and floors map
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LiftManager) {
			LiftManager other = (LiftManager)obj;
			return lifts.equals(other.getLifts()) && floors.equals(other.getFloors());
		}
		return false;
	}

	/*
	 * hashCode of Lift list and floors map
	 */
	@Override
	public int hashCode() {
		return Objects.hash(lifts, floors);
	}

}
