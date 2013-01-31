package ch.eth.scu.importer.gui.editors.data;

import java.awt.Dimension;
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
		
		// Set the preferred and minimum size
		panel.setMinimumSize(new Dimension(450, 700));
		panel.setPreferredSize(new Dimension(450, 700));
		
	}

	/**
	 * Observer update method
	 * @param obs Observable object
	 * @param arg Argument
	 */
	public void update(Observable obs, Object arg) {
        // Get the ObserverAction
        ObserverActionParameters observerActionParams = 
        		(ObserverActionParameters) arg;

        // Perform the correct action
        switch (observerActionParams.action) {
            case SCAN_COMPLETE:
                try {
                    init(observerActionParams);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            case EXPERIMENT_CHANGED:
                try {
                	updateAll(observerActionParams);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
	}

	/**
	 * Function that maps the metadata information from the openBIS and
	 * data viewers (when they notify being ready) and creates and 
	 * renders all required UI widgets for metadata editing. 
	 * @throws Exception if some of the openBIS identifiers cannot be computed
	 */
	abstract public void init(ObserverActionParameters params) throws Exception;
	
	/**
	 * Function that updates the metadata and the UI widgets when the user
	 * edits something in the editor. 
	 */
	abstract public void updateAll(ObserverActionParameters observerActionParams);
	
	/**
	 * Once the metadata has been completely filled, this method makes
	 * sure to update the data model (generated by the Processor) to be
	 * ready to be saved to the XML properties file. 
	 */
	abstract public boolean updateDataModel();
	
	/**
	 * Return the reference to the JPanel to be added to a container component
	 * @return JPanel reference
	 */
	public JPanel getPanel() {
		return panel;
	}

}
