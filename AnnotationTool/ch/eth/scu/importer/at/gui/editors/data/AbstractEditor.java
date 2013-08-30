package ch.eth.scu.importer.at.gui.editors.data;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.tree.TreeModel;

import ch.eth.scu.importer.at.gui.editors.data.model.AbstractMetadataMapper;
import ch.eth.scu.importer.at.gui.viewers.ObserverActionParameters;
import ch.eth.scu.importer.at.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.at.gui.viewers.openbis.OpenBISViewer;
import ch.eth.scu.importer.at.gui.viewers.openbis.model.AbstractOpenBISNode;
import ch.eth.scu.importer.at.gui.viewers.openbis.model.OpenBISProjectNode;
import ch.eth.scu.importer.ui_elements.lsrfortessa.gui.editors.data.model.BDLSRFortessaFCSMetadata;

/**
 * Abstract editor for processors
 * @author Aaron Ponti
 */
abstract public class AbstractEditor implements ActionListener, Observer {

	protected JPanel panel;
	
	/**
	 * References to the data viewer and model  
	 */
	protected AbstractViewer dataViewer;
	protected TreeModel dataModel;
	
	/**
	 * References to the openBIS viewer and model
	 */
	protected OpenBISViewer openBISViewer;
	protected TreeModel openBISModel;
	
	// List of openBISProjects
	protected List<OpenBISProjectNode> openBISProjects = 
			new ArrayList<OpenBISProjectNode>();
	
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
		panel.setMinimumSize(new Dimension(400, 700));
		panel.setPreferredSize(new Dimension(400, 700));
		
	}

	/**
	 * Clear elements from the editor.
	 */
	protected void clearUIElements() {
		// Remove elements and force a redraw of the panel
		if (panel.getComponentCount() > 0) {
			panel.removeAll();
			panel.validate();
			panel.repaint();
		}	
	}
	
	/**
	 * Resets the metadata mapping
	 */
	abstract protected void resetMetadata(ObserverActionParameters p);
	
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
        		case ABOUT_TO_RESCAN:
        			try {
        				resetMetadata(observerActionParams);
                } catch (Exception e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                }
                break;
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
                	updateUIElements(observerActionParams);
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
	 * Collects and stores openBIS projects for mapping
	 * @return list of openBIS nodes
	 */
	protected void storeOpenBISProjects() {
		
		// Store the openBIS model
		openBISModel = openBISViewer.getDataModel();
		
        // We extract all projects from the openBIS model and create a list
		// with which we will then create JComboBox associated to each project
		// from the data model
		openBISProjects = new ArrayList<OpenBISProjectNode>();
		
		AbstractOpenBISNode openBISRoot = 
				(AbstractOpenBISNode) openBISModel.getRoot();

		// Iterate over the space nodes (there should be at least one)
		for (int i = 0; i < openBISRoot.getChildCount(); i++) {

			// Get the Space
			AbstractOpenBISNode openBISSpaceNode = 
					(AbstractOpenBISNode) openBISRoot.getChildAt(i);

			// Go over the child Projects
			int n = openBISSpaceNode.getChildCount();

			for (int j = 0; j < n; j++) {

				// Get the OpenBISProjectNode
				OpenBISProjectNode openBISProjectNode = 
						(OpenBISProjectNode) openBISSpaceNode.getChildAt(j);

				// Add it to the list: we wrap it into a wrapper 
				// class to override the toString() method; we do 
				// this because in constrast to what happens in the
				// openBIS viewer, here we need the (openBIS) identifier
				//  instead of the code.
				openBISProjects.add(openBISProjectNode);

			}
		}
	}

	/**
	 * Function that maps the metadata information from the openBIS and
	 * data viewers (when they notify being ready) and creates and 
	 * renders all required UI widgets for metadata editing. 
	 * @throws Exception if some of the openBIS identifiers cannot be computed
	 */
	public void init(ObserverActionParameters params) throws Exception {
		
		// Make sure both viewers have completed their models
		if (!openBISViewer.isReady() || !dataViewer.isReady()) {
			return;
		}
		
		// Clear the editor
		clearUIElements();
		
		// Init the metadata
		if (initMetadata()) {
			
			// Create the widgets
			createUIElements(params);

		}
		
	}

	/**
	 * Creates a list of MetadataMappers used to map data entities to
	 * openBIS entities (with optionally additional metadata information
	 * the user will provide through UI elements in the editor).
	 * 
	 * @see AbstractMetadataMapper
	 * @throws Exception 
	 */
	abstract protected boolean initMetadata();

	/**
	 * Renders all widgets on the panel
	 * @throws Exception if some openBIS identifiers cannot be computed
	 */
	abstract protected void createUIElements(ObserverActionParameters params) throws Exception;

	/**
	 * Updates all widgets on the panel
	 * @throws Exception if some openBIS identifiers cannot be computed
	 */
	abstract protected void updateUIElements(ObserverActionParameters params);
	
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
