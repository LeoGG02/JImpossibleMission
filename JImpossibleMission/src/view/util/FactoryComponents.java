package view.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Factory used for the creation/distribution of components for the panels.
 */
public class FactoryComponents {

	// Colors used for text and background
	public static final Color DEFAULT_COLOR_BACKGROUND = new Color(132, 197, 204);
	public static final Color SCREEN_COLOR_BACKGROUND = new Color (89, 60, 7);
	public static final Color YELLOW_CHARACTER = new Color(227, 231, 110);
	public static final Color GREEN_CHARACTER = new Color(114, 177, 45);
	// Default font used on everything
	private static Font defaultFont;
	
	/**
	 * Loads the default Font and in case doesn't load it will be using an alternative
	 */
	private static void loadFont() {
		try (InputStream in = FactoryComponents.class.getResourceAsStream("/C64_Pro-STYLE.ttf")) {
			defaultFont = Font.createFont(Font.TRUETYPE_FONT, in);
		}
		catch (Exception e) {
			System.out.println("Default font not loaded. Go with the replacement.");
			defaultFont = new Font("System", Font.BOLD, 25);
		}
	}
	
	/**
	 * Gives the default Font with customized size, if is the first time loading the Font will call loadFont
	 * 
	 * @param size   Size of the text
	 * 
	 * @return Font  Give the font with adjusted size
	 */
	public static Font getFont(int size) {
		if (defaultFont == null)
			loadFont();
		return defaultFont.deriveFont(Font.PLAIN, size);
	}
	
	/**
	 * Creation of a JLabel having as input title (Text of the Label), size and color.
	 * 
	 * @param title  Text show to the label
	 * @param size   Size of the text
	 * @param color  The color of the text
	 * 
	 * @return       New customized label
	 */
	public static JLabel createLabel(String title, int size, Color color) {
		return new JLabel(title) {
			/*
			 * Default Serial UID
			 */
			private static final long serialVersionUID = 1L;

			{
				setFont(FactoryComponents.getFont(size));
				setForeground(color);
				setHorizontalAlignment(SwingConstants.CENTER);
			}
		};
	}
	
	/**
	 * Creation of a JLabel having as input title (Text of the Label), size, color and an additional horizontal alignment.
	 * 
	 * @param title                Text show to the label
	 * @param size                 Size of the text
	 * @param color                The color of the text
	 * @param horizontalAlignment  Used to place on the left or on the right side
	 * 
	 * @return       New customized label
	 */
	public static JLabel createLabel(String title, int size, Color color, int horizontalAlignment) {
		return new JLabel(title) {
			/*
			 * Default Serial UID
			 */
			private static final long serialVersionUID = 1L;

			{
				setFont(FactoryComponents.getFont(size));
				setForeground(color);
				setHorizontalAlignment(horizontalAlignment);
			}
		};
	} 
	
	/**
	 * Creation of a interactive button
	 * 
	 * @param title     Is the text shown on the button
	 * @param width     Width of the button
	 * @param height    Height of the button
	 * @return JButton  New customized interactive button and preferable size
	 */
	public static JButton createButton(String title, int width, int height) {
		return new InteractiveButton(title, width, height);
	}
	
	/**
	 * Inner class that transform a simple JButton into a more visually interactive one
	 */
	private static class InteractiveButton extends JButton {
		
		/**
		 * Default Serial UID
		 */
		private static final long serialVersionUID = 1L;
		
		//State of the button
		private boolean hover = false;
		private boolean pressed = false;
		
		public InteractiveButton(String title, int width, int height) {
			super(title);
			setPreferredSize(new Dimension(width, height));
			initMouseListener();
		}
	
		/**
		 * Basically tracks if the mouse actions if is hovering on top or pressing the button,
		 * depending the action it reacts by changing the look of it
		 */
		private void initMouseListener() {
			
			addMouseListener(new MouseAdapter() {
				
				@Override
				public void mouseEntered(MouseEvent e) {
					hover = true;
					repaint();
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
					hover = false;
					repaint();
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					pressed = true;
					repaint();
				}
				
				@Override
				public void mouseReleased(MouseEvent e) {
					pressed = false;
					repaint();
				}
				
			});
		}

		/**
		 * Paints the button, changing depending of it's state
		 */
		@Override
		protected void paintComponent(Graphics g) {
			// Width and height of the button
			int w = getWidth();
			int h = getHeight();
			Graphics2D g2 = (Graphics2D)g;
			// Button Background
			g2.setColor(SCREEN_COLOR_BACKGROUND);
			g2.setFont(FactoryComponents.getFont(13));
			g2.fillRect(0, 0, w, h);
			// Button text and draws it only if isn't blank
			String txt = getText();
			if (!txt.isEmpty()) {
				Rectangle2D r = g2.getFontMetrics().getStringBounds(txt, g);
				g2.setColor(YELLOW_CHARACTER);
				g2.drawString(txt, (int)(w - r.getWidth())/2, (int)(h + r.getHeight())/2);
			}
			// Button border
			if (isEnabled()) {
				if (pressed)
					g2.setStroke(new BasicStroke(25));
				else if (hover)
					g2.setStroke(new BasicStroke(10));
				else
					g2.setStroke(new BasicStroke(1));
			}
			g2.drawRect(0, 0, w-1, h-1);
			g2.dispose();
		}
	}
	
}
