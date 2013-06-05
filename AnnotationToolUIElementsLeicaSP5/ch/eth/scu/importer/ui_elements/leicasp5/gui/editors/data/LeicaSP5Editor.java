package ch.eth.scu.importer.ui_elements.leicasp5.gui.editors.data;

import java.awt.event.ActionEvent;

import ch.eth.scu.importer.at.gui.editors.data.AbstractEditor;
import ch.eth.scu.importer.at.gui.viewers.ObserverActionParameters;
import ch.eth.scu.importer.at.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.at.gui.viewers.openbis.OpenBISViewer;

/**
 * Metadata editor panel.
 * @author Aaron Ponti
 */
public class LeicaSP5Editor extends AbstractEditor {

	/**
	 * Constructor
	 */
	public LeicaSP5Editor(AbstractViewer dataViewer, 
			OpenBISViewer openBISViewer) {

		// Store the reference to the data and openBIS viewers
		super(dataViewer, openBISViewer);		

	}

	/**
	 * ActionPerformed method from the ActionListener interface
	 * @param e The ActionEvent object
	 */
	public void actionPerformed(ActionEvent e) {
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
		return true;
	}

}
