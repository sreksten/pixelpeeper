package com.threeamigos.pixelpeeper.implementations.ui.plugins;

import com.threeamigos.pixelpeeper.implementations.eventbus.EventBus;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.RepaintRequestEvent;
import com.threeamigos.pixelpeeper.interfaces.ui.MainWindow;
import com.threeamigos.pixelpeeper.interfaces.ui.MainWindowPlugin;

public abstract class AbstractMainWindowPlugin implements MainWindowPlugin {

    protected MainWindow mainWindow;

    @Override
    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        createMenu();
    }

    public abstract void createMenu();

    protected void repaint() {
        EventBus.get().publish(new RepaintRequestEvent());
    }
}
