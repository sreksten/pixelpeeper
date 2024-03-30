package com.threeamigos.pixelpeeper.implementations.ui;

import com.threeamigos.pixelpeeper.data.ExifMap;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifValue;
import com.threeamigos.pixelpeeper.implementations.datamodel.FileGrouper;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifCache;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

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

    public static List<ExifTag> getGroupableTags() {
        return groupableTags;
    }

    private final transient ExifCache exifCache;

    private final transient Map<File, ExifMap> filesToTagsMap;
    private final JComboBox<ExifTag> groupByComboBox;
    private final JComboBox<ExifTag> orderByComboBox;
    private final JSlider toleranceSlider;
    private final JLabel groupingResultsLabel;

    GroupingPanel(ExifCache exifCache) {

        this.exifCache = exifCache;

        filesToTagsMap = new HashMap<>();
        groupByComboBox = createGroupByComboBox();
        orderByComboBox = createOrderByComboBox();
        toleranceSlider = createToleranceSlider();
        groupingResultsLabel = new JLabel();

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JPanel groupingPanel = new JPanel();
        groupingPanel.setLayout(new BoxLayout(groupingPanel, BoxLayout.LINE_AXIS));
        groupingPanel.setBorder(BorderFactory.createTitledBorder("Group by"));
        groupingPanel.add(groupByComboBox);
        groupingPanel.add(Box.createHorizontalGlue());

        add(groupingPanel);

        JPanel tolerancePanel = new JPanel();
        tolerancePanel.setLayout(new BoxLayout(tolerancePanel, BoxLayout.LINE_AXIS));
        tolerancePanel.setBorder(BorderFactory.createTitledBorder("Focal Length tolerance"));
        tolerancePanel.add(toleranceSlider);

        add(tolerancePanel);

        JPanel orderingPanel = new JPanel();
        orderingPanel.setLayout(new BoxLayout(orderingPanel, BoxLayout.LINE_AXIS));
        orderingPanel.setBorder(BorderFactory.createTitledBorder("Order by"));
        orderingPanel.add(orderByComboBox);
        orderingPanel.add(Box.createHorizontalGlue());

        add(orderingPanel);

        JPanel groupingResultsPanel = new JPanel();
        groupingResultsPanel.setLayout(new BoxLayout(groupingResultsPanel, BoxLayout.LINE_AXIS));
        groupingResultsPanel.setBorder(BorderFactory.createTitledBorder("Grouping results"));
        groupingResultsLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        groupingResultsPanel.add(groupingResultsLabel);
        groupingResultsPanel.add(Box.createHorizontalGlue());

        add(groupingResultsPanel);

        groupByComboBox.addActionListener(e -> {
            ExifTag selectedItem = (ExifTag) groupByComboBox.getSelectedItem();
            toleranceSlider.setEnabled(
                    selectedItem == ExifTag.FOCAL_LENGTH || selectedItem == ExifTag.FOCAL_LENGTH_35MM_EQUIVALENT);
            updateGroupingResultsLabel(groupFiles());
        });

        toleranceSlider.addChangeListener(e -> updateGroupingResultsLabel(groupFiles()));
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
        return FileGrouper.groupFiles(filesToTagsMap, getExifTagToGroupBy(), getTolerance(), getExifTagToOrderBy());
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
        groupByComboBox.addActionListener(actionListener);
    }

    void addToleranceActionListener(ActionListener actionListener) {
        groupByComboBox.addActionListener(actionListener);
    }

    ExifTag getExifTagToGroupBy() {
        return (ExifTag) groupByComboBox.getSelectedItem();
    }

    ExifTag getExifTagToOrderBy() {
        return (ExifTag) orderByComboBox.getSelectedItem();
    }

    int getTolerance() {
        return toleranceSlider.getValue();
    }

    private JComboBox<ExifTag> createGroupByComboBox() {
        ExifTag[] listElements = new ExifTag[1 + groupableTags.size()];
        for (int i = 0; i < groupableTags.size(); i++) {
            listElements[i + 1] = groupableTags.get(i);
        }
        return new JComboBox<>(listElements);
    }

    private JComboBox<ExifTag> createOrderByComboBox() {
        ExifTag[] listElements = new ExifTag[1 + groupableTags.size()];
        for (int i = 0; i < groupableTags.size(); i++) {
            listElements[i + 1] = groupableTags.get(i);
        }
        return new JComboBox<>(listElements);
    }

    private JSlider createToleranceSlider() {
        Hashtable<Integer, JLabel> toleranceSliderLabelTable = new Hashtable<>();
        toleranceSliderLabelTable.put(MIN_TOLERANCE, new JLabel(MIN_TOLERANCE + "mm"));
        toleranceSliderLabelTable.put(HALF_TOLERANCE, new JLabel(HALF_TOLERANCE + "mm"));
        toleranceSliderLabelTable.put(MAX_TOLERANCE, new JLabel(MAX_TOLERANCE + "mm"));

        JSlider slider = new JSlider(SwingConstants.HORIZONTAL, 0, 50, 0);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setLabelTable(toleranceSliderLabelTable);
        slider.setPaintLabels(true);
        slider.setEnabled(false);

        return slider;
    }

}
