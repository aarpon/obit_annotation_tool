package ch.ethz.scu.obit.processors.data.model;

import java.io.File;
import java.util.Arrays;


/**
 * @author Aaron Ponti
 * Experiment descriptor.
 *
 */
public class ExperimentDescriptor extends PathAwareDescriptor{

	/**
	 *  List of accepted attachment file extensions
	 */
	protected final static String[] validAttachmentExtensions = 
		{".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx"};

	/** 
	 * Constructor
	 * @param fullPath Full path to the experiment folder.
	 * @param userRootDataPath Full path to the root of the user folder.
	 */
	public ExperimentDescriptor(File fullPath, File userRootDataPath) {
		super(fullPath, userRootDataPath);
	}

	/**
	 * Return a simplified class name to use in XML.
	 * @return simplified class name.
	 */
	@Override		
	public String getType() {
		return "Experiment";
	}

	/**
	 * Returns the properties file name with relative path 
	 * @return the properties file name with relative path
	 */
	public String getPropertiesFileNameWithRelPath() {
		return (getRelativePath() + File.separator +
				getName() + "_properties.oix");
	}

	/**
	 * Add the relative file path of the attachment to the attributes.
	 * @param attachment A file object.
	 * @return true if the attachment could be added; false otherwise.
	 */
	public boolean addAttachment(File attachment) {
		
		// Create attachment string
		String attachmentAttr = "";
		
		// Get current attachments
		if (attributes.containsKey("attachments")) {
			attachmentAttr = attributes.get("attachments");
		}
		
		// Build the attachment string
		if (attachment.getAbsolutePath().startsWith(fullPath.getAbsolutePath())) {
			
			// Check that the attachment is contained in the Experiment
			String filePath = attachment.getAbsolutePath().replace("\\", "/");
			int indx = filePath.lastIndexOf(
					relativePath.replace("\\", "/"));
			if (indx == -1) {
				return false;
			}
				
			// Append the relative attachment path to the semicolon-
			// separated path string
			String relAttachmentPath = filePath.substring(indx); 
			if (attachmentAttr.equals("")) {
				attachmentAttr = relAttachmentPath;
			} else {
				attachmentAttr += ";" + relAttachmentPath; 
			}
				
		}
		
		// Store the attachments as attributes
		attributes.put("attachments", attachmentAttr);
		
		// Return success
		return true;
	}
	
	/**
	 * Checks whether the passed file can be attached.
	 * @param file File to be checked.
	 * @return always false! The classes that inherit from AbstractProcessor 
	 * (and support attachments) should override this method. 
	 */
	public static boolean isValidAttachment(File file) {

		// Extract the file extension (lower case)
		String fileName = file.getName();
		int indx = fileName.lastIndexOf(".");
		if (indx == -1) {
			return false;
		}
		String ext = fileName.substring(indx).toLowerCase();

		// Check whether the file is a valid attachment
		return Arrays.asList(validAttachmentExtensions).contains(ext);

	}

}
