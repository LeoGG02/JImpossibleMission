package view.util;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * Class used to load single or group of images.
 */
public class LoadImage {

	/**
	 * Simply gives the image from the file name
	 * 
	 * @param fileName        Name of the file to get the image
	 * 
	 * @return BufferedImage  Returns the image gotten from the filename
	 */
	public static BufferedImage getImage(String fileName) {
		BufferedImage img = null;
		try (InputStream in = LoadImage.class.getResourceAsStream("/"+fileName)) {
			img = ImageIO.read(in);
		}
		catch (FileNotFoundException | NullPointerException f) {
			System.err.println("Image not found");
			f.printStackTrace();
		}
		catch (Exception e) {
			System.err.println("Error during the loading of the image");
			e.printStackTrace();
		}
		return img;
	}

	/**
	 * Gives a matrix of images used for the animation
	 * 
	 * @param fileName            Simply the file name of the sheet that wants collect the sprites
	 * @param types               Used to determined the height of the matrix, separating the arrays into amount of types
	 * or different actions
	 * @param maxIndex            Used to determined the width of the matrix, as each action has set of sprites use 
	 * for the animation
	 * @param width               Used to separating each sprite from the sheets, using width,
	 * into their own element in the array
	 * @param height              Used to separating each sprite from the sheets, using height,
	 * into their own element in the array
	 * 
	 * @return BufferedImage[][]  Returns a matrix of images
	 */
	public static BufferedImage[][] loadGroupImage(String fileName, int types, int maxIndex, int width, int height) {
		BufferedImage img = getImage(fileName);
		BufferedImage[][] group = new BufferedImage[types][maxIndex];
		for (int i = 0; i < group.length; i++)
			for (int j = 0; j < group[i].length; j++)
				group[i][j] = img.getSubimage(width * j, height * i, width, height);
		return group;
	}

}
