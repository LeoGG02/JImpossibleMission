package model.entity;

import java.util.Optional;

import model.entity.detectable.LiftManager;
import model.level.Level;
import model.util.MapReader;

/**
 * The entity that represents the Player and can be controlled it's movement and it's interaction by the user.
 */
public class Player extends Entity {

	// Width and height for the Player hitbox
	private static final int PLAYER_HITBOX_WIDTH = 27;
	private static final int PLAYER_HITBOX_HEIGHT = 84;
	
	// Hitbox changes
	private static final int OFFSET_JUMP = 27;
	private static final int JUMPING_HEIGHT = 39;

	// Speed and gravity stats
	private static final float PLAYER_SPEED = 3.0f;
	private static final float ORIGINAL_FALL_SPEED = 2.4f;
	private static final float GRAVITY = 0.06f;
	private static final int RUN_LIMIT = 84;
	private static final int JUMP_LIMIT = 72;
	
	/**
	 * Player states
	 */
	public enum PlayerActions {
		RUN, JUMP, IDLE, CHECK;
	}
	private PlayerActions playerAction;
	// Ticks used during actions to do certain actions
	private int actionTick = 0;
	// Falling speed that changes over time if is in the air
	private float fallSpeed = ORIGINAL_FALL_SPEED;
	// RIGHT = true LEFT = false
	private boolean originalDirection, direction;
	// Inputs
	private boolean L = false, R = false, J = false;
	// Actions States
	private boolean isMoving = false, inAir = false, isRolling = false, isDead = false, inMovingLift = false, isChecking = false;
	// Lives
	private int lives = 12;
	// Level datas and Lift datas, used for collisions
	private int[][] lvData;
	private Optional<LiftManager> lfData;
	// Inventory + Checks the amount of piece has a puzzle (int[pieces of puzzle 1, pieces of puzzle 2 ...])
	private int[] puzPieces;
	private int currentLResets = 0, lResets = 0, currentRSleep = 0, rSleep = 0;
	// Temp value used to save the original Y everytime it jumps
	private float previousY;

	/**
	 * Player constructor that initialize the entity hitbox together with the Player datas
	 * 
	 * @param x          Position x where the entity spawns into
	 * @param y          Position y where the entity spawns into
	 * @param currentLv  Level that is currently in (used to get the datas for the collision)
	 */
	public Player(float x, float y, Level currentLv) {
		super(x, y, PLAYER_HITBOX_WIDTH, PLAYER_HITBOX_HEIGHT);
		originalDirection = direction = true;
		initHitBox(x, y, PLAYER_HITBOX_WIDTH, PLAYER_HITBOX_HEIGHT);
		initPlayer();
		setNewLv(currentLv);
	}

	/**
	 * Initialize the player datas
	 */
	private void initPlayer() {
		playerAction = PlayerActions.IDLE;
		puzPieces = new int[] {0, 0};
	}

	/**
	 * Set the state of the player reading the actions is doing
	 */
	private void setAction() {
		PlayerActions initial_action = playerAction;
		if (isRolling)
			playerAction = PlayerActions.JUMP;
		else if (isMoving)
			playerAction = PlayerActions.RUN;
		else if (isChecking)
			playerAction = PlayerActions.CHECK;
		else
			playerAction = PlayerActions.IDLE;
		// If the player action changes it resets the ticks
		if (initial_action != playerAction)
			actionTick = 0;
	}

	/**
	 * Player update
	 */
	public void update() {
		if (!isDead) {
			// If the player is checking or is in a moving lift disables the inputs
			if (!inMovingLift && !isChecking)
				updatePos();
			// Doesn't update the current state and action tick when in the air
			if (!inAir) {
				setAction();
				// If not already jumping or in the air, checks if the player is always on a platform, if not then falls
				if (!isRolling && !MapReader.isOnSomething(hitbox, lvData, lfData))
					inAir = true;
			}
		}
	}

