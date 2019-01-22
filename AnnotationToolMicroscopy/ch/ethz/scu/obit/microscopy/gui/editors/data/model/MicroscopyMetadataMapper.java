package ch.ethz.scu.obit.microscopy.gui.editors.data.model;

import ch.ethz.scu.obit.at.gui.editors.data.model.AbstractMetadataMapper;
import ch.ethz.scu.obit.at.gui.viewers.data.model.ExperimentNode;
import ch.ethz.scu.obit.at.gui.viewers.openbis.model.OpenBISProjectNode;
import ch.ethz.scu.obit.microscopy.processors.data.MicroscopyProcessor.Experiment;

/**
 * Collects all relevant metadata to allow the registration of an
 * experiment from a generic light microscope (bioformats-compatible file format).
 * @author Aaron Ponti
 *
 */
public final class MicroscopyMetadataMapper extends AbstractMetadataMapper {

    /**
     * Experiment node
     */
    public ExperimentNode experimentNode;

    /**
     * openBIS project node
     */
    public OpenBISProjectNode openBISProjectNode;

    /**
     * Constructor
     * @param experimentNode experiment node (from the data viewer)
     * @param openBISProjectNode openBIS project node (from the openBIS viewer)
     */
    public MicroscopyMetadataMapper(ExperimentNode experimentNode,
            OpenBISProjectNode openBISProjectNode) {

        // Assign folder and openBIS project nodes
        this.experimentNode = experimentNode;
        this.openBISProjectNode = openBISProjectNode;

    }

    /**
     * Get the experiment descriptor
     * @return the experiment descriptor
     */
    public Experiment getExperiment() {
        return (Experiment)
                this.experimentNode.getUserObject();
    }

    /**
     * Get the experiment name
     * @return the experiment name
     */
    public String getExperimentName() {
        return this.experimentNode.toString();
    }

    /**
     * Get the project name
     * @return the experiment name
     */
    public String getProjectName() {
        return openBISProjectNode.toString();
    }

    /**
     * Get the openBIS Experiment Identifier
     * @return the openBIS Experiment identifier
     */
    public String getOpenBISCollectionIdentifier() {
        String openBISProjectID = openBISProjectNode.getIdentifier();
        return (openBISProjectID + "/MICROSCOPY_EXPERIMENTS_COLLECTION");
    }

    /**
     * Get the openBIS Experiment Identifier
     * @return the openBIS Experiment identifier
     */
    @Override
    public String getOpenBISExerimentIdentifier() {
        Experiment e = (Experiment) experimentNode.getUserObject();
        return (getOpenBISCollectionIdentifier() + "/" + e.getCode()).toUpperCase();
    }

    /**
     * Get the openBIS Space Identifier
     * @return the openBIS space identifier
     */
    @Override
    public String getOpenBISSpaceIdentifier() {
        String openBISProjectID = openBISProjectNode.getIdentifier();

        // Find the first occurrence of / which is NOT at the beginning
        int indx = openBISProjectID.indexOf("/", 1);

        if (indx == -1) {
            // This should not happen, sice the identifier came
            // from openBIS in the first place.
            System.err.println(
                    "Malformed openBIS project identifier!");
            return "INVALID";
        }

        return (openBISProjectID.substring(0, indx).toUpperCase());
    }

}
