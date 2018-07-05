package ch.ethz.scu.obit.flow.processors.data.model;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.scu.obit.processors.data.model.SampleDescriptor;

/**
 * Descriptor representing a tray obtained from the FCS file.
 * @author Aaron Ponti
 */
public class Tray extends SampleDescriptor {

	/**
	 *  Define the supported tray geometries as a static list
	 */
	public final List<String> supportedTrayGeometries =
			Arrays.asList("96_WELLS_8X12", "384_WELLS_16x24");

	/**
	 * ArrayList of Specimen's
	 */
	public Map<String, Specimen> specimens = 
			new LinkedHashMap<String, Specimen>();

	/**
	 * Geometry associated to the plate
	 */
	public String geometry;

	/**
	 * Constructor
	 * @param name name of the Tray.
	 */
	public Tray(String name) {

		this.setName(name);

		// Initialize geometry
		this.geometry = this.supportedTrayGeometries.get(0);
	}

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override		
	public String getType() {
		return "Tray";
	}

}