	/**
	 * Update the position of the player
	 */
	private void updatePos() {
		isMoving = false;
		// If is Idle it doesn't go further
		if (!L && !R && !J && !inAir && !isRolling)
			return;

		// Use to alter x position of the hitbox
		float xSpeed = 0;
		
		// If not in the air and not rolling reads the command give (L, R, J)
		if (!inAir && !isRolling) {
			// Left
			if (L & !R) {
				xSpeed -= PLAYER_SPEED;
				direction = false;
			}
			// Right
			if (R & !L) {
				xSpeed += PLAYER_SPEED;
				direction = true;
			}
			// Jump
			if (J) {
				isRolling = true;
				previousY = hitbox.y;
			}
			// Set isMoving if xSpeed is not 0
			if(xSpeed != 0) {
				isMoving = true;
				run();
			}
		}
		// If is in the air or rolling
		else {
			float currentSpeed = PLAYER_SPEED * 1.2f;
			// In the air
			if (inAir) {
				currentSpeed = PLAYER_SPEED / 1.8f;
				// Update the y position and accelerates the fall
				if (MapReader.directionalIsPathFree(hitbox, fallSpeed, false, lvData, lfData)) {
					hitbox.y += fallSpeed;
					fallSpeed += GRAVITY;
				}
				// When the player finally lands on the floor
				else {
					hitbox.y = MapReader.hitTheFloor(hitbox);
					resetAfterLand();
				}

			}
			// Rolling
			else if (isRolling)
				roll();
			// Depending on the direction it also updates xSpeed
			if (direction)
				xSpeed += currentSpeed;
			else
				xSpeed -= currentSpeed;
		}
		updateX(xSpeed);
		// If is goes under the map, immediately dies
		if (hitbox.y > Level.LV_REAL_HEIGHT + 10)
			kill();
	}

	/**
	 * Update Player x
	 * 
	 * @param xSpeed  The step that the entity is about to do
	 */
	private void updateX(float xSpeed) {
		if (MapReader.directionalIsPathFree(hitbox, xSpeed, true, lvData, lfData))
			hitbox.x += xSpeed;
		else
			hitbox.x = MapReader.hitTheWall(hitbox, xSpeed);
	}

	/**
	 * Action run that increases the tick in certain states
	 */
	private void run() {
		actionTick++;
		if (actionTick >= RUN_LIMIT)
			actionTick = 0;
	}

	/**
	 * Action roll that increases the tick in certain states and in certain point changes the size of the hitbox
	 */
	private void roll() {
		actionTick++;
		switch (actionTick) {
		// The beginning of the jumping
		case 12 -> {
			float newY = hitbox.y - OFFSET_JUMP;
			// Depending if on top of the player is free it changes the y
			if (MapReader.isPathFree(hitbox.x, newY, hitbox.width, JUMPING_HEIGHT, lvData, lfData))
				hitbox.y = newY;
			else
				hitbox.y = adjustToCeiling(hitbox.x, newY, hitbox.width, MapReader.getTilesAmount(JUMPING_HEIGHT));
			// New height
			hitbox.height = JUMPING_HEIGHT;
			}
		// Landing of the jump
		case 54 -> {
			// Maximum y that the player can get
			float maxHY = (float)(hitbox.getMaxY()) - PLAYER_HITBOX_HEIGHT;
			// Anti-Stuck System
			antiStuckCheck(maxHY);
			// Adjust
			if (MapReader.isPathFree(hitbox.x, previousY, hitbox.width, PLAYER_HITBOX_HEIGHT, lvData, lfData))
				hitbox.y = previousY;
			else if (MapReader.isPathFree(hitbox.x, maxHY, hitbox.width, PLAYER_HITBOX_HEIGHT, lvData, lfData))
				hitbox.y = adjustToFloor(hitbox.x, hitbox.y, hitbox.width, PLAYER_HITBOX_HEIGHT);
			hitbox.height = PLAYER_HITBOX_HEIGHT;
			}
		case JUMP_LIMIT - 1 -> inAir = true;
		}
	}

