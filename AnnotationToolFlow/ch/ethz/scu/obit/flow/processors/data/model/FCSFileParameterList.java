package ch.ethz.scu.obit.flow.processors.data.model;

import java.util.Map;

import ch.ethz.scu.obit.processors.data.model.AbstractDescriptor;

/**
 * Descriptor representing all parameters associated to an FCS File.
 * An FCSFileParameterList is always a child of a FCS File.
 * @author Aaron Ponti
 */
public class FCSFileParameterList extends AbstractDescriptor {

	/**
	 * Constructor
	 * @param numEvents Number of events.
	 * @param numParameters Number of parameters.
	 * @param parameterAttrib List of Parameter attributes.
	 */
	public FCSFileParameterList(int numEvents, int numParameters, 
			Map<String, String> parameterAttrib) {

		// Set the parameter number and lists and the associated event 
		// number.
		attributes.put("numEvents", Integer.toString(numEvents));
		attributes.put("numParameters", Integer.toString(numParameters));
		attributes.putAll(parameterAttrib);
	}

	/**
	 * Return a String representation of the extracted FCS file.
	 * @return String representation of the FCS file.
	 */
	@Override
	public String toString() {
		return "Parameters";
	}

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override		
	public String getType() {
		return "FCSFileParamList";
	}

}
