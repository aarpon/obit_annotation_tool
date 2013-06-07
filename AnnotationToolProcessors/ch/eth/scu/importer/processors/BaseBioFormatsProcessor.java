/**
 * 
 */
package ch.eth.scu.importer.processors;

import java.io.IOException;

import ch.eth.scu.importer.processors.validator.GenericValidator;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;

/**
 * A generic bio-formats based file processor to be specialized for the 
 * different, concrete file formats.
 * 
 * Please mind that the BaseBioFormatsProcessor sets up all is needed
 * to be able to parse the file, but does not parse! This is delegated
 * to the specialized classes.
 *  
 * @author Aaron Ponti
 *
 */
public class BaseBioFormatsProcessor extends AbstractProcessor {

	/* Protected instance variables */
	protected String filename;
	protected ImageProcessorReader reader;

	protected ServiceFactory factory;
	protected OMEXMLService service;
	protected IMetadata omexmlMeta;

	/**
	 * Constructor
	 */
	public BaseBioFormatsProcessor(String filename) {
		this.filename = filename;
		
		// Instantiate the validator
		validator = new GenericValidator();
	}

	/**
	 * Initialize the reader and sets up the OMEXML metadata store
	 * @throws DependencyException 
	 * @throws ServiceException 
	 */
	public boolean initReader() {

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
		      reader.setId(filename);
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
	 * Parse the file
	 * 
	 * Please mind that the BaseBioFormatsProcessor sets up all is needed
	 * to be able to parse the file, but does not parse! This is delegated 
	 * to specialized classes. 
	 */
	@Override
	public boolean parse() {
		return false;
	}

	/**
	 * Return information regarding the file format.
	 * @return descriptive String for the Processor.
	 */
	public String info() {
		return "Base bio-formats file reader. " + 
	"To be extended for concrete types.";
	}

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override	
	public String getType() {
		return "BaseBioFormats";
	}

}
