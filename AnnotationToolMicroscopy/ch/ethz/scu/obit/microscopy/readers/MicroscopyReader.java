/**
 *
 */
package ch.ethz.scu.obit.microscopy.readers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.scu.obit.readers.AbstractReader;

/**
 * A generic bio-formats based file processor that should manage to parse
 * metadata information from any microscopy file format supported by
 * LOCI's bio-formats library.
 *
 * @author Aaron Ponti
 *
 */
public class MicroscopyReader extends AbstractReader {

    /* Protected instance variables */
    protected BioFormatsWrapper bioFormatsWrapper = null;
    protected File filename;

    protected boolean isFileScanned = false;

    /**
     * @param filename File name with full path.
     */
    public MicroscopyReader(File filename) {

        // Store the filename
        this.filename = filename;

        // Initialize the BioFormats reader
        this.bioFormatsWrapper = new BioFormatsWrapper(filename);

    }

    @Override
    public boolean parse() {

        if (bioFormatsWrapper == null) {
            return false;
        }

        // Parse the file
        return bioFormatsWrapper.parse();
    }

    /**
     * String-string map of data attribute key:value pair.
     *
     * These will be uploaded to openBIS as associated information.
     * @return String-string map of attributes.
     */
    public Map<String, HashMap<String, String>> getAttributes() {

        // If we have already scanned the file we just return the attributes
        if (bioFormatsWrapper.isScanned() == true) {
            return bioFormatsWrapper.getAttributes();
        }

        // Otherwise, we first scan and then return them
        bioFormatsWrapper.parse();

        // Return the attributes (if the parsing failed for whatever reason,
        // the attribute map will be automatically cleared).
        return bioFormatsWrapper.getAttributes();
    }

    /**
     * Close file.
     * The client is responsible to call close to release the file handle!
     * @return true if the file could be closed, false otherwise.
     */
    public boolean close() {

        // Ask the BioFormatsWrapper to close the file
        return bioFormatsWrapper.close();
    }


    /**
     * Return true if the file was scanned already, false otherwise.
     * @return true if the file was scanned already, false otherwise.
     */
    public boolean isScanned() {
        return isFileScanned;
    }



    /**
     * Return the file name of associated to the MicroscopyReader.
     * @return String containing the file name associated to the MicroscopyReader.
     */
    @Override
    public String toString() {
        return filename.getName();
    }

    /**
     * Return a simplified class name to use in XML.
     * @return simplified class name.
     */
    @Override
    public String getType() {
        return "microscopy";
    }


    /**
     * Return information regarding the file format.
     * @return descriptive String for the Reader.
     */
    @Override
    public String info() {
        return "Bioformats-compatible microscopy file format.";
    }

    /**
     * Return the number of series in the file/dataset.
     * @return the number of series.
     */
    public int getNumberOfSeries() {
        return bioFormatsWrapper.getNumberOfSeries();
    }

}
