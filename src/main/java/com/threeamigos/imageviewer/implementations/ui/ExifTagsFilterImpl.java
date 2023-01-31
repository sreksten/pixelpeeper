package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import com.threeamigos.imageviewer.interfaces.datamodel.ExifCache;
import com.threeamigos.imageviewer.interfaces.datamodel.TagsClassifier;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagsFilter;

public class ExifTagsFilterImpl implements ExifTagsFilter {

	private final ExifCache exifCache;
	private final MessageHandler messageHandler;

	private Map<File, ExifMap> filesToTagsMap;
	private List<ExifTag> selectableTags = new ArrayList<>();
	private Map<ExifTag, Collection<ExifValue>> tagsToFilterBy;
	private Map<ExifTag, JList<ExifValue>> tagsToSelectedValues;
	private Map<ExifValue, Collection<File>> groupedMatchingFiles;

	JDialog dialog;
	JLabel matchingFilesLabel;
	JButton okButton;
	JButton cancelButton;
	boolean selectionSuccessful;
	ExifTag tagToGroupBy;
	int matchingFilesCount;

	public ExifTagsFilterImpl(ExifCache exifCache, MessageHandler messageHandler) {
		this.exifCache = exifCache;
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

		selectableTags = new ArrayList<>();
		selectableTags.add(ExifTag.CAMERA_MODEL);
		selectableTags.add(ExifTag.LENS_MODEL);
		selectableTags.add(ExifTag.APERTURE);
		selectableTags.add(ExifTag.ISO);
		selectableTags.add(ExifTag.EXPOSURE_TIME);

		tagsToFilterBy = tagsClassifier.getUncommonTagsToValues(selectableTags);

		if (tagsToFilterBy.isEmpty()) {
			messageHandler.handleWarnMessage("Images do not have different tags to filter by.");
			return Collections.emptyList();
		}

		Map<ExifTag, Collection<ExifValue>> filteredTags = filterTags(component);

		if (filteredTags == null) {
			return Collections.emptyList();
		} else {
			return getFilesBySelection(filteredTags);
		}
	}

	public ExifTag getTagToGroupBy() {
		return tagToGroupBy;
	}

