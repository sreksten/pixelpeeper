package com.threeamigos.pixelpeeper.implementations.ui.plugins;

import com.threeamigos.common.util.implementations.ui.StringHint;
import com.threeamigos.common.util.interfaces.ui.Hint;
import com.threeamigos.common.util.interfaces.ui.HintsProducer;
import com.threeamigos.common.util.interfaces.ui.InputConsumer;
import com.threeamigos.pixelpeeper.implementations.eventbus.EventBus;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.GridSpacingChangedEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.GridVisibilityChangedEvent;
import com.threeamigos.pixelpeeper.implementations.ui.InputAdapter;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.GridPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.KeyRegistry;
import jakarta.annotation.Nonnull;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GridPlugin extends AbstractMainWindowPlugin implements HintsProducer<String> {

    private final GridPreferences gridPreferences;
    private final Map<Integer, JMenuItem> gridSpacingBySize = new HashMap<>();
    private JMenuItem gridVisibleMenuItem;

    public GridPlugin(GridPreferences gridPreferences) {
        super();
        this.gridPreferences = gridPreferences;

        EventBus eventBus = EventBus.get();
        eventBus.subscribe(GridVisibilityChangedEvent.class,
                e -> gridVisibleMenuItem.setSelected(gridPreferences.isGridVisible()));
        eventBus.subscribe(GridSpacingChangedEvent.class,
                e -> updateGridSpacingMenu(gridPreferences.getGridSpacing()));
    }

    @Override
    public void createMenu() {

        JMenu imageHandlingMenu = mainWindow.getMenu("Image handling");

        gridVisibleMenuItem = addCheckboxMenuItem(imageHandlingMenu, "Show grid", KeyRegistry.SHOW_GRID_KEY,
                gridPreferences.isGridVisible(),
                event -> gridPreferences.setGridVisible(!gridPreferences.isGridVisible()));
        JMenu gridSpacingMenu = new JMenu("Grid spacing");
        imageHandlingMenu.add(gridSpacingMenu);
        for (int gridSpacing = GridPreferences.GRID_SPACING_MIN; gridSpacing <= GridPreferences.GRID_SPACING_MAX; gridSpacing += GridPreferences.GRID_SPACING_STEP) {
            final int currentSpacing = gridSpacing;
            JMenuItem gridSpacingItem = addCheckboxMenuItem(gridSpacingMenu, String.valueOf(gridSpacing),
                    KeyRegistry.NO_KEY, gridSpacing == gridPreferences.getGridSpacing(),
                    event -> gridPreferences.setGridSpacing(currentSpacing));
            gridSpacingBySize.put(gridSpacing, gridSpacingItem);
        }
    }

    private void updateGridSpacingMenu(final int gridSpacing) {
        for (Map.Entry<Integer, JMenuItem> entry : gridSpacingBySize.entrySet()) {
            entry.getValue().setSelected(entry.getKey() == gridSpacing);
        }
    }

    public InputConsumer getInputConsumer() {
        return new InputAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyRegistry.SHOW_GRID_KEY.getKeyCode()) {
                    processShowGrid(e);
                } else if (e.getKeyCode() == KeyRegistry.ENLARGE_KEY.getKeyCode()) {
                    processEnlargeKey(e);
                } else if (e.getKeyCode() == KeyRegistry.REDUCE_KEY.getKeyCode()) {
                    processReduceKey(e);
                }
            }

            private void processReduceKey(KeyEvent e) {
                if (gridPreferences.isGridVisible()) {
                    int spacing = gridPreferences.getGridSpacing();
                    if (spacing > GridPreferences.GRID_SPACING_MIN) {
                        gridPreferences.setGridSpacing(spacing - GridPreferences.GRID_SPACING_STEP);
                    }
                    e.consume();
                }
            }

            private void processEnlargeKey(KeyEvent e) {
                if (gridPreferences.isGridVisible()) {
                    int spacing = gridPreferences.getGridSpacing();
                    if (spacing < GridPreferences.GRID_SPACING_MAX) {
                        gridPreferences.setGridSpacing(spacing + GridPreferences.GRID_SPACING_STEP);
                    }
                    e.consume();
                }
            }

            private void processShowGrid(KeyEvent e) {
                gridPreferences.setGridVisible(!gridPreferences.isGridVisible());
                e.consume();
            }
        };
    }

    @Override
    public @Nonnull Collection<Hint<String>> getHints() {
        Collection<Hint<String>> hints = new ArrayList<>();
        hints.add(new StringHint("Press G to hide or show a grid."));
        hints.add(new StringHint(
                "If the grid is visible you can change its size using the plus or minus key on the numeric keypad."));
        return hints;
    }

}
