/**
 * 
 */
package ch.eth.scu.importer.microscopy.readers;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import ome.xml.model.primitives.Color;
import ome.xml.model.primitives.PositiveFloat;
import ome.xml.model.primitives.PositiveInteger;

/**
 * @author Aaron Ponti
 *
 */
public class MicroscopyReader extends BaseBioFormatsReader {

	/**
	 * Metadata attributes
	 */
	protected Map<String, HashMap<String, String>> attr;

	/**
	 * @param filename File name with full path.
	 */
	public MicroscopyReader(File filename) {
		
		// Call base constructor
		super(filename);

		// Initialize String-String attributes map
		attr = new HashMap<String, HashMap<String, String>>();

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
		parse();
		return attr;

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

				// Pixel type
				seriesAttr.put("pixelType",
						loci.formats.FormatTools.getPixelTypeString(
								reader.getPixelType()));

				// Acquisition date
				seriesAttr.put("acqDate", 
						omexmlMeta.getImageAcquisitionDate(i).getValue());
				
				// Voxel sizes
				double[] voxels = getVoxelSizes(i);
				seriesAttr.put("voxelX", Double.toString(voxels[0]));
				seriesAttr.put("voxelY", Double.toString(voxels[1]));
				seriesAttr.put("voxelZ", Double.toString(voxels[2]));

				// Channel names
				String[] channelNames = getChannelNames(i);
				for (int c = 0; c < reader.getSizeC(); c++) {
					seriesAttr.put("channelName" + c, channelNames[c]);
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
				
				// Key prefix
				String seriesKey = "series_" + i; 

				// Now add the metadata for current series
				attr.put(seriesKey, seriesAttr);
				
				}

			} catch (Exception e) {

				return false;

			}
			
		return true;

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
	        voxelY = Double.NaN;
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

	    // Workaround for 2-D ND2 files 
	    if (voxelY == 0 && voxelX > 0) {
	    	voxelY = voxelX;
	    }

	    // Pack the voxels into an array
	    double voxels[] = {voxelX, voxelY, voxelZ};
	    
	    // Return
	    return voxels;
	    
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
	 * Return the channel stage position for current series.
	 * 
	 * The series must have set in advance with reader.setSeries(num).
	 * 
	 * @param seriesNum index of the series to query
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
			x = (double) m;
		} else {
			x = Double.NaN;
		}

		if (keys.contains("Y position")) {
			Object m = metadata.get("Y position");
			y = (double) m;
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
