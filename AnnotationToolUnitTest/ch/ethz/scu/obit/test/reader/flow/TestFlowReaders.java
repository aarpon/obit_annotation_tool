package ch.ethz.scu.obit.test.reader.flow;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import ch.ethz.scu.obit.bdfacsdivafcs.processors.data.BDFACSDIVAFCSProcessor;
import ch.ethz.scu.obit.bdfacsdivafcs.processors.data.BDFACSDIVAFCSProcessor.Experiment;
import ch.ethz.scu.obit.bdfacsdivafcs.processors.data.BDFACSDIVAFCSProcessor.FCSFile;
import ch.ethz.scu.obit.bdfacsdivafcs.processors.data.BDFACSDIVAFCSProcessor.Specimen;
import ch.ethz.scu.obit.bdfacsdivafcs.processors.data.BDFACSDIVAFCSProcessor.Tray;
import ch.ethz.scu.obit.bdfacsdivafcs.processors.data.BDFACSDIVAFCSProcessor.Tube;
import ch.ethz.scu.obit.bdfacsdivafcs.readers.FCSReader;


/**
 * Test flow cytometry readers and processors.
 * @author Aaron Ponti
 *
 */
public class TestFlowReaders {

    String dataFolder = "";

    /**
     * Test reading an FACSAriaIII DIVA experiment.
     */
    @Test
    public void testFortessa6ExperimentScan() {

        // "User" folder
        String userFolder = dataFolder + "/fortessa/6";

        // Initialize the processor
        BDFACSDIVAFCSProcessor processor = new BDFACSDIVAFCSProcessor(userFolder);

        // Scan the project
        boolean success = processor.parse();

        // Check that the scanning was successful
        assertEquals(success, true);

        // Extract the one experiment
        Map<String, Experiment> expList = processor.folderDescriptor.experiments;
        Set<String> keys = expList.keySet();
        assertEquals(keys.size(), 1);

        // Iterate over the only experiment
        for (String key: keys) {

            // Get the Experiment
            Experiment exp = expList.get(key);

            // Now get some experiment properties
            String name = exp.getName();
            Map<String, String> attributes = exp.getAttributes();
            String code = exp.getCode();

            // Check
            assertEquals(name, "Eva Spore Counting 190612");
            assertEquals(code, "Eva_Spore_Counting_190612");
            assertEquals(attributes.get("acq_hardware"), "BD LSR Fortessa");
            assertEquals(attributes.get("acq_software"), "BD FACSDiva Software Version 6.1.3");
            assertEquals(attributes.get("owner_name"), "SingleCellUnit");

            // Get the trays (plates)
            Map<String, Tray> trayList = exp.trays;
            Set<String> trayKeys = trayList.keySet();
            assertEquals(trayKeys.size(), 1);

            // Iterate over the only tray
            for (String trayKey: trayKeys) {

                // Get the tray
                Tray tray = trayList.get(trayKey);

                // Get some attributes
                String trayName = tray.getName();
                String trayCode = tray.getCode();
                String trayGeometry = tray.geometry;

                // Test the attributes
                assertEquals(trayName, "96 Well - beads 10 ul");
                assertEquals(trayCode, "96_Well_-_beads_10_ul");
                assertEquals(trayGeometry, "96_WELLS_8X12");

                // Get the specimens
                Map<String, Specimen> traySpecimenList = tray.specimens;

                Set<String> traySpecimenKeys = traySpecimenList.keySet();
                assertEquals(traySpecimenKeys.size(), 1);

                // Iterate over the only tray specimen
                for (String traySpecimenKey: traySpecimenKeys) {

                    // Get the tray specimen
                    Specimen traySpecimen = traySpecimenList.get(traySpecimenKey);

                    String traySpecimenName = traySpecimen.getName();
                    String traySpecimenCode = traySpecimen.getCode();
                    assertEquals(traySpecimenName, "Specimen_001");
                    assertEquals(traySpecimenCode, "Specimen_001");

                    // Get the wells
                    Map<String, Tube> wellList = traySpecimen.tubes;
                    Set<String> wellKeys = wellList.keySet();
                    assertEquals(wellKeys.size(), 1);

                    // Iterate over the only well
                    for (String wellKey: wellKeys) {

                        // Get the tray
                        Tube well = wellList.get(wellKey);

                        // Get some attributes
                        String wellName = well.getName();
                        String wellCode = well.getCode();

                        // Test the attributes
                        assertEquals(wellName, "A01");
                        assertEquals(wellCode, "A01");

                    }

                }

            }

            // Get the specimens
            Map<String, Specimen> specimenList = exp.specimens;

            Set<String> specimenKeys = specimenList.keySet();
            assertEquals(specimenKeys.size(), 1);

            // Iterate over the only specimen
            for (String specimenKey: specimenKeys) {

                // Get the specimen
                Specimen specimen = specimenList.get(specimenKey);

                String specimenName = specimen.getName();
                String specimenCode = specimen.getCode();
                assertEquals(specimenName, "Specimen_001");
                assertEquals(specimenCode, "Specimen_001");

                // Get the tubes
                Map<String, Tube> tubeList = specimen.tubes;
                Set<String> tubeKeys = tubeList.keySet();
                assertEquals(tubeKeys.size(), 1);

                // Iterate over the only tube
                for (String tubeKey: tubeKeys) {

                    // Get the tray specimen
                    Tube tube = tubeList.get(tubeKey);

                    String tubeName = tube.getName();
                    String tubeCode = tube.getCode();
                    assertEquals(tubeName, "Tube_001");
                    assertEquals(tubeCode, "Tube_001");

                    FCSFile fcsFile = tube.fcsFile;
                    String fcsFileName = fcsFile.getName();
                    String fscFileRelPath = fcsFile.getRelativePath();
                    assertEquals(fcsFileName, "Specimen_001_Tube_001.fcs");
                    assertEquals(fscFileRelPath, "6\\Eva Spore Counting 190612\\Specimen_001_Tube_001.fcs");

                }
            }
        }

    }

