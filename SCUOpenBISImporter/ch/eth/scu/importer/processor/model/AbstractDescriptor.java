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
	
	/* Protected member variables */
	
	/**
	 * Name of the entity.
	 * 
	 * This is displayed e.g. in the tree viewers.
	 */
	protected String name = "";
	
	/**
	 * Output name of the entity. By default, this is the same as the name. 
	 * 
	 * For top-level descriptors, it returns the output file name to be used
	 * to save the data model to XML for the openBIS dropbox scripts.
	 */
	protected String outputName = "";
	
	/**
	 * String-string map of attribute key:value pair.
	 * 
	 * These will be uploaded to openBIS as associated information.
	 */
	protected Map<String, String> attributes = new Hashtable<String, String>();
	
	/**
	 * openBIS identifier associated to the entity.
	 */
	protected String openBISIdentifier = "";
	
	/**
	 * openBIS code associated to the entity.
	 */
	protected String openBISCode = "";
	
	/**
	 * If true, the openBIS identifiers and code must be set.
	 */
	protected boolean mustBeProvided = false;

	/**
	 * Return a simplified class name to be used by the tree viewers.
	 * @return simplified class name.
	 */
	abstract public String getType();
		
	/**
	 * Return the name of the entity.
	 * @return the name of the entity.
	 */
	public String getName() { return name; }

	/**
	 * Return the output name of the entity. By default, it returns the
	 * name of the entity. Override to customize.
	 * 
	 * @return the output name of the entity.
	 */
	public String getOutputName() { return name; }
	
	/**
	 * Sets the name of the entity.
	 */
	public void setName(String name) { this.name = name; }
	
	/**
	 * Return the openBIS identifier of the entity.
	 */
	public String getOpenBISIdentifier() { return openBISIdentifier; }

	/**
	 * Sets the openBIS identifier of the entity.
	 */
	public void setOpenBISIdentifier(String openBISIdentifier) { 
		this.openBISIdentifier = openBISIdentifier; 
	}
	
	/**
	 * Return the openBIS code of the entity.
	 */
	public String getOpenBISICode() { return openBISCode; }

	/**
	 * Sets the openBIS code of the entity.
	 */
	public void setOpenBISCode(String openBISCode) { 
		this.openBISCode = openBISCode; 
	}
	
	/**
	 * Return true if both openBIS code and identifier must be provided
	 * (because they are needed to map to openBIS), false otherwise
	 */
	public boolean mustBeProvided() { return mustBeProvided; }
	
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
	 * Returns the entity attributes in a comma-separated list.
	 * @return a comma-separated list of attribute name : value pairs.
	 */		
	public String attributesToString() {
		String str = attributes.toString();
		return str.substring(1, str.length() - 1);
	}
	
}
