package ch.eth.scu.importer.gui.panels.editors;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Metadata editor panel.
 * @author Aaron Ponti
 */
public class BDLSRFortessaEditor extends AbstractEditor implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor
	 */
	public BDLSRFortessaEditor() {

		// Set sizes
		setMinimumSize(new Dimension(400, 700));
		setPreferredSize(new Dimension(400, 700));
	}

	/**
	 * ActionPerformed method from the ActionListener interface
	 * @param e The ActionEvent object
	 */
	public void actionPerformed(ActionEvent e) {
    }

}
