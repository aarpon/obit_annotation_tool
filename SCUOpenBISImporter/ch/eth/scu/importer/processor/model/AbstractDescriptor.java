package ch.eth.scu.importer.processor.model;

import java.util.Hashtable;
import java.util.Map;

/**
 * Abstract class that represents an entity to be mapped to openBIS.
 * 
 * Derived classes of AbstractDescriptor will represent Experiments, Specimens,
 * Trays (Plates), Tubes, Images, SubImages that will be wrapped into the tree
 * viewers and will allow to map the entities to their final location in 
 * openBIS.
 *  
 * Examples are Experiment, Specimen, Tray, Tube for the BDFACSDIVAXMLProcessor.
 * @author Aaron Ponti
 */
abstract public class AbstractDescriptor {

	/**
	 * Name of the entity.
	 * 
	 * This is displayed e.g. in the tree viewers.
	 */
	protected String name = "";

	/**
	 * String-string map of attribute key:value pair.
	 * 
	 * These will be uploaded to openBIS as associated information.
	 */
	protected Map<String, String> attributes = new Hashtable<String, String>();

	/**
	 * String-string map of openBIS attribute key:value pair.
	 * 
	 * These will be uploaded to openBIS as associated information.
	 */
	protected Map<String, String> openBISAttributes = 
			new Hashtable<String, String>();

	/**
	 * Return a simplified class name to be used in the tree viewers (and XML).
	 * @return simplified class name.
	 */
	abstract public String getType();

	/**
	 * Return the name of the entity.
	 * @return the name of the entity.
	 */
	public String getName() { return name; }

	/**
	 * Sets the name of the entity.
	 */
	public void setName(String name) { this.name = name; }

	/**
	 * Return the openBIS identifier of the entity. This is used to get or
	 * create the openBIS entity associated to this Descriptor.
	 */
	public String getOpenBISIdentifier() {
		return "";
	}

	/**
	 * Return the openBIS code of the entity (based on its name).
	 */
	public String getOpenBISCode() { 
		return name.replaceAll(" ", "_").toUpperCase();
	}

	/**
	 * Return a string representation of the entity.
	 * @return string representation of the entity.
	 */
	public String toString() { return name; }

	/**
	 * Return the entity attributes.
	 * @return a string-string map with attribute name : value pairs.
	 */
	public Map<String, String> getAttributes() { return attributes; }

	/**
	 * Sets the entity attributes.
	 */
	public void setAttributes(Map<String, String> attributes) { 
		this.attributes = attributes; 
	}

	/**
	 * Return the entity openBIS attributes.
	 * @return a string-string map with attribute name : value pairs.
	 */
	public Map<String, String> getOpenBISAttributes() { 
		return openBISAttributes; 
	}

	/**
	 * Sets the entity openBIS attributes.
	 */
	public void setOpenBISAttributes(Map<String, String> openBISAttributes) { 
		this.openBISAttributes = openBISAttributes; 
	}

	/**
	 * Returns the entity attributes in a comma-separated list.
	 * @return a comma-separated list of attribute name : value pairs.
	 */
	public String attributesToString() {
		String str = attributes.toString();
		return str.substring(1, str.length() - 1);
	}

	/**
	 * Returns the entity openBIS attributes in a comma-separated list.
	 * @return a comma-separated list of attribute name : value pairs.
	 */		
	public String openBISAttributesToString() {
		String str = openBISAttributes.toString();
		return str.substring(1, str.length() - 1);
	}

}
