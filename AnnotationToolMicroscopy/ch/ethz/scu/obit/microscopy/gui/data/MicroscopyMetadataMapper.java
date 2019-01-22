package ch.ethz.scu.obit.microscopy.gui.data;

import ch.ethz.scu.obit.at.gui.data.AbstractMetadataMapper;
import ch.ethz.scu.obit.at.gui.data.model.ExperimentNode;
import ch.ethz.scu.obit.microscopy.processors.data.MicroscopyProcessor.Experiment;
import ch.ethz.scu.obit.processors.openbis.OpenBISProcessor.ProjectInfo;

/**
 * Collects all relevant metadata to allow the registration of an
 * experiment from a generic light microscope (bioformats-compatible file format).
 * @author Aaron Ponti
 *
 */
public final class MicroscopyMetadataMapper extends AbstractMetadataMapper {

    /**
     * Constructor
     * @param experimentNode experiment node (from the data viewer)
     * @param openBISProjectNode openBIS project node (from the openBIS viewer)
     */
    public MicroscopyMetadataMapper(ExperimentNode experimentNode,
            ProjectInfo projectInfo) {

        // Call the base constructor
        super(experimentNode, projectInfo);
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
        if (projectInfo == null) {
            return "";
        }
        return projectInfo.projectCode;
    }

    /**
     * Get the openBIS Experiment Identifier
     * @return the openBIS Experiment identifier
     */
    @Override
    public String getOpenBISExerimentIdentifier() {
        if (projectInfo == null) {
            return "";
        }
        String openBISProjectID = projectInfo.projectIdentifier;
        Experiment e = (Experiment) experimentNode.getUserObject();
        return (openBISProjectID + "/" + e.getCode()).toUpperCase();
    }

    /**
     * Get the openBIS Space Identifier
     * @return the openBIS space identifier
     */
    @Override
    public String getOpenBISSpaceIdentifier() {
        if (projectInfo == null) {
            return "";
        }
        String openBISProjectID = projectInfo.projectIdentifier;

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
