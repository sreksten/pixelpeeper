package com.threeamigos.pixelpeeper.implementations.ui.plugins;

import com.threeamigos.common.util.interfaces.ui.InputConsumer;
import com.threeamigos.pixelpeeper.implementations.ui.InputAdapter;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.ui.EdgesDetectorPreferencesSelectorFactory;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.KeyRegistry;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

public class EdgesDetectorPlugin extends AbstractMainWindowPlugin implements PropertyChangeListener {

    private final EdgesDetectorPreferences edgesDetectorPreferences;
    private final EdgesDetectorPreferencesSelectorFactory edgesDetectorPreferencesSelectorFactory;

    private JMenuItem showEdgesMenuItem;
    private final Map<EdgesDetectorFlavour, JMenuItem> edgesDetectorFlavourMenuItemsByFlavour = new EnumMap<>(
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

        showEdgesMenuItem = addCheckboxMenuItem(edgesDetectorMenu, "Show edges", KeyRegistry.SHOW_EDGES_KEY,
                edgesDetectorPreferences.isShowEdges(), event -> toggleShowEdges());
        JMenu edgesDetectorFlavourMenuItem = new JMenu("Flavours");
        edgesDetectorMenu.add(edgesDetectorFlavourMenuItem);
        for (EdgesDetectorFlavour flavour : EdgesDetectorFlavour.getActiveValues()) {
            JMenuItem flavourMenuItem = addCheckboxMenuItem(edgesDetectorFlavourMenuItem, flavour.getDescription(),
                    KeyRegistry.NO_KEY, edgesDetectorPreferences.getEdgesDetectorFlavour() == flavour,
                    event -> updateEdgesDetectorFlavour(flavour));
            edgesDetectorFlavourMenuItemsByFlavour.put(flavour, flavourMenuItem);
        }
        addMenuItem(edgesDetectorMenu, "Edge Detector parameters", KeyRegistry.SHOW_EDGES_DETETECTOR_PARAMETERS_KEY,
                event -> showEdgesDetectorParametersWindow());
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

    private void toggleShowEdges() {
        edgesDetectorPreferences.setShowEdges(!edgesDetectorPreferences.isShowEdges());
    }

    private void showEdgesDetectorParametersWindow() {
        edgesDetectorPreferencesSelectorFactory.createSelector(mainWindow.getComponent())
                .selectParameters(mainWindow.getComponent());
    }

    public InputConsumer getInputConsumer() {

        return new InputAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyRegistry.SHOW_EDGES_KEY.getKeyCode()) {
                    toggleShowEdges();
                } else if (key == KeyRegistry.SHOW_EDGES_DETETECTOR_PARAMETERS_KEY.getKeyCode()) {
                    showEdgesDetectorParametersWindow();
                }
            }
        };
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(CommunicationMessages.EDGES_VISIBILITY_CHANGED)) {
            showEdgesMenuItem.setSelected(edgesDetectorPreferences.isShowEdges());
        }
    }
}
