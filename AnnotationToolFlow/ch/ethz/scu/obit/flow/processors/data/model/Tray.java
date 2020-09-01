package ch.ethz.scu.obit.flow.processors.data.model;

import java.util.LinkedHashMap;
import java.util.Map;

import ch.ethz.scu.obit.processors.data.model.SampleDescriptor;

/**
 * Descriptor representing a tray obtained from the FCS file.
 *
 * @author Aaron Ponti
 */
public class Tray extends SampleDescriptor {

    /**
     * ArrayList of Specimen's
     */
    public Map<String, Specimen> specimens = new LinkedHashMap<String, Specimen>();

    /**
     * Constructor
     *
     * @param name name of the Tray.
     */
    public Tray(String name) {

        this.setName(name);
    }

    /**
     * Return a simplified class name to use in XML.
     *
     * @return simplified class name.
     */
    @Override
    public String getType() {
        return "Tray";
    }

    /**
     * Return the geometry of the Tray.
     *
     * @return string containing the geometry of the Tray.
     */
    public String getGeometry() {
        if (this.attributes.containsKey("geometry")) {
            return this.attributes.get("geometry");
        } else {
            return "";
        }
    }

    /**
     * Set the geometry of the Tray.
     *
     * @param geometry string containing the geometry of the Tray.
     */
    public void setGeometry(String geometry) {
        this.attributes.put("geometry", geometry);
    }
}
