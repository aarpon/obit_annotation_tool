package ch.ethz.scu.obit.at;

import java.lang.management.ManagementFactory;

import javax.swing.JOptionPane;

import ch.ethz.scu.obit.at.gui.AnnotationToolWindow;
import ch.ethz.scu.obit.common.settings.GlobalSettingsManager;


/**
 * AnnotationTool is an application to drive the import of data from the
 * acquisition stations into openBIS.
 * @author Aaron Ponti
 */
public class AnnotationTool {

    /**
     * @param args Ignored
     */
    public static void main(String[] args) {

        // Make sure we are running on JRE 8 or newer
        try {
            double version = Double.parseDouble(
                    ManagementFactory.getRuntimeMXBean().getSpecVersion());
            if (version < 1.8) {
                JOptionPane.showMessageDialog(null,
                        "Sorry, Annotation Tool requires Java 8 or newer.",
                        "Minimum JRE version not satisfied",
                        JOptionPane.WARNING_MESSAGE);
                System.exit(0);
            }
        } catch (Exception e) {
            // We proceed
        }

        // Check whether the application has been set up properly and it is ready
        // to be used.
        try {
            GlobalSettingsManager.isConfigurationValid();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Initialization error", JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }

        // Open the main window and run the scan in the background
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                // Create the Window and scan user's data folder and
                // openBIS structure.
                final AnnotationToolWindow w = new AnnotationToolWindow();

                // Scan data folder and openBIS
                w.scan();

            }
        });

    }

}
