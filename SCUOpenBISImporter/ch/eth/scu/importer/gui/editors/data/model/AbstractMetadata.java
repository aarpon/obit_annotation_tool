package ch.eth.scu.importer.gui.editors.data.model;

/**
 * Object that collects all relevant metadata from the Data Model and the
 * OpenBIS Model to complete the registration of an experiment in OpenBIS.
 * It is responsible to deliver all openBIS attributes for whatever 
 * Processor model object that requires them!
 * 
 * @author Aaron Ponti
 *
 */
public abstract class AbstractMetadata {

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
	
	
}
