package model.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Used to save and load files in the data folder
 */
public class DataManager {
	
	// Default folder for datas
	private static final String DATA_DIRECTORY = "data/";

	/**
	 * Static class to save the file
	 * 
	 * @param obj          Gets the object to be saved as a file
	 * @param objFileName  Name of the file
	 */
	public static void save(Object obj, String objFileName) {
		File dataDir = new File(DATA_DIRECTORY);
		String fileName = DATA_DIRECTORY + objFileName;
		// If the directory doesn't exists it creates it
		if (!dataDir.exists())
			dataDir.mkdirs();
		
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(fileName)))) {
			System.out.println("File '" + objFileName + "' saved.");
			oos.writeObject(obj);
		}
		catch (Exception e) {
			System.err.println("Error saving the file.");
			e.printStackTrace();
		}
	}

	/**
	 * Static class to load the file
	 * 
	 * @param objFileName  Name of the file
	 * 
	 * @return Object      Returns the file as an Object so later on can be down-casted
	 */
	public static Object load(String objFileName) {
		String fileName = DATA_DIRECTORY + objFileName;
		File objFile = new File(fileName);
		// Returns null if the directory isn't found
		if (!objFile.exists()) {
			System.err.println("File not found.");
			return null;
		}

		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(objFile))) {
			System.out.println("File '" + fileName + "' loaded.");
			return ois.readObject();
		}
		catch (Exception e) {
			System.err.println("Error loading the file.");
			return null;
		}
	}

}
