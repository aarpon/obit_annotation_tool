package ch.ethz.scu.obit.flow.processors.data.model.specializations;

import java.io.File;

import ch.ethz.scu.obit.flow.processors.data.model.AnalyzerExperiment;

public class BDLSRFortessaExperiment extends AnalyzerExperiment {

    /**
     * Constructor
     *
     * @param fullPath         Full path to the experiment folder.
     * @param userRootDataPath Full path to current user folder.
     */
    public BDLSRFortessaExperiment(File fullPath, File userRootDataPath) {

        super(fullPath, userRootDataPath);

        // Set the supported geometries
        this.supportedTrayGeometries.add("96_WELLS_8X12");
        this.supportedTrayGeometries.add("384_WELLS_16x24");
    }

    /**
     * Alternative constructor
     *
     * @param fullPath         Full path of the experiment.
     * @param name             Name of the experiment.
     * @param userRootDataPath Full path to current user folder.
     */
    public BDLSRFortessaExperiment(File fullPath, String name,
            File userRootDataPath) {

        super(fullPath, name, userRootDataPath);

        // Set the supported geometries
        this.supportedTrayGeometries.add("96_WELLS_8X12");
        this.supportedTrayGeometries.add("384_WELLS_16x24");
    }

}
