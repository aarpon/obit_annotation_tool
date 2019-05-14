package ch.ethz.scu.obit.processors.data.model;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

public class TagListExportTransferHandler extends TransferHandler {

    private static final long serialVersionUID = 1L;

    JList<Tag> component = null;

    public TagListExportTransferHandler(JList<Tag> component) {
        this.component = component;
    }

    public List<Tag> getValues() {
        return component.getSelectedValuesList();
    }

    @Override
    public int getSourceActions(JComponent c) {
        return DnDConstants.ACTION_COPY;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        Transferable t = new TagListTransferable(getValues());
        return t;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        super.exportDone(source, data, action);
    }
}
