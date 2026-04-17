package view.panel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import view.Navigator;
import view.util.*;

/**
 * Panel used to show the Terminal screen (access on passes for resetting the lifts and temporarily disable the enemies)
 */
public class TerminalPanel extends Panel {

	/**
	 * Serial UID Generated
	 */
	private static final long serialVersionUID = 1L;
	// Texts of the notification
	private static final String NO_PASS = "PASSWORD REQUIRED";
	private static final String YES_PASS = "PASSWORD ACCEPTED";
	// New datas for JPanel with GridBagLayout
	private static final int TERMINAL_X = 132;
	private static final int TERMINAL_Y = 60;
	private static final int TERMINAL_W = 696;
	private static final int TERMINAL_H = 384;
	// Buttons size
	private static final int BUTTON_W = 500;
	private static final int BUTTON_H = 65;
	private static final int BORDER_SIZE = 15;
	// Size of the text
	private static final int DEFAULT_FONT_SIZE = 20;
	// Components
	private JLabel title, resetNotif, sleepNotif;
	private JButton liftReset, robotSleep, exit;
	private List<JButton> buttons;
	private List<JLabel> notif;
	private BufferedImage background;
	// Timer to show the PASSWORD REQUIRED/ACCEPTED notification
	private Timer t;
	// Delayed timer each time presses the reset or disable
	private int notifTimer;

	/**
	 * TerminalPanel constructor that initialize and add the components and timer
	 * 
	 * @param n  Navigator used to give the command to change panel
	 */
	public TerminalPanel(Navigator n) {
		super(n, null);
		initComponents();
		initTimer();
		addComponents();
	}

	/**
	 * Initialize the timer that once it runs out all the button gets re-enable and disable the notification box
	 */
	private void initTimer() {
		t = new Timer(1000, e -> {
			if (notifTimer > 0)
				notifTimer--;
			else {
				notifTimer = 1;
				buttons.forEach(b -> b.setEnabled(true));
				buttons.stream()
				.filter(b -> !b.isVisible())
				.forEach(b -> b.setVisible(true));
				notif.stream()
				.filter(n -> n.isVisible())
				.forEach(n -> n.setVisible(false));
				repaint();
				t.stop();
			}
		});
	}
	
	/**
	 * Initialize components
	 */
	@Override
	protected void initComponents() {
		// Background
		background = LoadImage.getImage("terminal_screen_empty.png");
		// Terminal title
		title = FactoryComponents.createLabel("** SECURITY TERMINAL OF THIS ROOM **",
				DEFAULT_FONT_SIZE, FactoryComponents.YELLOW_CHARACTER);
		// Reset Lift button
		liftReset = FactoryComponents.createButton("RESET LIFTING PLATFORMS", BUTTON_W, BUTTON_H);
		// Reset Lift notification after pressing it
		resetNotif = FactoryComponents.createLabel("", DEFAULT_FONT_SIZE, FactoryComponents.YELLOW_CHARACTER);
		resetNotif.setPreferredSize(new Dimension(BUTTON_W, BUTTON_H));
		resetNotif.setBorder(BorderFactory.createLineBorder(FactoryComponents.YELLOW_CHARACTER, BORDER_SIZE));
		// Disable Robot button
		robotSleep = FactoryComponents.createButton("TEMPORARILY DISABLE ROBOTS", BUTTON_W, BUTTON_H);
		// Disable Robot notification
		sleepNotif = FactoryComponents.createLabel("", DEFAULT_FONT_SIZE, FactoryComponents.YELLOW_CHARACTER);
		sleepNotif.setPreferredSize(new Dimension(BUTTON_W, BUTTON_H));
		sleepNotif.setBorder(BorderFactory.createLineBorder(FactoryComponents.YELLOW_CHARACTER, BORDER_SIZE));
		// Exit button to get back on play
		exit = FactoryComponents.createButton("LOG OFF", BUTTON_W/2, BUTTON_H-15);
		// Collections of buttons and notification
		buttons = List.of(liftReset, robotSleep, exit);
		notif = List.of(resetNotif, sleepNotif);
		// Delay time to show the nofication
		notifTimer = 1;
	}

	/**
	 * Add the components in their position and in certain case add an ActionListeners inside an another panel
	 * that acts as screen monitor (the one used as background)
	 */
	@Override
	protected void addComponents() {
		
		add(new JPanel(new GridBagLayout()) {
			/**
			 * Default Serial UID
			 */
			private static final long serialVersionUID = 1L;

			{
				GridBagConstraints g = new GridBagConstraints();
				
				g.gridx = 0;
				g.gridy = 0;
				g.weighty = 1;
				add(title, g);

				g.gridy = 1;
				g.weighty = 0.1;
				add(resetNotif, g);
				liftReset.addActionListener(lr -> {
					buttons.forEach(b -> b.setEnabled(false));
					liftReset.setVisible(false);
					repaint();
					t.start();
				});
				add(liftReset, g);
				resetNotif.setVisible(false);

				g.gridy = 2;
				g.weighty = 0.1;
				add(sleepNotif, g);
				robotSleep.addActionListener(rs -> {
					buttons.forEach(b -> b.setEnabled(false));
					robotSleep.setVisible(false);
					repaint();
					t.start();
				});
				add(robotSleep, g);
				sleepNotif.setVisible(false);

				g.gridy = 3;
				g.weighty = 1;
				exit.addActionListener(e -> {
					AudioManager.getInstance().playButtonSound();
					n.navigate(Navigator.ScreenStates.PLAY);
				});
				add(exit, g);
				
				setBounds(TERMINAL_X, TERMINAL_Y, TERMINAL_W, TERMINAL_H);
			}

			/**
			 * Draw the background (filling with the default color background)
			 */
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(FactoryComponents.SCREEN_COLOR_BACKGROUND);
				g.fillRect(0, 0, getWidth(), getHeight());
			}

		});

	}

	/**
	 * Draws the terminal screen as background
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(FactoryComponents.DEFAULT_COLOR_BACKGROUND);
		g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
		g.drawImage(background, 0, 0, GAME_WIDTH, GAME_HEIGHT, null);
	}

	/**
	 * Show the result of the action after pressing the button liftReset
	 * 
	 * @param access  Boolean that tells if has the access to show the YES_PASS text, if not it shows NO_PASS
	 */
	public void showResetNotif(boolean access) {
		resetNotif.setText(access ? YES_PASS : NO_PASS);
		resetNotif.setVisible(true);
		AudioManager.getInstance().playSound(AudioManager.BEEP + (access ? 5 : 1));
	}

	/**
	 * Show the result of the action after pressing the button robotSleep
	 * 
	 * @param access  Boolean that tells if has the access to show the YES_PASS text, if not it shows NO_PASS
	 */
	public void showSleepNotif(boolean access) {
		sleepNotif.setText(access ? YES_PASS : NO_PASS);
		sleepNotif.setVisible(true);
		AudioManager.getInstance().playSound(AudioManager.BEEP + (access ? 5 : 1));
	}

	/**
	 * Getters
	 */
	public JButton getLiftReset() {
		return liftReset;
	}
	
	public JButton getRobotSleep() {
		return robotSleep;
	}
	
	public JButton getExit() {
		return exit;
	}
}