    /**
     * Test reading a FACS Sortware 1 experiment.
     */
    @Test
    public void testInflux1ExperimentScan() {

        // "User" folder
        String userFolder = dataFolder + "/influx/1";

        // Initialize the processor
        BDFACSDIVAFCSProcessor processor = new BDFACSDIVAFCSProcessor(userFolder);

        // Scan the project
        boolean success = processor.parse();

        // Check that the scanning was successful
        assertEquals(success, true);

        // Extract the one experiment
        Map<String, Experiment> expList = processor.folderDescriptor.experiments;
        Set<String> keys = expList.keySet();
        assertEquals(keys.size(), 1);

        // Iterate over the only experiment
        for (String key: keys) {

            // Get the Experiment
            Experiment exp = expList.get(key);

            // Now get some experiment properties
            String name = exp.getName();
            Map<String, String> attributes = exp.getAttributes();
            String code = exp.getCode();

            // Check
            assertEquals(name, "sort_20160427");
            assertEquals(code, "sort_20160427");
            assertEquals(attributes.get("acq_hardware"), "BD Influx");
            assertEquals(attributes.get("acq_software"), "BD FACS™ Sortware 1.2.0.142");
            assertEquals(attributes.get("owner_name"), "");

            // Get the specimens
            Map<String, Specimen> specimenList = exp.specimens;

            Set<String> specimenKeys = specimenList.keySet();
            assertEquals(specimenKeys.size(), 1);

            // Iterate over the only specimen
            for (String specimenKey: specimenKeys) {

                // Get the specimen
                Specimen specimen = specimenList.get(specimenKey);

                String specimenName = specimen.getName();
                String specimenCode = specimen.getCode();
                assertEquals(specimenName, "20160427");
                assertEquals(specimenCode, "20160427");

                // Get the tubes
                Map<String, Tube> tubeList = specimen.tubes;
                Set<String> tubeKeys = tubeList.keySet();
                assertEquals(tubeKeys.size(), 2);

                // Iterate over the only tube
                for (String tubeKey: tubeKeys) {

                    // Get the tray specimen
                    Tube tube = tubeList.get(tubeKey);

                    String tubeName = tube.getName();
                    String tubeCode = tube.getCode();
                    
                    boolean tubeNameMatch = tubeName.equals("Kash_J63") ||
                            tubeName.equals("Kash_J64");

                    boolean tubeCodeMatch = tubeCode.equals("Kash_J63") ||
                            tubeCode.equals("Kash_J64");
                    
                    assertEquals(tubeNameMatch, true);
                    assertEquals(tubeCodeMatch, true);

                    FCSFile fcsFile = tube.fcsFile;
                    String fcsFileName = fcsFile.getName();
                    String fscFileRelPath = fcsFile.getRelativePath();

                    boolean fileNameMatch = fcsFileName.equals("Kash_J63.fcs") ||
                            fcsFileName.equals("Kash_J64.fcs");

                    boolean filePathMatch = 
                            fscFileRelPath.equals("1\\sort_20160427\\Kash_J63.fcs") ||
                            fscFileRelPath.equals("1\\sort_20160427\\Kash_J64.fcs");

                    assertEquals(fileNameMatch, true);
                    assertEquals(filePathMatch, true);

                }
            }
        }

    }
    
