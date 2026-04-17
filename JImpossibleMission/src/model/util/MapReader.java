package model.util;

import java.awt.geom.Rectangle2D;
import java.util.Optional;

import model.entity.detectable.LiftManager;

/**
 * Class used to help entities to read the map and it's collisions.
 */
public class MapReader {

	// Size of a tile as reference
	public static final int TILE_SIZE = 24;

	/**
	 * Checks if the tile in the given position is solid in the given level data
	 * 
	 * @param x         Position x to check
	 * @param y         Position y to check
	 * @param lvData    Is a matrix formed by integers which tells which tile is a solid one
	 * (if is 4 or higher is a free tile)
	 * 
	 * @return boolean  Result if the point is solid
	 */
	public static boolean isSolid(float x, float y, int[][] lvData) {
		int roomW = TILE_SIZE * lvData[0].length;
		int roomH = TILE_SIZE * lvData.length;

		if (x < 0 || x >= roomW || y < 0 || y >= roomH)
			return false;

		float indexX = (int) (x / TILE_SIZE);
		float indexY = (int) (y / TILE_SIZE);
		int value = lvData[(int) indexY][(int) indexX];
		if (value <= 3)
			return true;
		return false;
	}
	
	/**
	 * Checks if the tile in the give position is a lift in the given lift data, unless is null which gives directly false
	 * 
	 * @param x         Position x to check
	 * @param y         Position y to check
	 * @param lfData    LiftManager that gives information of the lifts
	 * 
	 * @return boolean  Result if the point is a lift
	 */
	public static boolean isLift(float x, float y, Optional<LiftManager> lfData) {
		return lfData.map(lf -> lf.isLift(x, y)).orElse(false);
	}

	/**
	 * Checks if the tile in the give position is free from solid tiles and lift
	 * 
	 * @param x         Position x to check
	 * @param y         Position y to check
	 * @param lvData    Is a matrix formed by integers which tells which tile is a solid one
	 * (if is 4 or higher is a free tile)
	 * @param lfData    LiftManager that gives information of the lifts
	 * 
	 * @return boolean  Result if the point is free
	 */
	public static boolean isFree(float x, float y, int[][] lvData, Optional<LiftManager> lfData) {
		return !isSolid(x, y, lvData) && !isLift(x, y, lfData);
	}

	/**
	 * Horizontal solid check
	 * 
	 * @param x         Position x to check
	 * @param y         Position y to check
	 * @param lvData    Is a matrix formed by integers which tells which tile is a solid one
	 * (if is 4 or higher is a free tile)
	 * 
	 * @return boolean  Result if the horizontal check has a solid tile
	 */
	public static boolean solidCheckH(float x, float y, float w, int[][] lvData) {
		int wTiles = getTilesAmount(w);
		for (int i = 0; i < wTiles; i++) {
			float xL = x + (i * TILE_SIZE);
			float xR = x + w - (i * TILE_SIZE);
			if (xL >= xR)
				return isSolid(xL, y, lvData) || isSolid(xR, y, lvData);
			if (isSolid(xL, y, lvData))
				return true;
			if (isSolid(xR, y, lvData))
				return true;
		}
		return false;
	}

	/**
	 * Vertical solid check
	 * 
	 * @param x         Position x to check
	 * @param y         Position y to check
	 * @param lvData    Is a matrix formed by integers which tells which tile is a solid one
	 * (if is 4 or higher is a free tile)
	 * 
	 * @return boolean  Result if the vertical check has a solid tile
	 */
	public static boolean solidCheckV(float x, float y, float h, int[][] lvData) {
		int hTiles = getTilesAmount(h);
		for (int i = 0; i < hTiles; i++) {
			float yTop = y + (i * TILE_SIZE);
			float yBot = y + h - (i * TILE_SIZE);
			if (yTop >= yBot)
				return isSolid(x, yTop, lvData) || isSolid(x, yBot, lvData);
			if (isSolid(x, yTop, lvData))
				return true;
			if (isSolid(x, yBot, lvData))
				return true;
		}
		return false;
	}
	
	/**
	 * Horizontal Lift check
	 * 
	 * @param x         Position x to check
	 * @param y         Position y to check
	 * @param lfData    LiftManager that gives information of the lifts
	 * 
	 * @return boolean  Result if the horizontal check has a lift
	 */
	public static boolean liftCheckH(float x, float y, float w, Optional<LiftManager> lfData) {
		int wTiles = getTilesAmount(w);
		return lfData.map(lf -> lf.liftCheckH(x, y, wTiles)).orElse(false);
	}

