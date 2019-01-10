package ch.ethz.scu.obit.at.gui.editors.data.model;

import ch.ethz.scu.obit.at.gui.data.model.ExperimentNode;
import ch.ethz.scu.obit.at.gui.openbis.model.OpenBISProjectNode;

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
     * Experiment node (from the data viewer)
     */
    public ExperimentNode experimentNode;

    /**
     * openBIS project node (from the openBIS viewer)
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
     * Constructor
     * @param experimentNode experiment node (from the data viewer)
     * @param openBISProjectNode openBIS project node (from the openBIS viewer)
     */
    public AbstractMetadataMapper(ExperimentNode experimentNode,
            OpenBISProjectNode openBISProjectNode) {

        this.experimentNode = experimentNode;
        this.openBISProjectNode = openBISProjectNode;

    }
}
