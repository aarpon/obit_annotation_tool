package ch.eth.scu.importer.ui_elements.lsrfortessa.gui.viewers.data;

import ch.eth.scu.importer.at.gui.viewers.ObserverActionParameters;
import ch.eth.scu.importer.at.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.at.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.processors.lsrfortessa.BDFACSDIVAFCSProcessor;
import ch.eth.scu.importer.processors.lsrfortessa.BDFACSDIVAFCSProcessor.Well;
import ch.eth.scu.importer.processors.lsrfortessa.FCSProcessor;
import ch.eth.scu.importer.processors.lsrfortessa.BDFACSDIVAFCSProcessor.Experiment;
import ch.eth.scu.importer.processors.lsrfortessa.BDFACSDIVAFCSProcessor.Specimen;
import ch.eth.scu.importer.processors.lsrfortessa.BDFACSDIVAFCSProcessor.Tray;
import ch.eth.scu.importer.processors.lsrfortessa.BDFACSDIVAFCSProcessor.Tube;
import ch.eth.scu.importer.ui_elements.lsrfortessa.gui.viewers.data.model.ExperimentNode;
import ch.eth.scu.importer.ui_elements.lsrfortessa.gui.viewers.data.model.FCSFileNode;
import ch.eth.scu.importer.ui_elements.lsrfortessa.gui.viewers.data.model.FolderNode;
import ch.eth.scu.importer.ui_elements.lsrfortessa.gui.viewers.data.model.SpecimenNode;
import ch.eth.scu.importer.ui_elements.lsrfortessa.gui.viewers.data.model.TrayNode;
import ch.eth.scu.importer.ui_elements.lsrfortessa.gui.viewers.data.model.TubeNode;
import ch.eth.scu.importer.ui_elements.lsrfortessa.gui.viewers.data.model.WellNode;

import java.awt.event.*;

import javax.swing.table.DefaultTableModel;
import javax.swing.event.*;
import java.io.File;
import java.io.IOException;

/**
 * Simple graphical viewer for the BDFACSDIVAFCSProcessor
 * @author Aaron Ponti
 */
public class BDLSRFortessaFCSViewer extends AbstractViewer {

	/**
	 * Constructor
	 */
	public BDLSRFortessaFCSViewer() {
		
		// Call the AbstractViewer's constructor (to create the panel)
		super();

	}

	/**
	 *  Parse the FCS folder and append the resulting tree to the root
	 */
	public boolean parse(File folder, String userName) {

		// Process the file or folder
		BDFACSDIVAFCSProcessor divafcsprocessor;
		try {
			divafcsprocessor = new BDFACSDIVAFCSProcessor(
					folder.getCanonicalPath(), userName);
		} catch (IOException e) {
			outputPane.err("Could not parse the folder " + folder + "!");
			return false;
		}

		// We parse. If parsing fails, we just return (the dataset is invalid).
		if (!divafcsprocessor.parse()) {
			outputPane.err("Could not parse the folder " + folder + "!");
			divafcsprocessor = null;
			return false;
		}

		// If the subfolder is already annotated, we skip it (but still
		// we return success)
		if (divafcsprocessor.validator.isAnnotated) {
			outputPane.log("Dataset \"" + divafcsprocessor.folderDescriptor + 
					"\" is annotated.");
			return true;
		}

		// Make sure we have a valid dataset
		if (!divafcsprocessor.validator.isValid) {
			DefaultTableModel model = 
					(DefaultTableModel) invalidDatasetsTable.getModel();
			int nError = 0;
			StringBuilder err = new StringBuilder("");
			for (String errorString : divafcsprocessor.validator.errorMessages) {
				nError++;
				err.append("(").append(nError).append(") ").append(errorString);
			}
			model.addRow(new Object[] {folder.getName(), err});
			outputPane.err("Dataset \"" + 
				divafcsprocessor.folderDescriptor + 
				"\" failed validation. Please fix or remove.");
			return false;
		}

		// Create a folder note as a child of the root node
		FolderNode folderNode = 
				new FolderNode(divafcsprocessor.folderDescriptor);
		rootNode.add(folderNode);
		
		// We will append the experiment nodes directly to the root node
		createNodes(folderNode, divafcsprocessor.folderDescriptor);
		
		// Inform the user
		outputPane.log("Successfully processed folder \"" + 
				divafcsprocessor.toString() + "\"");
		return true;
	}

