package view.util.play;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import model.Play;
import model.entity.Player;
import model.entity.detectable.Interactable;
import model.entity.detectable.InteractableManager;
import model.entity.detectable.entities.Furniture;
import model.entity.enemy.entities.Robot;
import model.level.*;
import view.panel.Panel;
import view.util.FactoryComponents;
import view.util.SpritesCollection;

/**
 * Class used to render sprites of the Play
 */
public class RenderPlay {

	private static final Random r = new Random();
	
	// Default scaling of the image
	public static final int DEFAULT_SCALE = 3;

	// Elevator tiles and Room (or Normal) tiles size and pos
	private static final int ROOMTILES_SIZE = 24;
	private static final int ELVTILES_SIZE = 192;
	private static final int TUNNEL_SIZE = ELVTILES_SIZE * 3;
	private static final int OUT_ELVTILES_W_SIZE = ELVTILES_SIZE * 2;
	private static final int ELEV_X = 384;
	private static final int ELEV_Y = 96;
	private static final int INIT_ELVTILES_POS = -96;
	
	// Map HUD
	private static final int MAP_X= 192;
	private static final int MAP_Y = 408;
	
	// Entites offsets (adjusting the sprite position from the current hitbox position)
	// Player
	private static final int PLAYER_OFFESETX = 39;
	private static final int PLAYER_OFFESETY = 33;
	// Ball
	private static final int BALL_OFFSETX = 3;
	private static final int BALL_OFFSETY = 12;
	// Robot
	private static final int ROBOT_OFFSETY = 6;
	
	// Notification box
	private static final int NOTIF_BOX_W = 192;
	private static final int NOTIF_BOX_H = 114;
	private static final int BOX_DISTANCE = 48;
	private static final int NOTIF_SPRITE_W = 144;
	private static final int NOTIF_SPRITE_H = 69;
	private static final int NOTIF_SPRITE_DISTANCE = 24;
	private static final int BAR_DISTANCE = 81;
	private static final int BAR_HEIGHT = 9;
	
	// Copy of the current sprite of the player for the death animation
	private static BufferedImage playerCopy;
	// Components to used to help altering sprites
	private static int rLight = 0, rLightTick = 0, deathTick = 0, alpha = 0, add = -4;

	/**
	 * Draw the current level
	 * 
	 * @param p  Used to just read the information from it (The level datas)
	 */
	public static void drawLevel(Play p, Graphics2D g) {
		if (p.getLevels().currentInRoom())
			drawRoomLevel(p.getLevels().getCurrentLv().getLvData(), g);
		else
			drawElevatorLevel((Elevator)p.getLevels().getCurrentLv(), g);
	}
	
	/**
	 * Draw all the entities (includes the player)
	 * 
	 * @param p  Used to just read the information from it (The player and the entities datas)
	 */
	public static void drawAllEntities(Play p, Graphics2D g) {
		drawEntities(p, g);
		drawPlayer(p.getPlayer(), p.getLevels().currentInElevator(), g);
	}

	/**
	 * Draw all the stuff that shows in front of everything else (the elevator and the notification box)
	 * 
	 * @param p  Used to just read the information from it (For the furniture check)
	 */
	public static void drawOuterStuff(Play p, Graphics2D g) {
		if (p.getLevels().currentInRoom())
			p.getLevels().getCurrentLv().getEntities().getInteractablesData().ifPresent(in -> drawNotificationBox(in, p.getPlayer(), g));
		else
			drawElevator((Elevator)p.getLevels().getCurrentLv(), g);
	}
	
