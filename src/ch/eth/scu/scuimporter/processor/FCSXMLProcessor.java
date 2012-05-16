package ch.eth.scu.scuimporter.processor;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * 
 * @author Aaron Ponti
 *
 */
public class FCSXMLProcessor extends Processor {

	/* Instance variables */
	private String filename;
	private String version;
	private String releaseVersion;
	private DocumentBuilder parser = null;
	private Document doc = null;
	private ArrayList<Experiment> experiments = new ArrayList<Experiment>();

	/**
	 * 
	 * @param filename
	 * @throws ParserConfigurationException 
	 */
	public FCSXMLProcessor(String filename) throws ParserConfigurationException {

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
	 * Return information about the Processor
	 */
	public String info() {
		return "BD BioSciences FACSDiva\u2122 Software";
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
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
	 * 
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
	 * Parse and extract relevant information from the XML file
	 * @return true if the parsing was successful, false otherwise
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
	 * Class that represents an experiment parsed from the XML
	 * @author Aaron Ponti
	 *
	 */
	public class Experiment {

		private String name;
		private String date;
		private String owner_name;

		/* An Experiment can contain TRAYS that in turn contain SPECIMENs which contain TUBEs,
		 * or directly SPECIMENs containing TUBEs.
		 */
		private ArrayList<Tray> trays = new ArrayList<Tray>();
		private ArrayList<Specimen> specimens = new ArrayList<Specimen>();

		/**
		 * Constructor
		 * @param expNode
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
		 * Return summary of the extracted Experiment node
		 */
		public String toString() {
			String str =  "Experiment: " + name + " (owner: " + owner_name + ", " +
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
		 * Accessory function to repeat a String n times: repeat( "abc", 2 ) => "abcabc"
		 * @param str	String to be repeated
		 * @param n		Number of times to repeat the String
		 * @return		The repeated String
		 */
		private String repeat(String str, int n){
			StringBuilder ret = new StringBuilder();
			for(int i = 0;i < n;i++) {
				ret.append(str);
			}
			return ret.toString();
		}

		/**
		 * 
		 * @author Aaron Ponti
		 *
		 */
		public class Tray {

			public String name;
			public String tray_type;
			public String rows;
			public String cols;
			public String orientation;
			private ArrayList<Specimen> specimens = new ArrayList<Specimen>();

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
			 * 
			 */
			public String toString() {
				String str =  "|__Tray, name: " + name + " (type: " + tray_type + ", rows: " +
						rows + ", cols: " + cols + ", orientation: " + orientation + ")\n";
				for ( Specimen s : specimens) {
					str += s.toString() + "\n";
				}
				return str;
			}

		}

		/**
		 * A specimen can be a child of a Tray or directly of an Experiment
		 * @author Aaron Ponti
		 *
		 */
		public class Specimen {

			private String name;
			private int level;
			
			private ArrayList<Tube> tubes = new ArrayList<Tube>();

			public Specimen(org.w3c.dom.Node specimenNode, int level) {

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
			 * 
			 */
			public String toString() {
				String str =  "|" + repeat("__", level) + " Specimen: " + name + "\n";
				for ( Tube t : tubes) {
					str += t.toString() + "\n";
				}
				return str;
			}		
		}

		/**
		 * 
		 * @author Aaron Ponti
		 *
		 */
		public class Tube {

			private String name;
			private String date;
			private int level;
			private String dataFilename;
			
			/**
			 * Constructor
			 * @param tubeNode
			 */
			public Tube(org.w3c.dom.Node tubeNode, int level) {

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
			
			public String toString() {
				String str =  "|" + repeat("__", level) + "Tube: " + name + " (date: " + date + 
						", file name: " + dataFilename + ")";
				return str;
			}
		}
	}

}
