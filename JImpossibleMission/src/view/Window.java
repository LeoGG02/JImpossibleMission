package view;

import java.awt.CardLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JPanel;

import model.Game;
import model.Leaderboard;
import model.Play;
import model.Profile;
import view.panel.*;
import view.util.*;

/**
 * Main frame of the game where creates the Navigator (used to switch Panel),
 * the main Panels and helps giving them commands.
 */
@SuppressWarnings("deprecation")
public class Window extends JFrame implements Observer {
	/**
	 * Default Serial UID
	 */
	private static final long serialVersionUID = 1L;
	// Current panel
	private Navigator.ScreenStates currentState;
	// Navigator used to switch between panels
	private Navigator n;
	// Panels
	private JPanel general;
	private MenuPanel menu;
	private PlayPanel play;
	private TerminalPanel terminal;
	private ProfilePanel profile;
	private SettingsPanel settings;
	private ResultPanel result;

	/**
	 * Window constructor that initialize and set the panels together with icon, title and the navigator
	 */
	public Window() {
		super("JImpossibleMission");
		// Call of the instances with Draw and Audio so they can load all the files (preventing the NullException)
		n = new Navigator();
		n.addObserver(this);
		initPanels();
		add(general);
		setIconImage(LoadImage.getImage("icon.png"));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/**
	 * Initialize the panels of the Window associating with ScreenStates of the Navigator
	 * (MENU, PLAY, TERMINAL, RESULT, PROFILE, SETTINGS)
	 */
	private void initPanels() {
		System.out.println("Resolution: " + Panel.GAME_WIDTH + ", " + Panel.GAME_HEIGHT);
		general = new JPanel(new CardLayout());
		// The first inserted panel is also the first panel showing up upon opening the game so it sets the current panel to MENU
		general.add(menu = new MenuPanel(n), Navigator.ScreenStates.MENU.name());
		currentState = Navigator.ScreenStates.MENU;
		general.add(play = new PlayPanel(n), Navigator.ScreenStates.PLAY.name());
		general.add(terminal = new TerminalPanel(n), Navigator.ScreenStates.TERMINAL.name());
		general.add(profile = new ProfilePanel(n), Navigator.ScreenStates.PROFILE.name());
		general.add(settings = new SettingsPanel(n), Navigator.ScreenStates.SETTINGS.name());
		general.add(result = new ResultPanel(n), Navigator.ScreenStates.RESULT.name());
		if (AudioManager.getInstance().isTrackEnabled())
			AudioManager.getInstance().startTrack();
	}

	/**
	 * Used to render the play panel together with the audio
	 */
	private void renderPlay() {
		play.repaint();
		play.updateSounds();
	}

	/**
	 * Used to enable/disable the track audio when switches in certain Panel
	 */
	private void trackSwitch(Navigator.ScreenStates screen) {
		boolean isNoTrackPanel = screen == Navigator.ScreenStates.PLAY ||
				screen == Navigator.ScreenStates.RESULT || screen == Navigator.ScreenStates.TERMINAL;
		
		if (AudioManager.getInstance().getTrack().isRunning() && isNoTrackPanel)
			AudioManager.getInstance().stopTrack();
		else if (!AudioManager.getInstance().getTrack().isRunning() && !isNoTrackPanel)
			AudioManager.getInstance().startTrack();
	}

	/**
	 * Receive the notification from the model or navigator. With the navigator, it simply switches panels between ScreenStates.
	 * 
	 * @param o    The Observable
	 * @param arg  The Object set with the notification
	 * Depending on the arg, does certain action to specific panels:
	 * - Sets the current play on Play Panel so can directly get all the information from it
	 * - Updates the profile showing when it switches to a different one
	 * - Updates the leaderboard when resetted
	 * - Depending on the PlayState received, it switches Panel to Result (if WIN or LOST) or back to the Menu (if FORCEDSTOP)
	 * - If the arg is null simply updates the render and sounds (this each time the Play updates)
	 */
	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof Navigator && arg instanceof Navigator.ScreenStates screen) {
			if (currentState == Navigator.ScreenStates.PLAY)
				AudioManager.getInstance().stopAllSingularsSounds();
			
			if (AudioManager.getInstance().isTrackEnabled())
				trackSwitch(screen);
			
			currentState = screen;
			((CardLayout) general.getLayout()).show(general, currentState.name());
		}
		
		if (o instanceof Game game) {

			if (arg instanceof Play play) {
				this.play.setPlay(play);
			}

			if (arg instanceof Profile profile)
				this.profile.updateProfile(game, profile);
			
			if (arg instanceof Leaderboard leaderboard)
				settings.updateLeaderboard(leaderboard);

			if (arg instanceof Play.PlayState playState) {
				if (playState != Play.PlayState.FORCEDSTOP) {
					result.updateResult(game, playState);
					switch(playState) {
					case WIN -> play.setVictory();
					case LOST -> {
						n.navigate(Navigator.ScreenStates.RESULT);
						AudioManager.getInstance().playSingularSound(AudioManager.VILLAIN_VICTORY);
					}
					default -> {}
					}
				}
				else
					n.navigate(Navigator.ScreenStates.MENU);
			}
			
			if (arg == null)
				renderPlay();
		}
	}

	/*
	 * Getters
	 */
	public Navigator.ScreenStates getCurrentState() {
		return currentState;
	}

	public Navigator getNavigator() {
		return n;
	}

	public JPanel getPanel() {
		return general;
	}

	public MenuPanel getMenu() {
		return menu;
	}

	public PlayPanel getPlay() {
		return play;
	}
	
	public TerminalPanel getTerminal() {
		return terminal;
	}

	public ProfilePanel getProfile() {
		return profile;
	}
	
	public SettingsPanel getSettings() {
		return settings;
	}

	public ResultPanel getResult() {
		return result;
	}

}
