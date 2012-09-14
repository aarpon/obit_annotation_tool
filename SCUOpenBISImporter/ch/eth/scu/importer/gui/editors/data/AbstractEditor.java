package ch.eth.scu.importer.gui.editors.data;

import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import ch.eth.scu.importer.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.gui.viewers.openbis.OpenBISSpaceViewer;

/**
 * Abstract editor for processors
 * @author Aaron Ponti
 */
abstract public class AbstractEditor implements ActionListener, Observer {

	protected JPanel panel;
	
	protected AbstractViewer dataViewer;
	protected OpenBISSpaceViewer openBISViewer;
	
	/**
	 * Constructor
	 * 
	 * The inheriting classes must call this constructor.
	 *  
	 * @param dataViewer Reference to the viewer
	 * @param openBISViewer Reference to the openBIS viewer
	 */
	public AbstractEditor(AbstractViewer dataViewer, 
			OpenBISSpaceViewer openBISViewer) {
		
		panel = new JPanel();
		
		this.dataViewer = dataViewer;
		this.openBISViewer = openBISViewer;
	}

	/**
	 * Observer update method
	 * @param obs Observable object
	 * @param arg Argument
	 */
	public void update(Observable obs, Object arg) {
		try {
			render();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Function that creates and render all required widgets when both data 
	 * and openBIS viewers have notified that their data model is ready. 
	 * @throws Exception if some of the openBIS identifiers cannot be computed
	 */
	abstract public void render() throws Exception;
	
	/**
	 * Return the reference to the JPanel to be added to a container component
	 * @return JPanel reference
	 */
	public JPanel getPanel() {
		return panel;
	}

}