	/**
	 * Makes sure to prevent the player to be stuck in between platforms during a roll
	 * 
	 * @param maxHY  Maximum y that the player can get
	 */
	private void antiStuckCheck(float maxHY) {
		// Pushes the player in the direction until is finally free
		while (!MapReader.isPathFree(hitbox.x, previousY, hitbox.width, PLAYER_HITBOX_HEIGHT, lvData, lfData) &&
				!MapReader.isPathFree(hitbox.x, maxHY, hitbox.width, PLAYER_HITBOX_HEIGHT, lvData, lfData)) {
			int tileX = (int) (hitbox.x / MapReader.TILE_SIZE);
			if (direction)
				hitbox.x = (tileX + 1) * MapReader.TILE_SIZE;
			else {
				float xOffset = MapReader.TILE_SIZE - (hitbox.width % MapReader.TILE_SIZE);
				hitbox.x = ((tileX - 1) * MapReader.TILE_SIZE) + xOffset - 1;
			}
		}
	}
	
	/**
	 * Adjust player position under a ceiling during a roll
	 * 
	 * @param x    The x position to check
	 * @param y    The y position to check
	 * @param w    The current width of the player
	 * @param h    The current height of the player
	 * 
	 * @return float  The y position to place right under the ceiling
	 */
	private float adjustToCeiling(float x, float y, float w, float h) {
		int hTiles = MapReader.getTilesAmount(h);
		int currentTileY = (int) (y / MapReader.TILE_SIZE);
		float defaultResult = (currentTileY + 1) * MapReader.TILE_SIZE;
		for (int i = 0; i < hTiles; i++) {
			float checkY = (currentTileY + i) * MapReader.TILE_SIZE;
			if (!MapReader.isFree(x, checkY, lvData, lfData) || !MapReader.isFree(x + w, checkY, lvData, lfData))
				return (currentTileY + i + 1) * MapReader.TILE_SIZE;
		}
		return defaultResult;
	}

	/**
	 * Adjust player position after landing from a roll into a higher platform
	 * 
	 * @param x    The x position to check
	 * @param y    The y position to check
	 * @param w    The current width of the player
	 * @param h    The current height of the player
	 * 
	 * @return float  The y position to place right on top of the floor
	 */
	private float adjustToFloor(float x, float y, float w, float h) {
		int hTiles = MapReader.getTilesAmount(h);
		int currentTileY = (int) (y / MapReader.TILE_SIZE);
		for (int i = 0; i < hTiles; i++) {
			float checkY = (currentTileY + i) * MapReader.TILE_SIZE;
			if (!MapReader.isFree(x, checkY, lvData, lfData) || !MapReader.isFree(x + w, checkY, lvData, lfData))
				return ((currentTileY + i) * MapReader.TILE_SIZE) - h - 1;
		}
		return y;
	}

	/**
	 * Resets certain states and datas when the player lands from a fall
	 */
	private void resetAfterLand() {
		inAir = false;
		if (isRolling)
			isRolling = false;
		fallSpeed = ORIGINAL_FALL_SPEED;
	}

	/**
	 * During a level transition it sets the new x, new y, new lvData and new lfData
	 * 
	 * @param newCurrentLv  Simply gets the new datas from the level that transition into
	 * @param x             New original x position, setting as a new spawn point
	 * @param y             New original y position, setting as a new spawn point
	 */
	public void playerTransition(Level newCurrentLv, int x, int y) {
		this.x = x;
		this.y = y;
		setNewLv(newCurrentLv);
		reset();
		direction = originalDirection = x < Level.LV_REAL_WIDTH/2;
	}

	/**
	 * Sets the new level and lift datas when changes level
	 * 
	 * @param currentLv  Used to get the lvData and lfData
	 */
	private void setNewLv(Level currentLv) {
		lvData = currentLv.getLvData();
		lfData = currentLv.getEntities().getLiftsData();
	}

	/**
	 * Kills the player, reducing his lives too
	 */
	public void kill() {
		isDead = true;
		lives--;
	}

	/*
	 * Getters/Setters
	 */
	public boolean isChecking() {
		return playerAction == PlayerActions.CHECK;
	}

	public void check(boolean isChecking) {
		this.isChecking = isChecking;
	}

	public boolean isIdle() {
		return playerAction == PlayerActions.IDLE;
	}

	public boolean isRight() {
		return direction;
	}

	public boolean isDead() {
		return isDead;
	}
	
	public boolean isInMovingLift() {
		return inMovingLift;
	}

