package ch.eth.scu.importer.lsrfortessa.gui.viewers.data;

import ch.eth.scu.importer.at.gui.viewers.ObserverActionParameters;
import ch.eth.scu.importer.at.gui.viewers.data.AbstractViewer;
import ch.eth.scu.importer.at.gui.viewers.data.model.AbstractNode;
import ch.eth.scu.importer.at.gui.viewers.data.model.RootNode;
import ch.eth.scu.importer.lsrfortessa.gui.viewers.data.model.ExperimentNode;
import ch.eth.scu.importer.lsrfortessa.gui.viewers.data.model.FCSFileNode;
import ch.eth.scu.importer.lsrfortessa.gui.viewers.data.model.SpecimenNode;
import ch.eth.scu.importer.lsrfortessa.gui.viewers.data.model.TrayNode;
import ch.eth.scu.importer.lsrfortessa.gui.viewers.data.model.TubeNode;
import ch.eth.scu.importer.lsrfortessa.gui.viewers.data.model.WellNode;
import ch.eth.scu.importer.lsrfortessa.processors.BDLSRFortessaFCSProcessor;
import ch.eth.scu.importer.lsrfortessa.processors.BDLSRFortessaFCSProcessor.Experiment;
import ch.eth.scu.importer.lsrfortessa.processors.BDLSRFortessaFCSProcessor.Specimen;
import ch.eth.scu.importer.lsrfortessa.processors.BDLSRFortessaFCSProcessor.Tray;
import ch.eth.scu.importer.lsrfortessa.processors.BDLSRFortessaFCSProcessor.Tube;
import ch.eth.scu.importer.lsrfortessa.processors.BDLSRFortessaFCSProcessor.Well;
import ch.eth.scu.importer.lsrfortessa.readers.FCSReader;

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
	}

	/**
	 *  Parse the FCS folder and append the resulting tree to the root
	 */
	public boolean parse(File userFolder) {

		// Process the user folder
		BDLSRFortessaFCSProcessor divafcsprocessor;
		try {
			divafcsprocessor = new BDLSRFortessaFCSProcessor(
					userFolder.getCanonicalPath());
		} catch (IOException e) {
			outputPane.err("Could not parse the folder " + userFolder + "!");
			return false;
		}

		// We parse. If parsing fails, we just return (the dataset is invalid).
		if (!divafcsprocessor.parse()) {
			outputPane.err("Could not parse the folder " + userFolder + "!");
			divafcsprocessor = null;
			return false;
		}

		// Make sure we have a valid dataset
		if (!divafcsprocessor.validator.isValid) {
			DefaultTableModel model = 
					(DefaultTableModel) invalidDatasetsTable.getModel();
			for (File file : divafcsprocessor.validator.invalidFilesOrFolders.keySet()) {
				String filePath;
				try {
					filePath = file.getCanonicalPath();
					// Build a relative path
					int indx = filePath.indexOf(userName);
					if (indx != -1) {
						filePath = filePath.substring(indx);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					filePath = "Unknown";
				}
				model.addRow(new Object[] {filePath,
						divafcsprocessor.validator.invalidFilesOrFolders.get(file)});
			}
			return false;
		}

		// We will append the experiment nodes directly to the root node
		createNodes((RootNode)rootNode, divafcsprocessor.folderDescriptor);
		
		// Inform the user
		outputPane.log("Successfully processed user data folder.");
		return true;
	}

	/**
	 * Called when selection in the Tree View is changed.
	 * @param e A TreeSelectionEvent
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {

		// Get selected node		
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
		if (className.equals("Folder")) {
			clearMetadataTable();
			// Change to first experiment in the folder
			if (node.getChildCount() == 0) {
				return;
			}
			// Update the reference to the first contained experiment
			// to force a refresh of the editor when a new folder is
			// chosen
			node = (AbstractNode)node.getChildAt(0);
		} else if (className.equals("Experiment")) {
			clearMetadataTable();
			addAttributesToMetadataTable(
                    ((BDLSRFortessaFCSProcessor.Experiment)
                            nodeInfo).getAttributes());
		} else if (className.equals("Tray")) {
			clearMetadataTable();
			addAttributesToMetadataTable(
					((BDLSRFortessaFCSProcessor.Tray) 
							nodeInfo).getAttributes());
		} else if (className.equals("Specimen")) {
			clearMetadataTable();
			addAttributesToMetadataTable(
					((BDLSRFortessaFCSProcessor.Specimen)
							nodeInfo).getAttributes());
		} else if (className.equals("Tube")) {
			clearMetadataTable();
			addAttributesToMetadataTable(
					((BDLSRFortessaFCSProcessor.Tube)
							nodeInfo).getAttributes());
		} else if (className.equals("Well")) {
			clearMetadataTable();
			addAttributesToMetadataTable(
					((BDLSRFortessaFCSProcessor.Well)
							nodeInfo).getAttributes());
		} else if (className.equals("FCSFile")) {
			// Cast
			BDLSRFortessaFCSProcessor.FCSFile fcsFile =
					(BDLSRFortessaFCSProcessor.FCSFile) nodeInfo;
			FCSReader fcs = new FCSReader(
					new File(fcsFile.getFullPath()), false);
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

        // Get the experiment name
        AbstractNode expNode = getParentNodeByName(node, "Experiment");
        if (expNode != null && expNode != lastSelectedExperiment) {
        		
        		// Update the lastSelectedFolder property
        		lastSelectedExperiment = expNode;
        		
            // Notify the editor to update its view
            setChanged();
            notifyObservers(new ObserverActionParameters(
                ObserverActionParameters.Action.EXPERIMENT_CHANGED,
                expNode));
        }
	}

    /**
         * Create the nodes for the tree
         * @param top Root node for the tree
         * @param folderDescriptor A folder descriptor object.
         */
	protected void createNodes(RootNode top,
			BDLSRFortessaFCSProcessor.UserFolder folderDescriptor) {
		
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
