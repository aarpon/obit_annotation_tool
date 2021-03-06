package ch.ethz.scu.obit.microscopy.gui.editors.data;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ch.ethz.scu.obit.at.gui.editors.data.AbstractEditor;
import ch.ethz.scu.obit.at.gui.viewers.ObserverActionParameters;
import ch.ethz.scu.obit.at.gui.viewers.data.AbstractViewer;
import ch.ethz.scu.obit.at.gui.viewers.data.model.AbstractNode;
import ch.ethz.scu.obit.at.gui.viewers.data.model.ExperimentNode;
import ch.ethz.scu.obit.at.gui.viewers.openbis.OpenBISViewer;
import ch.ethz.scu.obit.at.gui.viewers.openbis.model.OpenBISProjectNode;
import ch.ethz.scu.obit.common.settings.GlobalSettingsManager;
import ch.ethz.scu.obit.common.utils.QueryOS;
import ch.ethz.scu.obit.microscopy.gui.editors.data.model.MicroscopyMetadataMapper;
import ch.ethz.scu.obit.microscopy.processors.data.MicroscopyProcessor.Experiment;
import ch.ethz.scu.obit.microscopy.processors.data.MicroscopyProcessor.MicroscopyCompositeFile;
import ch.ethz.scu.obit.microscopy.processors.data.MicroscopyProcessor.MicroscopyFile;
import ch.ethz.scu.obit.processors.data.model.DatasetDescriptor;
import ch.ethz.scu.obit.processors.data.model.Tag;
import ch.ethz.scu.obit.processors.data.model.TagListImportTransferHandler;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;

/**
 * Metadata editor panel.
 * @author Aaron Ponti
 *
 */
public final class MicroscopyEditor extends AbstractEditor {

    // List of metadata mappers
    private List<MicroscopyMetadataMapper> metadataMappersList =
            new ArrayList<MicroscopyMetadataMapper>();

    // Indicate which of the List<MicroscopyMetadataMapper> is the active one
    private int currentExperimentIndex = -1;

    // Indicate which is the currently selected file
    private AbstractNode currentlySelectedMicroscopyFileNode;

    // Icons
    private Icon experimentIcon;
    private Icon microscopyFileIcon;
    private Icon microscopyCompositeFileIcon;

    private JLabel labelExpName;
    private JLabel labelFileName;
    private JLabel labelExpDescription;
    private JLabel labelFileDescription;
    private JComboBox<String> comboProjectList;
    private JTextArea expDescription;
    private JTextArea fileDescription;
    private JScrollPane areaFileScrollPane;
    private JScrollPane areaExpScrollPane;

    /**
     * Constructor
     * @param dataViewer data viwer
     * @param openBISViewer openBIS viewer
     * @param globalSettingsManager global settings manager
     */
    public MicroscopyEditor(AbstractViewer dataViewer,
            OpenBISViewer openBISViewer,
            GlobalSettingsManager globalSettingsManager) {

        // Store the reference to the data and openBIS viewers
        super(dataViewer, openBISViewer, globalSettingsManager);

        // Create a GridBagLayout
        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);