	public void setLiftMoving(boolean inMovingLift) {
		this.inMovingLift = inMovingLift;
	}

	public boolean hasAccess() {
		return getPuzzlePieces() == 4 * puzPieces.length;
	}
	
	public boolean isInAir() {
		return inAir;
	}
	
	public boolean isMoving() {
		return isMoving;
	}
	
	public boolean isRolling() {
		return isRolling;
	}

	public PlayerActions getAction() {
		return playerAction;
	}

	public int getActionTick() {
		return actionTick;
	}

	/**
	 * Lift reset related methods
	 */
	public void addLResets() {
		lResets++;
		currentLResets++;
	}

	public int getResetsMaxFound() {
		return lResets;
	}
	
	public void useReset() {
		if (currentLResets > 0)
			currentLResets--;
	}
	
	public int getCurrentResets() {
		return currentLResets;
	}

	/**
	 * Robot sleep related methods
	 */
	public void addRDeacts() {
		rSleep++;
		currentRSleep++;
	}

	public int getSleepMaxFound() {
		return rSleep;
	}
	
	public void useSleep() {
		if (currentRSleep > 0)
			currentRSleep--;
	}
	
	public int getCurrentSleep() {
		return currentRSleep;
	}

	/**
	 * Adds the piece on one of the puzzles and if is all the puzzles has 4 pieces then the Player has the complete password
	 * 
	 * @param piece  The puzzle piece (represented as number. Example the first puzzle piece is "0")
	 */
	public void addPuzzlePiece(int piece) {
		puzPieces[piece/4]++;
	}

	/**
	 * Checks the amount of puzzle pieces to the give index
	 * 
	 * @param index  Index where to get the puzzle
	 */
	public int getPieces(int index) {
		return puzPieces[index];
	}

	/**
	 * Gives the total of puzzle pieces gotten
	 */
	public int getPuzzlePieces() {
		int sum = 0;
		for (int i : puzPieces)
			sum += i;
		return sum;
	}

	/**
	 * Checks the number of puzzles completed by check each index if the int is 4 (The total amount of piece that has a puzzle)
	 */
	public int numPuzzleCompleted() {
		int sum = 0;
		for (int i : puzPieces)
			if (i == 4)
				sum++;
		return sum;
	}
	
	public int getLives() {
		return lives;
	}

	/**
	 * Put the player in the original state
	 */
	public void reset() {
		resetBooleans();
		resetStats();
		resetStates();
	}

	/*
	 * Inputs states
	 */
	
	/**
	 * Left input check
	 * 
	 * @return  Returns a boolean that tells if the Player is moving on the left
	 */
	public boolean isL() {
		return L;
	}

	/**
	 * Set left input state
	 * 
	 * @param l  Boolean to set
	 */
	public void setL(boolean l) {
		L = l;
	}

	/**
	 * Right input check
	 * 
	 * @return  Returns a boolean that tells if the Player is moving on the right
	 */
	public boolean isR() {
		return R;
	}

	/**
	 * Set right input state
	 * 
	 * @param r  Boolean to set
	 */
	public void setR(boolean r) {
		R = r;
	}

	/**
	 * Jump input check
	 * 
	 * @return  Returns a boolean that tells if the Player is jumping
	 */
	public boolean isJ() {
		return J;
	}

	/**
	 * Set jump input state
	 * 
	 * @param j  Boolean to set
	 */
	public void setJ(boolean j) {
		J = j;
	}
	
	/**
	 * Inputs reset
	 */
	private void resetBooleans() {
		L = R = J = false;
	}

	/**
	 * Player datas reset
	 */
	private void resetStats() {
		playerAction = PlayerActions.IDLE;
		hitbox.x = x;
		hitbox.y = y;
		hitbox.width = PLAYER_HITBOX_WIDTH;
		hitbox.height = PLAYER_HITBOX_HEIGHT;
		direction = originalDirection;
		fallSpeed = ORIGINAL_FALL_SPEED;
	}

	/**
	 * Player states reset
	 */
	private void resetStates() {
		isMoving = inAir = isRolling = inMovingLift = isChecking = isDead = false;
	}
}
