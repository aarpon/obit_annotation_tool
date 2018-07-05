package ch.ethz.scu.obit.at.gui.pane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;

import ch.ethz.scu.obit.common.utils.QueryOS;
import ch.ethz.scu.obit.common.version.VersionInfo;

/**
 * Output pane to display any output from the application
 * @author Aaron Ponti
 *
 */
public final class OutputPane extends JScrollPane {

    final static Logger logger = Logger.getLogger("AnnotationTool");

    private static final long serialVersionUID = 1L;
    private static JTextPane outputPane = new JTextPane();

    /**
     * Message type
     */
    private enum Type { LOG, WARN, ERR }

    /**
     * Document
     */
    StyledDocument doc;

    /**
     * Base document style
     */
    Style base;

    /**
     * Style for standard output
     */
    Style log;

    /**
     * Style for warning
     */
    Style warn;

    /**
     * Style for error
     */
    Style err;

    /**
     * Offset from the beginning of the text
     */
    int offset = 0;

    /**
     * Constructor
     */
    public OutputPane() {

        // Call the base constructor
        super(outputPane);

        // Make sure the JScrollPane always shows the latest lines added
        DefaultCaret caret = (DefaultCaret)outputPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        // Set the document
        this.doc = new DefaultStyledDocument();
        outputPane.setDocument(this.doc);

        // Add three styles
        base = StyleContext.getDefaultStyleContext().
                getStyle(StyleContext.DEFAULT_STYLE);
        log  = doc.addStyle("log",  base);
        warn = doc.addStyle("warn", base);
        StyleConstants.setItalic(warn, true);
        StyleConstants.setForeground(warn, new Color(117, 188, 255));
        err  = doc.addStyle("err",  base);
        StyleConstants.setItalic(err, true);
        StyleConstants.setForeground(err, Color.red);

        // Set some margin
        outputPane.setMargin(new Insets(3, 3, 3, 3));

        // Set the minimum size
        outputPane.setMinimumSize(new Dimension(1500, 200));

        // Set the pane to non-editable
        outputPane.setEditable(false);

        // Add a context menu
        outputPane.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (QueryOS.isWindows()) {
                    return;
                }
                setListenerOnTextPane(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (QueryOS.isMac()) {
                    return;
                }
                setListenerOnTextPane(e);
            }
        });

        // Send to log file only
        logger.info("Annotation Tool version " + VersionInfo.version);

        // Add the first text
        log("Welcome.");
    }

    /**
     * Display the passed string
     * @param text String to be displayed
     */
    public void log(String text) {
        try {
            doc.insertString(doc.getLength(),
                    buildFullString(text, Type.LOG), log);
        } catch (BadLocationException e) {
            System.err.println("Could not print to output pane!");
        }

        // To log
        logger.info(text);
    }

    /**
     * Display the passed warning string
     * @param text String to be displayed
     */
    public void warn(String text) {
        try {
            doc.insertString(doc.getLength(),
                    buildFullString(text, Type.WARN), warn);
        } catch (BadLocationException e) {
            System.err.println("Could not print to output pane!");
        }

        // To log
        logger.warn(text);
    }

    /**
     * Display the passed error string
     * @param text String to be displayed
     */
    public void err(String text) {
        try {
            doc.insertString(doc.getLength(),
                    buildFullString(text, Type.ERR), err);
        } catch (BadLocationException e) {
            System.err.println("Could not print to output pane!");
        }

        // To log
        logger.error(text);
    }

    /**
     * Build the full text String
     * @param text The text to be displayed in the pane
     * @param type The type of the log.
     * @return full text String (with time stamp and line break)
     */
    private String buildFullString(String text, Type type) {
        String typeStr = "";
        switch (type) {
        case LOG:  typeStr = ""; break;
        case WARN: typeStr = "Warning: "; break;
        case ERR:  typeStr = "Error: "; break;
        }
        return (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")).format(
                new Date()) + ": " + typeStr + text + "\n";
    }

    /**
     * Sets a mouse event listener on the text pane
     * @param e Mouse event
     */
    private void setListenerOnTextPane(MouseEvent e) {

        if (e.isPopupTrigger() &&
                e.getComponent() instanceof JTextPane) {

            // Position of mouse click
            int x = e.getPoint().x;
            int y = e.getPoint().y;

            // Create the popup menu.
            JPopupMenu popup = new JPopupMenu();

            // Add "Copy" menu entry
            JMenuItem copyMenuItem = new JMenuItem("Copy");
            copyMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    copy();
                }
            });
            popup.add(copyMenuItem);

            // Add "Clear" menu entry
            JMenuItem clearMenuItem = new JMenuItem("Clear");
            clearMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    clear();
                }
            });
            popup.add(clearMenuItem);

            // Display the menu
            popup.show(e.getComponent(), x, y);
        }
    }

    /**
     * Copy text from outputPane to system clipboard
     */
    private void copy()
    {
        String selection = outputPane.getText();
        StringSelection data = new StringSelection(selection);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(data, data);
    }

    /**
     * Clear the text in outputPane
     */
    private void clear()
    {
        outputPane.setText("");
    }
}
