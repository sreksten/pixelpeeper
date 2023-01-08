package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.TrayIcon.MessageType;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagsFilter;

public class ExifTagsFilterImpl implements ExifTagsFilter {

	@Override
	public Map<ExifTag, Collection<String>> filterTags(Map<ExifTag, Collection<String>> map) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		Map<ExifTag, JList<String>> tagsToSelectedValues = new EnumMap<>(ExifTag.class);

		Dimension dimension = null;
		
		int componentCount = 0;
		for (Entry<ExifTag, Collection<String>> entry : map.entrySet()) {

			ExifTag tag = entry.getKey();
			Collection<String> values = entry.getValue();

			JList<String> list = new JList(values.toArray());
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			if (dimension == null) {
				dimension = list.getPreferredSize();
				dimension.width = 500;
			}

			JLabel label = new JLabel(tag.getDescription());
			label.setLabelFor(list);

			Box labelBox = Box.createHorizontalBox();
			labelBox.add(label);
			labelBox.add(Box.createHorizontalGlue());
			panel.add(labelBox);
			panel.add(Box.createVerticalStrut(5));
			Box listBox = Box.createHorizontalBox();
			listBox.add(list);
			listBox.add(Box.createHorizontalGlue());
			panel.add(listBox);
			panel.add(Box.createVerticalStrut(5));

			tagsToSelectedValues.put(tag, list);

			if (componentCount < map.size() - 1) {
				JSeparator separator = new JSeparator();
				panel.add(separator);
				panel.add(Box.createVerticalStrut(5));
				componentCount++;
			}
		}

		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		JScrollPane scrollPane = new JScrollPane(panel);
		
		int response = JOptionPane.showConfirmDialog(null, scrollPane, "Select tags to filter by", JOptionPane.OK_CANCEL_OPTION);

		if (response == JOptionPane.CANCEL_OPTION) {
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