	/**
	 * HUD
	 * 
	 * @param p  Used to just read the information from it (For the passes, puzzles, lives, time and position)
	 */
	public static void drawHud(Play p, Graphics2D g) {
		// Drawing the hud with the map
		BufferedImage hud = SpritesCollection.getInstance().getHUDSprite();
		g.drawImage(hud, 0, 360, hud.getWidth() * 3, hud.getHeight() * 3, null);
		drawMap(((Elevator)p.getLevels().getCurrentLv()), p.getLevels().getMap(), p.getLevels().getElevators(), g);
		
		// Text of the info in the hud (Lifts, Snooze resets and password)
		g.setColor(FactoryComponents.GREEN_CHARACTER);
		g.setFont(FactoryComponents.getFont(16));
		
		g.drawString("LIFT_INIT: " + p.getPlayer().getCurrentResets() + " - " + p.getPlayer().getResetsMaxFound() +
				"/" + LevelManager.MAX_LIFT_PASS, 390, 420);
		g.drawString("SNOOZE: " + p.getPlayer().getCurrentSleep() + " - " + p.getPlayer().getSleepMaxFound() +
				"/" + LevelManager.MAX_ROBOT_PASS, 390, 450);
		g.drawString("PSW: " + p.getPlayer().getPieces(0) + "/4 | " + p.getPlayer().getPieces(1) + "/4 - " +
				p.getPlayer().numPuzzleCompleted()+"/2", 390, 480);
		
		g.drawString("ATTEMPTS: " + p.getPlayer().getLives(), 390, 520);
		int remainingTime = p.getRemainingTime();
		g.drawString(remainingTime / 3600 +":"+ (remainingTime % 3600)/60 +":"+ remainingTime % 60, 390, 550);
	}

	/**
	 * Elevator Level
	 * 
	 * @param elev  Reads the points and rooms to decide which sprite to place
	 */
	private static void drawElevatorLevel(Elevator elev, Graphics2D g) {
		for (int y=0; y<elev.getEndPoint() - elev.getStartPoint() + 1; y++) {
			int entranceIndex = 0;
			for (int i=0; i<2; i++) {
				int type = elev.getRooms()[elev.getStartPoint() + y][i] != null ? 1 : 0;
				entranceIndex += (1 + i) * type;
				// Walls or Tunnels on the sides
				drawOuterElevator(elev, i, y, type, g);
			}
			// The Elevator area
			drawInnerElevator(elev, entranceIndex, y, g);
		}
		// The back of the central elevator
		g.drawImage(SpritesCollection.getInstance().getElevBackSprite(), ELEV_X, ELEV_Y, ELVTILES_SIZE, ELVTILES_SIZE, null);
	}
	
	/**
	 * Outside part of the Elevator Level
	 * 
	 * @param elev  To read the position is currently is the elevator
	 * @param pos   Deciding which side to draw (LEFT = 0 or RIGHT = 1)
	 * @param y     Position y to draw
	 * @param type  Which sprite to draw
	 */
	private static void drawOuterElevator(Elevator elev, int pos, int y, int type, Graphics2D g) {
		int yPos = (TUNNEL_SIZE * y) - elev.getYPos();
		// Checks is in the player's view, if isn't returns, not proceeding with the render
		if (yPos < -TUNNEL_SIZE || yPos > Panel.GAME_HEIGHT)
			return;
		
		int xPos = TUNNEL_SIZE * pos;
		for (int i=0; i<3; i++)
			g.drawImage(SpritesCollection.getInstance().getOutElevTileSprite(i == 1 ? type : 0, pos),
					xPos, INIT_ELVTILES_POS + (i * ELVTILES_SIZE) + yPos, OUT_ELVTILES_W_SIZE, ELVTILES_SIZE, null);
	}
	
