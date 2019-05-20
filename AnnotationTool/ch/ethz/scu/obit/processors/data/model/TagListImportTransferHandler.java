package ch.ethz.scu.obit.processors.data.model;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.TransferHandler;

import ch.ethz.scu.obit.at.gui.editors.data.AbstractEditor;

public class TagListImportTransferHandler extends TransferHandler {

    private static final long serialVersionUID = 1L;

    private AbstractEditor editor = null;

    // DataFlavor to import
    public static final DataFlavor SUPPORTED_DATE_FLAVOR =
            new DataFlavor(List.class, "TagListTransferable");


    public TagListImportTransferHandler(AbstractEditor editor) {
        this.editor = editor;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {

        DataFlavor[] flavors = support.getDataFlavors();
        if (flavors.length == 0) {
            return false;
        }
        return (flavors[0].getHumanPresentableName().equals("TagListTransferable"));
    }

    // Import and format the data
    @Override
    public boolean importData(TransferHandler.TransferSupport support) {

        // Only if we are dropping something onto the field
        if (!support.isDrop()) {
            return false;
        }

        if (editor == null) {
            return false;
        }

        // And only if it is a string flavor
        if (!support.isDataFlavorSupported(SUPPORTED_DATE_FLAVOR)) {
            return false;
        }

        // Get the transferred Tags
        try {

            // Get the transferred objects
            Transferable t = support.getTransferable();
            Object value = t.getTransferData(SUPPORTED_DATE_FLAVOR);

            if (value instanceof ArrayList<?>) {

                // Get the project from the editor
                String projectIdentifier = editor.getCurrentProjectIdentifier();

                // Check that the SPACE of the source and target are the same.
                // It is enough to test the first tag.
                List<Tag> listOfTags = (List<Tag>) value;

                if (listOfTags.size() == 0) {
                    return false;
                }

                Tag tag = listOfTags.get(0);
                String tagIdentifier = tag.getIdentifier();

                // Compare the spaces
                int projectIndex = projectIdentifier.substring(1).indexOf("/");
                int tagIndex = tagIdentifier.substring(1).indexOf("/");

                String projectSpace = projectIdentifier.substring(1, projectIndex + 1);
                String tagSpace = tagIdentifier.substring(1, tagIndex + 1);
                if (projectIndex == -1 || tagIndex == -1 ||
                        projectIndex != tagIndex ||
                        !projectSpace.equals(tagSpace)) {

                    String tagString = listOfTags.size() == 1 ?
                            "\u2022 The Tag belongs to " + tagSpace :
                                "\u2022 The Tags belong to " + tagSpace;

                    // Spaces do not match: display the error message in the output pane
                    String errorMessage = "The tags and the selected project must belong " +
                            "to the same space!\n" +
                            "\u2022 The assigned Project belongs to " + projectSpace + ".\n" +
                            tagString + ".\n\n" +
                            "Select " + projectSpace + " in the openBIS panel on the right to\n" +
                            "display the list of tags belonging to it.";

                    // Display an error dialog to make it clear
                    JOptionPane.showMessageDialog(null, errorMessage,
                            "Cannot assign tags!", JOptionPane.ERROR_MESSAGE);

                    // Return failure.
                    return false;
                }

                // Append the tags
                editor.appendTags(listOfTags);

                // Return success.
                return true;
            }

        } catch (UnsupportedFlavorException | IOException e) {

            // Return failure.
            return false;
        }

        // Return success
        return true;
    }

}
