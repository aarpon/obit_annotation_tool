package ch.ethz.scu.obit.test.reader.microscopy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import ch.ethz.scu.obit.microscopy.processors.data.MicroscopyProcessor;
import ch.ethz.scu.obit.microscopy.processors.data.MicroscopyProcessor.Experiment;
import ch.ethz.scu.obit.microscopy.processors.data.MicroscopyProcessor.MicroscopyFile;
import ch.ethz.scu.obit.microscopy.processors.data.MicroscopyProcessor.MicroscopyFileSeries;

public class TestMicroscopyReaders {

    /**
     * Entry point
     *
     * @param args Ignored.
     */
    public static void main(String[] args) {

        Result result = JUnitCore.runClasses(TestMicroscopyReaders.class);
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }

    }

    String dataFolder = "";

    /**
     * Constructor.
     */
    public TestMicroscopyReaders() {

        // Store data folder
        dataFolder = this.getClass().getResource("data").getFile();
    }

    /**
     * Test reading an FACSAriaIII DIVA experiment.
     *
     * @throws Exception
     */
    @Test
    public void testMicroscopyProcessor() throws Exception {

        // "User" folder
        String userFolder = dataFolder + "/user";

        // Initialize the processor
        MicroscopyProcessor processor = new MicroscopyProcessor(userFolder);

        // Scan the project
        boolean success = processor.parse();

        // Check that the scanning was successful
        assertEquals(success, true);

        // Get the experiment
        Map<String, Experiment> experiments = processor.folderDescriptor.experiments;

        // Check that one experiment was found
        assertEquals(experiments.keySet().size(), 1);

        // Process the files in the experiment
        for (String key : experiments.keySet()) {

            // Get the experiment
            Experiment exp = experiments.get(key);

            // Make sure the name of the experiment is "nd2"
            assertEquals(exp.getName(), "nd2");

            // Make sure there are two files and no composite files
            assertEquals(exp.microscopyCompositeFiles.size(), 0);
            assertEquals(exp.microscopyFiles.size(), 2);

            // Process the microscopy files
            for (String fileKey : exp.microscopyFiles.keySet()) {

                // Get the file
                MicroscopyFile file = exp.microscopyFiles.get(fileKey);

                // Scan for series
                assertTrue(file.scanForSeries());

                // Get the metadata
                Map<String, String> fileAttr = file.getAttributes();

                // Test the file metadata
                if (file.getName().equals("test_C3.nd2")) {
                    assertEquals("6496256", fileAttr.get("datasetSize"));
                    assertEquals("user/nd2/test_C3.nd2",
                            fileAttr.get("relativeFileName"));
                } else if (file.getName().equals("test_C5.nd2")) {
                    assertEquals("2846720", fileAttr.get("datasetSize"));
                    assertEquals("user/nd2/test_C5.nd2",
                            fileAttr.get("relativeFileName"));
                } else {
                    throw new Exception("Unexpected file name.");
                }

                // Get the series
                Map<String, MicroscopyFileSeries> series = file.getSeries();
                for (String seriesKey : series.keySet()) {

                    // Get series
                    MicroscopyFileSeries currentSeries = series.get(seriesKey);

                    // Get the metadata
                    Map<String, String> seriesAttr = currentSeries
                            .getAttributes();

                    // Test the series metadata
                    if (file.getName().equals("test_C3.nd2")) {
                        assertEquals("0.0, 0.0, 255.0, 255.0",
                                seriesAttr.get("channelColor0"));
                        assertEquals("0.0, 255.0, 0.0, 255.0",
                                seriesAttr.get("channelColor1"));
                        assertEquals("255.0, 0.0, 0.0, 255.0",
                                seriesAttr.get("channelColor2"));
                        assertEquals("", seriesAttr.get("acquisitionDate"));
                        assertEquals("false", seriesAttr.get("isSigned"));
                        assertEquals("true", seriesAttr.get("isLittleEndian"));
                        assertEquals("3918.5", seriesAttr.get("positionX"));
                        assertEquals("-10112.4", seriesAttr.get("positionY"));
                        assertEquals("uint16", seriesAttr.get("datatype"));
                        assertEquals("450.0", seriesAttr.get("emWavelength0"));
                        assertEquals("525.0", seriesAttr.get("emWavelength1"));
                        assertEquals("595.0", seriesAttr.get("emWavelength2"));
                        assertEquals("NaN", seriesAttr.get("exWavelength0"));
                        assertEquals("NaN", seriesAttr.get("exWavelength1"));
                        assertEquals("NaN", seriesAttr.get("exWavelength2"));
                        assertEquals("0", seriesAttr.get("numSeries"));
                        assertEquals("0.1241751041301025",
                                seriesAttr.get("voxelX"));
                        assertEquals("0.1241751041301025",
                                seriesAttr.get("voxelY"));
                        assertEquals("NaN", seriesAttr.get("voxelZ"));
                        assertEquals("3", seriesAttr.get("sizeC"));
                        assertEquals("1", seriesAttr.get("sizeT"));
                        assertEquals("1024", seriesAttr.get("sizeX"));
                        assertEquals("1024", seriesAttr.get("sizeY"));
                        assertEquals("1", seriesAttr.get("sizeZ"));
                        assertEquals("false", seriesAttr.get("isThumbnail"));
                        assertEquals("DAPI", seriesAttr.get("channelName0"));
                        assertEquals("EGFP", seriesAttr.get("channelName1"));
                        assertEquals("mCherry", seriesAttr.get("channelName2"));
                        assertEquals("NaN", seriesAttr.get("NA"));
                        assertEquals("test_C3.nd2 (series 1)",
                                seriesAttr.get("name"));
                    } else if (file.getName().equals("test_C5.nd2")) {
                        assertEquals("0.0, 255.0, 255.0, 255.0",
                                seriesAttr.get("channelColor0"));
                        assertEquals("0.0, 255.0, 0.0, 255.0",
                                seriesAttr.get("channelColor1"));
                        assertEquals("255.0, 0.0, 0.0, 255.0",
                                seriesAttr.get("channelColor2"));
                        assertEquals("128.0, 0.0, 255.0, 255.0",
                                seriesAttr.get("channelColor3"));
                        assertEquals("255.0, 255.0, 255.0, 255.0",
                                seriesAttr.get("channelColor4"));
                        assertEquals("", seriesAttr.get("acquisitionDate"));
                        assertEquals("false", seriesAttr.get("isSigned"));
                        assertEquals("true", seriesAttr.get("isLittleEndian"));
                        assertEquals("773.9", seriesAttr.get("positionX"));
                        assertEquals("3559.0", seriesAttr.get("positionY"));
                        assertEquals("uint16", seriesAttr.get("datatype"));
                        assertEquals("450.0", seriesAttr.get("emWavelength0"));
                        assertEquals("525.0", seriesAttr.get("emWavelength1"));
                        assertEquals("595.0", seriesAttr.get("emWavelength2"));
                        assertEquals("700.0", seriesAttr.get("emWavelength3"));
                        assertEquals("NaN", seriesAttr.get("emWavelength4"));
                        assertEquals("NaN", seriesAttr.get("exWavelength0"));
                        assertEquals("NaN", seriesAttr.get("exWavelength1"));
                        assertEquals("NaN", seriesAttr.get("exWavelength2"));
                        assertEquals("NaN", seriesAttr.get("exWavelength3"));
                        assertEquals("NaN", seriesAttr.get("exWavelength4"));
                        assertEquals("0", seriesAttr.get("numSeries"));
                        assertEquals("0.8625684516339195",
                                seriesAttr.get("voxelX"));
                        assertEquals("0.8625684516339195",
                                seriesAttr.get("voxelY"));
                        assertEquals("NaN", seriesAttr.get("voxelZ"));
                        assertEquals("5", seriesAttr.get("sizeC"));
                        assertEquals("1", seriesAttr.get("sizeT"));
                        assertEquals("512", seriesAttr.get("sizeX"));
                        assertEquals("512", seriesAttr.get("sizeY"));
                        assertEquals("1", seriesAttr.get("sizeZ"));
                        assertEquals("false", seriesAttr.get("isThumbnail"));
                        assertEquals("DAPI", seriesAttr.get("channelName0"));
                        assertEquals(
                                "Fluorescein goat anti–mouse IgG antibody/pH 8.0",
                                seriesAttr.get("channelName1"));
                        assertEquals("Cy3 dye–labeled IgG antibody/pH 7.2",
                                seriesAttr.get("channelName2"));
                        assertEquals("DiR", seriesAttr.get("channelName3"));
                        assertEquals("TD", seriesAttr.get("channelName4"));
                        assertEquals("NaN", seriesAttr.get("NA"));
                        assertEquals("test_C5.nd2 (series 1)",
                                seriesAttr.get("name"));
                    } else {
                        throw new Exception("Unexpected file name.");
                    }

                }

            }

        }

    }
}
