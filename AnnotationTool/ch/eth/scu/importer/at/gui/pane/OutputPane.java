package ch.eth.scu.importer.at.gui.pane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 * Output pane to display any output from the application
 * @author Aaron Ponti
 *
 */
public class OutputPane extends JScrollPane {

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
			scroll();
		} catch (BadLocationException e) {
			System.err.println("Could not print to output pane!");
		}
	}
	
	/**
	 * Display the passed warning string
	 * @param text String to be displayed
	 * 
	 * //TODO	 Add formatting
	 */
	public void warn(String text) {
		try {
			doc.insertString(doc.getLength(), 
					buildFullString(text, Type.WARN), warn);
			scroll();
		} catch (BadLocationException e) {
			System.err.println("Could not print to output pane!");
		}
	}	

	/**
	 * Display the passed error string
	 * @param text String to be displayed
	 *
	 * //TODO	 Add formatting
	 */
	public void err(String text) {
		try {
			doc.insertString(doc.getLength(), 
					buildFullString(text, Type.ERR), err);
			scroll();
		} catch (BadLocationException e) {
			System.err.println("Could not print to output pane!");
		}
	}

	/**
	 * Build the full text String
	 * @param text The text to be displayed in the pane
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
	
	// Scroll to the end of the JTextPane
	private void scroll() {
		// TODO: Implement!
	}
}
