package ch.ethz.scu.obit.processors.data.model;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TagListTransferable implements Transferable {

    public static final DataFlavor[] SUPPORTED_DATE_FLAVORS = { new DataFlavor(List.class, "TagListTransferable") };

    public List<Tag> tags = new ArrayList<Tag>();
    public TagListTransferable(List<Tag> tags) {
        this.tags = tags;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return SUPPORTED_DATE_FLAVORS;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        // TODO Auto-generated method stub
        System.out.println(flavor);
        return true;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        // Return the list of Tag objects
        return tags;
    }
}
