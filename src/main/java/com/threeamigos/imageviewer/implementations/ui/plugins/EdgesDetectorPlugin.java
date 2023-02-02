package com.threeamigos.imageviewer.implementations.ui.plugins;

import java.beans.PropertyChangeEvent;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.imageviewer.interfaces.edgedetect.ui.EdgesDetectorPreferencesSelectorFactory;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.EdgesDetectorPreferences;

public class EdgesDetectorPlugin extends AbstractMainWindowPlugin {

	private final EdgesDetectorPreferences edgesDetectorPreferences;
	private final EdgesDetectorPreferencesSelectorFactory edgesDetectorPreferencesSelectorFactory;

	private JMenuItem showEdgesMenuItem;
	private Map<EdgesDetectorFlavour, JMenuItem> edgesDetectorFlavourMenuItemsByFlavour = new EnumMap<>(
			EdgesDetectorFlavour.class);

	public EdgesDetectorPlugin(EdgesDetectorPreferences edgesDetectorPreferences,
			EdgesDetectorPreferencesSelectorFactory edgesDetectorPreferencesSelectorFactory) {
		super();
		this.edgesDetectorPreferences = edgesDetectorPreferences;
		this.edgesDetectorPreferencesSelectorFactory = edgesDetectorPreferencesSelectorFactory;
	}

	@Override
	public void createMenu() {

		JMenu edgesDetectorMenu = mainWindow.getMenu("Edges Detector");

		showEdgesMenuItem = addCheckboxMenuItem(edgesDetectorMenu, "Show edges", SHOW_EDGES_KEY,
				edgesDetectorPreferences.isShowEdges(), event -> {
					edgesDetectorPreferences.setShowEdges(!edgesDetectorPreferences.isShowEdges());
				});
		JMenu edgesDetectorFlavourMenuItem = new JMenu("Flavours");
		edgesDetectorMenu.add(edgesDetectorFlavourMenuItem);
		for (EdgesDetectorFlavour flavour : EdgesDetectorFlavour.values()) {
			JMenuItem flavourMenuItem = addCheckboxMenuItem(edgesDetectorFlavourMenuItem, flavour.getDescription(), -1,
					edgesDetectorPreferences.getEdgesDetectorFlavour() == flavour, event -> {
						updateEdgesDetectorFlavour(flavour);
					});
			edgesDetectorFlavourMenuItemsByFlavour.put(flavour, flavourMenuItem);
		}
		addMenuItem(edgesDetectorMenu, "Edge Detector parameters", SHOW_EDGES_DETETECTOR_PARAMETERS_KEY, event -> {
			edgesDetectorPreferencesSelectorFactory.createSelector(mainWindow.getComponent())
					.selectParameters(mainWindow.getComponent());
		});
	}

	private void updateEdgesDetectorFlavour(EdgesDetectorFlavour flavour) {
		edgesDetectorPreferences.setEdgesDetectorFlavour(flavour);
		for (Entry<EdgesDetectorFlavour, JMenuItem> entry : edgesDetectorFlavourMenuItemsByFlavour.entrySet()) {
			entry.getValue().setSelected(edgesDetectorPreferences.getEdgesDetectorFlavour() == entry.getKey());
		}
		if (edgesDetectorPreferences.isShowEdges()) {
			firePropertyChange(CommunicationMessages.REQUEST_EDGES_CALCULATION, null, null);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(CommunicationMessages.CHANGE_EDGES_VISIBILITY)) {
			showEdgesMenuItem.setSelected(edgesDetectorPreferences.isShowEdges());
		}
	}
}