	/**
	 * Inside part of the Elevator Level
	 * 
	 * @param elev           To get the position of the elevator with starting and ending point
	 * @param entranceIndex  The sprite to get from the spritesheet
	 * @param y              Position y to draw
	 */
	private static void drawInnerElevator(Elevator elev, int entranceIndex, int y, Graphics2D g) {
		int yPos = (TUNNEL_SIZE * y) - elev.getYPos();
		// Checks is in the player's view, if isn't returns, not proceeding with the render
		if (yPos < -TUNNEL_SIZE || yPos > Panel.GAME_HEIGHT)
			return;
		
		int elv_tileW = ELVTILES_SIZE * 2;
		// Top part
		if (y == 0)
			g.drawImage(SpritesCollection.getInstance().getInElevTileSprite(4),
					ELEV_X, INIT_ELVTILES_POS + yPos, ELVTILES_SIZE, ELVTILES_SIZE, null);
		else
			g.drawImage(SpritesCollection.getInstance().getInElevTileSprite(0),
					ELEV_X, INIT_ELVTILES_POS + yPos, ELVTILES_SIZE, ELVTILES_SIZE, null);
		// Middle part
		g.drawImage(SpritesCollection.getInstance().getInElevTileSprite(entranceIndex),
				ELEV_X, INIT_ELVTILES_POS + ELVTILES_SIZE + yPos, ELVTILES_SIZE, ELVTILES_SIZE, null);
		// Bottom part
		if (y == elev.getEndPoint() - elev.getStartPoint()) {
			g.drawImage(SpritesCollection.getInstance().getInElevTileSprite(5),
					ELEV_X, INIT_ELVTILES_POS + (ELVTILES_SIZE * 2) + yPos, ELVTILES_SIZE, ELVTILES_SIZE, null);
			for (int i=0; i<2; i++)
				g.drawImage(SpritesCollection.getInstance().getOutElevTileSprite(0, i),
						TUNNEL_SIZE * i, INIT_ELVTILES_POS + (ELVTILES_SIZE * 3) + yPos, elv_tileW, ELVTILES_SIZE, null);
			g.drawImage(SpritesCollection.getInstance().getInElevTileSprite(6),
					ELEV_X, INIT_ELVTILES_POS + (ELVTILES_SIZE * 3) + yPos, ELVTILES_SIZE, ELVTILES_SIZE, null);
		}
		else
			g.drawImage(SpritesCollection.getInstance().getInElevTileSprite(0),
					ELEV_X, INIT_ELVTILES_POS + (ELVTILES_SIZE * 2) + yPos, ELVTILES_SIZE, ELVTILES_SIZE, null);
	}
	
	/**
	 * Elevator Render (together with the borders)
	 * 
	 * @param elev  To draw the elevator border on the start and the finish together with the elevator back at the center
	 */
	private static void drawElevator(Elevator elev, Graphics2D g) {
		int startBorderY = -elev.getYPos();
		int endBorderY = 360 + (TUNNEL_SIZE * (elev.getEndPoint() - elev.getStartPoint())) - elev.getYPos();
		g.drawImage(SpritesCollection.getInstance().getElevFrontSprite(), ELEV_X, 0,
				SpritesCollection.getInstance().getElevFrontSprite().getWidth() * DEFAULT_SCALE,
				SpritesCollection.getInstance().getElevFrontSprite().getHeight() * DEFAULT_SCALE, null);
		if (startBorderY >= 0)
			g.drawImage(SpritesCollection.getInstance().getElevBorderSprite(), ELEV_X, startBorderY, ELVTILES_SIZE, ROOMTILES_SIZE, null);
		if (endBorderY < Panel.GAME_HEIGHT)
			g.drawImage(SpritesCollection.getInstance().getElevBorderSprite(), ELEV_X, endBorderY, ELVTILES_SIZE, -ROOMTILES_SIZE, null);
	}
	
	/**
	 * Room Level
	 * 
	 * @param lvData  Reads the integer of the level and depending which is places the tile sprite
	 */
	private static void drawRoomLevel(int[][] lvData, Graphics2D g) {
		for (int y = 0; y < lvData.length; y++)
			for (int x = 0; x < lvData[y].length; x++) {
				int value = lvData[y][x];
				g.drawImage(SpritesCollection.getInstance().getRoomTileSprite(value), ROOMTILES_SIZE * x, ROOMTILES_SIZE * y,
						ROOMTILES_SIZE, ROOMTILES_SIZE, null);
			}
	}
	