	/**
	 * Vertical Lift check
	 * 
	 * @param x         Position x to check
	 * @param y         Position y to check
	 * @param lfData    LiftManager that gives information of the lifts
	 * 
	 * @return boolean  Result if the vertical check has a lift
	 */
	public static boolean liftCheckV(float x, float y, int h, Optional<LiftManager> lfData) {
		int hTiles = getTilesAmount(h);
		return lfData.map(lf -> lf.liftCheckV(x, y, hTiles)).orElse(false);
	}

	/**
	 * Check if the hitbox is on top of a floor
	 * 
	 * @param hitbox    A rectangle used as hitbox of the entity
	 * @param lvData    Is a matrix formed by integers which tells which tile is a solid one
	 * (if is 4 or higher is a free tile)
	 * 
	 * @return boolean  Result if the hitbox is on top of a floor
	 */
	public static boolean isOnTheFloor(Rectangle2D.Float hitbox, int[][] lvData) {
		return solidCheckH((float)hitbox.getMinX(), (float)hitbox.getMaxY() + 1, (float)hitbox.getWidth(), lvData);
	}
	
	/**
	 * Check if the hitbox is on top of a lift
	 * 
	 * @param hitbox    A rectangle used as hitbox of the entity
	 * @param lfData    LiftManager that gives information of the lifts
	 * 
	 * @return boolean  Result if the hitbox is on top of a lift
	 */
	public static boolean isOnTheLift(Rectangle2D.Float hitbox, Optional<LiftManager> lfData) {
		int wTiles = getTilesAmount(hitbox.width);
		return lfData.map(lf -> lf.isOnTheLift(hitbox, wTiles)).orElse(false);
	}
	
	/**
	 * Check if the hitbox is on something that is a floor or a lift
	 * 
	 * @param hitbox    A rectangle used as hitbox of the entity
	 * @param lvData    Is a matrix formed by integers which tells which tile is a solid one
	 * (if is 4 or higher is a free tile)
	 * @param lfData    LiftManager that gives information of the lifts
	 * @return boolean  Result if the hitbox is on top of a floor or a lift
	 */
	public static boolean isOnSomething(Rectangle2D.Float hitbox, int[][] lvData, Optional<LiftManager> lfData) {
		return isOnTheFloor(hitbox, lvData) || isOnTheLift(hitbox, lfData);
	}

	/**
	 * Check if you can move in this position with lvData
	 * 
	 * @param x       Position x to check
	 * @param y       Position y to check
	 * @param width   Width of the hitbox of the entity
	 * @param height  Height of the hitbox of the entity
	 * @param lvData  Is a matrix formed by integers which tells which tile is a solid one
	 * (if is 4 or higher is a free tile)
	 * 
	 * @return boolean  Result of the check if the point is free
	 */
	public static boolean isPathFree(float x, float y, float width, float height, int[][] lvData) {
		return !solidCheckH(x, y, width, lvData) && !solidCheckH(x, y + height, width, lvData)
				&& !solidCheckV(x, y, height, lvData) && !solidCheckV(x + width, y, height, lvData);
	}

	/**
	 * Check if you can move in this position with lfData
	 * 
	 * @param x         Position x to check
	 * @param y         Position y to check
	 * @param width     Width of the hitbox of the entity
	 * @param height    Height of the hitbox of the entity
	 * @param lfData    LiftManager that gives information of the lifts
	 * 
	 * @return boolean  Result of the check if the point is free
	 */
	public static boolean isPathFree(float x, float y, float width, float height, Optional<LiftManager> lfData) {
		int wTiles = getTilesAmount(width);
		int hTiles = getTilesAmount(height);
		return lfData.map(lf -> lf.noLiftInTheWay(x, y, width, height, wTiles, hTiles)).orElse(true);
	}

