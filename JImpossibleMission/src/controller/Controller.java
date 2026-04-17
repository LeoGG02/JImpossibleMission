package controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import model.Game;
import model.Play;
import view.Navigator.ScreenStates;
import view.Window;

/**
 * Controller used to set the game model, the window view and all the controls and commands for the game
 * while also starting the game loop and manage the timers
 */
public class Controller implements Runnable {
	
	private static final int DEFAULT_UPS = 120;

	private Thread gameThread;
	private Game model;
	private Window view;
	private KeyListener inputP, inputR;
	private Timer gameTimer, deathTimer;

	/**
	 * From the constructor, other than getting the model and the view,
	 * I add some additional ActionListeners in some panels,
	 * add Observer for the Observable, the Timer of the game and KeyListeners
	 * 
	 * @param model  The main game logic
	 * @param view   The window of the game
	 */
	public Controller(Game model, Window view) {
		this.model = model;
		this.view = view;
		model.addObserver(view);
		initTimer();
		initInputs();
		initKeyListener();
		initMenuActionListeners();
		initTerminalActionListeners();
		initProfileActionListeners();
		initSettingActionListeners();
		startGameLoop();
	}
	
	/**
	 * Initialize timer
	 */
	private void initTimer() {
		// Game timer, checks if in the model is currently playing and the starting phase in the view has finished
		// before updating the time of the Play
		gameTimer = new Timer(1000, e -> {
			if (model.isPlaying()) {
				if (!view.getPlay().isStarting())
					model.getPlay().updateTime();
			}
			else
				gameTimer.stop();
		});
		// Timer used after dying, making a quick delay before resuming
		deathTimer = new Timer(1000, e -> {
			if (model.getPlay().getDeathTime() > 0)
				model.getPlay().updateDeathTime();
			else
				deathTimer.stop();
		});
	}
	
	/**
	 * Initialize inputs when playing and when in the result screen
	 */
	private void initInputs() {
		inputP = new InputPlay();
		inputR = new InputResult();
	}
	
	/**
	 * Initialize KeyListener, when the current panel is PLAY uses the PlayInputs, when is RESULT uses the ResultInputs
	 */
	private void initKeyListener() {
		view.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				//No Inputs
			}

