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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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

	private Map<File, ExifMap> filesToTagsMap;
	private Map<ExifTag, JList<ExifValue>> tagsToSelectedValues;

	JDialog dialog;
	JButton okButton;
	JButton cancelButton;
	boolean selectionSuccessful;
	int matchingFiles;

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
		} else {
			return getFilesBySelection(filteredTags);
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

		tagsToSelectedValues = new EnumMap<>(ExifTag.class);

		matchingFiles = filesToTagsMap.size();
		JLabel matchingFilesLabel = new JLabel();
		buildMatchingFilesLabel(matchingFilesLabel);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		JScrollPane scrollPane = new JScrollPane(createSelectionTagsPanel(map, matchingFilesLabel),
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		mainPanel.add(scrollPane);

		mainPanel.add(Box.createVerticalStrut(5));

		mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL));

		mainPanel.add(Box.createVerticalStrut(5));

		mainPanel.add(createMatchingFilesPanel(matchingFilesLabel));

		mainPanel.add(Box.createVerticalStrut(5));

		mainPanel.add(createOkCancelPanel());

		mainPanel.setPreferredSize(new Dimension(480, 400));

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

		return createSelectionMap(map);
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
				// TODO Auto-generated method stub
				selectionSuccessful = false;
				dialog.setVisible(false);
			}
		});
		panel.add(cancelButton);
		return panel;
	}

	private JPanel createSelectionTagsPanel(Map<ExifTag, Collection<ExifValue>> map, JLabel matchingFilesLabel) {

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		for (Entry<ExifTag, Collection<ExifValue>> entry : map.entrySet()) {

			panel.add(createSingleListPanel(map, entry, matchingFilesLabel));

			panel.add(Box.createVerticalStrut(5));
		}

		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		return panel;
	}

	private JPanel createSingleListPanel(Map<ExifTag, Collection<ExifValue>> map,
			Entry<ExifTag, Collection<ExifValue>> entry, JLabel matchingFilesLabel) {

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
				Map<ExifTag, Collection<ExifValue>> selectionMap = createSelectionMap(map);
				if (selectionMap.isEmpty()) {
					matchingFiles = filesToTagsMap.size();
				} else {
					matchingFiles = getFilesBySelection(selectionMap).size();
				}
				buildMatchingFilesLabel(matchingFilesLabel);
				updateOkButtonStatus();
			}
		});

		JPanel listPanel = new JPanel();
		listPanel.setBorder(BorderFactory.createTitledBorder(tag.getDescription()));
		listPanel.add(list);

		tagsToSelectedValues.put(tag, list);

		return listPanel;
	}

	private void buildMatchingFilesLabel(JLabel matchingFilesLabel) {
		String newLabel;
		if (matchingFiles == 0) {
			newLabel = "No file matches.";
		} else if (matchingFiles == 1) {
			newLabel = "One file matches.";
		} else {
			newLabel = String.format("%d files match.", matchingFiles);
		}

		if (matchingFiles > MAX_SELECTABLE_FILES_PER_GROUP) {
			newLabel += String.format("  Please add some filters.");
		}
		matchingFilesLabel.setText(newLabel);
	}

	private void updateOkButtonStatus() {
		okButton.setEnabled(matchingFiles <= MAX_SELECTABLE_FILES_PER_GROUP);
	}

	private JPanel createMatchingFilesPanel(JLabel matchingFilesLabel) {
		JPanel matchingFilesPanel = new JPanel();
		matchingFilesPanel.setLayout(new BoxLayout(matchingFilesPanel, BoxLayout.LINE_AXIS));
		matchingFilesPanel.add(matchingFilesLabel);
		matchingFilesPanel.add(Box.createHorizontalGlue());
		return matchingFilesPanel;
	}

}