	private void mapFilesToTags(Collection<File> files) {
		filesToTagsMap = new HashMap<>();
		for (File file : files) {
			if (file.isFile()) {
				Optional<ExifMap> exifMapOpt = exifCache.getExifMap(file);
				if (exifMapOpt.isPresent()) {
					filesToTagsMap.put(file, exifMapOpt.get());
				}
			}
		}
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

	private Map<ExifTag, Collection<ExifValue>> filterTags(Component component) {

		tagsToSelectedValues = new EnumMap<>(ExifTag.class);

		matchingFilesCount = filesToTagsMap.size();

		matchingFilesLabel = new JLabel();

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		JScrollPane scrollPane = new JScrollPane(createSelectionTagsPanel(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		mainPanel.add(scrollPane);

		mainPanel.add(Box.createVerticalStrut(5));

		mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL));

		mainPanel.add(Box.createVerticalStrut(5));

		mainPanel.add(createGroupingPanel());

		mainPanel.add(Box.createVerticalStrut(5));

		mainPanel.add(createMatchingFilesPanel());

		mainPanel.add(Box.createVerticalStrut(5));

		mainPanel.add(createOkCancelPanel());

		mainPanel.setPreferredSize(new Dimension(480, 400));

		updateSelectionInformation();

		JOptionPane optionPane = new JOptionPane();
		optionPane.setMessage(mainPanel);
		JButton[] options = new JButton[2];
		options[0] = okButton;
		options[1] = cancelButton;
		optionPane.setOptions(options);

		dialog = optionPane.createDialog(component, "Select tags to filter by");

		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dialog.setVisible(false);
				selectionSuccessful = false;
			}
		});

		dialog.pack();
		dialog.setVisible(true);
		// At this point flow is suspended until the user selects ok or cancel
		dialog.dispose();

		if (!selectionSuccessful) {
			return null;
		}

		return createSelectionMap(tagsToFilterBy);
	}

	private JPanel createSelectionTagsPanel() {

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		for (Entry<ExifTag, Collection<ExifValue>> entry : tagsToFilterBy.entrySet()) {

			panel.add(createSingleListPanel(entry));

			panel.add(Box.createVerticalStrut(5));
		}

		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		return panel;
	}

	private JPanel createSingleListPanel(Entry<ExifTag, Collection<ExifValue>> entry) {

		ExifTag tag = entry.getKey();

		List<ExifValue> values = new ArrayList<>();
		values.addAll(entry.getValue());
		Collections.sort(values, ExifValue.getComparator());

		JList<ExifValue> list = new JList(values.toArray());
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setFixedCellWidth(400);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateSelectionInformation();
			}
		});

		JPanel listPanel = new JPanel();
		listPanel.setBorder(BorderFactory.createTitledBorder(tag.getDescription()));
		listPanel.add(list);

		tagsToSelectedValues.put(tag, list);

		return listPanel;
	}

	private JPanel createGroupingPanel() {

		ExifTag[] listElements = new ExifTag[1 + selectableTags.size()];
		for (int i = 0; i < selectableTags.size(); i++) {
			listElements[i + 1] = selectableTags.get(i);
		}

		JComboBox<ExifTag> comboBox = new JComboBox<>(listElements);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tagToGroupBy = (ExifTag) comboBox.getSelectedItem();
				updateSelectionInformation();
			}
		});

		JPanel groupPanel = new JPanel();
		groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.PAGE_AXIS));
		groupPanel.setBorder(BorderFactory.createTitledBorder("Group by"));
		groupPanel.add(comboBox);
		groupPanel.add(Box.createHorizontalGlue());

		return groupPanel;
	}

	private JPanel createMatchingFilesPanel() {
		JPanel matchingFilesPanel = new JPanel();
		matchingFilesPanel.setLayout(new BoxLayout(matchingFilesPanel, BoxLayout.LINE_AXIS));
		matchingFilesPanel.add(matchingFilesLabel);
		matchingFilesPanel.add(Box.createHorizontalGlue());
		return matchingFilesPanel;
	}

	private Component createOkCancelPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionSuccessful = true;
				dialog.setVisible(false);
			}
		});
		panel.add(okButton);
		updateOkButtonStatus();

		panel.add(Box.createHorizontalStrut(10));

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionSuccessful = false;
				dialog.setVisible(false);
			}
		});
		panel.add(cancelButton);
		return panel;
	}

	private void updateSelectionInformation() {
		Map<ExifTag, Collection<ExifValue>> selectionMap = createSelectionMap(tagsToFilterBy);
		Collection<File> matchingFiles;
		groupedMatchingFiles = new HashMap<>();
		if (selectionMap.isEmpty()) {
			matchingFiles = filesToTagsMap.keySet();
			matchingFilesCount = filesToTagsMap.size();
		} else {
			matchingFiles = getFilesBySelection(selectionMap);
			matchingFilesCount = matchingFiles.size();
		}
		if (tagToGroupBy != null) {
			for (File file : matchingFiles) {
				ExifMap tags = filesToTagsMap.get(file);
				ExifValue value = tags.getExifValue(tagToGroupBy);
				groupedMatchingFiles.computeIfAbsent(value, k -> new ArrayList<>()).add(file);
			}
		} else {
			groupedMatchingFiles.put(null, matchingFiles);
		}

		updateMatchingFilesLabel();
		updateOkButtonStatus();
	}

	private Collection<File> getFilesBySelection(Map<ExifTag, Collection<ExifValue>> selectionMap) {
		List<File> filesToLoad = new ArrayList<>();
		for (Entry<File, ExifMap> entry : filesToTagsMap.entrySet()) {
			File file = entry.getKey();
			ExifMap exifMap = entry.getValue();
			if (exifMap.matches(selectionMap)) {
				filesToLoad.add(file);
			}
		}
		return filesToLoad;
	}

	private void updateMatchingFilesLabel() {
		String newLabel;
		if (matchingFilesCount == 0) {
			newLabel = "No file matches.";
		} else if (matchingFilesCount == 1) {
			newLabel = "One file matches.";
		} else {
			int groups = groupedMatchingFiles.entrySet().size();
			if (groups == 1) {
				newLabel = String.format("%d files match.", matchingFilesCount);
			} else {
				String groupsView = groupedMatchingFiles.values().stream().map(c -> String.valueOf(c.size()))
						.collect(Collectors.joining(", "));
				newLabel = String.format("%d files match in %d groups of %s files.", matchingFilesCount, groups,
						groupsView);
			}
		}

		for (Map.Entry<ExifValue, Collection<File>> entry : groupedMatchingFiles.entrySet()) {
			ExifValue tagValue = entry.getKey();
			Collection<File> files = entry.getValue();
			if (files.size() > MAX_SELECTABLE_FILES_PER_GROUP) {
				if (tagToGroupBy == null) {
					newLabel += "Please add some filters or specify how to group files.";
				} else {
					newLabel += String.format(" Group for %s %s contains %d elements. Please add some filters.",
							tagToGroupBy.getDescription(), tagValue.getDescription(), files.size());
				}
			}
		}

		matchingFilesLabel.setText(newLabel);
	}

	private void updateOkButtonStatus() {
		okButton.setEnabled(groupedMatchingFiles == null || groupedMatchingFiles.entrySet().stream()
				.noneMatch(e -> e.getValue().size() > MAX_SELECTABLE_FILES_PER_GROUP));
	}

}
