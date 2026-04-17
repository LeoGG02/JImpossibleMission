package view.panel;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import view.Navigator;
import view.util.*;

/**
 * Panel to show the main menu (access to Play, Profile, Settings and exiting the game)
 */
public class MenuPanel extends Panel {

	/**
	 * Default Serial UID
	 */
	private static final long serialVersionUID = 1L;
	
	// Buttons size
	private static final int BUTTON_W = 250;
	private static final int BUTTON_H = 50;
	// Components
	private JLabel title;
	private JButton play, profile, settings, exit;

	/**
	 * MenuPanel constructor that initialize and add the components
	 * 
	 * @param n  Navigator used to give the command to change panel
	 */
	public MenuPanel(Navigator n) {
		super(n, null);
		initComponents();
		addComponents();
	}

	/**
	 * Initialize components
	 */
	@Override
	protected void initComponents() {
		// Title "JIMPOSSIBLE MISSION"
		title = FactoryComponents.createLabel("JIMPOSSIBLE MISSION", 50, FactoryComponents.YELLOW_CHARACTER);
		// Play button
		play = FactoryComponents.createButton("PLAY", BUTTON_W, BUTTON_H);
		// Profile button
		profile = FactoryComponents.createButton("PROFILE", BUTTON_W, BUTTON_H);
		// Settings button
		settings = FactoryComponents.createButton("SETTINGS", BUTTON_W, BUTTON_H);
		// Exit button
		exit = FactoryComponents.createButton("EXIT", BUTTON_W, BUTTON_H);

	}

	/**
	 * Add the components in their position and in certain case add an ActionListeners inside an another panel
	 * that acts as screen monitor (the one used as background)
	 */
	@Override
	protected void addComponents() {
		
		add(new JPanel(new GridBagLayout()) {
			/*
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
				play.addActionListener(pl -> {
					AudioManager.getInstance().playButtonSound();
					n.navigate(Navigator.ScreenStates.PLAY);
				});
				add(play, g);
			
				g.gridy = 2;
				profile.addActionListener(pr -> {
					AudioManager.getInstance().playButtonSound();
					n.navigate(Navigator.ScreenStates.PROFILE);
				});
				add(profile, g);
				
				g.gridy = 3;
				settings.addActionListener(pr -> {
					AudioManager.getInstance().playButtonSound();
					n.navigate(Navigator.ScreenStates.SETTINGS);
				});
				add(settings, g);
			
				g.gridy = 4;
				g.weighty = 1;
				exit.addActionListener(ex -> System.exit(0));
				add(exit, g);
				
				setBounds(MENU_X, MENU_Y, MENU_W, MENU_H);
				
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

	/*
	 * Getters
	 */
	public JButton getPlay() {
		return play;
	}

	public JButton getProfile() {
		return profile;
	}
	
	public JButton getSettings() {
		return settings;
	}

	public JButton getExit() {
		return exit;
	}

}