	/*
	 * Entities of the Level
	 * 
	 * @param p  Used to just read the information from it (entities datas)
	 */
	private static void drawEntities(Play p, Graphics2D g) {
		
		// ----- Interactables -----

		p.getLevels().getCurrentLv().getEntities().getInteractablesData().ifPresent(in -> {
			in.getFurnitures().stream()
			.filter(f -> f.isActive())
			.forEach(f -> g.drawImage(SpritesCollection.getInstance().getFurnSprite(f.getType()),
					(int)f.getHitbox().x, (int)f.getHitbox().y, (int)f.getHitbox().width, (int)f.getHitbox().height, null));
			
			in.getTerminals()
			.forEach(t -> g.drawImage(SpritesCollection.getInstance().getTerminalSprite(), (int)t.getHitbox().x, (int)t.getHitbox().y,
					(int)t.getHitbox().width, (int)t.getHitbox().height, null));
			
			in.getControlRoom().ifPresent(c -> g.drawImage(SpritesCollection.getInstance().getControlRoomSprite(),
					(int)c.getHitbox().x, (int)c.getHitbox().y, (int)c.getHitbox().width, (int)c.getHitbox().height, null));
		});
		
		// ----- Lifts -----
		
		p.getLevels().getCurrentLv().getEntities().getLiftsData().ifPresent(lf -> lf.getLifts()
				.forEach(l -> g.drawImage(SpritesCollection.getInstance().getLiftSprite(), (int)l.getHitbox().x, (int)l.getHitbox().y,
				(int)l.getHitbox().width, (int)l.getHitbox().height, null)));

		// ----- Enemies -----
		
		p.getLevels().getCurrentLv().getEntities().getEnemiesData().ifPresent(en -> {
			if (en.areActive() && p.getPlayState() == Play.PlayState.INPROGRESS)
				updateLight();
			else if (!en.areActive())
				rLight = 0;
			
			en.getRobots().forEach(r -> drawRobot(r, en.areActive(), g));
			
			en.getBall().ifPresent(b -> {
				if (!b.isDestroyed())
					g.drawImage(SpritesCollection.getInstance().getBallSprite(),
							(int)b.getHitbox().x - BALL_OFFSETX, (int)b.getHitbox().y - BALL_OFFSETY,
							SpritesCollection.getInstance().getBallSprite().getWidth() * DEFAULT_SCALE,
							SpritesCollection.getInstance().getBallSprite().getHeight() * DEFAULT_SCALE, null);
			});
		});
	}
	
	/**
	 * Map in the HUD
	 * 
	 * @param elevator   To read which zone is currently on
	 * @param map        To draw the rooms on the map
	 * @param elevators  To draw all the elevator section
	 */
	private static void drawMap(Elevator elev, Level[][] map, List<Elevator> elevators, Graphics2D g) {
		// Rooms sprites
		int roomTileSize = SpritesCollection.getInstance().getMapRoomSprite().getHeight() * DEFAULT_SCALE;
		for (int x=0; x<map[0].length; x++) {
			for (int y=0; y<map.length; y++) {
				Level room = map[y][x];
				if (room!=null)
					g.drawImage(SpritesCollection.getInstance().getMapRoomSprite(),
							MAP_X + (48 * x), MAP_Y + (roomTileSize * y), roomTileSize, roomTileSize, null);
			}
		}
		// Elevators sprites
		int elevTileSize = SpritesCollection.getInstance().getMapElevTileSprite(0).getHeight() * DEFAULT_SCALE;
		for (int i=0; i<elevators.size(); i++) {
			Elevator currentElev = elevators.get(i);
			for (int j = currentElev.getStartPoint(); j<currentElev.getEndPoint()+1; j++) {
				Level[] rooms_floor = currentElev.getRoomsInFloor(j);
				for (int k=0; k<2; k++) {
					int index = k + (rooms_floor[k] != null ? 2 : 0);
					g.drawImage(SpritesCollection.getInstance().getMapElevTileSprite(index),
							MAP_X + 24 + (elevTileSize * k) + (48 * i), MAP_Y + (elevTileSize * j), elevTileSize, elevTileSize, null);
				}
			}
		}
		// Current position icon
		if (alpha + add >= 255 || alpha + add <= 0)
			add *= -1;
		alpha += add;
		g.setColor(new Color(255, 255, 255, alpha));
		g.fillRect(225 + (48 * elev.getZone()), MAP_Y + (12 * (elev.getStartPoint() + (elev.getYPos()/576))), 6, 9);
	}
	
