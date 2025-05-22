package com.threeamigos.pixelpeeper.implementations.ui;

import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ShortcutsWindowPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.KeyRegistry;
import com.threeamigos.pixelpeeper.interfaces.ui.ShortcutsWindow;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.stream.Stream;

public class ShortcutsWindowImpl extends JFrame implements ShortcutsWindow {

    private static final long serialVersionUID = 1L;

    public ShortcutsWindowImpl(ShortcutsWindowPreferences shortcutsWindowPreferences) {
        super("Shortcuts");
        setMinimumSize(new Dimension(250, 350));

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shortcutsWindowPreferences.setVisible(false);
                setVisible(false);
            }
        });

        setLayout(new BorderLayout());

        JTable table = buildShortcutsTable();
        TableColumn keyColumn = table.getColumnModel().getColumn(0);
        keyColumn.setMinWidth(100);
        keyColumn.setMaxWidth(100);
        keyColumn.setPreferredWidth(100);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);

        add(scrollPane, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                shortcutsWindowPreferences.setWidth(getWidth());
                shortcutsWindowPreferences.setHeight(getHeight());
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                shortcutsWindowPreferences.setX(getX());
                shortcutsWindowPreferences.setY(getY());
            }
        });

        pack();
        setResizable(true);
        setLocation(shortcutsWindowPreferences.getX(), shortcutsWindowPreferences.getY());
        setSize(shortcutsWindowPreferences.getWidth(), shortcutsWindowPreferences.getHeight());
        setVisible(shortcutsWindowPreferences.isVisible());
    }

    private JTable buildShortcutsTable() {
        Object[] mappedValues = Stream.of(KeyRegistry.values())
                .filter(entry -> entry.getKeyCode() > 0)
                .map(entry -> new Object[]{entry.getKeyName(), entry.getAction()}).toArray();
        Object[][] values = new Object[mappedValues.length][2];
        for (int i = 0; i < mappedValues.length; i++) {
            values[i][0] = ((Object[]) mappedValues[i])[0];
            values[i][1] = ((Object[]) mappedValues[i])[1];
        }
        return new JTable(values, new String[]{"Key", "Action"});
    }
}
