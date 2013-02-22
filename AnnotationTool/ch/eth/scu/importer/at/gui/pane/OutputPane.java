package ch.eth.scu.importer.at.gui.pane;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JTextArea;

/**
 * Output pane to display any output from the application
 * @author Aaron Ponti
 *
 */
public class OutputPane extends JTextArea {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public OutputPane() {
		setRows(5);
		setEditable(false);
		display("Ready");
	}

	/**
	 * Display the passed string
	 * @param text String to be displayed
	 */
	public void display(String text) {
		setText((new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")).format(
						new Date()) + ": " + text);
	}

}