	/**
	 * Player render
	 * 
	 * @param player      Used to read the player action and state
	 * @param inElevator  Checks if the player is currently in an Elevator Level (to adjust the position of the sprite)
	 */
	private static void drawPlayer(Player player, boolean inElevator, Graphics2D g) {
		BufferedImage currentSprite = null;
		// Initial sprite datas
		int yOffset = PLAYER_OFFESETY;
		// If is in an Elevator Level the sprite gets slightly pushed down
		if (inElevator)
			yOffset -= 13;
		// Depending of the current action selects the sprite from the sprite sheets and if necessary reduces the yOffset
		int currentPoint = player.getActionTick()/6;
		Player.PlayerActions currentAction = player.getAction();
		currentSprite = getPlayerSprite(currentPoint, currentAction);
		if (currentAction == Player.PlayerActions.JUMP && (currentPoint >= 2 && currentPoint < 9))
			yOffset -= 27;
		// Flips value of the sprite depending if the player is turned right or left
		int flipX = player.isRight() ? 0 : currentSprite.getWidth() * DEFAULT_SCALE;
		int flipW = player.isRight() ? 1 : -1;
		// When the player dies starts the animation of death
		if (player.isDead() && player.getHitbox().y < Level.LV_REAL_HEIGHT + 10) {
			// If the first frame of the player's death makes a copy of the sprite to freely manipulate it without modifying the original
			if (playerCopy == null)
				playerCopy = copy(currentSprite);
			updatePlayerDeath();
			currentSprite = playerCopy;
		}
		// Once the death sequence is done the tick and the copy datas will be resetted
		else if (deathTick > 0) {
			deathTick = 0;
			playerCopy = null;
		}
		
		g.drawImage(currentSprite, (int) player.getHitbox().x - PLAYER_OFFESETX + flipX, (int) player.getHitbox().y - yOffset,
				currentSprite.getWidth() * DEFAULT_SCALE * flipW, currentSprite.getHeight() * DEFAULT_SCALE, null);
	}

	/**
	 * Get the player sprite
	 * 
	 * @param currentPoint   The sprite to get
	 * @param currentAction  To decide which type of animation to set
	 */
	private static BufferedImage getPlayerSprite(int currentPoint, Player.PlayerActions currentAction) {
		BufferedImage result = null;
		
		switch (currentAction) {
		case RUN -> result = SpritesCollection.getInstance().getPlayerSprite(0, currentPoint);
		case JUMP -> result = SpritesCollection.getInstance().getPlayerSprite(1, currentPoint);
		case IDLE -> result = SpritesCollection.getInstance().getPlayerSprite(2, 0);
		case CHECK -> result = SpritesCollection.getInstance().getPlayerSprite(3, 0);
		}
		return result;
	}

	/**
	 * Update the copied sprite of the player to simulate the death animation
	 */
	private static void updatePlayerDeath() {
		deathTick++;
		// Every 10 ticks it switches it's colors
		if (deathTick % 10 == 0)
			reverse(playerCopy);
		// After certain time it slowly dissolves into nothing
		if (deathTick > 100)
			dissolve(playerCopy);
	}
	
	/**
	 * Copies the original sprite
	 * 
	 * @param original        The original image to copy from
	 * 
	 * @return BufferedImage  The copied image
	 */
	private static BufferedImage copy(BufferedImage original) {
		BufferedImage copy = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
		Graphics2D g = copy.createGraphics();
		g.drawImage(original, 0, 0, null);
		g.dispose();
		return copy;
	}
	
