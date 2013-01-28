package ch.eth.scu.importer.gui.editors.data;

import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import ch.eth.scu.importer.gui.viewers.ObserverActionParameters;
import ch.eth.scu.importer.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.gui.viewers.openbis.OpenBISViewer;

/**
 * Abstract editor for processors
 * @author Aaron Ponti
 */
abstract public class AbstractEditor implements ActionListener, Observer {

	protected JPanel panel;
	
	protected AbstractViewer dataViewer;
	protected OpenBISViewer openBISViewer;
	
	/**
	 * Constructor
	 * 
	 * The inheriting classes must call this constructor.
	 *  
	 * @param dataViewer Reference to the viewer
	 * @param openBISViewer Reference to the openBIS viewer
	 */
	public AbstractEditor(AbstractViewer dataViewer, 
			OpenBISViewer openBISViewer) {
		
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
        // Get the ObserverAction
        ObserverActionParameters observerActionParams = (ObserverActionParameters) arg;

        // Perform the correct action
        switch (observerActionParams.action) {
            case SCAN_COMPLETE:
                try {
                    render(observerActionParams);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            case EXPERIMENT_CHANGED:
                break;
            default:
                break;
        }
	}

	/**
	 * Function that creates and render all required widgets when both data 
	 * and openBIS viewers have notified that their data model is ready. 
	 * @throws Exception if some of the openBIS identifiers cannot be computed
	 */
	abstract public void render(ObserverActionParameters params) throws Exception;
	
	/**
	 * Return the reference to the JPanel to be added to a container component
	 * @return JPanel reference
	 */
	public JPanel getPanel() {
		return panel;
	}

}
