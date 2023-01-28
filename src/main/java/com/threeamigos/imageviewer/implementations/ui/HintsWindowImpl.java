package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.threeamigos.imageviewer.interfaces.preferences.flavours.HintsPreferences;
import com.threeamigos.imageviewer.interfaces.ui.HintsCollector;
import com.threeamigos.imageviewer.interfaces.ui.HintsWindow;

public class HintsWindowImpl implements HintsWindow {

	private final HintsPreferences hintsPreferences;

	private List<String> hints;
	private int hintIndex;

	public HintsWindowImpl(HintsPreferences hintsPreferences, HintsCollector hintsCollector) {
		this.hintsPreferences = hintsPreferences;
		hints = new ArrayList<>();
		hints.addAll(hintsCollector.getHints());
	}

	private String getNextHint() {
		return getHintImpl(1);
	}

	private String getPreviousHint() {
		return getHintImpl(-1);
	}

	private String getHintImpl(int offset) {
		hintIndex = (hintsPreferences.getLastHintIndex() + offset);
		if (hintIndex >= hints.size()) {
			hintIndex = 0;
		} else if (hintIndex < 0) {
			hintIndex = hints.size() - 1;
		}
		hintsPreferences.setLastHintIndex(hintIndex);
		return hints.get(hintIndex);
	}

	@Override
	public void showHints(Component component) {

		Box panel = Box.createVerticalBox();

		JPanel hintsBorderPanel = new JPanel();
		hintsBorderPanel.setBorder(BorderFactory.createTitledBorder("Hint"));
		panel.add(hintsBorderPanel);

		JTextArea hintLabel = new JTextArea(getNextHint());
		hintLabel.setSize(400, 100);
		hintLabel.setLineWrap(true);
		hintLabel.setWrapStyleWord(true);
		hintLabel.setEditable(false);
		hintsBorderPanel.add(hintLabel);

		panel.add(Box.createVerticalStrut(5));

		Box showHintsBox = Box.createHorizontalBox();
		JCheckBox showHintsCheckBox = new JCheckBox();
		showHintsCheckBox.setSelected(hintsPreferences.isHintsVisibleAtStartup());
		showHintsCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hintsPreferences.setHintsVisibleAtStartup(showHintsCheckBox.isSelected());
			}
		});
		showHintsBox.add(showHintsCheckBox);
		showHintsBox.add(new JLabel("Show hints at startup"));
		showHintsBox.add(Box.createGlue());
		panel.add(showHintsBox);

		panel.add(Box.createVerticalStrut(5));

		Box buttonsBox = Box.createHorizontalBox();
		Dimension buttonsDimension = new Dimension();
		buttonsDimension.width = 150;
		JButton previousHintButton = new JButton("Previous hint");
		previousHintButton.setSize(buttonsDimension);
		previousHintButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hintLabel.setText(getPreviousHint());
				panel.getParent().revalidate();
			}
		});
		buttonsBox.add(previousHintButton);
		buttonsBox.add(Box.createHorizontalStrut(20));
		JButton nextHintButton = new JButton("Next hint");
		nextHintButton.setSize(buttonsDimension);
		nextHintButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hintLabel.setText(getNextHint());
			}
		});
		buttonsBox.add(nextHintButton);
		panel.add(buttonsBox);

		panel.add(Box.createVerticalStrut(5));

		JOptionPane.showOptionDialog(component, panel, "3AM Image Viewer", JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE, null, null, null);
	}

}
