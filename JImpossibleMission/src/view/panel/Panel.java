package view.panel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import view.Navigator;
import view.util.LoadImage;

/**
 * Main class used for the panels
 */
public abstract class Panel extends JPanel {

	/**
	 * Serial UID Generated
	 */
	private static final long serialVersionUID = 1L;
	
	// Default resolution
	public static final int GAME_WIDTH = 960;
	public static final int GAME_HEIGHT = 600;
	
	// Default panel datas for JPanel with GridBagLayout if is using the menu background
	protected static final int MENU_X = 66;
	protected static final int MENU_Y = 72;
	protected static final int MENU_W = 828;
	protected static final int MENU_H = 456;
	
	protected Navigator n;
	private BufferedImage background;

	/**
	 * Constructors in case it doesn't have a LayoutManager as input
	 * 
	 * @param n  Navigator used to give the command to change panel
	 */
	public Panel(Navigator n) {
		this.n = n;
		setBackground();
		setPanelSize();
	}

	/**
	 * Constructors in case it does have a LayoutManager as input
	 * 
	 * @param layout  LayoutManager that used to set a certain layout
	 */
	public Panel(Navigator n, LayoutManager layout) {
		super(layout);
		this.n = n;
		setBackground();
		setPanelSize();
	}

	/**
	 * Set Panel resolution
	 */
	private void setPanelSize() {
		Dimension size = new Dimension(GAME_WIDTH, GAME_HEIGHT);
		setPreferredSize(size);
	}

	/**
	 * Set the background image
	 */
	private void setBackground() {
		background = LoadImage.getImage("menu.png");
	}

	/**
	 * Paint the background
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(background, 0, 0, GAME_WIDTH, GAME_HEIGHT, null);
	}

	/**
	 * Abstract methods that used to initialized components
	 */
	protected abstract void initComponents();

	/**
	 * Abstract methods that used to add components
	 */
	protected abstract void addComponents();
	
}
