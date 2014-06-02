/**
 * 
 */
package ch.ethz.scu.obit.microscopy.readers;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;
import ome.xml.model.primitives.Color;
import ome.xml.model.primitives.PositiveFloat;
import ome.xml.model.primitives.PositiveInteger;
import ome.xml.model.primitives.Timestamp;
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
	protected File filename;
	protected ImageProcessorReader reader;

	protected ServiceFactory factory;
	protected OMEXMLService service;
	protected IMetadata omexmlMeta;
	
	protected boolean isFileScanned = false;

	/**
	 * Metadata attributes
	 */
	protected Map<String, HashMap<String, String>> attr;

	/**
	 * @param filename File name with full path.
	 */
	public MicroscopyReader(File filename) {

		// Initialize String-String attributes map
		attr = new HashMap<String, HashMap<String, String>>();
		
		// Store the filename
		this.filename = filename;
		
		// Initialize
		init();

	}

	/**
	 * Initialize the reader and sets up the OMEXML metadata store
	 */
	protected boolean init() {

		// Create the reader
		reader = new ImageProcessorReader(
				new ChannelSeparator(
						LociPrefs.makeImageReader()));

		// Set OME-XML metadata
		factory = null;
		service = null;
		omexmlMeta = null;

		try {
			factory = new ServiceFactory();
			service = factory.getInstance(OMEXMLService.class);
		} catch (DependencyException e) {
			this.errorMessage =
					"Could not initialize bio-formats library. " +
			"Error was: " + e.getMessage();
			return false;
		}
		try {
			omexmlMeta = service.createOMEXMLMetadata();
		} catch (ServiceException e) {
			this.errorMessage =
				    "Could not initialize bio-formats library. " +
			"Error was: " + e.getMessage();
			return false;
		}
		reader.setMetadataStore(omexmlMeta);

		// Try to open the image file
		try {
		      reader.setId(filename.getCanonicalPath());
		} catch (FormatException e) {
			this.errorMessage =
					"Could not open file. Error was: " + e.getMessage();
			return false;
		} catch (IOException e) {
			this.errorMessage =
					"Could not open file. Error was: " + e.getMessage();
			return false;
		}

		this.errorMessage = "";
		return true;
	}
	
	/**
	 * Close file. 
	 * The client is responsible to call close to release the file handle!
	 * @return true if the file could be closed, false otherwise.
	 */
	public boolean close() {
		
		// If the file is closed already, we return true
		if (reader == null) {
			return true;
		}
		
		// Try closing it
		try {
			reader.close();
		} catch (IOException e) {
			
			// Return failure
			return false;
		}
		
		// Return success
		return true;
	}
	
	/**
	 * Implement finalize() to make sure that all file handles are released.
	 * @return 
	 */
	public void finalize() {
		
		// Call close
		close();
	}
	
	/**
	 * String-string map of data attribute key:value pair.
	 * 
	 * These will be uploaded to openBIS as associated information.
	 */
	public Map<String, HashMap<String, String>> getAttributes() {
		
		// If we have already scanned the file we just return the attributes
		if (attr.size() > 0) {
			return attr;
		}

		// Otherwise, we first scan and then return them
		if (! parse()) {
			
			// Reset attr
			attr = new HashMap<String, HashMap<String, String>>();
		}

		return attr;
	}

	/**
	 * Return the version of the bio-formats library used.
	 * @return String with the bio-formats library version.
	 */
	public String bioformatsVersion() {
		return loci.formats.FormatTools.VERSION;
	}

	/**
	 * Scan the file for metadata information and stores it as a 
	 * String-String map of key-value attributes.
	 * @return true if parsing was successful, false otherwise.
	 */
	@Override
	public boolean parse() {

		// If we have already scanned the file we just return true
		if (attr.size() > 0) {
			return true;
		}

		// Initialize file scanned to false
		isFileScanned = false;
		
		// Now process the file
		try {

			// Number of series
			int numSeries = reader.getSeriesCount();

			// Get and store all series
			for (int i = 0; i < numSeries; i++) {

				// Set the series
				reader.setSeries(i);

				// Create a hashmap for the attributes of current series
				HashMap<String, String> seriesAttr = new HashMap<String, String>();
				
				// Series number
				seriesAttr.put("numSeries", Integer.toString(i));

				// Image name
				seriesAttr.put("name", omexmlMeta.getImageName(i));

				// Image size X
				seriesAttr.put("sizeX", Integer.toString(reader.getSizeX()));

				// Image size Y
				seriesAttr.put("sizeY", Integer.toString(reader.getSizeY()));

				// Image size Z
				seriesAttr.put("sizeZ", Integer.toString(reader.getSizeZ()));

				// Image size C
				seriesAttr.put("sizeC", Integer.toString(reader.getSizeC()));

				// Image size T
				seriesAttr.put("sizeT", Integer.toString(reader.getSizeT()));

				// Data type
				seriesAttr.put("datatype", getDataType());

				// Is Signed
				seriesAttr.put("isSigned",
						Boolean.toString(loci.formats.FormatTools.isSigned(
								reader.getPixelType())));

				// Is Little Endian
				seriesAttr.put("isLittleEndian", 
						Boolean.toString(reader.isLittleEndian()));
				
				// Acquisition date
				seriesAttr.put("acquisitionDate", getAcquisitionDate(i));

				// Voxel sizes
				double[] voxels = getVoxelSizes(i);
				seriesAttr.put("voxelX", Double.toString(voxels[0]));
				seriesAttr.put("voxelY", Double.toString(voxels[1]));
				seriesAttr.put("voxelZ", Double.toString(voxels[2]));

				// Channel names
				String[] channelNames = getChannelNames(i);
				for (int c = 0; c < reader.getSizeC(); c++) {
					seriesAttr.put("channelName" + c,
                            Normalizer.normalize(channelNames[c],
                                    Normalizer.Form.NFD));
				}

				// Emission wavelengths
				double[] emWavelengths = getEmissionWavelengths(i);
				for (int c = 0; c < reader.getSizeC(); c++) {
					seriesAttr.put("emWavelength" + c,
							Double.toString(emWavelengths[c]));
				}

				// Excitation wavelengths
				double[] exWavelengths = getExcitationWavelengths(i);
				for (int c = 0; c < reader.getSizeC(); c++) {
					seriesAttr.put("exWavelength" + c,
							Double.toString(exWavelengths[c]));
				}

				// Stage position
				double[] stagePosition = getStagePosition();
				seriesAttr.put("positionX", Double.toString(stagePosition[0]));
				seriesAttr.put("positionY", Double.toString(stagePosition[1]));
				
				// Channel colors
				double[][] colors = getChannelColors(i);
				for (int c = 0; c < reader.getSizeC(); c++) {
					seriesAttr.put("channelColor" + c , "" + 
							colors[c][0] + ", " +
							colors[c][1] + ", " +
							colors[c][2] + ", " +
							colors[c][3]);
				}

				// NAs
				double[] NAs = getNAs();
				if (NAs.length == 0) {
					seriesAttr.put("NA", Double.toString(Double.NaN)); 
				} else if (NAs.length == 1) {
					seriesAttr.put("NA", Double.toString(NAs[0]));
				} else {
					String strNAs = "";
					for (int c = 0; c < NAs.length - 1; c++) {
						strNAs += Double.toString(NAs[c]) + ", ";
					}
					strNAs += Double.toString(NAs[NAs.length - 1]);
					seriesAttr.put("NA", strNAs);
				}
				

				// Key prefix
				String seriesKey = "series_" + i; 

				// Now add the metadata for current series
				attr.put(seriesKey, seriesAttr);
				
				}

			} catch (Exception e) {

				// Make sure the isFileScanned flag is set to false
				isFileScanned = false;
				
				// Return failure
				return false;

			}
			
		// Now we can set the isFileScanned flag to true
		isFileScanned = true;
		
		// And return success
		return true;

	}

	/**
	 * Return true if the file was scanned already, false otherwise.
	 * @return true if the file was scanned already, false otherwise.
	 */
	public boolean isScanned() {
		return isFileScanned;
	}

    /**
     * Return the number of series in the file.
     * @return Int Number of series in the file.
     */
    public int getNumberOfSeries() {
        try {
            return reader.getSeriesCount();
        } catch (Exception e) {
            return 0;
        }
    }

	/**
	 * Return the file name of associated to the MicroscopyReader.
	 * @return String containing the file name associated to the MicroscopyReader. 
	 */
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
	public String info() {
		return "Bioformats-compatible microscopy file format.";
	}

	/**
	 * Return the acquisition date for the specified series
	 * @param seriesNum index of series to query
	 * @return string acquisition date
	 * Values that could not be retrieved will be replaced by Double.NaN.
	 */
	public String getAcquisitionDate(int seriesNum) {
		
		Timestamp timestamp = omexmlMeta.getImageAcquisitionDate(seriesNum);
		String acqDate;
		if (timestamp == null) {
			acqDate = "";
		} else {
			acqDate = timestamp.getValue();
		}
		
		return acqDate;
	}

	/**
	 * Return the voxel sizes for given series
	 * @param seriesNum index of series to query
	 * @return array of doubles [voxelX, voxelY, voxelZ]
	 * Values that could not be retrieved will be replaced by Double.NaN.
	 */
	public double[] getVoxelSizes(int seriesNum) {
		
		PositiveFloat pVoxelX, pVoxelY, pVoxelZ;
		double voxelX, voxelY, voxelZ;
		
	    // Voxel size X
	    pVoxelX = omexmlMeta.getPixelsPhysicalSizeX(seriesNum);
	    if (pVoxelX == null) {
	        voxelX = Double.NaN;
	    } else {
	        voxelX = pVoxelX.getValue();
	    }
	    
	    // Voxel size Y
	    pVoxelY = omexmlMeta.getPixelsPhysicalSizeY(seriesNum);
	    if (pVoxelY == null) {
	    	if (pVoxelX == null) {
	    		voxelY = Double.NaN;
	    	} else {
	    		// Some formats store just one size for X and Y,
	    		// if they are equal.
	    		voxelY = voxelX;
	    	}
	    	
	    } else {
	        voxelY = pVoxelY.getValue();
	    }
	    
	    // Voxel size Z
	    pVoxelZ = omexmlMeta.getPixelsPhysicalSizeZ(seriesNum);
	    if (pVoxelZ == null) {
	        voxelZ = Double.NaN;
	    } else {
	        voxelZ = pVoxelZ.getValue();
	    }

        // Return
	    return new double[]{voxelX, voxelY, voxelZ};
	    
	}
	
	/**
	 * Return the channel names
	 * @param seriesNum index of the series to query
	 * @return array of channel names
	 */
	public String[] getChannelNames(int seriesNum) {
        
		// Allocate space to store the channel names
		int nChannels = reader.getSizeC();
		String[] channelNames = new String[nChannels];
		
		for (int i = 0; i < nChannels; i++) {
			channelNames[i] = omexmlMeta.getChannelName(seriesNum, i);
		}
		
		return channelNames;
	}
	
	/**
	 * Return the data type
	 * @return string datatype, one of "uint8", "uint16", "float", "unsupported".
	 */
	public String getDataType() {
        
		// Get and store the dataset type
		String datatype;
		switch (loci.formats.FormatTools.getBytesPerPixel(reader.getPixelType())) {
			case 1:
				datatype = "uint8";
				break;
			case 2:
				datatype = "uint16";
				break;
			case 4:
				datatype = "float";
				break;
			default:
				datatype = "unsupported";
				break;
		}
		
		return datatype;
	}

	/**
	 * Return the channel excitation wavelengths
	 * @param seriesNum index of the series to query
	 * @return array of excitation wavelengths. 
	 * Values that could not be retrieved will be replaced by Double.NaN.
	 */
	public double[] getExcitationWavelengths(int seriesNum) {
        
		// Allocate space to store the channel names
		int nChannels = reader.getSizeC();
		double[] exWavelengths = new double[nChannels];
		
		for (int i = 0; i < nChannels; i++) {
			PositiveInteger pEx =
					omexmlMeta.getChannelExcitationWavelength(seriesNum, i);
		    double ex;
			if (pEx != null) {
		        ex = (double) pEx.getValue();
		    } else {
		    	ex = Double.NaN;
		    }
			exWavelengths[i] = ex;
		}
		
		return exWavelengths;
	}
	
	/**
	 * Return the channel emission wavelengths
	 * @param seriesNum index of the series to query
	 * @return array of emission wavelengths.
	 * Values that could not be retrieved will be replaced by Double.NaN.
	 */
	public double[] getEmissionWavelengths(int seriesNum) {
        
		// Allocate space to store the channel names
		int nChannels = reader.getSizeC();
		double[] emWavelengths = new double[nChannels];
		
		for (int i = 0; i < nChannels; i++) {
			PositiveInteger pEm =
					omexmlMeta.getChannelEmissionWavelength(seriesNum, i);
		    double em;
			if (pEm != null) {
		        em = (double) pEm.getValue();
		    } else {
		    	em = Double.NaN;
		    }
			emWavelengths[i] = em;
		}
		
		return emWavelengths;
	}

	/**
	 * Return the objective numerical aperture(s)
	 * @return array of numerical apertures.
	 * Values that could not be retrieved will be replaced by Double.NaN.
	 */
	public double[] getNAs() {
        
		// Number of instruments
		int nInstr = omexmlMeta.getInstrumentCount();
		
		// Count objectives for this series (expected: 1)
		int nTotObj = 0;
		for (int i = 0; i < nInstr; i++) {
			for (int j = 0; j < omexmlMeta.getObjectiveCount(i); j++) {
				nTotObj++;
			}
		}
		
		// Allocate space to store the numerical apertures
		double[] numericalApertures = new double[nTotObj];

		// Get all NAs (expected: 1)
		int c = 0;
		for (int i = 0; i < nInstr; i++) {
			for (int j = 0; j < omexmlMeta.getObjectiveCount(i); j++) {
				Double NA = omexmlMeta.getObjectiveLensNA(i, j);
				if (NA == null) {
					NA = Double.NaN;
				}
				numericalApertures[c++] = NA;
			}
		}
		return numericalApertures;
	}

	/**
	 * Return the channel stage position for current series.
	 * 
	 * The series must have set in advance with reader.setSeries(num).
	 * 
	 * @return array of [X Y] stage position.
	 * Values that could not be retrieved will be replaced by Double.NaN.
	 */
	public double[] getStagePosition() {
	    
		// Allocate memory to store the stage position
		double[] stagePosition = new double[2];
		
		// The series has been set earlier
		
		double x, y;
		
		// Does the stage position information exist?
		Hashtable<String, Object> metadata = reader.getSeriesMetadata();
		Set<String> keys = metadata.keySet();
		if (keys.contains("X position")) {
			Object m = metadata.get("X position");
			x = ((Number)m).doubleValue(); // Java 6 compatibility
		} else {
			x = Double.NaN;
		}

		if (keys.contains("Y position")) {
			Object m = metadata.get("Y position");
			y = ((Number)m).doubleValue(); // Java 6 compatibility
		} else {
			y = Double.NaN;
		}

		// Now store the retrieved stage positions 
        stagePosition[0] = x;
        stagePosition[1] = y;
        
        // And return them
        return stagePosition;
	}
	
	/**
	 * Return the channel colors [R, G, B, A]n for current series
	 * @param seriesNum index of the series to query
	 * @return array of arrays of [R, G, B, A]n, with n = 1 .. nChannels
	 * Values that could not be retrieved will be replaced by Double.NaN.
	 */
	public double[][] getChannelColors(int seriesNum) {
		
		// Allocate space to store the channel names
		int nChannels = reader.getSizeC();
		double[][] colors = new double[nChannels][4];
		
		for (int i = 0; i < nChannels; i++) {
			Color color = omexmlMeta.getChannelColor(seriesNum, i);
			if (color == null) {
				colors[i][0] = Double.NaN;
				colors[i][1] = Double.NaN;
				colors[i][2] = Double.NaN;
				colors[i][3] = Double.NaN;
			} else {
				colors[i][0] = color.getRed();
				colors[i][1] = color.getGreen();
				colors[i][2] = color.getBlue();
				colors[i][3] = color.getAlpha();				
			}
		}

		// Return the colors
		return colors;
	}
}
