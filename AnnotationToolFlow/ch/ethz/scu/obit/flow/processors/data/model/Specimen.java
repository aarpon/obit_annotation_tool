package ch.ethz.scu.obit.flow.processors.data.model;

import java.util.LinkedHashMap;
import java.util.Map;

import ch.ethz.scu.obit.processors.data.model.SampleDescriptor;

/**
 * Descriptor representing a specimen obtained from the FCS file.
 * A Specimen can be a child of a Tray or directly of an Experiment.
 * @author Aaron Ponti
 */
public class Specimen extends SampleDescriptor {

	/* Public instance variables */

	/**
	 * ArrayList of Tube's
	 */			
	public Map<String, Tube> tubes =
			new LinkedHashMap<String, Tube>();

	/**
	 * Constructor.
	 * @param name Name of the specimen
	 */
	public Specimen(String name) {

		this.setName(name);

	}

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override		
	public String getType() {
		return "Specimen";
	}

}
