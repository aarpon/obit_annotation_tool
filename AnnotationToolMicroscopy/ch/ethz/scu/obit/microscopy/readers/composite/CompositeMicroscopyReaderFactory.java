package ch.ethz.scu.obit.microscopy.readers.composite;

import java.io.File;

/**
 * The Composite Microscopy Reader factory returns the AbstractCompositeMicroscopyReader
 * that announces to be able to read and interpret the content of the input
 * folder. 
 * 
 * @author Aaron Ponti
 */

public class CompositeMicroscopyReaderFactory {

	/**
	 * Creates a composite microscope reader viewer depending on the
	 * answer of their canRead() method.
	 * @param folder Folder to be processed.
	 * @return a concrete implementation of an AbstractCompositeMicroscopyReader
	 */
	public static AbstractCompositeMicroscopyReader createReader(File folder) {

		// The first concrete reader to announce it can parse the folder is
		// accepted and returned.
		if (YouScopeReader.canRead(folder)) {
			return new YouScopeReader(folder);
		} else if (LeicaTIFFSeriesReader.canRead(folder)) {
			return new LeicaTIFFSeriesReader(folder);
		} else {
			return null;
		}
	}

}
