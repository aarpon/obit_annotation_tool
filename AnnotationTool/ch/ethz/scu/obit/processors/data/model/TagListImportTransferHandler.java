package ch.ethz.scu.obit.processors.data.model;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    // Import and format the data    @Override
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

            if (value instanceof ArrayList) {

                // Cast the object to a list of tags
                editor.appendTags((List<Tag>) value);
            }

        } catch (UnsupportedFlavorException | IOException e) {

            return false;
        }

        // Return success
        return true;
    }

}
