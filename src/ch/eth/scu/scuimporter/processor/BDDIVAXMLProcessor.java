package ch.eth.scu.scuimporter.processor;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * This Processor parses "BD BioSciences FACSDivaª Software" XML files.
 * @author Aaron Ponti
 */
public class BDDIVAXMLProcessor extends Processor {

	/* Private instance variables */
	private String filename;
	private String version;
	private String releaseVersion;
	private DocumentBuilder parser = null;
	private Document doc = null;

	/* Public instance variables */

	/**
	 * ArrayList of Experiment's
	 */
	public ArrayList<Experiment> experiments = new ArrayList<Experiment>();

	/**
	 * Constructor
	 * @param filename Name with full path of the file to be opened.
	 */
	public BDDIVAXMLProcessor(String filename) {

		// Set the filename
		this.filename = filename;

		// Instantiate the factory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringComments(true); 	// Ignore comments 
		factory.setCoalescing(true); 		// Convert CDATA to Text nodes 
		factory.setNamespaceAware(false); 	// No namespaces
		factory.setValidating(false);		// Don't validate DTD

		// Create and store a parser
		try {
			parser = factory.newDocumentBuilder();
		} catch ( ParserConfigurationException pce) {
			pce.printStackTrace();
			parser = null;
		}
	}

	/**
	 * Return information regarding the file format.
	 * @return descriptive String for the Processor.
	 */
	@Override
	public String info() {
		return "BD BioSciences FACSDiva\u2122 Software";
	}

	/**
	 * Parse the file to extract data and metadata. 
	 * @return true if parsing was successful, false otherwise.
	 */
	@Override
	public boolean parse() {

		// Check the parser
		if (parser == null) {
			System.out.println("Invalid parser. Aborting.");
			return false;
		}

		// Parse the file
		try {
			File file = new File(filename);		
			doc = parser.parse(file);
			//doc.getDocumentElement().normalize();
		} catch (SAXException se) {
			se.printStackTrace();
			doc = null;
			return false;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			doc = null;
			return false;
		}	

		// Extract info
		return processDoc();

	}

	/**
	 * Return a String representation of the DBDIVAXMLProcessor.
	 * @return String containing a description of the DBDIVAXMLProcessor. 
	 */
	public String toString() {
		String str = "XML file: " + filename + (" (version: " + version + ", " +
				"release version: " + releaseVersion + ")\n\n");
		for ( Experiment e : experiments) {
			str += e.toString() + "\n";
		}
		return str;
	}

	/**
	 * Parse and extract relevant information from the XML file.
	 * @return true if the parsing was successful, false otherwise.
	 */
	private boolean processDoc() {

		// Nothing to parse, return
		if (doc == null) {
			System.out.println("Document not parsed. Aborting.");
			return false;
		}

		// Get the root node
		Node rootNode = doc.getDocumentElement();

		// Get the version information
		this.version = rootNode.getAttributes().getNamedItem("version").getNodeValue();
		this.releaseVersion = rootNode.getAttributes().getNamedItem("release_version").getNodeValue();

		// Get children
		NodeList children = rootNode.getChildNodes();

		// Process them
		for ( int i = 0; i < children.getLength(); i++ ) {

			Node n = children.item(i);

			if (n.getNodeName().equals("experiment")) {

				// Tray
				experiments.add(new Experiment(n));

			} else {

				// Skip
			}
		}

		return true;
	}

	/**
	 * Class that represents an experiment parsed from the XML.
	 * @author Aaron Ponti
	 *
	 */
	public class Experiment {

		/* Public instance variables */

		/**
		 * Experiment name
		 */
		public String name;

		/**
		 * Experiment date
		 */
		public String date;

		/**
		 * Experiment's owner name
		 */
		public String owner_name;

		// An Experiment can contain TRAYS that in turn contain SPECIMENs which contain TUBEs,
		// or directly SPECIMENs containing TUBEs.