	/**
	 * Check if you can move in this position with lvData and lfData
	 * 
	 * @param x         Position x to check
	 * @param y         Position y to check
	 * @param width     Width of the hitbox of the entity
	 * @param height    Height of the hitbox of the entity
	 * @param lvData    Is a matrix formed by integers which tells which tile is a solid one
	 * (if is 4 or higher is a free tile)
	 * @param lfData    LiftManager that gives information of the lifts
	 * 
	 * @return boolean  Result of the check if the point is free
	 */
	public static boolean isPathFree(float x, float y, float width, float height, int[][] lvData, Optional<LiftManager> lfData) {
		return isPathFree(x, y, width, height, lvData) && isPathFree(x, y, width, height, lfData);
	}
	
	/**
	 * Checks if the path is free towards that direction
	 * 
	 * @param hitbox    A rectangle used as hitbox of the entity
	 * @param changer   Use to add with the current position to read the next step
	 * @param isX       Boolean used to see if the changer is used for pos x or pos y
	 * 
	 * @return boolean  Result of the check if the hitbox going that path is free
	 */
	public static boolean directionalIsPathFree(Rectangle2D.Float hitbox, float changer, boolean isX, 
			int[][] lvData, Optional<LiftManager> lfData) {
		return isX ? isPathFree(hitbox.x + changer, hitbox.y, hitbox.width, hitbox.height, lvData, lfData) : 
			isPathFree(hitbox.x, hitbox.y + changer, hitbox.width, hitbox.height, lvData, lfData);
	}

	/**
	 * Adjust the position of the hitbox to be right next to the wall
	 * 
	 * @param hitbox  A rectangle used as hitbox of the entity
	 * @param xSpeed  The step that the entity is about to do (used to see which direction is going)
	 * 
	 * @return float  The x position used to place the hitbox right next to the wall
	 */
	public static float hitTheWall(Rectangle2D.Float hitbox, float xSpeed) {
		int wTiles = getTilesAmount(hitbox.width); 
		int currentTile = (int)(hitbox.x / TILE_SIZE);
		int xTile = currentTile * TILE_SIZE;
		if (xSpeed > 0) {
			// Wall in the right
			int xOffSet = (int)((TILE_SIZE * wTiles) - hitbox.width);
			return xTile + xOffSet - 1;
		}
		else
			// Wall in the left
			return xTile;
	}

	/**
	 * Adjust the position of the hitbox to be right on the floor
	 * 
	 * @param hitbox  A rectangle used as hitbox of the entity
	 * 
	 * @return float  The y to position the hitbox on the floor
	 */
	public static float hitTheFloor(Rectangle2D.Float hitbox) {
		int hTiles = getTilesAmount(hitbox.height); 
		int currentTile = (int) (hitbox.y / TILE_SIZE);
		int yTile = currentTile * TILE_SIZE;
		int yOffSet = (int) ((TILE_SIZE * hTiles) - hitbox.height);
		return yTile + yOffSet - 1;
	}

	/**
	 * Adjust the position of the hitbox to be right below the ceiling
	 * 
	 * @param hitbox  A rectangle used as hitbox of the entity
	 * 
	 * @return float  The y to position the hitbox under the ceiling
	 */
	public static float hitCeiling(Rectangle2D.Float hitbox) {
		int currentTile = (int) (hitbox.y / TILE_SIZE);
		return currentTile * TILE_SIZE;
	}

	/**
	 * Checks if there's still floor on the way (used for the grounded mobile entity)
	 * 
	 * @param hitbox     A rectangle used as hitbox of the entity
	 * @param xSpeed     The step that the entity is about to do (used to see which direction is going)
	 * @param lvData     Is a matrix formed by integers which tells which tile is a solid one
	 * (if is 4 or higher is a free tile)
	 * @param direction  Boolean used to see which direction is going
	 * 
	 * @return boolean   Result there's the floor is present
	 */
	public static boolean checkFloor(Rectangle2D.Float hitbox, float xSpeed, int[][] lvData, boolean direction) {
		return direction ? isSolid((float)hitbox.getMaxX() + xSpeed, (float)hitbox.getMaxY() + 1, lvData) :
			isSolid((float)hitbox.getMinX() + xSpeed, (float)hitbox.getMaxY() + 1, lvData);
	}

	/**
	 * With the given size, it gives the amount of tiles that occupy
	 * 
	 * @param size  Usually being the width or the height
	 * 
	 * @return int  Amount of tiles that basically occupy this length
	 */
	public static int getTilesAmount(double size) {
		return (int)(size / TILE_SIZE) + (size % TILE_SIZE == 0 ? 0 : 1);
	}
}
