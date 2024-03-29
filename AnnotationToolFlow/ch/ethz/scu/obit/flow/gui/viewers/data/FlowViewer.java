package ch.ethz.scu.obit.flow.gui.viewers.data;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.table.DefaultTableModel;

import ch.ethz.scu.obit.at.gui.viewers.ObserverActionParameters;
import ch.ethz.scu.obit.at.gui.viewers.data.AbstractViewer;
import ch.ethz.scu.obit.at.gui.viewers.data.model.AbstractNode;
import ch.ethz.scu.obit.at.gui.viewers.data.model.ExperimentNode;
import ch.ethz.scu.obit.at.gui.viewers.data.model.RootNode;
import ch.ethz.scu.obit.common.settings.GlobalSettingsManager;
import ch.ethz.scu.obit.flow.gui.viewers.data.model.FCSFileNode;
import ch.ethz.scu.obit.flow.gui.viewers.data.model.FCSFileParemeterListNode;
import ch.ethz.scu.obit.flow.gui.viewers.data.model.SpecimenNode;
import ch.ethz.scu.obit.flow.gui.viewers.data.model.TrayNode;
import ch.ethz.scu.obit.flow.gui.viewers.data.model.TubeNode;
import ch.ethz.scu.obit.flow.gui.viewers.data.model.WellNode;
import ch.ethz.scu.obit.flow.processors.data.AbstractFlowProcessor;
import ch.ethz.scu.obit.flow.processors.data.FlowProcessorFactory;
import ch.ethz.scu.obit.flow.processors.data.model.Experiment;
import ch.ethz.scu.obit.flow.processors.data.model.FCSFile;
import ch.ethz.scu.obit.flow.processors.data.model.FCSFileParameterList;
import ch.ethz.scu.obit.flow.processors.data.model.Specimen;
import ch.ethz.scu.obit.flow.processors.data.model.Tray;
import ch.ethz.scu.obit.flow.processors.data.model.Tube;
import ch.ethz.scu.obit.flow.processors.data.model.UserFolder;
import ch.ethz.scu.obit.flow.processors.data.model.Well;
import ch.ethz.scu.obit.flow.readers.FCSReader;

/**
 * Simple graphical viewer for the AbstractFlowProcessor
 *
 * @author Aaron Ponti
 */
public final class FlowViewer extends AbstractViewer {

    /**
     * Constructor
     *
     * @param globalSettingsManager global settings manager.
     */
    public FlowViewer(GlobalSettingsManager globalSettingsManager) {

        super(globalSettingsManager);
    }

    /**
     * Parse the FCS folder and append the resulting tree to the root
     */
    @Override
    public boolean parse(File userFolder) {

        // Process the user folder
        AbstractFlowProcessor flowprocessor;
        try {
            flowprocessor = FlowProcessorFactory
                    .createProcessor(userFolder.getCanonicalPath());
        } catch (IOException e) {
            outputPane.err(e.getMessage());
            return false;
        }

        // We parse. If parsing fails, we just return (the dataset is invalid).
        if (!flowprocessor.parse()) {
            outputPane.err("Could not parse the folder " + userFolder + "!");
            flowprocessor = null;
            return false;
        }

        // Make sure we have a valid dataset
        if (!flowprocessor.validator.isValid) {
            DefaultTableModel model = (DefaultTableModel) invalidDatasetsTable
                    .getModel();
            for (File file : flowprocessor.validator.invalidFilesOrFolders
                    .keySet()) {
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
                model.addRow(new Object[] { filePath,
                        flowprocessor.validator.invalidFilesOrFolders
                                .get(file) });
            }
            return false;
        }

        // We will append the experiment nodes directly to the root node
        createNodes((RootNode) rootNode, flowprocessor.folderDescriptor);

        return true;
    }

    /**
     * Called when selection in the Tree View is changed.
     *
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
        if (node == lastSelectedNode) {
            return;
        }
        lastSelectedNode = node;

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
            node = (AbstractNode) node.getChildAt(0);
        } else if (className.equals("SorterExperiment")
                || className.equals("AnalyzerExperiment")) {
            clearMetadataTable();
            addAttributesToMetadataTable(
                    ((Experiment) nodeInfo).getAttributes());
        } else if (className.equals("Tray")) {
            clearMetadataTable();
            addAttributesToMetadataTable(((Tray) nodeInfo).getAttributes());
        } else if (className.equals("Specimen")) {
            clearMetadataTable();
            addAttributesToMetadataTable(((Specimen) nodeInfo).getAttributes());
        } else if (className.equals("Tube")) {
            clearMetadataTable();
            addAttributesToMetadataTable(((Tube) nodeInfo).getAttributes());
        } else if (className.equals("Well")) {
            clearMetadataTable();
            addAttributesToMetadataTable(((Well) nodeInfo).getAttributes());
        } else if (className.equals("FCSFile")) {
            // Cast
            FCSFile fcsFile = (FCSFile) nodeInfo;
            FCSReader fcs = new FCSReader(new File(fcsFile.getFullPath()),
                    false);
            try {
                fcs.parse();
                clearMetadataTable();
                addAttributesToMetadataTable(fcs.getAllKeywords());
            } catch (IOException e1) {
                outputPane.err("Could not parse file " + fcsFile + "!");
            }
        } else if (className.equals("FCSFileParameterList")) {
            clearMetadataTable();
            addAttributesToMetadataTable(
                    ((FCSFileParameterList) nodeInfo).getAttributes());
        } else {
            clearMetadataTable();
        }

        // Get the experiment name
        AbstractNode expNode = getParentNodeByNameFuzzy(node, "Experiment",
                "endswith");
        if (expNode != null && expNode != lastSelectedExperiment) {

            // Update the lastSelectedFolder property
            lastSelectedExperiment = expNode;

            // Notify the editor to update its view
            synchronized (this) {
                setChanged();
                notifyObservers(new ObserverActionParameters(
                        ObserverActionParameters.Action.EXPERIMENT_CHANGED,
                        expNode));
            }
        }
    }

    /**
     * Create the nodes for the tree
     *
     * @param top              Root node for the tree
     * @param folderDescriptor A folder descriptor object.
     */
    protected void createNodes(RootNode top, UserFolder folderDescriptor) {

        ExperimentNode experiment;
        TrayNode tray;
        SpecimenNode specimen;
        TubeNode tube;
        WellNode well;
        FCSFileNode fcs;
        FCSFileParemeterListNode fcsparam;

        for (String expKey : folderDescriptor.experiments.keySet()) {

            // Get the ExperimentDescriptor
            Experiment e = folderDescriptor.experiments.get(expKey);

            // Add the experiments
            experiment = new ExperimentNode(e);
            top.add(experiment);

            for (String trayKey : e.trays.keySet()) {

                // Get the TrayDescriptor
                Tray t = e.trays.get(trayKey);

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
                        Well wl = (Well) s.tubes.get(wellKey);

                        // Add the tubes
                        well = new WellNode(wl);
                        specimen.add(well);

                        // Add the fcs files
                        fcs = new FCSFileNode(wl.fcsFile);
                        well.add(fcs);

                        // Add the fcs file parameter list
                        fcsparam = new FCSFileParemeterListNode(
                                wl.fcsFile.parameterList);
                        fcs.add(fcsparam);
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

                    // Add the fcs file parameter list
                    fcsparam = new FCSFileParemeterListNode(
                            tb.fcsFile.parameterList);
                    fcs.add(fcsparam);
                }

            }

        }
    }

    /**
     * React to actions
     *
     * @param e An ActionEvent
     */
    @Override
    public void actionPerformed(ActionEvent e) {
    }

}
