package ch.eth.scu.scuimporter.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.*;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;

/**
 * LeicaLifProcessor parses Leica LIF files.
 * 
 * @author Aaron Ponti
 */
public class LeicaLifProcessor extends AbstractProcessor {

	/* Private instance variables */
	private String filename;
	private ImageProcessorReader reader;
	
	/**
	 * ArrayList of ImageDescriptor's
	 */
	public List<ImageDescriptor> imageDescriptors;
	
	/**
	 * Constructor 
	 * @param filename Name with full path of the file to be opened.
	 */
	public LeicaLifProcessor(String filename) {
		this.filename = filename;
	}
	
	@Override
	public boolean parse() throws DependencyException, ServiceException {

		// Create the reader
		reader = new ImageProcessorReader(
				new ChannelSeparator(LociPrefs.makeImageReader()));

		// Set OME-XML metadata
		ServiceFactory factory = new ServiceFactory();
		OMEXMLService service = factory.getInstance(OMEXMLService.class);
		IMetadata omexmlMeta = service.createOMEXMLMetadata();
		reader.setMetadataStore(omexmlMeta);

		// Try to open the image file
		try {
		      reader.setId(filename);
		} catch (FormatException e) {
			JOptionPane.showMessageDialog(null,
				    "Could not open file. Error was: " + e.getMessage(),
				    "Error",
				    JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
				    "Could not open file. Error was: " + e.getMessage(),
				    "Error",
				    JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		// Number of series
		int numSeries = reader.getSeriesCount();
		
		// Get all dataset information
		imageDescriptors = new ArrayList<ImageDescriptor>(numSeries);
		
		// Go over all datasets and extract relevant metadata
		for (int i = 0; i < reader.getSeriesCount(); i++) {
			reader.setSeries(i);
			imageDescriptors.add( 
					new ImageDescriptor(
					omexmlMeta.getImageName(i),
					reader.getSizeX(), reader.getSizeY(), reader.getSizeZ(),
					reader.getSizeC( ), reader.getSizeT( ), 
					loci.formats.FormatTools.getPixelTypeString(
							reader.getPixelType())));
		}
		
		// Return success
		return true;
	}

	/**
	 * Return information regarding the file format.
	 * @return descriptive String for the Processor.
	 */
	public String info() {
		return "Leica LIF format.";
	}

	public String toString() {
		return filename;
		/*String str = "";
		for (int i = 0; i < imageDescriptors.size(); i++) {
			str += imageDescriptors.get(i).summary() + "\n";
		}
		return str;*/
	}
	
	/**
	 * Simple class to store image information
	 * @author Aaron Ponti
	 *
	 */
	public class ImageDescriptor {
		
		/** Protected instance variables */
		protected int sizeX;
		protected int sizeY;
		protected int sizeZ;
		protected int sizeC;
		protected int sizeT;
		protected String name;
		protected String datatype;
		
		/**
		 * Constructor
		 * @param name      image name
		 * @param sizeX		image size in X direction
		 * @param sizeY		image size in Y direction
		 * @param sizeZ		image size in Z direction
		 * @param sizeC		number of channels
		 * @param sizeT		number of time points
		 * @param datatype	image data type
		 */
		public ImageDescriptor(String name, int sizeX, int sizeY, int sizeZ,
				int sizeC, int sizeT, String datatype) {
			
			this.name = name;
			this.sizeX = sizeX;
			this.sizeY = sizeY;
			this.sizeZ = sizeZ;
			this.sizeC = sizeC;
			this.sizeT = sizeT;
			this.datatype = datatype;
		}
		
		/**
		 * Class toString() method
		 * @return a String representation of the object
		 */
		public String toString() {
			return name;
		}
		
		/**
		 * Return a String with the Image attributes
		 * @return Comma-separated String with attribute key: value pairs.
		 */		
		public String attributesToString() {
			return ("name: " + name + ", x: " + sizeX + ", y: " + sizeY + 
					", z: " + sizeZ + ", c: " + sizeC + ", t: " + sizeT + 
					", data type: " + datatype);
		}
	}
}