package ch.ethz.scu.obit.at;

import java.awt.Font;
import java.util.Enumeration;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

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

        // Check whether the application has been set up properly and it is ready
        // to be used.
        try {
            GlobalSettingsManager.isConfigurationValid();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Initialization error", JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }

        // Set the global font
        setUIFont(new javax.swing.plaf.FontUIResource("", Font.PLAIN, 13));

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

    /**
     * Globally change font in the application
     * @param f Font resource.
     */
    public static void setUIFont(javax.swing.plaf.FontUIResource f){
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get (key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put(key, f);
        }
    }

}