			@Override
			public void keyPressed(KeyEvent e) {
				switch(view.getCurrentState()) {
				case PLAY -> {
					if (model.getPlay().getPlayState() == Play.PlayState.INPROGRESS && !view.getPlay().isStarting())
						inputP.keyPressed(e);
				}
				case RESULT -> inputR.keyPressed(e);
				default -> {}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (view.getCurrentState() == ScreenStates.PLAY)
					inputP.keyReleased(e);
			}
			
		});
		view.setFocusable(true);
		view.requestFocusInWindow();
	}
	
	/**
	 * ActionListeners for MENU
	 */
	private void initMenuActionListeners() {
		// The play button starts a new Play and starting the timer
		view.getMenu().getPlay().addActionListener(p -> {
			model.start(new Play(model));
			gameTimer.start();
		});
		// Update the current profile to be shown
		view.getMenu().getProfile().addActionListener(p -> view.getProfile().updateProfile(model, model.getCurrentProfile()));
		// Update the leaderboard to be shown
		view.getMenu().getSettings().addActionListener(s -> view.getSettings().updateLeaderboard(model.getLeaderboard()));
	}
	
	/**
	 * ActionListeners for TERMINAL during Play
	 */
	private void initTerminalActionListeners() {
		// Resets the lifts in the room and show a temporary Label that tells if you applied it or not
		view.getTerminal().getLiftReset().addActionListener(lr -> view.getTerminal().showResetNotif(model.getPlay().resetLifts()));
		// Disable the robots in the room and show a temporary Label that tells if you applied it or not
		view.getTerminal().getRobotSleep().addActionListener(rs -> view.getTerminal().showSleepNotif(model.getPlay().disableRobots()));
		// Return to play
		view.getTerminal().getExit().addActionListener(ex -> {
			model.getPlay().setPlayState(Play.PlayState.INPROGRESS);
			model.getPlay().getPlayer().check(false);
		});
	}

	/**
	 * ActionListeners for PROFILE
	 */
	private void initProfileActionListeners() {
		// Changes the profile by getting the index from the dropbox
		view.getProfile().getProfileChoose().addActionListener(p -> 
		model.changeProfile((int)view.getProfile().getProfileChoose().getSelectedItem() - 1));
		// Applies the changes on the current profile (Setting the name and the avatar then saving)
		view.getProfile().getApply().addActionListener(m -> {
			{
				model.getCurrentProfile().setNickname(view.getProfile().getChangeName().getText());
				model.getCurrentProfile().setAvatar((String) view.getProfile().getAvatarChoose().getSelectedItem());
				model.getCurrentProfile().save();
			}
		});
		// Opens an warning window to confirm your action
		view.getProfile().getReset().addActionListener(m -> {
			{
				int answer = JOptionPane.showConfirmDialog(null,
						"Are you sure you want to reset your profile? (This is a irriversable action)",
						"Reset Profile", JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.YES_OPTION)
					model.resetProfile();
			}
		});
	}

	/**
	 * ActionListeners for SETTINGS
	 */
	private void initSettingActionListeners() {
		// Opens an warning window to confirm your action
		view.getSettings().getResetLeaderboard().addActionListener(r -> {
			{
				int answer = JOptionPane.showConfirmDialog(null,
						"Are you sure you want to reset the current Leaderboard? (This is a irriversable action)",
						"Reset Leaderboard", JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.YES_OPTION)
					model.resetLeaderboard();
			}
		});
	}

	/**
	 * Starts a thread for the GameLoop
	 */
	private void startGameLoop() {
		gameThread = new Thread(this);
		gameThread.start();
	}

	/**
	 * Update the Play when is PLAY State
	 */
	private void update() {
		if (view.getCurrentState() == ScreenStates.PLAY) {
			model.update();
			// The player died it starts the death timer
			if (model.getPlay().getPlayer().isDead() && !deathTimer.isRunning())
				deathTimer.start();
		}
	}
	
	/**
	 * Runs the game loop and depending the UPS it updates and render the Play
	 */
	@Override
	public void run() {

		double timePerFrame = 1000000000 / DEFAULT_UPS;

		// Previous Time
		long pTime = System.nanoTime();
		// Last Check
		long lCheck = System.currentTimeMillis();
		// Temp used to show fps in the console (System.out.println)
		int frames = 0;
		// Used to update in a constant way
		double deltaF = 0;

		while (true) {

			// Current Time
			long cTime = System.nanoTime();

			deltaF += (cTime - pTime) / timePerFrame;

			pTime = cTime;

			if (deltaF >= 1) {
				update();
				frames++;
				deltaF--;
			}

			// Temp to show the FPS
			if (System.currentTimeMillis() - lCheck >= 1000) {
				lCheck = System.currentTimeMillis();
				System.out.println("UPS: " + frames);
				System.out.println("Game Timer ON: "+gameTimer.isRunning());
				frames = 0;
			}
		}
	}
	
	/**
	 * Inputs when Play
	 */
	private class InputPlay implements KeyListener {
		
		@Override
		public void keyTyped(KeyEvent e) {
			// empty
		}

		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT, KeyEvent.VK_A -> model.getPlay().getPlayer().setL(true);
			case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> model.getPlay().getPlayer().setR(true);
			case KeyEvent.VK_SPACE -> model.getPlay().getPlayer().setJ(true);
			case KeyEvent.VK_UP, KeyEvent.VK_W -> {
				if (model.getPlay().getPlayer().isIdle()) {
					//Elevator controls
					if (model.getPlay().getLevels().currentInElevator())
						model.getPlay().getLevels().moveElevator(-1);
					//Room controls
					else {
						// Lifts
						model.getPlay().getLevels().getCurrentLv().getEntities().getLiftsData()
						.ifPresent(lf -> lf.commandLift(-1, model.getPlay().getPlayer()));
						// Interactables
						model.getPlay().getLevels().getCurrentLv().getEntities().getInteractablesData().ifPresent(in -> {
							if (in.checkInteractable(model.getPlay()) && in.isTerminal())
								view.getNavigator().navigate(ScreenStates.TERMINAL);
						});
					}
					
				}
			}
			case KeyEvent.VK_DOWN, KeyEvent.VK_S -> {
				if (model.getPlay().getPlayer().isIdle()) {
					//Elevator controls
					if (model.getPlay().getLevels().currentInElevator())
						model.getPlay().getLevels().moveElevator(1);
					//Room controls
					else
						model.getPlay().getLevels().getCurrentLv().getEntities().getLiftsData()
						.ifPresent(lf -> lf.commandLift(1, model.getPlay().getPlayer()));
				}
			}
			case KeyEvent.VK_ESCAPE -> model.end();
			case KeyEvent.VK_R -> {
				model.restart(new Play(model));
				gameTimer.start();
			}
			// Temp used for Debug
			case KeyEvent.VK_BACK_SLASH -> view.getPlay().debug();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			
			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT, KeyEvent.VK_A -> model.getPlay().getPlayer().setL(false);
			case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> model.getPlay().getPlayer().setR(false);
			case KeyEvent.VK_SPACE -> model.getPlay().getPlayer().setJ(false);
			case KeyEvent.VK_UP, KeyEvent.VK_W -> {
				if (model.getPlay().getPlayer().isChecking())
					model.getPlay().getPlayer().check(false);
			}
			}
		}
		
	}
	
	/**
	 * Inputs when Result
	 */
	private class InputResult implements KeyListener {
		
		@Override
		public void keyTyped(KeyEvent e) {
			// Empty
		}

		@Override
		public void keyPressed(KeyEvent e) {
			switch(e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE -> {
				view.getNavigator().navigate(ScreenStates.MENU);
				view.getResult().reset();
			}
			case KeyEvent.VK_R -> {
				model.start(new Play(model));
				gameTimer.start();
				view.getNavigator().navigate(ScreenStates.PLAY);
				view.getResult().reset();
			}
			}
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
			// Empty
		}
		
	}
}
