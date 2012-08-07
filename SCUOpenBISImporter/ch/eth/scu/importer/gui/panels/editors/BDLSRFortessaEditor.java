package ch.eth.scu.importer.gui.panels.editors;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.tree.TreeModel;

import ch.eth.scu.importer.gui.components.viewers.CustomTreeNode;
import ch.eth.scu.importer.gui.components.viewers.RootNode;
import ch.eth.scu.importer.gui.panels.openbis.OpenBISSpaceViewer;
import ch.eth.scu.importer.gui.panels.openbis.OpenBISSpaceViewer.CustomOpenBISNode;
import ch.eth.scu.importer.gui.panels.viewers.AbstractViewer;

/**
 * Metadata editor panel.
 * @author Aaron Ponti
 */
public class BDLSRFortessaEditor extends AbstractEditor {

	/**
	 * Constructor
	 */
	public BDLSRFortessaEditor(AbstractViewer dataViewer, 
			OpenBISSpaceViewer openBISViewer) {

		// Store the reference to the data and openBIS viewers
		super(dataViewer, openBISViewer);
		
		// Set sizes
		panel.setMinimumSize(new Dimension(400, 700));
		panel.setPreferredSize(new Dimension(400, 700));
	}

	/**
	 * ActionPerformed method from the ActionListener interface
	 * @param e The ActionEvent object
	 */
	public void actionPerformed(ActionEvent e) {
    }

	/**
	 * Renders all widgets on the panel
	 */
	public void render() {

		// Make sure both viewers have completed their models
		if (dataViewer.isReady() == false || openBISViewer.isReady() == false) {
			return;
		}
		
		// We get the models
		TreeModel dataModel = dataViewer.getDataModel();
		TreeModel openBISModel = openBISViewer.getDataModel();
		
		// We extract all experiments from the data model
		RootNode dataRoot = (RootNode) dataModel.getRoot();

		// First level are the XML files 
		int m = dataRoot.getChildCount();
		for (int i = 0; i < m; i++) {
			CustomTreeNode dataXmlNode = (CustomTreeNode) dataRoot.getChildAt(i);
			int n = dataXmlNode.getChildCount();
			for (int j = 0; j < n; j++) {
				CustomTreeNode dataExpNode = (CustomTreeNode) dataXmlNode.getChildAt(j);
				System.out.println(dataExpNode + ": " + dataExpNode.getType() );
			}
		}
		
		// We extract all experiments from the data model
		CustomOpenBISNode openBISRoot = 
				(CustomOpenBISNode) openBISModel.getRoot();

		// First level are spaces 
		m = openBISRoot.getChildCount();
		for (int i = 0; i < m; i++) {
			CustomOpenBISNode openBISSpaceNode = 
					(CustomOpenBISNode) openBISRoot.getChildAt(i);
			int n = openBISSpaceNode.getChildCount();
			for (int j = 0; j < n; j++) {
				CustomOpenBISNode openBISProjectNode = (CustomOpenBISNode) openBISSpaceNode.getChildAt(j);
				System.out.println(openBISProjectNode);
			}
		}
		
	}

	@Override
	public void update(Observable obs, Object arg) {
		render();
	}
}
