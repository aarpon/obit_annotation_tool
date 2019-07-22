package ch.ethz.scu.obit.at.gui.editors.data.model;

import ch.ethz.scu.obit.at.gui.viewers.data.model.ExperimentNode;
import ch.ethz.scu.obit.at.gui.viewers.openbis.model.OpenBISProjectNode;

/**
 * Object that collects all relevant metadata from the Data Model and the
 * OpenBIS Model to complete the registration of an experiment in OpenBIS.
 * It is responsible to deliver all openBIS attributes for whatever
 * Processor model object that requires them!
 *
 * @author Aaron Ponti
 *
 */
public abstract class AbstractMetadataMapper {

    /**
     * Experiment node
     */
    public ExperimentNode experimentNode;

    /**
     * openBIS project node
     */
    public OpenBISProjectNode openBISProjectNode;

    /**
     * Get the openBIS Experiment Identifier
     * @return the openBIS Experiment identifier
     */
    abstract public String getOpenBISExerimentIdentifier();

    /**
     * Get the openBIS Space Identifier
     * @return the openBIS space identifier
     */
    abstract public String getOpenBISSpaceIdentifier();

    /**
     * Return the stored ExperimentNode
     * @return The stored ExperimentNode.
     */
    public ExperimentNode getExperimentNode() {
        return this.experimentNode;
    }

    /**
     * Return the stored OpenBISProjectNode
     * @return The stored OpenBISProjectNode.
     */
    public OpenBISProjectNode getOpenBISProjectNode() {
        return this.openBISProjectNode;
    }
}
