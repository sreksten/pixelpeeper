package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import com.threeamigos.imageviewer.interfaces.preferences.flavours.HintsPreferences;
import com.threeamigos.imageviewer.interfaces.ui.HintsCollector;
import com.threeamigos.imageviewer.interfaces.ui.HintsWindow;

public class HintsWindowImpl implements HintsWindow {

	private final HintsPreferences hintsPreferences;
	private final List<String> hints;

	private int hintIndex;

	public HintsWindowImpl(HintsPreferences hintsPreferences, HintsCollector hintsCollector) {
		this.hintsPreferences = hintsPreferences;
		hints = new ArrayList<>();
		hints.addAll(hintsCollector.getHints());
	}

	private String getNextHint() {
		hintIndex = (hintsPreferences.getLastHintIndex() + 1) % hints.size();
		hintsPreferences.setLastHintIndex(hintIndex);
		return hints.get(hintIndex);
	}

	private String getPreviousHint() {
		hintIndex = (hintsPreferences.getLastHintIndex() - 1) % hints.size();
		hintsPreferences.setLastHintIndex(hintIndex);
		return hints.get(hintIndex);
	}

	@Override
	public void showHints(Component component) {

		Box panel = Box.createVerticalBox();

		JTextArea hintLabel = new JTextArea(getNextHint());
		hintLabel.setSize(400, 100);
		hintLabel.setLineWrap(true);
		hintLabel.setWrapStyleWord(true);
		hintLabel.setEditable(false);
		hintLabel.setBorder(BorderFactory.createTitledBorder("Hint"));
		panel.add(hintLabel);

		panel.add(Box.createVerticalStrut(5));

		Box buttonsBox = Box.createHorizontalBox();
		JButton previousHintButton = new JButton("Previous hint");
		previousHintButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hintLabel.setText(getPreviousHint());
			}
		});
		buttonsBox.add(previousHintButton);
		buttonsBox.add(Box.createGlue());
		JButton nextHintButton = new JButton("Next hint");
		nextHintButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hintLabel.setText(getNextHint());
			}
		});
		buttonsBox.add(nextHintButton);
		panel.add(buttonsBox);

		JOptionPane.showOptionDialog(component, panel, "3AM Image Viewer", JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE, null, null, null);
	}

}
