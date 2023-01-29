package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.threeamigos.common.util.interfaces.MessageHandler;
import com.threeamigos.imageviewer.data.ExifMap;
import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.ExifValue;
import com.threeamigos.imageviewer.implementations.datamodel.TagsClassifierImpl;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifImageReader;
import com.threeamigos.imageviewer.interfaces.datamodel.TagsClassifier;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagsFilter;

public class ExifTagsFilterImpl implements ExifTagsFilter {

	private static final String OK_OPTION = "OK";
	private static final String CANCEL_OPTION = "Cancel";

	private final ExifImageReader imageReader;
	private final MessageHandler messageHandler;

	private boolean selectionSuccesful = false;
	private Map<File, ExifMap> filesToTagsMap;
	private Map<ExifTag, JList<ExifValue>> tagsToSelectedValues;

	public ExifTagsFilterImpl(ExifImageReader imageReader, MessageHandler messageHandler) {
		this.imageReader = imageReader;
		this.messageHandler = messageHandler;
	}

	@Override
	public Collection<File> filterByTags(Component component, Collection<File> files) {

		mapFilesToTags(files);

		if (filesToTagsMap.isEmpty()) {
			messageHandler.handleWarnMessage("Did not find any image with Exif tags.");
			return Collections.emptyList();
		}

		TagsClassifier tagsClassifier = new TagsClassifierImpl();

		tagsClassifier.classifyTags(filesToTagsMap.values());

		Map<ExifTag, Collection<ExifValue>> tagsToFilterBy = findTagsBy(tagsClassifier, ExifTag.CAMERA_MODEL,
				ExifTag.LENS_MODEL, ExifTag.APERTURE, ExifTag.ISO, ExifTag.EXPOSURE_TIME);

		if (tagsToFilterBy.isEmpty()) {
			messageHandler.handleWarnMessage("Images do not have different tags to filter by.");
			return Collections.emptyList();
		}

		Map<ExifTag, Collection<ExifValue>> filteredTags = filterTags(component, tagsToFilterBy);

		if (filteredTags == null) {
			return Collections.emptyList();
		}

		return getFilesBySelection(filteredTags);
	}

	private void mapFilesToTags(Collection<File> files) {

		filesToTagsMap = new HashMap<>();

		for (File file : files) {
			if (file.isFile()) {
				Optional<ExifMap> exifMapOpt = imageReader.readExifMap(file);
				if (exifMapOpt.isPresent()) {
					filesToTagsMap.put(file, exifMapOpt.get());
				}
			}
		}
	}

	private Map<ExifTag, Collection<ExifValue>> findTagsBy(TagsClassifier localTagsClassifier, ExifTag... tagsToCheck) {
		Map<ExifTag, Collection<ExifValue>> tagsToFilter = new EnumMap<>(ExifTag.class);
		for (ExifTag exifTag : tagsToCheck) {
			if (!localTagsClassifier.isCommonTag(exifTag)) {
				tagsToFilter.put(exifTag, localTagsClassifier.getUncommonTagsToValues().get(exifTag));
			}
		}
		return tagsToFilter;
	}

	private Map<ExifTag, Collection<ExifValue>> filterTags(Component component,
			Map<ExifTag, Collection<ExifValue>> map) {

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		JLabel matchingFilesLabel = new JLabel("All files match");

		tagsToSelectedValues = new EnumMap<>(ExifTag.class);

		for (Entry<ExifTag, Collection<ExifValue>> entry : map.entrySet()) {

			panel.add(createListPanel(map, entry, matchingFilesLabel));

			panel.add(Box.createVerticalStrut(5));
		}

		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		panel.add(new JSeparator(SwingConstants.HORIZONTAL));

		panel.add(createMatchingFilesPanel(matchingFilesLabel));

		JScrollPane scrollPane = new JScrollPane(panel);

		String[] options = { OK_OPTION, CANCEL_OPTION };

		JOptionPane optionPane = new JOptionPane(scrollPane, -1, JOptionPane.OK_CANCEL_OPTION, null, options,
				options[1]);

		JDialog dialog = optionPane.createDialog(component, "Select tags to filter by");

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

		return createSelectionMap(map);
	}

	private JPanel createListPanel(Map<ExifTag, Collection<ExifValue>> map, Entry<ExifTag, Collection<ExifValue>> entry,
			JLabel matchingFilesLabel) {

		ExifTag tag = entry.getKey();

		List<ExifValue> values = new ArrayList<>();
		values.addAll(entry.getValue());
		Collections.sort(values, (ev1, ev2) -> {
			if (ev1 == null) {
				return -1;
			} else if (ev2 == null) {
				return 1;
			} else {
				return ev1.asComparable().compareTo(ev2.asComparable());
			}
		});

		JList<ExifValue> list = new JList(values.toArray());
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setFixedCellWidth(400);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				String newLabel;
				Map<ExifTag, Collection<ExifValue>> selectionMap = createSelectionMap(map);
				if (selectionMap.isEmpty()) {
					newLabel = "All files match";
				} else {
					int matchingFiles = getFilesBySelection(selectionMap).size();
					if (matchingFiles == 0) {
						newLabel = "No files match";
					} else if (matchingFiles == 1) {
						newLabel = "One file matches";
					} else {
						newLabel = String.format("%d files match", matchingFiles);
					}
				}
				matchingFilesLabel.setText(newLabel);
			}
		});

		JPanel listPanel = new JPanel();
		listPanel.setBorder(BorderFactory.createTitledBorder(tag.getDescription()));
		listPanel.add(list);

		tagsToSelectedValues.put(tag, list);

		return listPanel;
	}

	private JPanel createMatchingFilesPanel(JLabel matchingFilesLabel) {
		JPanel matchingFilesPanel = new JPanel();
		matchingFilesPanel.setLayout(new BoxLayout(matchingFilesPanel, BoxLayout.LINE_AXIS));
		matchingFilesPanel.add(matchingFilesLabel);
		matchingFilesPanel.add(Box.createHorizontalGlue());
		return matchingFilesPanel;
	}

	private Map<ExifTag, Collection<ExifValue>> createSelectionMap(Map<ExifTag, Collection<ExifValue>> map) {
		Map<ExifTag, Collection<ExifValue>> selectionMap = new EnumMap<>(ExifTag.class);

		for (ExifTag exifTag : map.keySet()) {
			JList<ExifValue> list = tagsToSelectedValues.get(exifTag);
			List<ExifValue> selectedValues = list.getSelectedValuesList();
			if (!selectedValues.isEmpty()) {
				selectionMap.put(exifTag, selectedValues);
			}
		}

		return selectionMap;
	}

	private Collection<File> getFilesBySelection(Map<ExifTag, Collection<ExifValue>> selectionMap) {
		List<File> filesToLoad = new ArrayList<>();
		for (Entry<File, ExifMap> entry : filesToTagsMap.entrySet()) {
			File file = entry.getKey();
			ExifMap exifMap = entry.getValue();
			if (exifMapMatchesSelection(exifMap, selectionMap)) {
				filesToLoad.add(file);
			}
		}
		return filesToLoad;
	}

	private boolean exifMapMatchesSelection(ExifMap exifMap, Map<ExifTag, Collection<ExifValue>> selectionMap) {
		for (Map.Entry<ExifTag, Collection<ExifValue>> selectionEntry : selectionMap.entrySet()) {
			ExifTag selectedTag = selectionEntry.getKey();
			Collection<ExifValue> selectedValues = selectionEntry.getValue();
			ExifValue value = exifMap.getExifValue(selectedTag);
			if (!selectedValues.contains(value)) {
				return false;
			}
		}
		return true;
	}

}
