package ch.ethz.scu.obit.flow.processors.data.model;

import java.io.File;
import java.io.IOException;

import ch.ethz.scu.obit.processors.data.model.SampleDescriptor;

/**
 * Descriptor representing a tube obtained from the FCS file.
 * A Tube is always a child of a Specimen.
 *
 * @author Aaron Ponti
 */
public class Tube extends SampleDescriptor {

    /**
     * An FCS file processor.
     */
    public FCSFile fcsFile;

    /**
     * Constructor.
     * @param name Name of the Tube.
     * @param fcsFullFileName Full file name of the FCS file associated
     * with the Tube.
     * @param userRootFolder Full path to the root of the user folder.
     * @throws IOException If parsing the FCS file failed.
     */
    public Tube(String name, File fcsFullFileName, File userRootFolder)
            throws IOException {

        this.setName(name);

        // Associate the FCS file to the Tube
        fcsFile = new FCSFile(fcsFullFileName, userRootFolder);

    }

    /**
     * Return a String representation of the extracted Tube node.
     * @return String representation of the Tube node.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Return a simplified class name to use in XML.
     * @return simplified class name.
     */
    @Override
    public String getType() {
        return "Tube";
    }

}
