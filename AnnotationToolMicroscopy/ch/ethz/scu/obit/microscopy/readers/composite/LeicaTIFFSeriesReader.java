package ch.ethz.scu.obit.microscopy.readers.composite;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import loci.common.DebugTools;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;

/**
 * A composite reader for Leica TIFF series.
 * 
 * @author Aaron Ponti
 *
 */
public class LeicaTIFFSeriesReader extends AbstractCompositeMicroscopyReader {

	/* Protected instance variables */
	private File folder;
	private final String REGEX =
			"^(.*?)" +                    // Series basename: group 1
			"((_Series|_s)(\\d.*?))?" +   // Series number (optional): group 4
		    "(_t(\\d.*?))?" +             // Time index (optional): group 6
		    "_z(\\d.*?)" +                // Plane number: group 7
		    "_ch(\\d.*?)" +               // Channel number: group 8
		    "\\.tif{1,2}$";               // File extension

	private Pattern p = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);
    
	private File metadataFolder; 
	private String basename = "";
	
	private boolean isValid = false;

	private ImageProcessorReader reader = null;
	
	// Constructor
	public LeicaTIFFSeriesReader(File folder) {

		DebugTools.enableLogging("ERROR");
		
		this.folder = folder;
	}

	/**
	 * Return true if the reader can parse the passed folder
	 * @param folder Folder to be parsed.
	 * @return true if the folder can be parsed, false otherwise.
	 */
	static public boolean canRead(File folder) {
		
		// TODO: Implement!
		return true; 
	
	}

	/**
	 * Return true if the composite microscopy dataset could be parsed
	 * successfully.
	 * @return true if the composite microscopy dataset could be parsed
	 * successfully, false otherwise.
	 */
	public boolean isParsed() {
		return isValid;
	}

	@Override
	public boolean parse() throws Exception {

		// Get a list of all files
		File[] allFiles = folder.listFiles();

		// Now process them
		for (File file : allFiles) {

			// Get the file name
			String name = file.getName();

			// Do we have the 'MetaData' fodler?
			if (file.isDirectory()) {
				if (name.equalsIgnoreCase("MetaData")) {

					// Store the metadata folder
					metadataFolder = file;
					
					// Skip to the next iteration
					continue;

				} else {
					
					// We can only have one folder called 'MetaData'
					isValid = false;
					errorMessage = "Multiple subfolders found.";
					return isValid;
					
				}
			}

			// Current series metadata
			HashMap<String, String> metadata;

			// Extract the information
			Matcher m = p.matcher(name);
			if (m.find()) {

				// Get base name
				String currentBase = m.group(1);
				if (basename.equals("")) {
					basename = currentBase;
				} else if (currentBase.equals(basename)) {
					// Nothing to do
				} else {
					// More than one base name. Multiple Leica TIFF series.
					isValid = false;
					errorMessage = "Multiple series per folder not supported.";
					return isValid;
				}

				//
				// Extract index information from the file name structure
				//
				
				// The series index is not always defined
				int seriesNum = 0;
				if (m.group(2) != null) {
					seriesNum = Integer.parseInt(m.group(4));
				}

				// The time number is not always defined
				int TimeNum = 0;
				if (m.group(5) != null) {
					TimeNum = Integer.parseInt(m.group(6));
				}

				// Plane number (z)
				int planeNum = Integer.parseInt(m.group(7));
				
				// Channel number
				int channelNum = Integer.parseInt(m.group(8));
					
				// Build the key
				String key = "series_" + seriesNum;

				// Store the series index if not yet present
				if (attr.containsKey(key)) {
					
					metadata = attr.get(key);
					
				} else {
						
					// Create a new SeriesMetadata object 
					metadata = new HashMap<String, String>();
						
					// And add it to the arribute map
					attr.put(key, metadata);
						
					// Read the file
					if (reader == null) {
							
						// Initialize the reader
						reader = new ImageProcessorReader(
								new ChannelSeparator(
										LociPrefs.makeImageReader()));
					}
						
					// Try to open the image file
					try {
						reader.setId(file.getCanonicalPath());
					} catch (FormatException e) {
						reader = null;
					} catch (IOException e) {
						reader = null;
					}

					// Store width and height
					int width = 0;
					int heigth = 0;
					String datatype = "Unknown";
					if (reader != null) {

						// Get width
						width = reader.getSizeX();
							
						// Get heigth
						heigth = reader.getSizeY();

						// Get datatype
						datatype = getDataType();
							
						// Now close the file
						try {
							reader.close();
						} catch (IOException e) {
							// Report
							System.err.println("Could not close file!");
						}

					}
						
					// Store the extracted values 
					metadata.put("sizeX", Integer.toString(width));
					metadata.put("sizeY", Integer.toString(heigth));
					metadata.put("datatype", datatype);
						
					// Store default values. These should be replaced
					// with information extracted from the properties XML
					// file in the Metadata folder.
					metadata.put("voxelX", "1.0");
					metadata.put("voxelY", "1.0");
					metadata.put("voxelZ", "1.0");
						
					// TODO: Get wavelengths from metadata
					
					// TODO: Get acquisition time from metadata
					metadata.put("acquisitionDate", "NaN");
					
						
				}
					
				// Update the metadata object
				int numPlanes = getMetadataValueOrZero(metadata, "sizeZ");
				if ((planeNum + 1) > numPlanes) {
					metadata.put("sizeZ", Integer.toString(planeNum + 1));
				}

				int numChannels = getMetadataValueOrZero(metadata, "sizeC");
				if ((channelNum + 1) > numChannels) {
					metadata.put("sizeC", Integer.toString(channelNum + 1));
				}
						
				int numTimepoints = getMetadataValueOrZero(metadata, "sizeT");
				if ((TimeNum + 1) > numTimepoints) {
					metadata.put("sizeT", Integer.toString(TimeNum + 1));
				}
							
			}

		}
		
		// Now scan the Metadata folder
		// (when all files have been processed already!)
		if (!scanMetadataFolder()) {
			isValid = false;
			return isValid;
		}

		// Mark success
		isValid = true;
		errorMessage = "";
		return isValid;

	}

	/**
	 * Return information regarding the file format.
	 * @return descriptive String for the Reader.
	 */
	@Override
	public String info() {
		return "Leica TIFF series (composite) file format.";
	}

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override
	public String getType() {
		return "Leica TIFF Series";
	}

	/**
	 * Return the (representative) name of the composite microscopy dataset.
	 * @return the name of the composite microscopy dataset.
	 */		
	@Override
	public String getName() {
		return basename;
	}

	/**
	 * Return the folder that contains the composite microscopy dataset.
	 * @return the dataset folder.
	 */	
	@Override
	public File getFolder() {
		return folder;
	}

	/**
	 * Return the list of series indices extracted from the file names 
	 * @return List of series indices
	 */
	public List<Integer> getSeriesIndices() {
		List<Integer> indices = new ArrayList<Integer>();
		for (String key : attr.keySet()) {
			// Get the index from the series_{n} key
			indices.add(Integer.parseInt(key.substring(7)));
		}
		Collections.sort(indices);
		return indices;
	}

	/**
	 * Scans the MetadataFolder and stores all relevant information.
	 */
	private boolean scanMetadataFolder() {

		// Get the required XML file
		String[] xmlFiles = metadataFolder.list(new FilenameFilter() {
		    public boolean accept(File directory, String fileName) {
		        return (fileName.endsWith("_Properties.xml"));
		    }
		});
		
		if (xmlFiles.length != 1) {
			errorMessage = "Multiple metadata XML files found!";
			return false;
		}
		
		// Now process
		File xmlFile = new File(metadataFolder + File.separator + xmlFiles[0]);
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			return false;
		}
		Document doc;
		try {
			doc = db.parse(xmlFile);
		} catch (SAXException | IOException e) {
			return false;
		}
		doc.getDocumentElement().normalize();
		  
		// Get root node
		Node root = doc.getDocumentElement();
		if (! root.getNodeName().equals("Data")) {
			errorMessage = "Unexpected metadata XML content.";
			return false;
		}
		
		// Get the series indices
		List<Integer> seriesIndices = getSeriesIndices();
		int nSeriesFromFiles = seriesIndices.size();
		
		//
		// Extract relevant metadata information from the "Image" node
		//
		
		// Get the Image node
		NodeList nodeList = doc.getElementsByTagName("Image");
		if (nodeList.getLength() != 1) {
			errorMessage = "Unexpected metadata XML content.";
			return false;
		}
		Node imageNode = nodeList.item(0);

		// Get the stage (tile) positions
		NodeList tileNodes = ((Element)imageNode).getElementsByTagName("Tile");
		
		int nSeries = 0;
		for (int k = 0; k < tileNodes.getLength(); k++) {
			
			Node cNode = tileNodes.item(k);
			
			if (cNode.getNodeType() == Node.ELEMENT_NODE) {
				
				Element cEl = (Element) cNode;
				
				String key = "series_" + seriesIndices.get(k);
				
				if (attr.containsKey(key)) {
					HashMap<String, String> seriesMetadata = 
							attr.get(key);
					seriesMetadata.put("positionX",
							cEl.getAttribute("PosX"));
					seriesMetadata.put("positionY",
							cEl.getAttribute("PosY"));
				}
				
				nSeries++;
			}
		}
		
		// If there were no tiles, make sure to set nSeries to match the 
		// number of series found when scanning the files!
		if (nSeries == 0) {
			nSeries = nSeriesFromFiles;
		}

		//
		// Extract relevant metadata information from the "ImageDescription" node
		//

		// Get the Image node
		nodeList = doc.getElementsByTagName("ImageDescription");
		if (nodeList.getLength() != 1) {
			errorMessage = "Unexpected metadata XML content.";
			return false;
		}
		Node imageDescriptionNode = nodeList.item(0);

		// Get the channel descriptions
		NodeList channelNodes =
				((Element)imageDescriptionNode).getElementsByTagName(
						"ChannelDescription");

		int nChannels = channelNodes.getLength();
		Map<Integer, Float[]> RGBColors = new HashMap<Integer, Float[]>();
		for (int k = 0; k < nChannels; k++) {
			
			Node cNode = channelNodes.item(k);
			
			if (cNode.getNodeType() == Node.ELEMENT_NODE) {
				
				Element cEl = (Element) cNode;
				
				// Get the channel information
				String LUT = cEl.getAttribute("LUTName");
				
				Float[] c;
				switch (LUT) {
				case "Red":
					c = new Float[]{1.0f, 0.0f, 0.0f, 0.0f};
					break;
				case "Green":
					c = new Float[]{0.0f, 1.0f, 0.0f, 0.0f};
					break;
				case "Blue":
					c = new Float[]{0.0f, 0.0f, 1.0f, 0.0f};
					break;
				case "Yellow":
					c = new Float[]{1.0f, 1.0f, 0.0f, 0.0f};
					break;
				default:
					if (k == 0) {
						c = new Float[]{1.0f, 0.0f, 0.0f, 0.0f};
					} else if (k == 1) {
						c = new Float[]{0.0f, 1.0f, 0.0f, 0.0f}; 
					} else if (k == 2) {
						c = new Float[]{0.0f, 0.0f, 1.0f, 0.0f}; 
					} else { 
						c = new Float[]{1.0f, 1.0f, 1.0f, 0.0f}; 
					}
					break;
				}
				RGBColors.put(k, c);
			}
		}
		
		// Add the channel colors to the metadata
		for (int s = 0; s < nSeries; s++) {
			
			String key = "series_" + seriesIndices.get(s);
			
			if (attr.containsKey(key)) {
				HashMap<String, String> seriesMetadata = 
						attr.get(key);
				
				// Get the number of channels
				int nChannelsInMetadata =
						Integer.parseInt(seriesMetadata.get("sizeC"));
				
				for (int c = 0; c < nChannels; c++) {
					
					if (c < nChannelsInMetadata) {

						Float[] color = RGBColors.get(c);
						
						seriesMetadata.put("channelColor" + c,
								"" + color[0] + ", " +	color[1] + ", " +
								color[2] + ", " + color[3]);

					}
				}
			}

		}
			
		// Get the dimension descriptions
		NodeList dimensionNodes =
				((Element)imageDescriptionNode).getElementsByTagName(
						"DimensionDescription");

		int nDims = dimensionNodes.getLength();
		float voxelX = 1.0f;
		float voxelY = 1.0f;
		float voxelZ = 1.0f;
		int nSeriesFromFile = 0;
		
		for (int k = 0; k < nDims; k++) {
			
			Node cNode = dimensionNodes.item(k);
			
			if (cNode.getNodeType() == Node.ELEMENT_NODE) {
				
				Element cEl = (Element) cNode;
				
				// Get the channel information
				String dimID = cEl.getAttribute("DimID");
				
				switch (dimID) {
				case "X":
					voxelX = Float.parseFloat(cEl.getAttribute("Voxel"));
					break;
				case "Y":
					voxelY = Float.parseFloat(cEl.getAttribute("Voxel"));
					break;
				case "Z":
					voxelZ = Float.parseFloat(cEl.getAttribute("Voxel"));
					break;
				case "Stage":
					nSeriesFromFile = Integer.parseInt(
							cEl.getAttribute("NumberOfElements"));
					if (nSeries != nSeriesFromFile) {
						System.err.println(
								"Mismatch in the expected number of series!");
					}
					break;
				default:
					// Nothing to do.
				}
			}
		}
		
		// Add the voxel sizes to the metadata
		for (int s = 0; s < nSeries; s++) {
			
			String key = "series_" + seriesIndices.get(s);
			
			if (attr.containsKey(key)) {
				HashMap<String, String> seriesMetadata = 
						attr.get(key);
				
				seriesMetadata.put("voxelX", Float.toString(voxelX));
				seriesMetadata.put("voxelY", Float.toString(voxelY));
				seriesMetadata.put("voxelZ", Float.toString(voxelZ));

			}

		}
		
		return true;
	}
	
	/**
	 * Return current value for given metadata entry, or zero if not in the map.
	 * @param metadata Map of string - string key:value pairs.
	 * @param key Name of the metadata value to query.
	 * @return the value for the requested metadata value, or zero if
	 * it is not in the map.
	 */
	private int getMetadataValueOrZero(Map<String, String> metadata, String key) {
		int value = 0;
		if (metadata.containsKey(key)) {
			value = Integer.parseInt(metadata.get(key));
		}
		return value;
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
	 * Returns the last error message.
	 * @return String containing the last error message.
	 */
	public String getLastError() {
		return errorMessage;
	}
}
