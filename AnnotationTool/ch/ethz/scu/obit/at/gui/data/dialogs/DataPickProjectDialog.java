package ch.ethz.scu.obit.at.gui.data.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import ch.ethz.scu.obit.processors.openbis.OpenBISProcessor.ProjectInfo;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;

public class DataPickProjectDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    JComboBox<SpaceWrapper> comboSpaceWrapperList;
    JComboBox<ProjectWrapper> comboProjectWrapperList;
    JButton acceptButton;
    JButton setAsDefaultButton;
    JButton rejectButton;
    ProjectInfo projectInfo;

    public DataPickProjectDialog(SearchResult<Space> spaces, Space defaultSpace, Project defaultProject) {

        // Set dialog title
        setTitle("Pick target project");

        // Initialize result
        projectInfo = null;

        // Make the dialog modal and not resizable
        setModal(true);
        setResizable(false);

        // Create a GridBagLayout
        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);

        // Constraints
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        // Use a variable y to keep track of the row number in the grid layout
        int gridy = 0;

        // Create a label for the Space
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        JLabel labelSpaces = new JLabel("<html><b>Space");
        labelSpaces.setHorizontalAlignment(JLabel.CENTER);
        add(labelSpaces, constraints);

        /*
         *  Space combo box
         */

        // Add the space identifier
        comboSpaceWrapperList = new JComboBox<SpaceWrapper>();

        for (int i = 0; i < spaces.getTotalCount(); i++) {

            Space s = spaces.getObjects().get(i);

            // Add the openBIS Project node
            comboSpaceWrapperList.addItem(new SpaceWrapper(s));

            // Set as default if needed
            if (s == defaultSpace) {
                comboSpaceWrapperList.setSelectedIndex(i);
            }

        }

        // Add callback
        comboSpaceWrapperList.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("comboBoxChanged")) {

                    // Get the space
                    SpaceWrapper spaceWrapper =
                            (SpaceWrapper) comboSpaceWrapperList.getSelectedItem();
                    updateProjectsOnSpaceChange(spaceWrapper);
                }
            }
        });

        // Add the space combo box
        constraints.insets = new Insets(0, 15, 5, 15);
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        add(comboSpaceWrapperList, constraints);

        // Create a label for the Project
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        JLabel labelProjects = new JLabel("<html><b>Project");
        labelProjects.setHorizontalAlignment(JLabel.CENTER);
        add(labelProjects, constraints);

        /*
         *  Project combo box
         */

        // Get the projects for the default space
        List<Project> projects = defaultSpace.getProjects();

        // Add the space identifier
        comboProjectWrapperList = new JComboBox<ProjectWrapper>();

        for (int i = 0; i < projects.size(); i++) {

            Project p = projects.get(i);

            if (p.getCode().equals("COMMON_ORGANIZATION_UNITS")) {
                continue;
            }

            // Add the openBIS Project node
            comboProjectWrapperList.addItem(new ProjectWrapper(p));

            // Set as default if needed
            if (p == defaultProject) {
                comboProjectWrapperList.setSelectedIndex(i);
            }

        }

        // Add the project combo box
        constraints.insets = new Insets(0, 15, 5, 15);
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.gridwidth = 3;
        constraints.gridx = 0;
        constraints.gridy = gridy++;
        add(comboProjectWrapperList, constraints);

        // Create an Accept button
        acceptButton = new JButton("Accept");
        acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //                // Are all the Settings set?
                //                if (!manager.allSet()) {
                //                    JOptionPane.showMessageDialog(null,
                //                            "Incomplete configuration!",
                //                            "Error", JOptionPane.ERROR_MESSAGE);
                //
                //                    return;
                //                }
                //
                //                // Save
                //                if (!manager.save()) {
                //                    JOptionPane.showMessageDialog(null,
                //                            "Error saving settings: " +
                //                                    manager.getLastErrorMessage(),
                //                                    "Error", JOptionPane.ERROR_MESSAGE);
                //
                //                    return;
                //                } else {
                //
                //                    saveButton.setText("<html><b>Saved!</b></html>");
                //                    ActionListener ls = new ActionListener() {
                //                        @Override
                //                        public void actionPerformed(ActionEvent evt) {
                //                            saveButton.setText("Save");
                //                        }
                //                    };
                //                    Timer timer = new Timer(2000, ls);
                //                    timer.setRepeats(false);
                //                    timer.start();
                //
                //                }
            }
        });
        constraints.gridx = 0;
        constraints.gridy = gridy;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 0.1;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(acceptButton, constraints);

        // Create a close button
        setAsDefaultButton = new JButton("Set as default");
        setAsDefaultButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });
        constraints.gridx = 1;
        constraints.gridy = gridy;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 0.1;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(setAsDefaultButton, constraints);

        // Create a close button
        rejectButton = new JButton("Reject");
        rejectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                //dispose();
            }
        });
        constraints.gridx = 2;
        constraints.gridy = gridy;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 0.1;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(rejectButton, constraints);

        // Make the login button react to the enter key
        getRootPane().setDefaultButton(acceptButton);

        // Display the dialog
        pack();
        setLocationRelativeTo(null);
        //setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setVisible(true);

    }

    /**
     * Fill the projects combobox depending on the selection of space
     * @param space
     */
    private void updateProjectsOnSpaceChange(SpaceWrapper spaceWrapper) {

        // Remove current items
        comboProjectWrapperList.removeAllItems();

        // Get the projects from space
        List<Project> projects = spaceWrapper.space.getProjects();

        // Number of added projects
        int numAddedProjects = 0;

        for (int i = 0; i < projects.size(); i++) {

            Project p = projects.get(i);

            if (p.getCode().equals("COMMON_ORGANIZATION_UNITS")) {
                continue;
            }

            // Add the openBIS Project node
            comboProjectWrapperList.addItem(new ProjectWrapper(p));

            // Increase counter
            numAddedProjects++;
        }

        // Set the first project as the selected one
        if (numAddedProjects == 0) {
            // Failed login. Inform
            JOptionPane.showMessageDialog(null,
                    "Space " + spaceWrapper.space.getCode() + " does not contain projects!",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            comboProjectWrapperList.setSelectedIndex(0);
        }
    }

    private class SpaceWrapper {

        protected Space space;

        public SpaceWrapper(Space space) {
            this.space = space;
        }

        @Override
        public String toString() {
            return space.getCode();
        }
    }

    private class ProjectWrapper {

        protected Project project;

        public ProjectWrapper(Project project) {
            this.project = project;
        }

        @Override
        public String toString() {
            return project.getCode();
        }
    }

    /**
     * Return the selected target project.
     * @return A ProjectInfo object for the selected target project.
     */
    public ProjectInfo getTargetProject() {
        return null;

    }
}
