package ch.ethz.scu.obit.flow.processors.data.model;

import java.io.File;
import java.util.ArrayList;

/**
 * Descriptor representing an experiment obtained from the FCS file.
 *
 * @author Aaron Ponti
 */
public class AnalyzerExperiment extends Experiment {

    /**
     * Support tray geometries.
     *
     * Do be defined by the inheriting classes.
     */
    public ArrayList<String> supportedTrayGeometries = new ArrayList<String>();

    /**
     * Constructor
     *
     * @param fullPath         Full path to the experiment folder.
     * @param userRootDataPath Full path to current user folder.
     */
    public AnalyzerExperiment(File fullPath, File userRootDataPath) {

        super(fullPath, userRootDataPath);
    }

    /**
     * Alternative constructor
     *
     * @param fullPath         Full path of the experiment.
     * @param name             Name of the experiment.
     * @param userRootDataPath Full path to current user folder.
     */
    public AnalyzerExperiment(File fullPath, String name,
            File userRootDataPath) {

        super(fullPath, name, userRootDataPath);
    }

    /**
     * Return the identifier prefix of the collection type.
     *
     * @return "FLOW_ANALYZERS_"
     */
    @Override
    public String getOpenBISCollectionIdentifierPrefix() {
        return "FLOW_ANALYZERS_";

    }
}
