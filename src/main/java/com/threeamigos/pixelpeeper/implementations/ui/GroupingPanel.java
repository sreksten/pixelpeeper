package com.threeamigos.pixelpeeper.implementations.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.threeamigos.pixelpeeper.data.ExifMap;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifValue;
import com.threeamigos.pixelpeeper.implementations.datamodel.FileGrouper;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifCache;

class GroupingPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final int MIN_TOLERANCE = 0;
	private static final int MAX_TOLERANCE = 50;
	private static final int HALF_TOLERANCE = (MAX_TOLERANCE - MIN_TOLERANCE) / 2;

	private static final List<ExifTag> groupableTags;
	static {
		List<ExifTag> tags = new ArrayList<>();
		tags.add(ExifTag.CAMERA_MODEL);
		tags.add(ExifTag.LENS_MODEL);
		tags.add(ExifTag.APERTURE);
		tags.add(ExifTag.ISO);
		tags.add(ExifTag.EXPOSURE_TIME);
		tags.add(ExifTag.FOCAL_LENGTH);
		tags.add(ExifTag.FOCAL_LENGTH_35MM_EQUIVALENT);
		groupableTags = Collections.unmodifiableList(tags);
	}

	public static final List<ExifTag> getGroupableTags() {
		return groupableTags;
	}

	private final ExifCache exifCache;

	private Map<File, ExifMap> filesToTagsMap;
	private JComboBox<ExifTag> groupingByComboBox;
	private JSlider toleranceSlider;
	private JLabel groupingResultsLabel;

	GroupingPanel(ExifCache exifCache) {

		this.exifCache = exifCache;

		filesToTagsMap = new HashMap<>();
		groupingByComboBox = createGroupingByComboBox();
		toleranceSlider = createToleranceSlider();
		groupingResultsLabel = new JLabel();

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		JPanel groupingPanel = new JPanel();
		groupingPanel.setLayout(new BoxLayout(groupingPanel, BoxLayout.LINE_AXIS));
		groupingPanel.setBorder(BorderFactory.createTitledBorder("Group by"));
		groupingPanel.add(groupingByComboBox);
		groupingPanel.add(Box.createHorizontalGlue());

		add(groupingPanel);

		JPanel tolerancePanel = new JPanel();
		tolerancePanel.setLayout(new BoxLayout(tolerancePanel, BoxLayout.LINE_AXIS));
		tolerancePanel.setBorder(BorderFactory.createTitledBorder("Focal Length tolerance"));
		tolerancePanel.add(toleranceSlider);

		add(tolerancePanel);

		JPanel groupingResultsPanel = new JPanel();
		groupingResultsPanel.setLayout(new BoxLayout(groupingResultsPanel, BoxLayout.LINE_AXIS));
		groupingResultsPanel.setBorder(BorderFactory.createTitledBorder("Grouping results"));
		groupingResultsLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		groupingResultsPanel.add(groupingResultsLabel);
		groupingResultsPanel.add(Box.createHorizontalGlue());

		add(groupingResultsPanel);

		groupingByComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ExifTag selectedItem = (ExifTag) groupingByComboBox.getSelectedItem();
				toleranceSlider.setEnabled(
						selectedItem == ExifTag.FOCAL_LENGTH || selectedItem == ExifTag.FOCAL_LENGTH_35MM_EQUIVALENT);
				updateGroupingResultsLabel(groupFiles());
			}
		});

		toleranceSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateGroupingResultsLabel(groupFiles());
			}
		});
	}

	void clearMap() {
		filesToTagsMap.clear();
		updateGroupingResultsLabel(Collections.emptyMap());
	}

	void mapFilesToTags(Collection<File> files) {
		for (File file : files) {
			mapFileToTags(file);
		}
	}

	void mapFileToTags(File file) {
		if (file.isFile()) {
			Optional<ExifMap> exifMapOpt = exifCache.getExifMap(file);
			if (exifMapOpt.isPresent()) {
				filesToTagsMap.put(file, exifMapOpt.get());
				updateGroupingResultsLabel(groupFiles());
			}
		}
	}

	public Map<ExifValue, Collection<File>> groupFiles() {
		return FileGrouper.groupFiles(filesToTagsMap, getSelection(), getTolerance());
	}

	boolean isTagsMapEmpty() {
		return filesToTagsMap.isEmpty();
	}

	Map<File, ExifMap> getMap() {
		return filesToTagsMap;
	}

	void updateGroupingResultsLabel(Map<ExifValue, Collection<File>> groupedFiles) {
		int groups = groupedFiles.entrySet().size();
		int totalFiles = groupedFiles.values().stream().mapToInt(Collection::size).sum();
		String newLabel;
		if (groups == 1) {
			newLabel = String.format("%d files in a single group.", totalFiles);
		} else {
			String groupsView = groupedFiles.values().stream().map(c -> String.valueOf(c.size()))
					.collect(Collectors.joining(", "));
			newLabel = String.format("%d files in %d groups of %s files.", totalFiles, groups, groupsView);
		}
		groupingResultsLabel.setText(newLabel);
	}

	void addGroupingByActionListener(ActionListener actionListener) {
		groupingByComboBox.addActionListener(actionListener);
	}

	void addToleranceActionListener(ActionListener actionListener) {
		groupingByComboBox.addActionListener(actionListener);
	}

	ExifTag getSelection() {
		return (ExifTag) groupingByComboBox.getSelectedItem();
	}

	int getTolerance() {
		return toleranceSlider.getValue();
	}

	private final JComboBox<ExifTag> createGroupingByComboBox() {
		ExifTag[] listElements = new ExifTag[1 + groupableTags.size()];
		for (int i = 0; i < groupableTags.size(); i++) {
			listElements[i + 1] = groupableTags.get(i);
		}
		return new JComboBox<>(listElements);
	}

	private final JSlider createToleranceSlider() {
		Hashtable<Integer, JLabel> toleranceSliderLabelTable = new Hashtable<>();
		toleranceSliderLabelTable.put(MIN_TOLERANCE, new JLabel(MIN_TOLERANCE + "mm"));
		toleranceSliderLabelTable.put(HALF_TOLERANCE, new JLabel(HALF_TOLERANCE + "mm"));
		toleranceSliderLabelTable.put(MAX_TOLERANCE, new JLabel(MAX_TOLERANCE + "mm"));

		JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 50, 0);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(5);
		slider.setPaintTicks(true);
		slider.setLabelTable(toleranceSliderLabelTable);
		slider.setPaintLabels(true);
		slider.setEnabled(false);

		return slider;
	}

}