		/**
		 * ArrayList of Tray's
		 */
		public ArrayList<Tray> trays = new ArrayList<Tray>();

		/**
		 * ArrayList of Specimen's
		 */
		public ArrayList<Specimen> specimens = new ArrayList<Specimen>();

		/**
		 * Constructor
		 * @param expNode DOM node that refers to an Experiment.
		 */
		public Experiment(org.w3c.dom.Node expNode) {

			// Get the experiment name
			this.name = expNode.getAttributes().getNamedItem("name").getNodeValue();

			// Get children
			NodeList children = expNode.getChildNodes();

			// Process them
			for ( int i = 0; i < children.getLength(); i++ ) {

				Node n = children.item(i);

				if (n.getNodeName().equals("date")) {

					// Experiment date
					date = n.getTextContent();

				} else if (n.getNodeName().equals("owner_name")) {

					// Experiment owner
					owner_name = n.getTextContent();

				} else if (n.getNodeName().equals("tray")) {

					// Tray
					trays.add(new Tray(n));

				} else if (n.getNodeName().equals("specimen")) {

					// Specimen (level 1, since it is a child of an Experiment)  
					specimens.add(new Specimen(n, 1));

				} else {

					// Skip
				}
			}
		}

		/**
		 * Return summary of the extracted Experiment node.
		 * @return string with a summary of the Experiment info.
		 */
		public String toString() {
			String str =  "[ Experiment ], name: " + name + " (owner: " + owner_name + ", " +
					"date: " + date + ").\n";
			for ( Tray t : trays) {
				str += t.toString() + "\n";
			}
			for ( Specimen s : specimens) {
				str += s.toString() + "\n";
			}
			return str;
		}

		/**
		 * Accessory function to repeat a String n times: repeat( "abc", 2 ) => "abcabc".
		 * @param str	String to be repeated.
		 * @param n		Number of times to repeat the String.
		 * @return		The repeated String.
		 * TODO Extract this into some util package.
		 */
		private String repeat(String str, int n){
			StringBuilder ret = new StringBuilder();
			for(int i = 0;i < n;i++) {
				ret.append(str);
			}
			return ret.toString();
		}

		/**
		 * Class that represents a tray parsed from the XML.
		 * @author Aaron Ponti
		 */
		public class Tray {

			/* Public instance variables */

			/** 
			 * Tray name
			 */
			public String name;

			/** 
			 * Tray type
			 */
			public String tray_type;

			/**
			 * Number of rows (String)
			 */
			public String rows;

			/**
			 * Number of cols (String)
			 */
			public String cols;

			/**
			 * Tray orientation
			 */			
			public String orientation;

			/**
			 * ArrayList of Specimen's
			 */
			public ArrayList<Specimen> specimens = new ArrayList<Specimen>();

			/**
			 * Constructor
			 * @param trayNode DOM node that refers to a Tray.
			 */
			public Tray(org.w3c.dom.Node trayNode) {

				// Get the attributes
				NamedNodeMap attrs = trayNode.getAttributes();

				name        = attrs.getNamedItem("name").getNodeValue();
				tray_type   = attrs.getNamedItem("tray_type").getNodeValue();;
				rows        = attrs.getNamedItem("rows").getNodeValue();
				cols        = attrs.getNamedItem("cols").getNodeValue();
				orientation = attrs.getNamedItem("Orientation").getNodeValue();

				// Get children
				NodeList children = trayNode.getChildNodes();

				// Process them
				for ( int i = 0; i < children.getLength(); i++ ) {

					Node n = children.item(i);

					if (n.getNodeName().equals("specimen")) {

						// Specimen (level 2, since it is a child of a Tray)  
						specimens.add(new Specimen(n, 2));

					} else {

						// Skip
					}

				}

			}

			/**
			 * Return summary of the extracted Tray node.
			 * @return string with a summary of the Tray info.
			 */
			public String toString() {
				String str =  "|__[ Tray ], name: " + name + " (type: " + tray_type + ", rows: " +
						rows + ", cols: " + cols + ", orientation: " + orientation + ")\n";
				for ( Specimen s : specimens) {
					str += s.toString() + "\n";
				}
				return str;
			}

		}

