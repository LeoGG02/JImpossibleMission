package view.panel;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import model.Game;
import model.Profile;
import view.Navigator;
import view.util.*;

/**
 * Panel to shows 8 profiles and their stats
 */
public class ProfilePanel extends Panel {
	
	/**
	 * Default Serial UID
	 */
	private static final long serialVersionUID = 1L;
	// Avatar directory + Default avatar
	private static final String AVATAR_DIRECTORY = "avatar/";
	private static final String DEFAULT_AVATAR = AVATAR_DIRECTORY + Profile.DEFAULT_AVATAR;
	// Buttons size
	private static final int BUTTON_W = 150;
	private static final int BUTTON_H = 50;
	// Size of the text
	private static final int DEFAULT_FONT_SIZE = 15;
	private static final int COMPONENT_FONT_SIZE = 13;
	// Components
	private JLabel title, profile, nickname, avatar, missions, stats, level;
	private JComboBox<Integer> profileChoose;
	private JTextField changeName;
	private JComboBox<String> avatarChoose;
	private BufferedImage avatarImg;
	private JButton apply, menu, reset;

	/**
	 * ProfilePanel constructor that initialize and add the components
	 * 
	 * @param n  Navigator used to give the command to change panel
	 */
	public ProfilePanel(Navigator n) {
		super(n, null);
		initComponents();
		addComponents();
	}