	/**
	 * Called when selection in the Tree View is changed.
	 * @param e A TreeSelectionEvent
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		AbstractNode node = (AbstractNode) tree.getLastSelectedPathComponent();
		if (node == null) {
			return;
		}
		
		// The valuedChanged() method is called twice when the a node is
		// chosen in the tree. Workaround: do not process the same node 
		// twice in a row  
		if (node.toString().equals(lastSelectedNode)) {
			return;
		}
		lastSelectedNode = node.toString();
		
		// Get the node object
		Object nodeInfo = node.getUserObject();

		// Print the attributes
		String className = nodeInfo.getClass().getSimpleName();
		if (className.equals("Experiment")) {
			clearMetadataTable();
			addAttributesToMetadataTable(
                    ((BDFACSDIVAFCSProcessor.Experiment)
                            nodeInfo).getAttributes());
		} else if (className.equals("Tray")) {
			clearMetadataTable();
			addAttributesToMetadataTable(
					((BDFACSDIVAFCSProcessor.Tray) 
							nodeInfo).getAttributes());
		} else if (className.equals("Specimen")) {
			clearMetadataTable();
			addAttributesToMetadataTable(
					((BDFACSDIVAFCSProcessor.Specimen)
							nodeInfo).getAttributes());
		} else if (className.equals("Tube")) {
			clearMetadataTable();
			addAttributesToMetadataTable(
					((BDFACSDIVAFCSProcessor.Tube)
							nodeInfo).getAttributes());
		} else if (className.equals("Well")) {
			clearMetadataTable();
			addAttributesToMetadataTable(
					((BDFACSDIVAFCSProcessor.Well)
							nodeInfo).getAttributes());
		} else if (className.equals("FCSFile")) {
			// Cast
			BDFACSDIVAFCSProcessor.FCSFile fcsFile =
					(BDFACSDIVAFCSProcessor.FCSFile) nodeInfo;
			String fcsFileName = fcsFile.getFileName();
			FCSProcessor fcs = new FCSProcessor(fcsFileName, false);
			try {
				fcs.parse();
				clearMetadataTable();
				addAttributesToMetadataTable(fcs.getAllKeywords());
			} catch (IOException e1) {
				outputPane.err("Could not parse file " + fcsFile + "!");
			}
		} else {
			clearMetadataTable();
		}

        // Get the folder name
        AbstractNode folderNode = getFolderNode(node);
        if (folderNode != null && folderNode != lastSelectedFolder) {
        		
        		// Update the lastSelectedFolder property
        		lastSelectedFolder = folderNode;
        		
            // Notify the editor to update its view
            setChanged();
            notifyObservers(new ObserverActionParameters(
                ObserverActionParameters.Action.EXPERIMENT_CHANGED, folderNode));
        }
	}

    /**
     * Climb the JTree to find the folder node and return its name
     * @param selectedNode Node selected in the JTree
     * @return folder node name
     */
    protected AbstractNode getFolderNode(AbstractNode selectedNode) {

        // Get the class name of the selected node
        String className = selectedNode.getUserObject().getClass().getSimpleName();

        // Try to get the folder name
        if (className.equals("RootDescriptor")) {
            // We are above the folder node, we return ""
            return null;
        } else if (className.equals("Folder")) {
            // We are at the folder node , we return its name
            return selectedNode;
        } else {
            // We are somewhere below the folder node: we climb up the tree
            // until we find it and then return its name
            AbstractNode parentNode = (AbstractNode) selectedNode.getParent();
            while (parentNode != null) {
                // Are we at the folder node?
                if (parentNode.getUserObject().getClass().getSimpleName().equals("Folder")) {
                    return parentNode;
                } else {
                    parentNode = (AbstractNode) parentNode.getParent();
                }
            }
        }
        return null;
    }

    /**
         * Create the nodes for the tree
         * @param top Root node for the tree
         * @param folderDescriptor A folder descriptor object.
         */
	protected void createNodes(AbstractNode top,
			BDFACSDIVAFCSProcessor.Folder folderDescriptor) {
		
		ExperimentNode experiment;
		TrayNode tray;
		SpecimenNode specimen;
		TubeNode tube;
		WellNode well;
		FCSFileNode fcs;

		for (String expKey : folderDescriptor.experiments.keySet()) {

			// Get the ExperimentDescriptor
			Experiment e = 
					folderDescriptor.experiments.get(expKey);

			// Add the experiments
			experiment = new ExperimentNode(e);
			top.add(experiment);

			for (String trayKey: e.trays.keySet()) {

				// Get the TrayDescriptor
				Tray t  = e.trays.get(trayKey);
				
				// Add the trays
				tray = new TrayNode(t);
				experiment.add(tray);

				for (String specKey : t.specimens.keySet()) {

					// Get the SpecimenDescriptor
					Specimen s = t.specimens.get(specKey);
					
					// Add the specimens
					specimen = new SpecimenNode(s);
					tray.add(specimen);

					for (String wellKey : s.tubes.keySet()) {

						// Get the TubeDescriptor
						Well wl  = (Well) s.tubes.get(wellKey);
						
						// Add the tubes
						well = new WellNode(wl);
						specimen.add(well);
						
						// Add the fcs files
						fcs = new FCSFileNode(wl.fcsFile);
						well.add(fcs);
					}

				}

			}

			for (String specKey : e.specimens.keySet()) {

				// Get the SpecimenDescriptor
				Specimen s = e.specimens.get(specKey);
				
				// Add the specimens
				specimen = new SpecimenNode(s);
				experiment.add(specimen);

				for (String tubeKey : s.tubes.keySet()) {

					// Get the TubeDescriptor
					Tube tb = s.tubes.get(tubeKey);
					
					// Add the tubes
					tube = new TubeNode(tb);
					specimen.add(tube);

					// Add the fcs files
					fcs = new FCSFileNode(tb.fcsFile);
					tube.add(fcs);
				}

			}

		}
	}

	/**
	 *  React to actions
	 *  @param e An ActionEvent 
	 */
	public void actionPerformed(ActionEvent e) {
    }

}
