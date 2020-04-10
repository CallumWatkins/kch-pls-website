package main.java.com.projectBackEnd.Services.Image;

import java.io.File;

/**
 * DirectoryHolder is responsible for generating and providing user-specific directory information to allow
 * for image-access operations which require dynamic accessing of specific directories
 */
public class DirectoryHolder {


	private static String dir = System.getProperty("user.dir")+"/src/main/resources/images/";

	private static DirectoryHolder holder;

	/**
	 * Private constructor implementing singleton design pattern
	 */
	private DirectoryHolder(){
		holder = this;
	}

	/**
	 * Retrieve the directory holder
	 * @return The directory holder
	 */
	public static DirectoryHolder getDirectoryHolder() {
		if (holder == null) return new DirectoryHolder();
		else return holder;
	}

	/**
	 * Set a new value to the directory attribute, effectively changing the access location
	 * If no directory exists, one is created.
	 * @param newDir The new directory to be set
	 */
	public void setDir(String newDir) {

		File locationOfImageStorage = new File(newDir);
		if (!locationOfImageStorage.exists()) {
			if (locationOfImageStorage.mkdir()) System.out.println("Directory is created!");
			else System.out.println("Failed to create directory!");
		}
		this.dir = newDir;

	}

	/**
	 * Set the directory back to its default user-specific setting
	 */
	public void setDefaultDir(){dir = System.getProperty("user.dir")+"/src/main/resources/images/";}


	/**
	 * Retrieve the directory attribute
	 * @return The directory
	 */
	static String getDir() {
		return dir;
	}
}