package ch.eth.scu.scuimporter.processor;

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * 
 * @author Aaron Ponti
 *
 */
public class FCSXMLProcessor extends Processor {

	/**
	 * 
	 * @param filename
	 */
	public FCSXMLProcessor(String filename) {
		this.filename = filename;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean parse() throws IOException {

		try {
			File file = new File(filename);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			//dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
		} catch (Exception e) {
			e.printStackTrace();
		}
		valid = true;
		return true;

	}
	
	/**
	 * 
	 */
	public String toString() {
		return ("FCSXMLProcessor - to be implemented.");
	}
	
	/* Instance variables */
	private String filename;
	boolean valid = false;
}

