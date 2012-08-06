package ch.eth.scu.importer.gui.panels.editors;

import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Abstract editor for processors
 * @author Aaron Ponti
 */
abstract public class AbstractEditor extends JPanel implements Observer {

	private static final long serialVersionUID = 1L;

	public JButton uploadButton;
	
	abstract public boolean uploadToOpenBIS();

}
