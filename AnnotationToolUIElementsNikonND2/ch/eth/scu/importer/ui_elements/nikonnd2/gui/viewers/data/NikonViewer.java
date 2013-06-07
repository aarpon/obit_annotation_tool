package ch.eth.scu.importer.ui_elements.nikonnd2.gui.viewers.data;

import java.awt.event.ActionEvent;

import javax.swing.event.TreeSelectionEvent;

import ch.eth.scu.importer.at.gui.viewers.data.AbstractViewer;

/**
 * Simple graphical viewer for the NikonProcessor.
 * @author Aaron Ponti
 */
public class NikonViewer extends AbstractViewer {

	/**
	 * Constructor
	 */
	public NikonViewer() {
		
		// Call the AbstractViewer's constructor (to create the panel)
		super();

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		// TODO Auto-generated method stub

	}

	/**
	 * Scans the user subfolder of the datamover incoming directory for
	 * datasets to be processed
	 */
	public void scan() {
		// TODO Auto-generated method stub

	}

}
