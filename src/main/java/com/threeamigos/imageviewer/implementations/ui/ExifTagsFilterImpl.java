package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagsFilter;

public class ExifTagsFilterImpl implements ExifTagsFilter {

	private static final String OK_OPTION = "OK";
	private static final String CANCEL_OPTION = "Cancel";

	private boolean selectionSuccesful = false;

	@Override
	public Map<ExifTag, Collection<String>> filterTags(Map<ExifTag, Collection<String>> map) {

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		Map<ExifTag, JList<String>> tagsToSelectedValues = new EnumMap<>(ExifTag.class);

		for (Entry<ExifTag, Collection<String>> entry : map.entrySet()) {

			ExifTag tag = entry.getKey();

			List<String> values = new ArrayList<>();
			values.addAll(entry.getValue());
			Collections.sort(values);

			JList<String> list = new JList(values.toArray());
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			list.setFixedCellWidth(400);

			JPanel listPanel = new JPanel();
			listPanel.setBorder(BorderFactory.createTitledBorder(tag.getDescription()));
			listPanel.add(list);

			panel.add(listPanel);

			panel.add(Box.createVerticalStrut(5));

			tagsToSelectedValues.put(tag, list);
		}

		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JScrollPane scrollPane = new JScrollPane(panel);

		String[] options = { OK_OPTION, CANCEL_OPTION };

		JOptionPane optionPane = new JOptionPane(scrollPane, -1, JOptionPane.OK_CANCEL_OPTION, null, options,
				options[1]);

		JDialog dialog = optionPane.createDialog(null, "Select tags to filter by");

		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dialog.setVisible(false);
			}
		});

		dialog.pack();
		dialog.setVisible(true);

		if (OK_OPTION.equals(optionPane.getValue())) {
			selectionSuccesful = true;
		}

		dialog.dispose();

		if (!selectionSuccesful) {
			return null;
		}

		Map<ExifTag, Collection<String>> selectionMap = new EnumMap<>(ExifTag.class);

		for (ExifTag exifTag : map.keySet()) {
			JList<String> list = tagsToSelectedValues.get(exifTag);
			List<String> selectedValues = list.getSelectedValuesList();
			if (!selectedValues.isEmpty()) {
				selectionMap.put(exifTag, selectedValues);
			}
		}

		return selectionMap;
	}

}
