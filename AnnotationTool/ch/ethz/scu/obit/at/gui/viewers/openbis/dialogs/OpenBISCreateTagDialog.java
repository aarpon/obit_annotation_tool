package ch.ethz.scu.obit.at.gui.viewers.openbis.dialogs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;

/**
 * openBIS pick server dialog
 * @author Aaron Ponti
 */
public class OpenBISCreateTagDialog extends JDialog {

    /* Private instance variables */
    private static final long serialVersionUID = 1L;

    private JComboBox<SpaceWrapper> spaceList;
    private JTextField tagNameField;
    private JTextField tagDescriptionField;
    private List<Space> spaces;
    private Space selectedSpace;
    private String tagName;
    private String tagDescription;

    private boolean cancelled = false;

    /**
     * Constructor
     * @param servers List of openBIS server URLs.
     * @param current URL of the currently active openBIS server.
     * @param globalSettigsManager The global settings manager.
     */
    public OpenBISCreateTagDialog(List<Space> spaces, Space selSpace) {

        // Set window title
        setTitle("Create new tag...");

        // Assign the arguments
        this.spaces = spaces;
        this.selectedSpace = selSpace;

        // Reset
        this.tagName = "";
        this.tagDescription = "";

        // Create a GridBagLayout
        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);

        // Common constraints
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;

        // Make the dialog modal and not resizable
        setModal(true);
        setResizable(false);

        // Add a label for the selection of the URL
        JLabel spaceLabel = new JLabel("Select space");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(spaceLabel, constraints);

        // Add a drop-down menu for the selection of the URL
        spaceList = new JComboBox<SpaceWrapper>();
        for (Space space: spaces) {
            spaceList.addItem(new SpaceWrapper(space));
        }
        spaceList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("comboBoxChanged")) {
                    selectedSpace = ((SpaceWrapper) spaceList.getSelectedItem()).getSpace();
                }
            }
        });
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(spaceList, constraints);

        // Select the space that was passed as an argument.
        setSelectedSpace(this.selectedSpace);

        // Add a label for the tag name
        JLabel tagNameLabel = new JLabel("Tag name");
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(tagNameLabel, constraints);

        // Add a JTextField for the tag name
        tagNameField = new JTextField(tagName);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(tagNameField, constraints);

        // Add a label for the tag description
        JLabel tagDescriptionLabel = new JLabel("Tag description (optional)");
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(tagDescriptionLabel, constraints);

        // Add a JTextField for the tag description
        tagDescriptionField = new JTextField(tagDescription);
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(tagDescriptionField, constraints);

        // Create an "Ok" button
        JButton selectButton = new JButton("Ok");
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 0.50;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Do not accept the action is space and
                // tag name have not been assigned. The
                // description is optional.
                if (tagNameField.getText().equals("")) {
                    JOptionPane.showMessageDialog(null,
                            "Please specify a tag name.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                tagName = tagNameField.getText();
                tagDescription = tagDescriptionField.getText();
                selectedSpace = ((SpaceWrapper) spaceList.getSelectedItem()).getSpace();
                setVisible(false);
                //dispose();
            }
        });
        add(selectButton, constraints);

        // Create a cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Restore the original server
                selectedSpace = null;
                tagName = "";
                tagDescription = "";
                setVisible(false);
            }
        });
        constraints.gridx = 1;
        constraints.gridy = 6;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 0.50;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 0, 5, 5);
        add(cancelButton, constraints);

        // Make the ok button react to the enter key
        getRootPane().setDefaultButton(selectButton);

        // If the user just closes the login dialog, we reset all choices.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Restore the original server
                selectedSpace = null;
                tagName = "";
                tagDescription = "";
                setVisible(false);
            }
        });

        // Display the dialog
        setMinimumSize(new Dimension(350, 120));
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Set the selected Space in the pull-down menu.
     * @param space Selected space.
     */
    public void setSelectedSpace(Space space) {
        if (space == null) {
            space = this.spaces.get(0);
            spaceList.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < spaceList.getItemCount(); i++) {
            Space current = spaceList.getItemAt(i).getSpace();
            if (current.getPermId().toString().equals(space.getPermId().toString())) {
                spaceList.setSelectedIndex(i);
                return;
            }
        }

        // Did not find: reset to first space
        space = this.spaces.get(0);
        spaceList.setSelectedIndex(0);
    }

    /**
     * Reset the tag name and description fields.
     */
    public void resetTagFields() {
        this.tagName = "";
        this.tagNameField.setText(tagName);
        this.tagDescription = "";
        this.tagDescriptionField.setText(tagDescription);
    }

    /**
     * Get the selected space
     * @return selected space
     */
    public Space getSelectedSpace() {
        return this.selectedSpace;
    }

    /**
     * Get the selected space
     * @return selected space
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * Get the selected space
     * @return selected space
     */
    public String getTagDescription() {
        return tagDescription;
    }

    /**
     * Return true if the user cancelled.
     * @return true if the user cancelled, false otherwise.
     */
    public boolean wasCancelled() {
        return this.cancelled;
    }

    /**
     * Space wrapper for the combox box.
     */
    private class SpaceWrapper {
        Space space;
        private SpaceWrapper(Space space) {
            this.space = space;
        }

        @Override
        public String toString() {
            return this.space.getCode().toString();
        }

        public Space getSpace() {
            return this.space;
        }
    }
}
