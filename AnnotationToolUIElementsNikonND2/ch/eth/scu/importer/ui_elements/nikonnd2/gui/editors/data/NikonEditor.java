package ch.eth.scu.importer.ui_elements.nikonnd2.gui.editors.data;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import ch.eth.scu.importer.at.gui.editors.data.AbstractEditor;
import ch.eth.scu.importer.at.gui.viewers.ObserverActionParameters;
import ch.eth.scu.importer.at.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.at.gui.viewers.openbis.OpenBISViewer;

/**
 * Metadata editor panel.
 * @author Aaron Ponti
 *
 */
public class NikonEditor extends AbstractEditor {

	/**
	 * Constructor
	 */
	public NikonEditor(AbstractViewer dataViewer, 
			OpenBISViewer openBISViewer) {

		// Store the reference to the data and openBIS viewers
		super(dataViewer, openBISViewer);

		// Create a GridBagLayout
		GridBagLayout gridBagLayout = new GridBagLayout();
		panel.setLayout(gridBagLayout);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(ObserverActionParameters params) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateAll(ObserverActionParameters observerActionParams) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean updateDataModel() {
		// TODO Auto-generated method stub
		return false;
	}

}
