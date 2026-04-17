package view.panel;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import model.Leaderboard;
import model.RecordSave;
import view.Navigator;
import view.util.*;

/**
 * Panel used to get access to the settings
 * (mute/play the sounds and the soundtrack of the menu, and resetting the leaderboard)
 */
public class SettingsPanel extends Panel {

	/**
	 * Default Serial UID
	 */
	private static final long serialVersionUID = 1L;
	// Used for leaderboard
	private static final String DEFAULT_ID = "-----";
	private static final String DEFAULT_POINTS = "0";
	// Buttons size
	private static final int BUTTON_W = 150;
	private static final int BUTTON_H = 50;
	// Components
	private JLabel title;
	private List<JLabel> ids, points;
	private JButton menu, resetLB, muteTrack, muteSounds;

	/**
	 * SettingsPanel constructor that initialize and add the components
	 * 
	 * @param n  Navigator used to give the command to change panel
	 */
	public SettingsPanel(Navigator n) {
		super(n, null);
		initComponents();
		addComponents();
	}

	/**
	 * Initialize components
	 */
	@Override
	protected void initComponents() {
		// Settings title
		title = FactoryComponents.createLabel("SETTINGS", 30, FactoryComponents.YELLOW_CHARACTER);
		// Back to the Menu button
		menu = FactoryComponents.createButton("< BACK", BUTTON_W, BUTTON_H);
		// Reset Leaderboard button
		resetLB = FactoryComponents.createButton("RESET THE LEADERBOARD", BUTTON_W * 2, BUTTON_H);
		// Enable/Disable music button
		muteTrack = FactoryComponents.createButton("MUSIC: ON", BUTTON_W, BUTTON_H);
		// Enable/Disable sounds button
		muteSounds = FactoryComponents.createButton("SOUNDS: ON", BUTTON_W, BUTTON_H);
		// Leaderboard
		ids = new ArrayList<JLabel>();
		points = new ArrayList<JLabel>();
		for (int i=0; i<Leaderboard.MAX_CAPACITY; i++) {
			ids.add(FactoryComponents.createLabel(DEFAULT_ID, 20, FactoryComponents.YELLOW_CHARACTER, SwingConstants.RIGHT));
			points.add(FactoryComponents.createLabel(DEFAULT_POINTS, 20, FactoryComponents.YELLOW_CHARACTER, SwingConstants.LEFT));
		}
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

				// Title
				g.gridx = 0;
				g.gridy = 0;
				g.gridwidth = 6;
				g.weighty = 1;
				add(title, g);

				// Leaderboard
				g.gridwidth = 1;
				g.weightx = 0.5;
				g.weighty = 0.1;
				for (int i=0; i<Leaderboard.MAX_CAPACITY; i++) {
					// Current pos in the grid
					int x = 2 * (i/5);
					int y = (i%5) + 1;
					// ID
					g.gridx = x;
					g.gridy = y;
					add(ids.get(i), g);
					// Points
					g.gridx = x + 1;
					g.gridy = y;
					add(points.get(i), g);
				}

				// Reset Leaderboard
				g.gridx = 0;
				g.gridy = 6;
				g.gridwidth = 6;
				g.weighty = 1;
				resetLB.addActionListener(rl -> AudioManager.getInstance().playButtonSound());
				add(resetLB, g);

				// Menu
				g.gridx = 0;
				g.gridy = 7;
				g.gridwidth = 2;
				g.weighty = 0.3;
				g.weightx = 0.3;
				menu.addActionListener(m -> {
					AudioManager.getInstance().playButtonSound();
					n.navigate(Navigator.ScreenStates.MENU);
				});
				add(menu, g);
				
				// Mute Track
				g.gridx = 2;
				muteTrack.addActionListener(mt -> {
					AudioManager.getInstance().playButtonSound();
					
					if (AudioManager.getInstance().isTrackEnabled()) {
						AudioManager.getInstance().stopTrack();
						AudioManager.getInstance().setRunningTrack(false);
					}
					else {
						AudioManager.getInstance().startTrack();
						AudioManager.getInstance().setRunningTrack(true);
					}
					
					muteTrack.setText("MUSIC: " + (AudioManager.getInstance().isTrackEnabled() ? "ON" : "OFF"));
				});
				add(muteTrack, g);
				
				// Mute Sounds Effects
				g.gridx = 4;
				muteSounds.addActionListener(s -> {
					if (AudioManager.getInstance().isSoundsEnabled())
						AudioManager.getInstance().setRunningSound(false);
					else
						AudioManager.getInstance().setRunningSound(true);
					
					muteSounds.setText("SOUNDS: " + (AudioManager.getInstance().isSoundsEnabled() ? "ON" : "OFF"));
				});
				add(muteSounds, g);
				
				setBounds(MENU_X, MENU_Y, MENU_W, MENU_H);
			}
			
			/**
			 * Draw the background (filling with the default color background) + border of the leaderboard
			 */
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D)g;
				// Screen Background
				g2.setColor(FactoryComponents.SCREEN_COLOR_BACKGROUND);
				g2.fillRect(0, 0, getWidth(), getHeight());
				// Rectangle border of the Leaderboard
				int yRect = ids.get(0).getY() - 25;
				int hRect = points.get(points.size() - 1).getY() + points.get(points.size() - 1).getHeight() - yRect + 25;
				g2.setColor(FactoryComponents.YELLOW_CHARACTER);
				g2.setStroke(new BasicStroke(2));
				g2.drawRect(10, yRect, getWidth() - 20, hRect);
			}
		});

	}

	/**
	 * Updates the text of the Leaderboard
	 * 
	 * @param leaderboard  Gets the leaderboard and updates all the texts
	 */
	public void updateLeaderboard(Leaderboard leaderboard) {
		int index = 0;
		for (RecordSave r : leaderboard.getLeaderboardList()) {
			ids.get(index).setText(r.getID());
			points.get(index).setText(Integer.toString(r.getPoints()));
			index++;
		}
		
		for (int i=index; i < ids.size(); i++) {
			ids.get(i).setText(DEFAULT_ID);
			points.get(i).setText(DEFAULT_POINTS);
		}
	}

	/*
	 * Getters
	 */
	public JButton getResetLeaderboard() {
		return resetLB;
	}

	public JButton getMenu() {
		return menu;
	}
	
	public JButton getMuteTrack() {
		return muteTrack;
	}
	
	public JButton getMuteSounds() {
		return muteSounds;
	}
	
}
