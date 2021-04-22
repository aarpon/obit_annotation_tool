package ch.ethz.scu.obit.flow.processors.data.model.specializations;

import java.io.File;

import ch.ethz.scu.obit.flow.processors.data.model.AnalyzerExperiment;

public class BCCytoFLEXSExperiment extends AnalyzerExperiment {

    /**
     * Constructor
     *
     * @param fullPath         Full path to the experiment folder.
     * @param userRootDataPath Full path to current user folder.
     */
    public BCCytoFLEXSExperiment(File fullPath, File userRootDataPath) {

        super(fullPath, userRootDataPath);

        // Set the supported geometries
        this.supportedTrayGeometries.add("96_WELLS_8X12");
    }

    /**
     * Alternative constructor
     *
     * @param fullPath         Full path of the experiment.
     * @param name             Name of the experiment.
     * @param userRootDataPath Full path to current user folder.
     */
    public BCCytoFLEXSExperiment(File fullPath, String name,
            File userRootDataPath) {

        super(fullPath, name, userRootDataPath);

        // Set the supported geometries
        this.supportedTrayGeometries.add("96_WELLS_8X12");
    }
}
