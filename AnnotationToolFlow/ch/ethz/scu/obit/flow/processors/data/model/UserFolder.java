package ch.ethz.scu.obit.flow.processors.data.model;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.ethz.scu.obit.processors.data.model.RootDescriptor;

/**
 * Descriptor that represents a the user top folder. 
 * @author Aaron Ponti
 */
public class UserFolder extends RootDescriptor {

	/**
	 * Hash map of experiments.
	 */ 
	public Map<String, Experiment> experiments = 
			new LinkedHashMap<String, Experiment>();

	/**
	 * Constructor
	 * @param fullFolder Full path to the global users folder path.
	 * @param userRootDataPath Full path to current user folder.
	 */
	public UserFolder(File fullFolder, File userRootDataPath) {

		// Invoke parent constructor
		super(fullFolder, userRootDataPath);

		// Set the descriptor name
		this.setName(fullFolder.getName());

	}

	@Override
	public String getType() {
		return "Folder";
	}

}
