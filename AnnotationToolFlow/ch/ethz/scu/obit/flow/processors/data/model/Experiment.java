package ch.ethz.scu.obit.flow.processors.data.model;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.ethz.scu.obit.processors.data.model.ExperimentDescriptor;

/**
 * Descriptor representing an experiment obtained from the FCS file.
 * @author Aaron Ponti
 */
public abstract class Experiment extends ExperimentDescriptor {

    // An Experiment can contain TRAYS that in turn contain SPECIMENs
    // which contain TUBEs, or directly SPECIMENs containing TUBEs.

    /**
     * ArrayList of Tray's
     */
    public Map<String, Tray> trays =
            new LinkedHashMap<String, Tray>();

    /**
     * ArrayList of Specimen's
     */
    public Map<String, Specimen> specimens =
            new LinkedHashMap<String, Specimen>();

    /**
     * Constructor
     * @param fullPath Full path to the experiment folder.
     * @param userRootDataPath Full path to current user folder.
     */
    public Experiment(File fullPath, File userRootDataPath) {

        super(fullPath, userRootDataPath);
        this.setName(fullPath.getName());

        // Set the attribute relative path. Since this will be
        // used by the openBIS dropboxes running on a Unix machine,
        // we make sure to use forward slashes for path separators
        // when we set it as an attribute.
        attributes.put("relativePath",
                this.relativePath.replace("\\", "/"));

    }

    /**
     * Alternative constructor
     * @param fullPath Full path of the experiment.
     * @param name Name of the experiment.
     * @param userRootDataPath Full path to current user folder.
     */
    public Experiment(File fullPath, String name, File userRootDataPath) {

        super(fullPath, userRootDataPath);
        this.setName(name);

        // Set the attribute relative path. Since this will be
        // used by the openBIS dropboxes running on a Unix machine,
        // we make sure to use forward slashes for path separators
        // when we set it as an attribute.
        attributes.put("relativePath",
                this.relativePath.replace("\\", "/"));

    }

    /**
     * Return a simplified class name to use in XML.
     * @return simplified class name.
     */
    @Override
    public String getType() {
        return "Experiment";
    }

    /**
     * Return the identifier prefix of the collection type.
     *
     * The Sorter and Analyzer Experiment classes must re-implement this method.
     *
     * @return One of {"FLOW_ANALYZERS_", "FLOW_SORTERS_"}
     */
    public abstract String getOpenBISCollectionIdentifierPrefix();

}
