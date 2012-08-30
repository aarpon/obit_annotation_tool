package ch.eth.scu.importer.gui.editors.data;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import ch.eth.scu.importer.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.gui.viewers.openbis.OpenBISSpaceViewer;

/**
 * Metadata editor panel.
 * @author Aaron Ponti
 */
public class LeicaSP5Editor extends AbstractEditor {

	/**
	 * Constructor
	 */
	public LeicaSP5Editor(AbstractViewer dataViewer, 
			OpenBISSpaceViewer openBISViewer) {

		// Store the reference to the data and openBIS viewers
		super(dataViewer, openBISViewer);		

		// Set sizes
		panel.setMinimumSize(new Dimension(400, 700));
		panel.setPreferredSize(new Dimension(400, 700));
	}

	/**
	 * Renders all widgets on the panel
	 */
	public void render() {

		// Make sure both viewers have completed their data model
		if (dataViewer.isReady() == false || openBISViewer.isReady() == false) {
			return;
		}
	}
	
	/**
	 * ActionPerformed method from the ActionListener interface
	 * @param e The ActionEvent object
	 */
	public void actionPerformed(ActionEvent e) {
    }

}
