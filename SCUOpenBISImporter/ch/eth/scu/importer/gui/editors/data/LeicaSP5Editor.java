package ch.eth.scu.importer.gui.editors.data;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import ch.eth.scu.importer.gui.viewers.ObserverActionParameters;
import ch.eth.scu.importer.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.gui.viewers.openbis.OpenBISViewer;

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

		// Set sizes
		panel.setMinimumSize(new Dimension(400, 700));
		panel.setPreferredSize(new Dimension(400, 700));
	}

	/**
	 * Renders all widgets on the panel
	 */
	public void render(ObserverActionParameters params) {

		// Make sure both viewers have completed their data model
		if (!dataViewer.isReady() || !openBISViewer.isReady()) {
			return;
		}

		// TODO Implement!
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

}
