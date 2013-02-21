package ch.eth.scu.importer.processors.leicasp5;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;

import ch.eth.scu.importer.processors.AbstractProcessor;
import ch.eth.scu.importer.processors.model.AbstractDescriptor;
import ch.eth.scu.importer.processors.model.FirstLevelDescriptor;
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
 * @author Aaron Ponti
 */
public class LeicaLifProcessor extends AbstractProcessor {

	/* Private instance variables */
	private String filename;
	private ImageProcessorReader reader;

	/* Public instance variables */
	public ImageDescriptor image;
	
	/**
	 * ArrayList of SubImage's
	 */
	
	/**
	 * Constructor 
	 * @param filename Name with full path of the file to be opened.
	 */
	public LeicaLifProcessor(String filename) {
		this.filename = filename;
	}
	
	@Override
	public boolean parse() throws DependencyException, ServiceException {

		// Create a new image
		image = new ImageDescriptor(filename);

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
		
		// Get and store all subimages
		for (int i = 0; i < numSeries; i++) {
			reader.setSeries(i);
			image.addSubImage(new SubImageDescriptor(omexmlMeta.getImageName(i),
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
		return (new File(filename)).getName();
	}

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplidied class name.
	 */
	@Override	
	public String getType() {
		return "LeicaLIF";
	}
	
	/**
	 * Image class
	 * @author Aaron Ponti
	 *
	 */
	public class ImageDescriptor extends FirstLevelDescriptor {

		/**
		 * SubImages
		 */
		public ArrayList<SubImageDescriptor> subImages =
				new ArrayList<SubImageDescriptor>();

		/**
		 * Constructor
		 */
		public ImageDescriptor(String filename) {
			this.name = new File(filename).getName();
			
			// Set the output name
			String outputNameFile = (new File(name)).getName();
			int indx = outputNameFile.lastIndexOf(".");
			if (indx != -1) {
				outputNameFile = outputNameFile.substring(0, indx);
			}
		}
		
		/**
		 * Add a subimage
		 */		
		public void addSubImage(SubImageDescriptor subImage) {
			subImages.add(subImage);
		}

		/**
		 * Return a simplified class name to use in the viewers.
		 * @return simplified class name.
		 */
		@Override		
		public String getType() {
			return "Image";
		}

		@Override
		public String getRelativePath() {
			// TODO IMPLEMENT THIS!
			return null;
		}
	}

	/**
	 * SubImage class
	 * @author Aaron Ponti
	 *
	 */
	public class SubImageDescriptor extends AbstractDescriptor {
		
		/**
		 * Constructor
		 */
		public SubImageDescriptor(String name, int sizeX, int sizeY, int sizeZ,
				int sizeC, int sizeT, String datatype) {
			
			// Set the name
			this.name = name;
			
			// Set the attributes
			attributes.put("sizeX", Integer.toString(sizeX));
			attributes.put("sizeY", Integer.toString(sizeY));
			attributes.put("sizeZ", Integer.toString(sizeZ));
			attributes.put("sizeC", Integer.toString(sizeC));
			attributes.put("sizeT", Integer.toString(sizeT));
			attributes.put("datatype", datatype);
			
		}

		/**
		 * Return a simplified class name to use in the viewers.
		 * @return simplified class name.
		 */
		@Override		
		public String getType() {
			return "SubImage";
		}
	}
	
}