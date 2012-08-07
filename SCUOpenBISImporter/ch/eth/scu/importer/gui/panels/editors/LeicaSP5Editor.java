package ch.eth.scu.importer.gui.panels.editors;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Observable;

import ch.eth.scu.importer.gui.panels.openbis.OpenBISSpaceViewer;
import ch.eth.scu.importer.gui.panels.viewers.AbstractViewer;

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

		if (dataViewer.isReady() == false || openBISViewer.isReady() == false) {
			System.out.println("Noth both viewers ready.");
			return;
		}
		System.out.println("Both viewers ready.");
	}
	
	/**
	 * ActionPerformed method from the ActionListener interface
	 * @param e The ActionEvent object
	 */
	public void actionPerformed(ActionEvent e) {
    }

	@Override
	public void update(Observable obs, Object arg) {
		render();
	}
}
