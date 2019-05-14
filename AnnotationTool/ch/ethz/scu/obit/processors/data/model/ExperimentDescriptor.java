package ch.ethz.scu.obit.processors.data.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Aaron Ponti
 * Experiment descriptor.
 *
 */
public class ExperimentDescriptor extends PathAwareDescriptor{

    /**
     *  Experiment description
     */
    public String description = "";

    /**
     *  Experiment tags (comma-separated list)
     */
    public List<Tag> tags = new ArrayList<Tag>();

    /**
     *  List of accepted attachment file extensions
     */
    protected final static String[] validAttachmentExtensions =
        {".pdf", ".doc", ".docx", ".xls", ".xlsx",
                ".ppt", ".pptx", ".csv", ".tsv", ".xps",
        ".wps"};

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

        // openBIS only supports attachments with file name of 100 characters or less
        if (attachment.getName().length() >= 100) {
            return false;
        }

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


    /**
     * Return the list of Tags as comma-separated string.
     * @return string of comma-separated tags.
     */
    public String getTagList() {

        // Build the string with a buffer
        StringBuffer buff = new StringBuffer();

        for (Tag tag : tags) {
            buff.append(tag.toString());
            buff.append(" ");
        }

        // Return the string
        return buff.toString().trim().replaceAll(" ", ", ");

    }

    /**
     * Return the list of Tag identifiers as comma-separated string.
     * @return string of comma-separated tags.
     */
    public String getTagIdentifierList() {

        // Build the string with a buffer
        StringBuffer buff = new StringBuffer();

        for (Tag tag : tags) {
            buff.append(tag.getIdentifier());
            buff.append(" ");
        }

        // Return the string
        return buff.toString().trim().replaceAll(" ", ", ");

    }

}