	/**
	 * Initialize components
	 */
	@Override
	protected void initComponents() {
		// Profile title
		title = FactoryComponents.createLabel("PROFILE", 30, FactoryComponents.YELLOW_CHARACTER);
		// "Profile: " placed next to the combo box
		profile = FactoryComponents.createLabel("Profile: ", DEFAULT_FONT_SIZE, FactoryComponents.YELLOW_CHARACTER, SwingConstants.LEFT);
		// Combo box that make you choose the profile depending on the index chosen
		profileChoose = new JComboBox<Integer>() {
			/*
			 * Default Serial UID
			 */
			private static final long serialVersionUID = 1L;

			{
				setFont(FactoryComponents.getFont(COMPONENT_FONT_SIZE));
				setForeground(FactoryComponents.YELLOW_CHARACTER);
				setBackground(FactoryComponents.SCREEN_COLOR_BACKGROUND);
				for (int i = 1; i <= Game.MAX_AMOUNT_PROFILES; i++)
					addItem(i);
			}
		};
		// "Nickname: " placed next to the text field
		nickname = FactoryComponents.createLabel("Nickname: ", DEFAULT_FONT_SIZE, FactoryComponents.YELLOW_CHARACTER, SwingConstants.LEFT);
		// Text field to set the nickname
		changeName = new JTextField("Agent") {
			/*
			 * Default Serial UID
			 */
			private static final long serialVersionUID = 1L;

			{
				setFont(FactoryComponents.getFont(COMPONENT_FONT_SIZE));
				setPreferredSize(new Dimension(BUTTON_W, BUTTON_H/2));
				setForeground(FactoryComponents.YELLOW_CHARACTER);
				setBackground(FactoryComponents.SCREEN_COLOR_BACKGROUND);
				setCaretColor(FactoryComponents.YELLOW_CHARACTER);
			}
		};
		// "Avatar: " placed next to the combo box
		avatar = FactoryComponents.createLabel("Avatar: ", DEFAULT_FONT_SIZE, FactoryComponents.YELLOW_CHARACTER, SwingConstants.LEFT);
		// Combo box that make you choose the avatar depending on the name chosen
		avatarChoose = new JComboBox<String>() {
			/*
			 * Default Serial UID
			 */
			private static final long serialVersionUID = 1L;

			{
				setFont(FactoryComponents.getFont(COMPONENT_FONT_SIZE));
				setForeground(FactoryComponents.YELLOW_CHARACTER);
				setBackground(FactoryComponents.SCREEN_COLOR_BACKGROUND);
				// Reads the avatar directory to collect all the name of the avatar
				File avatarDir = new File("resources/"+AVATAR_DIRECTORY);
				if (avatarDir.exists() && avatarDir.isDirectory()) {
					File[] avatarFiles = avatarDir
							.listFiles((dir, name) -> name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg"));
					if (avatarFiles != null && avatarFiles.length > 0) {
						for (File f : avatarFiles)
							if (f.isFile())
								addItem(f.getName());
					}
					else
						addItem(Profile.DEFAULT_AVATAR);
				}
			}
		};
		// Avatar image
		avatarImg = LoadImage.getImage(DEFAULT_AVATAR);
		// Amount of plays that the player did in this profile
		missions = FactoryComponents.createLabel("Missions = 0", DEFAULT_FONT_SIZE, FactoryComponents.YELLOW_CHARACTER);
		// Amount of wins and loses that the player did in this profile
		stats = FactoryComponents.createLabel("Accomplished/Failed = 0/0", DEFAULT_FONT_SIZE, FactoryComponents.YELLOW_CHARACTER);
		// Amount of level + Sum of the points in the profile / Amount of points to reach the next level
		level = FactoryComponents.createLabel("Level 0 = 0/0", DEFAULT_FONT_SIZE, FactoryComponents.YELLOW_CHARACTER, SwingConstants.LEFT);
		// Back button
		menu = FactoryComponents.createButton("< BACK", BUTTON_W, BUTTON_H);
		// Apply button to save the changes of the profile
		apply = FactoryComponents.createButton("APPLY CHANGES", BUTTON_W + 25, BUTTON_H);
		// Reset button to reset the profile
		reset = FactoryComponents.createButton("RESET", BUTTON_W, BUTTON_H);
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
				
				// Title (Profile)
				g.gridx = 0;
				g.gridy = 0;
				g.gridwidth = 3;
				g.weighty = 0.5;
				add(title, g);

				// Profile:
				g.gridwidth = 2;
				g.gridx = 0;
				g.gridy = 1;
				g.weightx = 1;
				g.weighty = 0.1;
				add(profile, g);
				
				// Profile Dropbox
				g.gridwidth = 1;
				g.gridx = 1;
				g.weightx = 0.1;
				add(profileChoose, g);
				
				// Nickname:
				g.gridwidth = 2;
				g.gridx = 0;
				g.gridy = 2;
				g.weightx = 1;
				add(nickname, g);

				// Nickname box
				g.gridwidth = 1;
				g.gridx = 1;
				g.weightx = 0.1;
				add(changeName, g);

				// Avatar:
				g.gridwidth = 2;
				g.gridx = 0;
				g.gridy = 3;
				g.weightx = 1;
				add(avatar, g);

				// Avatar Dropbox
				g.gridwidth = 1;
				g.gridx = 1;
				g.weightx = 0.1;
				avatarChoose.addActionListener(a -> {
					avatarImg = LoadImage.getImage(AVATAR_DIRECTORY + (String)avatarChoose.getSelectedItem());
					repaint();
				});
				add(avatarChoose, g);

				// Number of missions(plays)
				g.gridwidth = 3;
				g.gridx = 0;
				g.gridy = 4;
				add(missions, g);

				// Number of wins/lose
				g.gridx = 0;
				g.gridy = 5;
				add(stats, g);

				// Level
				g.gridx = 0;
				g.gridy = 6;
				add(level, g);

				// Back to menu button
				g.gridwidth = 1;
				g.gridx = 0;
				g.gridy = 7;
				g.weightx = 0.3;
				g.weighty = 0.5;
				menu.addActionListener(m -> {
					AudioManager.getInstance().playButtonSound();
					n.navigate(Navigator.ScreenStates.MENU);
				});
				add(menu, g);

				// Profile apply
				g.gridx = 1;
				apply.addActionListener(a -> AudioManager.getInstance().playButtonSound());
				add(apply, g);

				// Reset profile
				g.gridx = 2;
				reset.addActionListener(r -> AudioManager.getInstance().playButtonSound());
				add(reset, g);
				
				setBounds(MENU_X, MENU_Y, MENU_W, MENU_H);
			}
			
			/**
			 * Draw the background (filling with the default color background) + border of the profile
			 */
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				// Background
				g2.setColor(FactoryComponents.SCREEN_COLOR_BACKGROUND);
				g2.fillRect(0, 0, getWidth(), getHeight());
				// Avatar border and avatar img datas
				g2.setColor(FactoryComponents.YELLOW_CHARACTER);
				g2.setStroke(new BasicStroke(2));
				int xRect = profile.getX() - 50;
				int yRect = profile.getY() - 25;
				int imgX = changeName.getX() + changeName.getWidth() + 25;
				int imgY = profileChoose.getY();
				int imgW = 100;
				int imgH = 100;
				int wRect = imgX + imgW - xRect + 25;
				int hRect = level.getY() + level.getHeight() - yRect + 25;
				// Draw avatar border + avatar img
				g2.drawRect(xRect, yRect, wRect, hRect);
				g2.drawRect(imgX - 1, imgY - 1, imgW + 2, imgH + 2);
				g2.drawImage(avatarImg, imgX, imgY, imgW, imgH, null);
			}
		});

	}

	/**
	 * Updates the profile datas once it change profile
	 * 
	 * @param game     To read which index is the current profile
	 * @param profile  The current profile to get the information from
	 */
	public void updateProfile(Game game, Profile profile) {
		// Set the index of the profile
		profileChoose.setSelectedItem(game.getProfiles().indexOf(profile)+1);
		// Set the avatar
		avatarChoose.setSelectedItem(profile.getAvatar());
		avatarImg = LoadImage.getImage(AVATAR_DIRECTORY + profile.getAvatar());
		// Set the name
		changeName.setText(profile.getNickname());
		// Set the number of game the current profile played
		missions.setText("Missions = " + profile.getPlays());
		// Set the number of victory and loses
		stats.setText("Accomplished/Failed = " + profile.getWins() + "/" + profile.getLoses());
		// Set the current level gained by summed points
		level.setText("Level " + profile.getLevels() +" = "+profile.getPoints()+"/"+profile.getRequiredPoints());
		
		repaint();
	}

	/*
	 * Getters/Setters
	 */
	public JComboBox<Integer> getProfileChoose() {
		return profileChoose;
	}

	public JTextField getChangeName() {
		return changeName;
	}

	public JComboBox<String> getAvatarChoose() {
		return avatarChoose;
	}

	public JButton getApply() {
		return apply;
	}

	public JButton getMenu() {
		return menu;
	}

	public JButton getReset() {
		return reset;
	}
	
	public BufferedImage getAvatarImg() {
		return avatarImg;
	}
	
	public void setAvatarImg(BufferedImage avatarImg) {
		this.avatarImg = avatarImg;
	}

}
