package com.threeamigos.pixelpeeper.implementations.ui.plugins;

import com.threeamigos.common.util.interfaces.ui.InputConsumer;
import com.threeamigos.pixelpeeper.implementations.ui.InputAdapter;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.filters.ui.FilterPreferencesSelectorFactory;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.KeyRegistry;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

public class FilterPlugin extends AbstractMainWindowPlugin implements PropertyChangeListener {

    private final FilterPreferences filterPreferences;
    private final FilterPreferencesSelectorFactory filterPreferencesSelectorFactory;

    private JMenuItem showFilterResultsMenuItem;
    private final Map<FilterFlavor, JMenuItem> filterFlavorMenuItemsByFlavor = new EnumMap<>(
            FilterFlavor.class);

    public FilterPlugin(FilterPreferences filterPreferences,
                        FilterPreferencesSelectorFactory filterPreferencesSelectorFactory) {
        super();
        this.filterPreferences = filterPreferences;
        this.filterPreferencesSelectorFactory = filterPreferencesSelectorFactory;
    }

    @Override
    public void createMenu() {

        JMenu filterMenu = mainWindow.getMenu("Filters");

        showFilterResultsMenuItem = addCheckboxMenuItem(filterMenu, "Show results", KeyRegistry.SHOW_RESULTS_KEY,
                filterPreferences.isShowResults(), event -> toggleShowResults());
        JMenu filterFlavorMenuItem = new JMenu("Flavors");
        filterMenu.add(filterFlavorMenuItem);
        for (FilterFlavor flavor : FilterFlavor.getActiveValues()) {
            JMenuItem flavorMenuItem = addCheckboxMenuItem(filterFlavorMenuItem, flavor.getDescription(),
                    KeyRegistry.NO_KEY, filterPreferences.getFilterFlavor() == flavor,
                    event -> updateFilterFlavor(flavor));
            filterFlavorMenuItemsByFlavor.put(flavor, flavorMenuItem);
        }
        addMenuItem(filterMenu, "Filter preferences", KeyRegistry.SHOW_FILTER_PARAMETERS_KEY,
                event -> showFilterParametersWindow());
    }

    private void updateFilterFlavor(FilterFlavor flavor) {
        filterPreferences.setFilterFlavor(flavor);
        for (Entry<FilterFlavor, JMenuItem> entry : filterFlavorMenuItemsByFlavor.entrySet()) {
            entry.getValue().setSelected(filterPreferences.getFilterFlavor() == entry.getKey());
        }
        if (filterPreferences.isShowResults()) {
            firePropertyChange(CommunicationMessages.REQUEST_FILTER_CALCULATION, null, null);
        }
    }

    private void toggleShowResults() {
        filterPreferences.setShowResults(!filterPreferences.isShowResults());
    }

    private void showFilterParametersWindow() {
        filterPreferencesSelectorFactory.createSelector(mainWindow.getComponent())
                .selectParameters(mainWindow.getComponent());
    }

    public InputConsumer getInputConsumer() {

        return new InputAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyRegistry.SHOW_RESULTS_KEY.getKeyCode()) {
                    toggleShowResults();
                } else if (key == KeyRegistry.SHOW_FILTER_PARAMETERS_KEY.getKeyCode()) {
                    showFilterParametersWindow();
                }
            }
        };
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(CommunicationMessages.FILTER_VISIBILITY_CHANGED)) {
            showFilterResultsMenuItem.setSelected(filterPreferences.isShowResults());
        }
    }
}
