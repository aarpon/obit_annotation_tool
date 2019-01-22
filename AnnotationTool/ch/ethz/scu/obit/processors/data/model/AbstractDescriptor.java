package ch.ethz.scu.obit.processors.data.model;

import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Pattern;

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

    // Used to filter the name of the Descriptor
    static final Pattern p = Pattern.compile("[^a-zA-Z0-9\\-\\_]");

    /**
     * Name of the entity.
     *
     * This is displayed in the viewers and in openBIS.
     */
    private String name = "";

    /**
     * Code of the entity.
     *
     * This is a filtered version of the name that can be used as an
     * openBIS code.
     *
     */
    private String code = "";


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
    public String getCode() { return code; }

    /**
     * Return the name of the entity.
     * @return the name of the entity.
     */
    public String getName() { return name; }

    /**
     * Set the name of the descriptor.
     * @param name Name of the Descriptor.
     */
    public void setName(String name) {
        this.name = name;
        setCode(name);
    }

    /**
     * Return a string representation of the entity.
     * @return string representation of the entity.
     */
    @Override
    public String toString() { return name; }

    /**
     * Return the entity attributes.
     * @return a string-string map with attribute name:value pairs.
     */
    public Map<String, String> getAttributes() { return attributes; }

    /**
     * Appends the entity attributes to the existing ones.
     * @param attributes String-string map with attribute name:value pairs.
     */
    public void addAttributes(Map<String, String> attributes) {
        this.attributes.putAll(attributes);
    }

    /**
     * Return the entity openBIS attributes.
     * @return a string-string map with openBIS attribute name:value pairs.
     */
    public Map<String, String> getOpenBISAttributes() {
        return openBISAttributes;
    }

    /**
     * Appends the entity openBIS attributes to the existing ones.
     * @param openBISAttributes String-string map with openBIS attribute
     * name : value pairs.
     */
    public void addOpenBISAttributes(Map<String, String> openBISAttributes) {
        this.openBISAttributes.putAll(openBISAttributes);
    }

    /**
     * Return the entity user attributes.
     * @return a string-string map with user attribute name:value pairs.
     */
    public Map<String, String> getUserAttributes() {
        return userAttributes;
    }

    /**
     * Appends the entity user attributes to the existing ones.
     * @param userAttributes String-string map with openBIS attribute
     * name : value pairs.
     */
    public void addUserAttributes(Map<String, String> userAttributes) {
        this.userAttributes.putAll(userAttributes);
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

    /**
     * Sets the code of the descriptor from the descriptor name.
     * Since the name is mapped to an openBIS entity and cannot contain
     * characters in the set: [^a-zA-Z0-9-_], this method cleans the
     * passed string.
     * @param name Name of the Descriptor.
     */
    private void setCode(String name) {
        String code = p.matcher(name).replaceAll("_");
        this.code = code;
    }
}
