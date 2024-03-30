package com.threeamigos.pixelpeeper.implementations.ui;

import com.threeamigos.common.util.interfaces.messagehandler.MessageHandler;
import com.threeamigos.pixelpeeper.data.ExifMap;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifValue;
import com.threeamigos.pixelpeeper.implementations.datamodel.TagsClassifierImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifCache;
import com.threeamigos.pixelpeeper.interfaces.datamodel.TagsClassifier;
import com.threeamigos.pixelpeeper.interfaces.ui.ExifTagsFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ExifTagsFilterImpl implements ExifTagsFilter {

    private final ExifCache exifCache;
    private final MessageHandler messageHandler;

    private Collection<File> files;
    private Map<ExifTag, Collection<ExifValue>> tagsToFilterBy;
    private Map<ExifTag, JList<ExifValue>> tagsToSelectedValues;
    private Map<ExifValue, Collection<File>> groupedFiles;

    GroupingPanel groupingPanel;
    JDialog dialog;
    JLabel matchingFilesLabel;
    JButton okButton;
    JButton cancelButton;
    boolean selectionSuccessful;
    int matchingFilesCount;

    public ExifTagsFilterImpl(ExifCache exifCache, MessageHandler messageHandler) {
        this.exifCache = exifCache;
        this.messageHandler = messageHandler;
    }

    @Override
    public Collection<File> filterByTags(Component component, Collection<File> files) {

        this.files = files;

        groupingPanel = createGroupingPanel();

        groupingPanel.mapFilesToTags(files);

        if (groupingPanel.isTagsMapEmpty()) {
            messageHandler.handleWarnMessage("Did not find any image with Exif tags.");
            return Collections.emptyList();
        }

        TagsClassifier tagsClassifier = new TagsClassifierImpl();
        tagsClassifier.classifyTags(groupingPanel.getMap().values());

        tagsToFilterBy = tagsClassifier.getUncommonTagsToValues(GroupingPanel.getGroupableTags());

        if (tagsToFilterBy.isEmpty()) {
            messageHandler.handleWarnMessage("Images do not have different tags to filter by.");
            return Collections.emptyList();
        }

        Map<ExifTag, Collection<ExifValue>> filteredTags = filterTags(component);

        if (filteredTags == null) {
            return Collections.emptyList();
        } else {
            return getMatchingFiles(filteredTags);
        }
    }

    public ExifTag getTagToGroupBy() {
        return groupingPanel.getExifTagToGroupBy();
    }

    public ExifTag getTagToOrderBy() {
        return groupingPanel.getExifTagToOrderBy();
    }

    public int getTolerance() {
        return groupingPanel.getTolerance();
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

        matchingFilesCount = files.size();

        matchingFilesLabel = new JLabel();

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        JScrollPane scrollPane = new JScrollPane(createSelectionTagsPanel(), ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(scrollPane);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(groupingPanel);
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

        List<ExifValue> values = new ArrayList<>(entry.getValue());
        values.sort(ExifValue.getComparator());

        JList<ExifValue> list = new JList<ExifValue>((ExifValue[]) values.toArray());
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setFixedCellWidth(400);
        list.addListSelectionListener(e -> updateSelectionInformation());

        JPanel listPanel = new JPanel();
        listPanel.setBorder(BorderFactory.createTitledBorder(tag.getDescription()));
        listPanel.add(list);

        tagsToSelectedValues.put(tag, list);

        return listPanel;
    }

    private GroupingPanel createGroupingPanel() {
        GroupingPanel newGroupingPanel = new GroupingPanel(exifCache);
        newGroupingPanel.addGroupingByActionListener(e -> updateSelectionInformation());
        return newGroupingPanel;
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
        okButton.addActionListener(e -> {
            selectionSuccessful = true;
            dialog.setVisible(false);
        });
        panel.add(okButton);
        updateOkButtonStatus();

        panel.add(Box.createHorizontalStrut(10));

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            selectionSuccessful = false;
            dialog.setVisible(false);
        });
        panel.add(cancelButton);
        return panel;
    }

    private void updateSelectionInformation() {
        Map<ExifTag, Collection<ExifValue>> selectionMap = createSelectionMap(tagsToFilterBy);
        Collection<File> matchingFiles;

        if (selectionMap.isEmpty()) {
            matchingFiles = files;
            matchingFilesCount = files.size();
        } else {
            matchingFiles = getMatchingFiles(selectionMap);
            matchingFilesCount = matchingFiles.size();
        }

        groupingPanel.clearMap();
        groupingPanel.mapFilesToTags(matchingFiles);

        groupedFiles = groupingPanel.groupFiles();

        updateMatchingFilesLabel();
        updateOkButtonStatus();
    }

    private Collection<File> getMatchingFiles(Map<ExifTag, Collection<ExifValue>> selectionMap) {
        Collection<File> matchingFiles = new ArrayList<>();
        for (File file : files) {
            Optional<ExifMap> exifMap = exifCache.getExifMap(file);
            if (exifMap.isPresent() && exifMap.get().matches(selectionMap)) {
                matchingFiles.add(file);
            }
        }
        return matchingFiles;
    }

    private void updateMatchingFilesLabel() {
        String newLabel;
        if (matchingFilesCount == 0) {
            newLabel = "No file matches.";
        } else if (matchingFilesCount == 1) {
            newLabel = "One file matches.";
        } else {
            int groups = groupedFiles.entrySet().size();
            if (groups == 1) {
                newLabel = String.format("%d files match.", matchingFilesCount);
            } else {
                String groupsView = groupedFiles.values().stream().map(c -> String.valueOf(c.size()))
                        .collect(Collectors.joining(", "));
                newLabel = String.format("%d files match in %d groups of %s files.", matchingFilesCount, groups,
                        groupsView);
            }
        }

        for (Map.Entry<ExifValue, Collection<File>> entry : groupedFiles.entrySet()) {
            ExifValue tagValue = entry.getKey();
            Collection<File> files = entry.getValue();
            if (files.size() > MAX_SELECTABLE_FILES_PER_GROUP) {
                ExifTag tagToGroupBy = groupingPanel.getExifTagToGroupBy();
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
        okButton.setEnabled(groupedFiles == null || groupedFiles.entrySet().stream()
                .noneMatch(e -> e.getValue().size() > MAX_SELECTABLE_FILES_PER_GROUP));
    }

}
