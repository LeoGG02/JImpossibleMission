package model.entity.detectable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import model.Play;
import model.entity.Player;
import model.entity.detectable.entities.*;

/**
 * Main manager of the interactables entities, used to help with the interactions between
 * Interactable (closer to the Player) and Player.
 */
public class InteractableManager {

	// Furnitures
	private List<Furniture> furnitures;
	// Terminals
	private List<Terminal> terminals;
	// Control Room
	private Optional<ControlRoom> cRoom;
	// Last interactable that the player was near
	private Interactable currentInteractable;

	/**
	 * InteractableManager constructor that collects the furnitures, the terminals and the control room
	 * 
	 * @param furnitures  All the furnitures
	 * @param terminals   All the terminals
	 * @param cRoom       Control Room if present
	 */
	public InteractableManager(List<Furniture> furnitures, List<Terminal> terminals, Optional<ControlRoom> cRoom) {
		this.furnitures = furnitures;
		this.terminals = terminals;
		this.cRoom = cRoom;
	}

	/**
	 * Update checks first if the current interactable is a furniture and 
	 * let update the percent of the furniture if the player is interacting with it.
	 * If not it sets the furniture interaction to false and if the percent reached 0 when the player is not interacting it anymore,
	 * it gives to the player the item and disable the furniture
	 * 
	 * @param player  Used to read his actions and giving the item
	 */
	public void update(Player player) {
		if (isFurniture()) {
			Furniture currentFurniture = (Furniture)currentInteractable;

			currentFurniture.updatePercent();
			// Pull out from checking
			if (!player.isChecking() || player.isDead()) {
				// Case > 0
				if (currentFurniture.isInteracting())
					currentFurniture.setInteraction(false);
				// Case <= 0 (Gives the item and deactivate the furniture)
				if (currentFurniture.getCurrentPercent() <= 0) {
					giveItem(player);
					currentFurniture.deactivate();
					currentInteractable = null;
				}
			}
		}
	}

	/**
	 * Player interaction to the interactable
	 * 
	 * @param p         Used in some interactables to set the playState
	 * 
	 * @return boolean  Returns the result of the action if you successfully interacted or not
	 */
	public boolean checkInteractable(Play p) {
		if (!p.getPlayer().isDead() && nearInteractable(p.getPlayer())) {
			p.getPlayer().check(true);
			currentInteractable.interact(p);
			return true;
		}
		return false;
	}

	/**
	 * Checks if the player is near to a furniture and makes it ready to be interacted by setting the currentInteractable
	 * 
	 * @param player    To check if is near a furniture
	 * 
	 * @return boolean  Result of the check if the player is near any furniture
	 */
	private boolean nearFurniture(Player player) {
		return furnitures.stream()
				.filter(f -> f.isNear(player) && f.isActive())
				.findFirst()
				.map(f -> {
					currentInteractable = f;
					return true;
				}).orElse(false);
	}

	/**
	 * Checks if the player is near to a terminal and makes it ready to be interacted by setting the currentInteractable
	 * 
	 * @param player    To check if is near a terminal
	 * 
	 * @return boolean  Result of the check if the player is near any terminal
	 */
	private boolean nearTerminal(Player player) {
		return terminals.stream()
				.filter(t -> t.isNear(player))
				.findFirst()
				.map(t -> {
					currentInteractable = t;
					return true;
				}).orElse(false);
	}

	/**
	 * Checks if the player is near to the control room (The finish line) and sets the currentInteractable if is present
	 * 
	 * @param player    To check if is near a control room (if even exists)
	 * 
	 * @return boolean  Result of the check if the player is near a control room (if even exists)
	 */
	private boolean nearCRoom(Player player) {
		return cRoom.filter(cr -> cr.isNear(player))
				.map(cr -> {
					currentInteractable = cr;
					return true;
				}).orElse(false);
	}

	/**
	 * General check if the player is close to a furniture, terminal or a control room
	 * 
	 * @param player    To check if is near any interactable
	 * 
	 * @return boolean  Result of the check if the player is near any interactable
	 */
	private boolean nearInteractable(Player player) {
		return nearFurniture(player) || nearTerminal(player) || nearCRoom(player);
	}

	/**
	 * Gives the item to the player
	 * 
	 * @param player  Used to give the item
	 */
	private void giveItem(Player player) {
		Furniture currentFurniture = (Furniture)currentInteractable;
		switch (currentFurniture.getItem()) {
		case 0 -> {}
		case 1 -> player.addLResets();
		case 2 -> player.addRDeacts();
		default -> player.addPuzzlePiece(currentFurniture.getItem() - 3);
		}
	}

	/*
	 * Getters
	 */
	
	/**
	 * Checks the current percent if the current interactable is a furniture
	 * 
	 * @return float  The current percent of the current interactable if is a furniture
	 */
	public float checkCurrentPercent() {
		return isFurniture() ? ((Furniture)currentInteractable).getCurrentPercent() : -1;
	}

	public Interactable getCurrentInteractable() {
		return currentInteractable;
	}

	public List<Furniture> getFurnitures() {
		return furnitures;
	}

	public List<Terminal> getTerminals() {
		return terminals;
	}

	public Optional<ControlRoom> getControlRoom() {
		return cRoom;
	}
	
	public boolean isFurniture() {
		return currentInteractable instanceof Furniture;
	}
	
	public boolean isTerminal() {
		return currentInteractable instanceof Terminal;
	}
	
	public boolean isControlRoom() {
		return currentInteractable instanceof ControlRoom;
	}

	/**
	 * The difference between this and nearInteractable is this one doesn't alterate the currentInteractable,
	 * giving only the information if the player is near an interactable
	 * 
	 * @param player    To check if is near any interactable
	 * 
	 * @return boolean  Result of the check if the player is near any interactable 
	 */
	public boolean isNear(Player player) {
		return furnitures.stream().anyMatch(f -> f.isNear(player)) ||
				terminals.stream().anyMatch(t -> t.isNear(player)) ||
				cRoom.map(cr -> cr.isNear(player)).orElse(false);
	}

	/**
	 * equals with Furniture, Terminal list and Control Room
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof InteractableManager) {
			InteractableManager other = (InteractableManager)obj;
			return furnitures.equals(other.getFurnitures()) &&
					terminals.equals(other.getTerminals()) &&
					cRoom.equals(other.getControlRoom());
		}
		return false;
	}

	/**
	 * hashCode of Furniture, Terminal list and Control Room
	 */
	@Override
	public int hashCode() {
		return Objects.hash(furnitures, terminals, cRoom);
	}

}
