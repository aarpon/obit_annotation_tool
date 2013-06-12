package ch.eth.scu.importer.processors.model;

import java.util.Hashtable;
import java.util.Map;

/**
 * Abstract class that represents an entity to be mapped to openBIS.
 * 
 * Derived classes of AbstractDescriptor will represent Experiments, 
 * Specimens, Trays (Plates), Tubes, Images, SubImages that will be 
 * wrapped into the tree viewers and will allow to map the entities to
 * their final location in openBIS.
 *  
 * @author Aaron Ponti
 */
abstract public class AbstractDescriptor {

	/**
	 * Name of the entity.
	 * 
	 * This is displayed in the viewers and in openBIS.
	 */
	protected String name = "";

	/**
	 * String-string map of data attribute key:value pair.
	 * 
	 * These will be uploaded to openBIS as associated information.
	 */
	protected Map<String, String> attributes =
			new Hashtable<String, String>();

	/**
	 * String-string map of openBIS attribute key:value pair.
	 * 
	 * These will be uploaded to openBIS as associated information.
	 */
	protected Map<String, String> openBISAttributes = 
			new Hashtable<String, String>();

	/**
	 * String-string map of user attribute key:value pair.
	 * 
	 * These will be uploaded to openBIS as associated information.
	 */
	protected Map<String, String> userAttributes =
			new Hashtable<String, String>();
	
	/**
	 * Return a simplified class name to be used in the tree 
	 * viewers (and XML).
	 * @return simplified class name.
	 */
	abstract public String getType();

	/**
	 * Return the name of the entity.
	 * @return the name of the entity.
	 */
	public String getName() { return name; }

	/**
	 * Set the name of the entity.
	 */
	public void setName(String name) { this.name = name; }

	/**
	 * Return a string representation of the entity.
	 * @return string representation of the entity.
	 */
	public String toString() { return name; }

	/**
	 * Return the entity attributes.
	 * @return a string-string map with attribute name:value pairs.
	 */
	public Map<String, String> getAttributes() { return attributes; }

	/**
	 * Set the entity attributes.
	 * @param attributes String-string map with attribute name:value pairs.
	 */
	public void setAttributes(Map<String, String> attributes) { 
		this.attributes = attributes; 
	}

	/**
	 * Return the entity openBIS attributes.
	 * @return a string-string map with openBIS attribute name:value pairs.
	 */
	public Map<String, String> getOpenBISAttributes() { 
		return openBISAttributes; 
	}

	/**
	 * Set the entity openBIS attributes.
	 * @param openBISAttributes String-string map with openBIS attribute 
	 * name : value pairs.
	 */
	public void setOpenBISAttributes(Map<String, String> openBISAttributes) { 
		this.openBISAttributes = openBISAttributes; 
	}

	/**
	 * Return the entity user attributes.
	 * @return a string-string map with user attribute name:value pairs.
	 */
	public Map<String, String> getUserAttributes() { 
		return userAttributes; 
	}

	/**
	 * Set the entity user attributes.
	 * @param userAttributes String-string map with openBIS attribute 
	 * name : value pairs.
	 */
	public void setUserAttributes(Map<String, String> userAttributes) { 
		this.userAttributes = userAttributes; 
	}
	
	/**
	 * Return the entity attributes in a comma-separated list.
	 * @return a comma-separated list of attribute name : value pairs.
	 */
	public String attributesToString() {
		String str = attributes.toString();
		return str.substring(1, str.length() - 1);
	}

	/**
	 * Return the entity openBIS attributes in a comma-separated list.
	 * @return a comma-separated list of attribute name : value pairs.
	 */		
	public String openBISAttributesToString() {
		String str = openBISAttributes.toString();
		return str.substring(1, str.length() - 1);
	}

	/**
	 * Return the user attributes in a comma-separated list.
	 * @return a comma-separated list of attribute name : value pairs.
	 */		
	public String userAttributesToString() {
		String str = userAttributes.toString();
		return str.substring(1, str.length() - 1);
	}
}