        // Store references to the icons to use
        experimentIcon = new ImageIcon(
                this.getClass().getResource("icons/experiment.png"));
        microscopyFileIcon = new ImageIcon(
                this.getClass().getResource("icons/microscopyfile.png"));
        microscopyCompositeFileIcon = new ImageIcon(
                this.getClass().getResource("icons/microscopycompositefile.png"));

    }

    /**
     * ActionPerformed method from the ActionListener interface
     * @param e The ActionEvent object
     */
    @Override
    public void actionPerformed(ActionEvent e) {
    }

    /**
     * Once the metadata has been completely filled, this method makes
     * sure to update the data model (generated by the Processor) to be
     * ready to be saved to the XML properties file.
     */
    @Override
    public boolean updateDataModel() {

        // Check whether we have some experiments to update
        if (metadataMappersList.size() == 0) {
            return false;
        }

        // Go over all experiments
        for (MicroscopyMetadataMapper currentMetadata : metadataMappersList) {

            // Get the experiment node
            ExperimentNode expNode = currentMetadata.experimentNode;
            assert(expNode.getType().equals("Experiment"));

            // We first start by updating the Experiment descriptor itself
            Experiment expDescr = (Experiment) expNode.getUserObject();

            // Set the openBIS experiment identifier
            Map<String, String> expOpenBISAttributes =
                    new Hashtable<String, String>();
            expOpenBISAttributes.put("openBISCollectionIdentifier",
                    currentMetadata.getOpenBISCollectionIdentifier());
            expOpenBISAttributes.put("openBISIdentifier",
                    currentMetadata.getOpenBISExerimentIdentifier());
            expOpenBISAttributes.put("openBISSpaceIdentifier",
                    currentMetadata.getOpenBISSpaceIdentifier());
            expDescr.addOpenBISAttributes(expOpenBISAttributes);

            // Set the description
            Map<String, String> expUserAttributes =
                    new Hashtable<String, String>();
            expUserAttributes.put("description",
                    currentMetadata.getExperiment().description);
            expUserAttributes.put("tags", expDescr.getTagIdentifierList());
            expDescr.addUserAttributes(expUserAttributes);

            // Now get the MicroscopyFile children of the Experiment
            for (int i = 0; i < expNode.getChildCount(); i++) {

                // Get the i-th child node
                AbstractNode microscopyFileNode =
                        (AbstractNode) expNode.getChildAt(i);

                // Get the MicroscopyFile Descriptor
                DatasetDescriptor microscopyFileDescriptor =
                        (DatasetDescriptor) microscopyFileNode.getUserObject();

                // Make sure we have a Tray or a Specimen
                assert(microscopyFileNode.getType().equals("MicroscopyFile") ||
                        microscopyFileNode.getType().equals("MicroscopyCompositeFile"));

                // Set the openBIS space and experiment identifiers
                Map<String, String> microscopyFileOpenBISAttributes =
                        new Hashtable<String, String>();
                microscopyFileOpenBISAttributes.put(
                        "openBISSpaceIdentifier",
                        currentMetadata.getOpenBISSpaceIdentifier());
                microscopyFileOpenBISAttributes.put(
                        "openBISCollectionIdentifier",
                        currentMetadata.getOpenBISCollectionIdentifier());
                microscopyFileOpenBISAttributes.put(
                        "openBISExperimentIdentifier",
                        currentMetadata.getOpenBISExerimentIdentifier());
                microscopyFileDescriptor.addOpenBISAttributes(
                        microscopyFileOpenBISAttributes);

                // Set the description
                Map<String, String> fileUserAttributes =
                        new Hashtable<String, String>();
                fileUserAttributes.put("description",
                        microscopyFileDescriptor.description);
                microscopyFileDescriptor.addUserAttributes(fileUserAttributes);


            }
        }

        return true;
    }

    @Override
    public String getCurrentProjectIdentifier() {

        MicroscopyMetadataMapper mapper = metadataMappersList.get(currentExperimentIndex);
        Project project = (Project) mapper.getOpenBISProjectNode().getUserObject();
        String identifier = project.getIdentifier().toString();
        return identifier;
    }

    /**
     * Map the data and openBIS models
     * @throws Exception it the metadata could not be initialized.
     */
    @Override
    protected boolean initMetadata() throws Exception {

        // Make sure both viewers have completed their models
        if (!openBISViewer.isReady() || !dataViewer.isReady()) {
            return false;
        }

        // Store the and openBIS nodes
        openBISProjects = openBISViewer.getOpenBISProjectNodes();
        experiments = dataViewer.getExperimentNodes();

        // Check that there is at least one entry in each of the
        // arrays
        if (experiments.size() == 0 || openBISProjects.size() == 0) {
            return false;
        }

        // Retrieve the default target project from the User settings or
        // revert to the first project in the list if none is set.
        OpenBISProjectNode defaultProjectNode = openBISViewer.getDefaultProjectOrFirst();
        if (defaultProjectNode == null) {
            JOptionPane optionPane = new JOptionPane(
                    "No projects could be found in openBIS!\n\n" +
                            "You will need to create one before you can continue.",
                            JOptionPane.ERROR_MESSAGE);
            JDialog dialog = optionPane.createDialog("Error");
            dialog.setAlwaysOnTop(true);
            dialog.setVisible(true);
            dialog.setModal(true);
            return false;
        }

        // Create all MicroscopyMetadataMapper objects and initially assign all
        // experiments to the first project
        for (ExperimentNode node : experiments) {
            metadataMappersList.add(
                    new MicroscopyMetadataMapper(node, defaultProjectNode));
        }

        // Set the index of the experiment (if needed)
        if (currentExperimentIndex < 0
                || currentExperimentIndex > (experiments.size() - 1)) {
            currentExperimentIndex = 0;
        }

        // Return success
        return true;
    }

    /**
     * Renders all widgets on the panel
     * @throws Exception if some openBIS identifiers cannot be computed
     */
    @Override
    protected void createUIElements(ObserverActionParameters params) throws Exception {

        // Get selected metadata element
        if (metadataMappersList.size() < (currentExperimentIndex + 1)) {
            return;
        }
        MicroscopyMetadataMapper metadata = metadataMappersList.get(
                currentExperimentIndex);

        // If we are repainting this panel, let's make sure we delete the
        // child components already displayed.
        clearUIElements();

        // Make sure to clear references to selected file if it no longer
        // exists; this is the case if the data folder was rescanned, but not
        // if openBIS was rescanned.
        if (currentlySelectedMicroscopyFileNode != null) {

            // Get the type of the node
            String className =
                    currentlySelectedMicroscopyFileNode.getClass().getSimpleName();

            boolean found = false;

            if (className.equals("MicroscopyFileNode")) {

                MicroscopyFile microscopyFile =
                        (MicroscopyFile) currentlySelectedMicroscopyFileNode
                        .getUserObject();

                // Check the MicroscopyFile list first
                Iterator<Map.Entry<String, MicroscopyFile>> it = metadata
                        .getExperiment().microscopyFiles.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, MicroscopyFile> entry = it.next();
                    if (entry.getValue() == microscopyFile) {
                        found = true;
                        break;
                    }
                }

            } else if (className.equalsIgnoreCase("MicroscopyCompositeFileNode")) {

                MicroscopyCompositeFile microscopyCompositeFile =
                        (MicroscopyCompositeFile) currentlySelectedMicroscopyFileNode
                        .getUserObject();

                // Then, if needed, check the MicroscopyCompositeFile list
                Iterator<Map.Entry<String, MicroscopyCompositeFile>> it = metadata
                        .getExperiment().microscopyCompositeFiles.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, MicroscopyCompositeFile> entry = it.next();
                    if (entry.getValue() == microscopyCompositeFile) {
                        found = true;
                        break;
                    }
                }

            } else {

                found = false;

            }

            // Check the MicroscopyFile's first
            if (!found) {
                currentlySelectedMicroscopyFileNode = null;
            }
        }

        // Make sure both viewers have completed their models
        if (metadataMappersList.size() == 0) {
            return;
        }

        // Constraints
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        // Use a variable y to keep track of the row number in the grid layout
        int gridy = 0;

        /*
         *  Experiment name
         */

        String expName = metadata.experimentNode.toString();

        // Create a label for the experiment
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        labelExpName = new JLabel(expName);
        labelExpName.setIcon(experimentIcon);
        panel.add(labelExpName, constraints);

        /*
         * Tags title
         */

        // Create a label for the experiment tags
        constraints.insets = new Insets(0, 10, 5, 10);
        constraints.gridwidth = 2;
        constraints.weightx = 2;
        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        JLabel labelExpTags = new JLabel("<html><u>Tags");
        labelExpTags.setHorizontalAlignment(JLabel.CENTER);
        panel.add(labelExpTags, constraints);

        /*
         * Tags
         */

        // Create a label for the experiment tags
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.gridwidth = 2;
        constraints.weightx = 2;
        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        expTags = new JTextArea("");
        Font f = expTags.getFont();
        expTags.setFont(new Font(f.getFontName(), f.getStyle(), 11));
        expTags.setEditable(false);

        // Add a context menu to clear the tags
        expTags.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (QueryOS.isWindows()) {
                    return;
                }
                setListenerOnTagsTextArea(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (QueryOS.isMac()) {
                    return;
                }
                setListenerOnTagsTextArea(e);
            }
        });

        // Append a custom transfer handler
        expTags.setTransferHandler(new TagListImportTransferHandler(this));
        panel.add(expTags, constraints);

        // Create a label for the explanation
        constraints.insets = new Insets(5, 0, 10, 0);
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        JLabel labelTagsExpl = new JLabel(
                "Drag and drop your tags here from the openBIS Viewer.");
        labelTagsExpl.setHorizontalAlignment(JLabel.CENTER);
        panel.add(labelTagsExpl, constraints);

        /*
         * Experiment description label
         */

        // Create a label for the experiment description
        constraints.insets = new Insets(0, 0, 5, 0);
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        labelExpDescription = new JLabel("<html><u>Description");
        labelExpDescription.setHorizontalAlignment(JLabel.CENTER);
        panel.add(labelExpDescription, constraints);

        /*
         * Experiment description text area
         */
        constraints.insets = new Insets(0, 0, 5, 0);
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        expDescription = new JTextArea(
                metadata.getExperiment().description);
        f = expDescription.getFont();
        expDescription.setFont(new Font(f.getFontName(), f.getStyle(), 11));
        expDescription.setLineWrap(true);
        expDescription.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateExpDescription();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateExpDescription();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateExpDescription();
            }
        });
        areaExpScrollPane = new JScrollPane(expDescription);
        areaExpScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        areaExpScrollPane.setMinimumSize(new Dimension(400, 50));
        areaExpScrollPane.setPreferredSize(new Dimension(400, 50));
        panel.add(areaExpScrollPane, constraints);

        // Create a label for the file
        constraints.insets = new Insets(10, 0, 0, 0);
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        labelFileName = new JLabel("");
        labelFileName.setIcon(microscopyFileIcon);
        panel.add(labelFileName, constraints);

        // Create a label for the file description
        constraints.insets = new Insets(0, 0, 5, 0);
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        labelFileDescription = new JLabel("<html><u>Description");
        labelFileDescription.setHorizontalAlignment(JLabel.CENTER);
        panel.add(labelFileDescription, constraints);

        /*
         * File description text area
         */
        constraints.insets = new Insets(0, 0, 5, 0);
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        fileDescription = new JTextArea("");
        f = fileDescription.getFont();
        fileDescription.setFont(new Font(f.getFontName(), f.getStyle(), 11));
        fileDescription.setLineWrap(true);
        fileDescription.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFileDescription();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFileDescription();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFileDescription();
            }
        });
        areaFileScrollPane = new JScrollPane(fileDescription);
        areaFileScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        areaFileScrollPane.setMinimumSize(new Dimension(400, 50));
        areaFileScrollPane.setPreferredSize(new Dimension(400, 50));
        panel.add(areaFileScrollPane, constraints);

        // Depending on whether a file is selected, show or hide
        // the UI elements needed to edit it
        if (currentlySelectedMicroscopyFileNode != null) {
            DatasetDescriptor microscopyFile = (DatasetDescriptor)
                    currentlySelectedMicroscopyFileNode.getUserObject();

            // Update the UI elements
            showFileEditUIElements(microscopyFile.toString(),
                    microscopyFile.description,
                    currentlySelectedMicroscopyFileNode.getClass().getSimpleName());
        } else {
            hideFileEditUIElements();
        }

        /*
         *  Label openBIS projects
         */

        // Create a label for the openBIS projects
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        JLabel labelProjects = new JLabel("<html><u>Target openBIS project");
        labelProjects.setHorizontalAlignment(JLabel.CENTER);
        panel.add(labelProjects, constraints);


        /*
         *  Tray openBIS projects
         */

        // Add the project identifier
        comboProjectList = new JComboBox<String>();

        for (OpenBISProjectNode s : openBISProjects) {

            // Add the openBIS Project node
            comboProjectList.addItem(s.getIdentifier());

        }

        // Select the correct one
        for (int i = 0; i < openBISProjects.size(); i++) {
            if (openBISProjects.get(i).getIdentifier().equals(
                    metadata.openBISProjectNode.getIdentifier())) {
                comboProjectList.setSelectedIndex(i);
                break;
            }
        }

        // When a project is selected, update the corresponding
        // experiment in the data model
        comboProjectList.addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("comboBoxChanged")) {

                    // Get selected project identifier
                    String projectID;
                    projectID = (String)
                            ((JComboBox<String>)
                                    e.getSource()).getSelectedItem();

                    // Get the selected project node
                    OpenBISProjectNode selProjNode = null;
                    for (OpenBISProjectNode projNode : openBISProjects) {
                        if (projNode.getIdentifier().equals(projectID)) {
                            selProjNode = projNode;
                            break;
                        }
                    }

                    // How many experiments do we have?
                    int nExperiments = metadataMappersList.size();

                    // Default to set the project for current experiment only.
                    int n = 0;
                    if (nExperiments > 1) {

                        // Ask the user if he wants to set this project only
                        // to this experiment or to all
                        Object[] options = {"To this only", "To all"};
                        n = JOptionPane.showOptionDialog(null,
                                "Set this project to this experiment only or to all?",
                                "Question",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[0]);
                    }

                    // Apply user's choice
                    if (n == 1) {

                        // Apply to all
                        for (int i = 0; i < nExperiments; i++) {

                            // Get the metadata mapper
                            MicroscopyMetadataMapper mapper = metadataMappersList.get(i);

                            // Set the project
                            mapper.openBISProjectNode = selProjNode;

                            // Reset tags
                            mapper.getExperiment().tags = new ArrayList<Tag>();
                        }

                    } else {

                        // Get the metadata mapper
                        MicroscopyMetadataMapper mapper = metadataMappersList.get(currentExperimentIndex);

                        // Apply to current experiment only
                        mapper.openBISProjectNode = selProjNode;

                        // Reset tags
                        mapper.getExperiment().tags = new ArrayList<Tag>();

                    }

                    // Clear the tag field
                    expTags.setText("");
                }
            }
        });

        // Add the project combo box
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        panel.add(comboProjectList, constraints);

        /*
         *  Spacer
         */

        // Add a spacer
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        panel.add(new JLabel(""), constraints);

        // Now redraw
        panel.validate();
        panel.repaint();

    }

    /**
     * Update all widgets on the panel
     */
    @Override
    protected void updateUIElements(ObserverActionParameters params) {

        // If the action is FILE_CHANGED, we just update the UI elements that
        // relate to the microscopy file and return.

        // Should we should the file editing elements?
        if (params.action == ObserverActionParameters.Action.FILE_CHANGED) {

            // Store currently selected node
            currentlySelectedMicroscopyFileNode = params.node;

            DatasetDescriptor d = (DatasetDescriptor)
                    currentlySelectedMicroscopyFileNode.getUserObject();

            // Get current file name, description and tags
            String fileName = currentlySelectedMicroscopyFileNode
                    .toString();
            String description = d.description;

            // Set file name and description and show the UI edit elements
            showFileEditUIElements(fileName, description,
                    currentlySelectedMicroscopyFileNode.getClass().getSimpleName());

            // Return
            return;

        }

        // Forget the currently selected file
        currentlySelectedMicroscopyFileNode = null;

        // If the experiment changed, we hide the UI elements related to
        // the Microscopy file.
        hideFileEditUIElements();

        // If the experiment changed, update the currentExperimentIndex
        // property and refresh the UI
        int index = experiments.indexOf(params.node);
        if (currentExperimentIndex == index) {
            return;
        }

        // Update the current experiment
        currentExperimentIndex = index;

        // Get the active metadata object
        MicroscopyMetadataMapper metadata = metadataMappersList.get(
                currentExperimentIndex);

        // Update the experiment name
        labelExpName.setText(metadata.getExperimentName());

        // Update the description
        expDescription.setText(metadata.getExperiment().description);

        // Update the tags
        expTags.setText(metadata.getExperiment().getTagList());

        // Remove listeners on the comboProjectList element to prevent firing
        // while we set the value.
        ActionListener[] listeners =
                comboProjectList.getListeners(ActionListener.class);
        for (ActionListener l : listeners) {
            comboProjectList.removeActionListener(l);
        }

        // Select the correct one
        for (int i = 0; i < openBISProjects.size(); i++) {
            if (openBISProjects.get(i).getIdentifier().equals(
                    metadata.openBISProjectNode.getIdentifier())) {
                comboProjectList.setSelectedIndex(i);
                break;
            }
        }

        // Restore listeners
        for (ActionListener l : listeners) {
            comboProjectList.addActionListener(l);
        }

    }

    /**
     * We update the experiment description on the fly while the user
     * is typing in the Text Area.
     */
    protected void updateExpDescription() {

        // Get the active metadata object
        MicroscopyMetadataMapper metadata = metadataMappersList.get(
                currentExperimentIndex);

        // Store the experiment description
        metadata.getExperiment().description = expDescription.getText();
    }

    /**
     * We update the file description on the fly while the user
     * is typing in the Text Area.
     */
    protected void updateFileDescription() {

        if (currentlySelectedMicroscopyFileNode == null) {
            return;
        }
        // This can be either a MicroscopyFile or a MicroscopyCompositeFile,
        // which are both DatasetDescriptor's
        DatasetDescriptor microscopyFile = (DatasetDescriptor)
                currentlySelectedMicroscopyFileNode.getUserObject();
        microscopyFile.description = fileDescription.getText();

    }

    /**
     * Discard metadata information since it went out of sync with the data
     * and openBIS models.
     */
    @Override
    protected void resetMetadata(ObserverActionParameters p) {
        // Make sure it is the correct action
        if (p.action != ObserverActionParameters.Action.ABOUT_TO_RESCAN) {
            return;
        }
        metadataMappersList = new ArrayList<MicroscopyMetadataMapper>();
    }

    /**
     * Hides all UI elements needed to edit File properties and sets
     * file name and description to empty strings.
     */
    protected void hideFileEditUIElements() {
        labelFileName.setText("");
        fileDescription.setText("");
        labelFileName.setVisible(false);
        labelFileDescription.setVisible(false);
        areaFileScrollPane.setVisible(false);
    }

    /**
     * Shows all UI elements needed to edit File properties and sets the
     * file name and description
     * @param fileName Name of the selected file
     * @param description Description for the selected file.
     * @param className Name of the class to use to filter the UI elements to show.
     */
    protected void showFileEditUIElements(String fileName, String description,
            String className) {
        labelFileName.setText(fileName);
        fileDescription.setText(description);
        labelFileName.setVisible(true);
        if (className.equals("MicroscopyFileNode")) {
            labelFileName.setIcon(microscopyFileIcon);
        } else if (className.equals("MicroscopyCompositeFileNode")) {
            labelFileName.setIcon(microscopyCompositeFileIcon);
        } else {
            // This should not happen.
        }
        labelFileDescription.setVisible(true);
        areaFileScrollPane.setVisible(true);
    }

    /**
     * Sets a mouse event listener on the tags text area
     * @param e Mouse event
     */
    private void setListenerOnTagsTextArea(MouseEvent e) {

        if (e.isPopupTrigger() &&
                e.getComponent() instanceof JTextArea) {

            // Position of mouse click
            int x = e.getPoint().x;
            int y = e.getPoint().y;

            // Create the popup menu.
            JPopupMenu popup = new JPopupMenu();

            // Add "Clear" menu entry
            JMenuItem clearMenuItem = new JMenuItem("Clear");
            clearMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // Clean the text area
                    expTags.setText("");

                    // Get the active metadata object
                    MicroscopyMetadataMapper metadata = metadataMappersList.get(
                            currentExperimentIndex);

                    // Store the experiment description
                    metadata.getExperiment().tags = new ArrayList<Tag>();
                }
            });
            popup.add(clearMenuItem);

            // Display the menu
            popup.show(e.getComponent(), x, y);
        }
    }

    /**
     * We update the experiment tags.
     */
    @Override
    protected void updateExpTags(List<Tag> tagList) {

        // How many experiments do we have?
        int nExperiments = metadataMappersList.size();

        // Default to set the tags for current experiment only.
        int n = 0;
        if (nExperiments > 1) {

            // Ask the user if he wants to set the tags only
            // to this experiment or to all
            Object[] options = {"To this only", "To all"};
            n = JOptionPane.showOptionDialog(null,
                    "Set the tag(s) to this experiment only or to all?",
                    "Question",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);
        }

        // Apply user's choice
        if (n == 1) {

            // Apply to all
            for (int i = 0; i < nExperiments; i++) {
                ((Experiment)
                        metadataMappersList.get(i).experimentNode.getUserObject()).tags =
                        tagList;
            }

        } else {

            // Apply to current experiment only

            // Get the active metadata object
            MicroscopyMetadataMapper metadata = metadataMappersList.get(
                    currentExperimentIndex);

            // Store the experiment description
            metadata.getExperiment().tags = tagList;
        }

    }

}