	/**
	 * Make the player sprite colors go reverse (grey to white and white to grey)
	 * 
	 * @param image  The sprite that switches the color into
	 */
	private static void reverse(BufferedImage image) {
		Color greyPart = new Color(71, 70, 70);
		int w = image.getWidth();
		int h = image.getHeight();
		for (int y=0; y<h; y++)
			for (int x=0; x<w; x++) {
				int p = image.getRGB(x, y);
				Color c = new Color(p, true);
				if (c.getAlpha() == 0)
					continue;
				int a = c.getAlpha();
				int r = (c.getRed() == Color.WHITE.getRed()) ? greyPart.getRed() : Color.WHITE.getRed();
				int g = (c.getGreen() == Color.WHITE.getGreen()) ? greyPart.getGreen() : Color.WHITE.getGreen();
				int b = (c.getBlue() == Color.WHITE.getBlue()) ? greyPart.getBlue() : Color.WHITE.getBlue();
				int newP = (a << 24) | (r << 16) | (g << 8) | b;
				image.setRGB(x, y, newP);
			}
	}

	/**
	 * Picks a random pixel and makes it invisible
	 * 
	 * @param image  The sprite that makes a random colored pixel invisible
	 */
	private static void dissolve(BufferedImage image) {
		int forceStop = 0;
		while (forceStop < image.getWidth() * image.getHeight()) {
			int x = r.nextInt(image.getWidth());
			int y = r.nextInt(image.getHeight());
			Color c = new Color(image.getRGB(x, y), true);
			if (c.getAlpha() != 0) {
				int newP = (0 << 24) | (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
				image.setRGB(x, y, newP);
				return;
			}
			forceStop++;
		}
	}

	/**
	 * Robot Render
	 * 
	 * @param r         Used to read the robot actions and states
	 * @param isActive  Telling if the robots are active or not
	 */
	private static void drawRobot(Robot robot, boolean isActive, Graphics2D g) {
		// Initial datas needed
		int rIndex = robot.getRobotAction() == Robot.RobotAction.TURN ? robot.getActionTick() / (8 / robot.getCurrentSpeed()) : 0;
		if (rIndex > 3)
			rIndex = 3 - (rIndex % 3);
		BufferedImage currentSprite = SpritesCollection.getInstance().getRobotSprite(rLight, rIndex);
		int width = currentSprite.getWidth() * DEFAULT_SCALE;
		// Flips value of the sprite depending if the player is turned right or left 
		int flipX = robot.isRight() ? 0 : width;
		int flipW = robot.isRight() ? 1 : -1;

		g.drawImage(currentSprite, (int) robot.getHitbox().x + flipX, (int) robot.getHitbox().y - ROBOT_OFFSETY,
				width * flipW, currentSprite.getHeight() * DEFAULT_SCALE, null);
		// If is attack renders the attack sprite
		if (robot.getRobotAction() == Robot.RobotAction.ATTACK && isActive)
			drawAttack(robot, g);
	}
	
	/**
	 * Render of Robot range attack
	 * 
	 * @param r  To read the current tick of it's attack
	 */
	private static void drawAttack(Robot robot, Graphics2D g) {
		int atkColor = (robot.getActionTick()/10) % 2;
		int atkIndex = (robot.getActionTick()/12) % 4;
		BufferedImage currentSprite = SpritesCollection.getInstance().getRobotAtkSprite(atkColor, atkIndex);
		int width = currentSprite.getWidth() * DEFAULT_SCALE;
		int atkX = robot.isRight() ? (int)robot.getHitbox().getMaxX() : (int)robot.getHitbox().getMinX() - width;
		g.drawImage(currentSprite, atkX, (int) robot.getHitbox().y,
				width, currentSprite.getHeight() * DEFAULT_SCALE, null);
	}

	/**
	 * Update light used for Robot sprite
	 */
	private static void updateLight() {
		rLightTick++;
		if (rLightTick == 50) {
			rLightTick = 0;
			rLight = rLight == 0 ? 1 : 0;
		}
	}

	/**
	 * Notification box when the player interacts with a furniture or the control room
	 * 
	 * @param interactable  Used to read the current state (If isn't a Terminal)
	 * @param player        Simply used to see if is checking or not before drawing the notification
	 */
	private static void drawNotificationBox(InteractableManager interactable, Player player, Graphics2D g) {
		if (player.isChecking() && !player.isDead() && interactable.isNear(player)) {
			Interactable currentInteractable = interactable.getCurrentInteractable();
			if (!interactable.isTerminal())
				drawCheckItem(currentInteractable, interactable.isFurniture(), player, g);
		}
	}

	/**
	 * Checking a Furniture
	 * 
	 * @param interactable  Used to read the current state (If isn't a Terminal)
	 * @param isFurniture   To tell if is a furniture to draw the progression bar/pass/puzzle
	 * @param player        Simply used to see if is checking or not before drawing the notification
	 */
	private static void drawCheckItem(Interactable interactable, boolean isFurniture, Player player, Graphics2D g) {
		int xBox = (int)player.getHitbox().getMaxX() + BOX_DISTANCE;
		if (xBox + NOTIF_BOX_W > Panel.GAME_WIDTH)
			xBox = (int)player.getHitbox().getMinX() - NOTIF_BOX_W - BOX_DISTANCE;
		int yBox = Math.max(0, (int)player.getHitbox().y - BOX_DISTANCE);
		drawBox(xBox, yBox, g);
		
		g.setColor(Color.BLACK);
		if (isFurniture) {
			Furniture furniture = (Furniture)interactable;
			float currentPercent = furniture.getCurrentPercent();
			if (currentPercent <= 0)
				switch (furniture.getItem()) {
				case 0 -> drawNothingHere(xBox, yBox, g);
				case 1 -> g.drawImage(SpritesCollection.getInstance().getLiftResetSprite(),
						xBox + NOTIF_SPRITE_DISTANCE, yBox + NOTIF_SPRITE_DISTANCE, NOTIF_SPRITE_W, NOTIF_SPRITE_H, null);
				case 2 -> g.drawImage(SpritesCollection.getInstance().getRobotSleepSprite(),
						xBox + NOTIF_SPRITE_DISTANCE, yBox + NOTIF_SPRITE_DISTANCE, NOTIF_SPRITE_W, NOTIF_SPRITE_H, null);
				default -> g.drawImage(SpritesCollection.getInstance().getPuzzleSprite(furniture.getItem()-3),
						xBox + NOTIF_SPRITE_DISTANCE, yBox + NOTIF_SPRITE_DISTANCE, NOTIF_SPRITE_W, NOTIF_SPRITE_H, null);
				}
			else
				drawCurrentStatus(xBox, yBox, currentPercent, g);
		}
		else if (!player.hasAccess())
			drawNothingHere(xBox, yBox, g);
	}

	/**
	 * Box of the notification
	 * 
	 * @param x  Position x to draw
	 * @param y  Position y to draw
	 */
	private static void drawBox(int x, int y, Graphics2D g) {
		g.setColor(Color.WHITE);
		g.fillRect(x, y, NOTIF_BOX_W, NOTIF_BOX_H);
	}

	/**
	 * "Nothing here"
	 * 
	 * @param x  Position x to draw
	 * @param y  Position y to draw
	 */
	private static void drawNothingHere(int x, int y, Graphics2D g) {
		g.setFont(FactoryComponents.getFont(24));
		
		Rectangle2D r = g.getFontMetrics().getStringBounds("Nothing", g);
		int centralX = (NOTIF_BOX_W - (int)r.getWidth()) / 2;
		int upperY = ((NOTIF_BOX_H/2) + (int)r.getHeight()) / 2;
		int lowerY = (NOTIF_BOX_H/2) + upperY;
		g.drawString("Nothing", x + centralX, y + upperY);
		
		r = g.getFontMetrics().getStringBounds("here.", g);
		centralX = (NOTIF_BOX_W - (int)r.getWidth()) / 2;
		g.drawString("here.", x + centralX, y + lowerY);
	}

	/**
	 * Progress bar
	 * 
	 * @param x               Position x to draw
	 * @param y               Position y to draw
	 * @param currentPercent  Used to draw the progression bar of the current percent of the furniture
	 */
	private static void drawCurrentStatus(int x, int y, float currentPercent, Graphics2D g) {
		g.setFont(FactoryComponents.getFont(24));
		Rectangle2D r = g.getFontMetrics().getStringBounds("Searching", g);
		int centralX = (NOTIF_BOX_W - (int)r.getWidth()) / 2;
		int upperY = ((NOTIF_BOX_H/2) + (int)r.getHeight()) / 2;
		g.drawString("Searching", x + centralX, y + upperY);
		
		float currentLengthPercentual = NOTIF_BOX_W * (currentPercent / 100);
		g.fillRect(x, y + BAR_DISTANCE, (int)(currentLengthPercentual), BAR_HEIGHT);
	}
	
	/**
	 * Debug information render
	 * 
	 * @param p  Used to read the information for the debug stuff (hitbox, time and etc...) 
	 */
	public static void showDebugStuff(Play p, Graphics2D g) {
		g.setFont(FactoryComponents.getFont(10));
		
		/*
		 * GREEN = InteractableBox, RED = AttackBox, BLUE = HitBox
		 */
		
		// ----- Interactables -----

		p.getLevels().getCurrentLv().getEntities().getInteractablesData().ifPresent(in -> {
			in.getTerminals().forEach(t -> drawEntBox(t.getDetectbox(), Color.GREEN, g));
			in.getFurnitures().stream().filter(f -> f.isActive()).forEach(f -> drawEntBox(f.getDetectbox(), Color.GREEN, g));
		});
				
		// ----- Lifts -----
				
		p.getLevels().getCurrentLv().getEntities().getLiftsData().ifPresent(lf -> lf.getLifts().forEach(l -> {
					drawEntBox(l.getDetectbox(), Color.GREEN, g);
					drawEntBox(l.getHitbox(), Color.BLUE, g);
		}));

		// ----- Enemies -----
		
		p.getLevels().getCurrentLv().getEntities().getEnemiesData().ifPresent(en -> {
			en.getRobots().forEach(r -> {
				drawEntBox(r.getAttackbox(), Color.RED, g);
				drawEntBox(r.getHitbox(), Color.BLUE, g);
			});
					
			en.getBall().ifPresent(b -> {
				if (!b.isDestroyed()) {
					drawEntBox(b.getAttackbox(), Color.RED, g);
					drawEntBox(b.getHitbox(), Color.BLUE, g);
				}
			});
		});

		drawEntBox(p.getPlayer().getHitbox(), Color.BLUE, g);
		g.setColor(Color.RED);
		int lives = p.getPlayer().getLives();
		g.drawString("PLAYER'S LIVES: " + lives, 50, 10);
		if (p.getLevels().currentInElevator()) {
			String roomR = Optional.ofNullable(((Elevator)p.getLevels().getCurrentLv()).getRightRoom())
					.map(room -> room.getName()).orElse("null");
			String roomL = Optional.ofNullable(((Elevator)p.getLevels().getCurrentLv()).getLeftRoom())
					.map(room -> room.getName()).orElse("null");
			g.drawString(Integer.toString(((Elevator)p.getLevels().getCurrentLv()).getYPos()), 430, 50);
			g.drawString(roomL, 50, 155);
			g.drawString(roomR, 650, 155);
		}
		else {
			int time = p.getRemainingTime();
			g.drawString(time / 3600 +":"+ (time % 3600)/60 +":"+ time % 60, 50, 20);
			if (((Room)p.getLevels().getCurrentLv()).getEntities().getEnemiesData().map(en -> !en.areActive()).orElse(false)) {
				String rSleepSec = "ROBOT WAKE UP IN: " + ((Room)p.getLevels().getCurrentLv()).getRemainingRobotSleep();
				g.drawString(rSleepSec, 50, 30);
			}
		}

	}

	/**
	 * Visual of the hitbox/atkbox/dtcbox
	 * 
	 * @param hitbox  The rectangle to draw
	 * @param c       In which color to draw
	 */
	private static void drawEntBox(Rectangle2D.Float hitbox, Color c, Graphics2D g) {
		g.setColor(c);
		g.drawRect((int)hitbox.x, (int)hitbox.y, (int)hitbox.width, (int)hitbox.height);
	}

}
