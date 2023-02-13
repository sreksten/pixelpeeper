package com.threeamigos.pixelpeeper.implementations.ui;

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

import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.HintsPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.HintsCollector;
import com.threeamigos.pixelpeeper.interfaces.ui.HintsWindow;

public class HintsWindowImpl implements HintsWindow {

	private final HintsPreferences hintsPreferences;

	private List<String> hints;
	private int hintIndex;
	private JLabel hintIndexLabel;

	public HintsWindowImpl(HintsPreferences hintsPreferences, HintsCollector hintsCollector) {
		this.hintsPreferences = hintsPreferences;
		hints = new ArrayList<>();
		hints.addAll(hintsCollector.getHints());
		hintIndex = hintsPreferences.getLastHintIndex();
		if (hints.isEmpty()) {
			hints = new ArrayList<>();
			hints.add("No hints were provided.");
		}
		if (hintIndex > hints.size()) {
			hintIndex = 0;
		}
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

		hintIndexLabel = new JLabel();
		updateHintsLabel();

		Box panel = Box.createVerticalBox();

		JPanel hintsBorderPanel = new JPanel();
		hintsBorderPanel.setBorder(BorderFactory.createTitledBorder("Hint"));
		panel.add(hintsBorderPanel);

		JTextArea hintLabel = new JTextArea(hints.get(hintIndex));
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
				updateHintsLabel();
			}
		});
		buttonsBox.add(previousHintButton);
		buttonsBox.add(Box.createHorizontalStrut(10));
		buttonsBox.add(hintIndexLabel);
		buttonsBox.add(Box.createHorizontalStrut(10));
		JButton nextHintButton = new JButton("Next hint");
		nextHintButton.setSize(buttonsDimension);
		nextHintButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hintLabel.setText(getNextHint());
				updateHintsLabel();
			}
		});
		buttonsBox.add(nextHintButton);
		panel.add(buttonsBox);

		panel.add(Box.createVerticalStrut(5));

		JOptionPane.showOptionDialog(component, panel, "3AM Pixel Peeper", JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE, null, null, null);
	}
	
	private void updateHintsLabel() {
		hintIndexLabel.setText(String.format("(%d/%d)", hintIndex + 1, hints.size()));
	}

}
