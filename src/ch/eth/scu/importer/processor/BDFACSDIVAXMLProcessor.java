package ch.eth.scu.importer.processor;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import ch.eth.scu.importer.processor.BDFACSDIVAXMLProcessor.Experiment.Specimen;
import ch.eth.scu.importer.processor.BDFACSDIVAXMLProcessor.Experiment.Tray;
import ch.eth.scu.importer.processor.BDFACSDIVAXMLProcessor.Experiment.Specimen.Tube;

/**
 * BDFACSDIVAXMLProcessor parses "BD BioSciences FACSDiva" XML files.
 * @author Aaron Ponti
 */
public class BDFACSDIVAXMLProcessor extends AbstractProcessor {

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
	public BDFACSDIVAXMLProcessor(String filename) {

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
			System.out.println("The file appears to be contain invalid XML.");
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
		return (new File(filename)).getName();
	}

	/**
	 * Return a String with the Processor attributes
	 * @return Comma-separated String with attribute key: value pairs.
	 */
	public String attributesToString() {
		String str =  "full filename: " + filename + ", " + 
				"version: " + version + ", " + 
				"release version: " + releaseVersion;
		return str;
	}

	/**
	 * Return a textual tree representation of the DBDIVAXMLProcessor 
	 * @return String containing a tree representation of the DBDIVAXMLProcessor
	 */	
	public String treeView() {

		String str = "[XML file] " + toString() + " (" +
				attributesToString() + ").\n|\n";        

		for (Experiment e : experiments) {

			str +=  "[ Experiment ], name: " + e.name + " (" +
					e.attributesToString() + ").\n";

			for (Tray t : e.trays) {

				str +=  "|__[ Tray ], name: " + t.name + " (" +
						t.attributesToString() + ").\n";

				for (Specimen s : t.specimens) {

					str +=  "|____[ Specimen ], name: " + s.name + " (" +
							s.attributesToString() + ").\n";

					for (Tube tb : s.tubes) {

						str +=  "|______[ Tube ], name: " + tb.name + " (" +
								tb.attributesToString() + ").\n";
					}
				}

			}

			for (Specimen s : e.specimens) {

				str +=  "|__[ Specimen ], name: " + s.name + "(" +
						s.attributesToString() + ").\n";

				for (Tube tb : s.tubes) {

					str +=  "|____[ Tube ], name: " + tb.name + " (" +
							tb.attributesToString() + ").\n";
				}
			}

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

		// An Experiment can contain TRAYS that in turn contain SPECIMENs 
		// which contain TUBEs, or directly SPECIMENs containing TUBEs.

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

					// Specimen  
					specimens.add(new Specimen(n));

				} else {

					// Skip
				}
			}
		}

		/**
		 * Return a String representation of the extracted Experiment node.
		 * @return String representation of the Experiment node.
		 */
		public String toString() {
			String str =  "[Experiment] " + name;
			return str;
		}

		/**
		 * Return a String with the Experiment attributes
		 * @return Comma-separated String with attribute key: value pairs.
		 */
		public String attributesToString() {
			String str =  "owner: " + owner_name + ", " + 
					"date: " + date;
			return str;
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
			 * Number of columns (String)
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

						// Specimen  
						specimens.add(new Specimen(n));

					} else {

						// Skip
					}

				}

			}

			/**
			 * Return a String representation of the extracted Tray node.
			 * @return String representation of the Tray node.
			 */
			public String toString() {
				String str =  "[Tray] " + name;
				return str;
			}

			/**
			 * Return a String with the Tray attributes
			 * @return Comma-separated String with attribute key: value pairs.
			 */
			public String attributesToString() {
				String str =  "type: " + tray_type + ", " + 
						"rows: " + rows + ", " +
						"columns: " + cols + ", " +
						"orientation: " + orientation;
				return str;
			}
		}

		/**
		 * Class that represents a specimen parsed from the XML.
		 * A Specimen can be a child of a Tray or directly of an Experiment.
		 * @author Aaron Ponti
		 */
		public class Specimen {

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
			 */
			public Specimen(org.w3c.dom.Node specimenNode) {

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
						tubes.add(new Tube(n));

					} else {

						// Skip
					}
				}

			}

			/**
			 * Return a String representation of the extracted Specimen node.
			 * @return String representation of the Specimen node.
			 */
			public String toString() {
				String str =  "[Specimen] " + name;
				return str;
			}

			/**
			 * Return a String with the Specimen attributes
			 * @return Comma-separated String with attribute key: value pairs.
			 */
			public String attributesToString() {
				String str =  "no attributes";
				return str;
			}


			/**
			 * Class that represents a tube parsed from the XML.
			 * A Tube is always a child of a Specimen.
			 * @author Aaron Ponti
			 */
			public class Tube {

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
				 */
				public Tube(org.w3c.dom.Node tubeNode) {

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
				 * Return a String representation of the extracted Tube node.
				 * @return String representation of the Tube node.
				 */
				public String toString() {
					String str =  "[Tube] " + name + "   (" + dataFilename + ")";
					return str;
				}

				/**
				 * Return a String with the Tube attributes
				 * @return Comma-separated String with attribute key: value pairs.
				 */
				public String attributesToString() {
					String str =  "date: " + date + ", " + 
							"file name: " + dataFilename;
					return str;
				}
			}
		}

	}

}