    /**
     * Test reading a single FCS 3.0 file from Influx (DIVA Sortware 1.2).
     */
    @Test
    public void testSingleInflux1FileRead() {

        // Test an FCS 3.1 file from FACSAriaIII (DIVA 8.0.1)
        File fcsFile = new File(dataFolder + 
                "/influx/1/sort_20160427/Kash_J63.fcs");

        // Open the file (with data scan)
        FCSReader reader = new FCSReader(fcsFile, true);

        // Scan the file
        boolean success;
        try {
            success = reader.parse();
        } catch (IOException e) {
            success = false;
        }
        assertEquals(success, true);

        // Test several keywords
        assertEquals(reader.getFCSVersion(), "FCS3.0");
        assertEquals(reader.getStandardKeyword("$CYT"), "BD Influx System (USB)");
        assertEquals(reader.getStandardKeyword("$DATATYPE"), "I");

        // Warning: the APPLICATION key is called CREATOR in FCS files coming
        // from Aria or Fortessa! Moreover, the string says "soRtware"...
        assertEquals(reader.getCustomKeyword("APPLICATION"),
                "BD FACS™ Sortware 1.2.0.142");

        // Warning: the $FIL key contains the ABSOLUTE path to the file!
        // In the case of Aria and Fortessa, only the file name is stored!
        String fileName = new File(reader.getStandardKeyword("$FIL")).getName();
        assertEquals(fileName, "Kash_J63.fcs");

        // Warning there is no EXPERIMENT NAME key!
        
        // Warning there is no TUBE NAME key!
        
        // Test data
        assertEquals(reader.numEvents(), 50000);
        assertEquals(reader.numParameters(), 27);
    }

    /**
     * Test reading a single FCS 3.1 file from FACSAriaIII (DIVA 8.0.1).
     */
    @Test
    public void testSingleAria8FileRead() {

        // Test an FCS 3.1 file from FACSAriaIII (DIVA 8.0.1)
        File fcsFile = new File(dataFolder + 
                "/aria/8/150115KK YVI - Exp1/BM YVI male_test sort_002.fcs");

        // Open the file (with data scan)
        FCSReader reader = new FCSReader(fcsFile, true);

        // Scan the file
        boolean success;
        try {
            success = reader.parse();
        } catch (IOException e) {
            success = false;
        }
        assertEquals(success, true);

        // Test several keywords
        assertEquals(reader.getFCSVersion(), "FCS3.1");
        assertEquals(reader.getStandardKeyword("$CYT"), "FACSAriaIII");
        assertEquals(reader.getStandardKeyword("$DATATYPE"), "F");
        assertEquals(reader.getStandardKeyword("$FIL"), "BM YVI male_test sort_002.fcs");
        assertEquals(reader.getCustomKeyword("CREATOR"), "BD FACSDiva Software Version 8.0.1");
        assertEquals(reader.getCustomKeyword("EXPERIMENT NAME"), "150115KK YVI - Exp1");
        assertEquals(reader.getCustomKeyword("TUBE NAME"), "test sort");

        // Test data
        assertEquals(reader.numEvents(), 87);
        assertEquals(reader.numParameters(), 14);
    }

    /**
     * Constructor.
     */
    public TestFlowReaders() {

        // Store data folder
        dataFolder = this.getClass().getResource("data").getFile();

    }

    /** 
     * Entry point
     * @param args Ignored.
     */   
    public static void main(String[] args) {

        Result result = JUnitCore.runClasses(TestFlowReaders.class);
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }
        System.out.println(result.wasSuccessful());

    }

}
