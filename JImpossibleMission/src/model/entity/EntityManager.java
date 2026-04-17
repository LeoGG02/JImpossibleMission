package model.entity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import model.entity.detectable.InteractableManager;
import model.entity.detectable.LiftManager;
import model.entity.detectable.entities.*;
import model.entity.enemy.EnemyManager;
import model.entity.enemy.entities.*;
import model.util.EntityFactory;
import model.util.EntityFactory.EntityTypes;

/**
 * Manage the main 3 entities manager, which is Enemy, Lift and Interactables, with updates and creation of the entities.
 * The managers can be null and if all 3 are null this EntityManager will be consider empty.
 */
public class EntityManager {
	
	// Enemies
	private EnemyManager enData;
	// Lifts
	private LiftManager lfData;
	// Interactables
	private InteractableManager inData;
	// Level data used for the enemies to interact with the map
	private int[][] lvData;

	/**
	 * EntityManager constructor that save the lvData and initialize the entities
	 * 
	 * @param fileEntities  Name of the file used to create the entities
	 * @param lvData        Used for the enemies to traverse the Level
	 */
	public EntityManager(String fileEntities, int[][] lvData) {
		this.lvData = lvData;
		loadEntitiesData(fileEntities);
	}
	
	/**
	 * Load all the entities data from a file .txt (Optional if doesn't have any)
	 * 
	 * @throws FileNotFoundException  In case there's no entity file simply let the EntityManager exist but empty
	 * @throws NullPointerException   In case there's no entity file simply let the EntityManager exist but empty
	 * @throws Exception              The rest works like a normal exception,
	 * giving the error message and stopping everything
	 * 
	 * @param fileEntities            Name of the file used to create the entities (can be null)
	 */
	private void loadEntitiesData(String fileEntities) {
		// Datas that about to be used to the other managers
		List<Entity> entities = new ArrayList<Entity>();
		Map<Integer, List<Integer>> floors = new HashMap<Integer, List<Integer>>();
		
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(EntityManager.class.getResourceAsStream("/entities_data/" + fileEntities)))) {
			// Reads the file
			String line = br.readLine();
			// While used to read every line
			while (line != null) {
				// Splits the line, getting all the int
				String[] values = line.split(" ");
				// value 1 = Type of entity, value 2 = Position x,
				// value 3 = Position y, value 4 = Special datas used with certain entity types
				int type = Integer.parseInt(values[0]);
				int x = Integer.parseInt(values[1]);
				int y = Integer.parseInt(values[2]);
				int specialData = Integer.parseInt(values[3]);
				// Adds the floor if is the type is LIFT
				if (EntityTypes.values()[type] == EntityTypes.LIFT) {
					floors.putIfAbsent(x, new ArrayList<Integer>());
					floors.get(x).add(y);
				}
				// Create the entity by a factory and adds it to the list if isn't null
				Optional.ofNullable(EntityFactory.createEntity(type, x, y, specialData)).ifPresent(e -> entities.add(e));
				// Read the next line
				line = br.readLine();
			}
			// Initialize the managers
			initManagers(entities, floors);
		}
		// If isn't present the entity file it simply tells that this EntityManager is empty without throwing the exception
		catch (FileNotFoundException | NullPointerException f) {
			System.out.println("File entities " + fileEntities + " is not present. This EntityManager is empty.");
		}
		catch (Exception e) {
			System.err.println("Error during the entity loading operation!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Initialize the managers given the list of all entities and the map floors (used for the lift data)
	 * 
	 * @param entities  List of the entities to read on
	 * @param floors    Map of the floors used for the LiftManager
	 */
	private void initManagers(List<Entity> entities, Map<Integer, List<Integer>> floors) {
		enData = getEnemyManager(entities, lvData);
		lfData = getLiftManager(entities, floors);
		inData = getInteractManager(entities);
	}
	
	/**
	 * Checks if there's some robots or there's a ball in the given entity list, if is it creates an EnemyManager
	 * 
	 * @param entities       List of entities where it gets only the enemies (Robot and Ball)
	 * @param lvData         Used to help the enemies traverse the level
	 * 
	 * @return EnemyManager  Returns the manager (can be null)
	 */
	private EnemyManager getEnemyManager(List<Entity> entities, int[][] lvData) {
		List<Robot> robots = entities.stream()
				.filter(entity -> entity instanceof Robot)
				.map(entity -> (Robot)entity)
				.collect(Collectors.toList());
		
		Optional<Ball> ball = entities.stream()
				.filter(entity -> entity instanceof Ball)
				.map(entity -> (Ball)entity)
				.findFirst();
		
		if (!robots.isEmpty() || ball.isPresent())
			return new EnemyManager(robots, ball, lvData);
		return null;
	}

	/**
	 * Checks if there's any lifts in the given entity list, if is it creates a LiftManager
	 * 
	 * @param entities      List of entities where it gets only the lifts
	 * @param floor         Floors used by the lifts to where to move
	 * 
	 * @return LiftManager  Returns the manager (can be null)
	 */
	private LiftManager getLiftManager(List<Entity> entities, Map<Integer, List<Integer>> floor) {
		List<Lift> lifts = entities.stream()
				.filter(entity -> entity instanceof Lift)
				.map(entity -> (Lift)entity)
				.collect(Collectors.toList());
		
		if (!lifts.isEmpty())
			return new LiftManager(lifts, floor);
		return null;
	}

	/**
	 * Checks if there's some any interactable in the given entity list, if is it creates an InteractableManager
	 * 
	 * @param entities              List of entities where it gets only the interactables (Furniture, Terminals and Control Room)
	 * 
	 * @return InteractableManager  Returns the manager (can be null)
	 */
	private InteractableManager getInteractManager(List<Entity> entities) {
		List<Furniture> furnitures = entities.stream()
				.filter(entity -> entity instanceof Furniture)
				.map(entity -> (Furniture)entity)
				.collect(Collectors.toList());
		
		List<Terminal> terminals = entities.stream()
				.filter(entity -> entity instanceof Terminal)
				.map(entity -> (Terminal)entity)
				.collect(Collectors.toList());
		
		Optional<ControlRoom> cRoom = entities.stream()
				.filter(entity -> entity instanceof ControlRoom)
				.map(entity -> (ControlRoom)entity)
				.findFirst();
		
		if (!furnitures.isEmpty() || !terminals.isEmpty() || cRoom.isPresent())
			return new InteractableManager(furnitures, terminals, cRoom);
		return null;
	}

	/**
	 * Updates all presented managers
	 * 
	 * @param player  Used to read the player action and datas
	 */
	public void update(Player player) {
		getEnemiesData().ifPresent(e -> e.update(player, getLiftsData()));
		getLiftsData().ifPresent(l -> l.update(player, getEnemiesData().map(e -> e.getBall()).orElse(Optional.empty())));
		getInteractablesData().ifPresent(i -> i.update(player));
	}

	/**
	 * Resets all presented managers
	 */
	public void reset() {
		getEnemiesData().ifPresent(e -> e.resetAllEnemies());
		getLiftsData().ifPresent(l -> l.resetAllLifts());
	}

	/*
	 * Getters
	 */
	public Optional<EnemyManager> getEnemiesData() {
		return Optional.ofNullable(enData);
	}

	public Optional<LiftManager> getLiftsData() {
		return Optional.ofNullable(lfData);
	}

	public Optional<InteractableManager> getInteractablesData() {
		return Optional.ofNullable(inData);
	}
	
	/**
	 * Checks if the EntityManager isn't empty
	 * 
	 * @return boolean  Result that tells if the manager is empty or not
	 */
	public boolean hasData() {
		return getEnemiesData().isPresent() || getLiftsData().isPresent() || getInteractablesData().isPresent();
	}

	/**
	 * equals with Enemy, Lift and Interactables Manager
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EntityManager) {
			EntityManager other = (EntityManager)obj;
			return getEnemiesData().equals(other.getEnemiesData()) &&
					getLiftsData().equals(other.getLiftsData()) &&
					getInteractablesData().equals(other.getInteractablesData());
		}
		return false;
	}

	/**
	 * hashCode of Enemy, Lift and Interactables Manager
	 */
	@Override
	public int hashCode() {
		return Objects.hash(enData, lfData, inData);
	}
}
