package ch.ethz.scu.obit.flow.processors.data;

/**
 * The Composite Microscopy Reader factory returns the AbstractCompositeMicroscopyReader
 * that announces to be able to read and interpret the content of the input
 * folder. 
 * 
 * @author Aaron Ponti
 */
public class FlowProcessorFactory {

	/**
	 * Creates a composite microscope reader viewer depending on the
	 * answer of their canRead() method.
	 * @param folder Folder to be processed.
	 * @return a concrete implementation of an AbstractCompositeMicroscopyReader
	 */
	public static AbstractFlowProcessor createProcessor(String folder) {

		return new BDLSRFortessaFlowProcessor(folder);

	}

}