		/**
		 * Class that represents a specimen parsed from the XML.
		 * A Specimen can be a child of a Tray or directly of an Experiment.
		 * @author Aaron Ponti
		 */
		public class Specimen {

			/* Private instance variables */
			private int level;

			/* Public instance variables */

			/**
			 * Specimen name
			 */
			public String name;

			/**
			 * ArrayList of Tube's
			 */			
			public ArrayList<Tube> tubes = new ArrayList<Tube>();

			/**
			 * Constructor.
			 * @param specimenNode DOM node that refers to a Specimen.
			 * @param level 1 if the Specimen is a child of an Experiment, 
			 * 		  		2 if it is the child of a Tray
			 * @throws IllegalArgumentException
			 */
			public Specimen(org.w3c.dom.Node specimenNode, int level) throws IllegalArgumentException {

				// Check the level
				if (level < 1 || level > 2) {
					throw new IllegalArgumentException("Error: level must be either 1 or 2.");
				}

				// Store the level
				this.level = level;

				// Get the attributes
				NamedNodeMap attrs = specimenNode.getAttributes();

				name = attrs.getNamedItem("name").getNodeValue();

				// Get children
				NodeList children = specimenNode.getChildNodes();

				// Process them
				for ( int i = 0; i < children.getLength(); i++ ) {

					Node n = children.item(i);

					if (n.getNodeName().equals("tube")) {

						// Tray
						tubes.add(new Tube(n, level + 1));

					} else {

						// Skip
					}
				}

			}

			/**
			 * Return summary of the extracted Specimen node.
			 * @return string with a summary of the Specimen info.
			 */
			public String toString() {
				String str =  "|" + repeat("__", level) + "[ Specimen ], name: " + name + "\n";
				for ( Tube t : tubes) {
					str += t.toString() + "\n";
				}
				return str;
			}		

			/**
			 * Class that represents a tube parsed from the XML.
			 * A Tube is always a child of a Specimen.
			 * @author Aaron Ponti
			 */
			public class Tube {

				/* Private instance variables */
				private int level;

				/* Public instance variables */  		

				/**
				 * Tube name
				 */
				public String name;

				/**
				 * Date of the acquisition of Tube
				 */
				public String date;

				/**
				 * Name of the fcs file associated with the Tube 
				 */				
				public String dataFilename;

				/**
				 * Constructor.
				 * @param tubeNode DOM node that refers to a Tube.
				 * @param level 2 if the parent Specimen is a child of an Experiment, 
				 * 		  		3 if the parent Specimen is a child of a Tray.
				 * @throws IllegalArgumentException
				 */
				public Tube(org.w3c.dom.Node tubeNode, int level) throws IllegalArgumentException{

					// Check the level
					if (level < 1 || level > 3) {
						throw new IllegalArgumentException("Error: level must be either 2 or 3.");
					}

					// Store the level
					this.level = level;

					// Get the attributes
					NamedNodeMap attrs = tubeNode.getAttributes();

					name = attrs.getNamedItem("name").getNodeValue();

					// Get children
					NodeList children = tubeNode.getChildNodes();

					// Process them
					for ( int i = 0; i < children.getLength(); i++ ) {

						Node n = children.item(i);

						if (n.getNodeName().equals("date")) {

							// Tube date
							date = n.getTextContent();

						} else if (n.getNodeName().equals("data_filename")) {

							// Tube file name
							dataFilename = n.getTextContent();

						} else {

							// Skip
						}
					}

				}

				/**
				 * Return summary of the extracted Tube node.
				 * @return string with a summary of the Tube info.
				 */
				public String toString() {
					String str =  "|" + repeat("__", level) + "[ Tube ], name: " + name + " (date: " + date + 
							", file name: " + dataFilename + ")";
					return str;
				}
			}
		}

	}

}
