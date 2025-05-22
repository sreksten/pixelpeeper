package com.threeamigos.pixelpeeper.implementations.ui;

import com.threeamigos.common.util.implementations.ui.StringHint;
import com.threeamigos.common.util.interfaces.messagehandler.MessageHandler;
import com.threeamigos.common.util.interfaces.ui.FontService;
import com.threeamigos.common.util.interfaces.ui.Hint;
import com.threeamigos.common.util.ui.draganddrop.DragAndDropSupportHelper;
import com.threeamigos.common.util.ui.effects.text.BorderedStringRenderer;
import com.threeamigos.pixelpeeper.data.ExifMap;
import com.threeamigos.pixelpeeper.data.ExifValue;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifCache;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.DragAndDropWindowPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.DragAndDropWindow;
import com.threeamigos.pixelpeeper.interfaces.ui.ImageConsumer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

public class DragAndDropWindowImpl extends JFrame implements DragAndDropWindow {

    private static final long serialVersionUID = 1L;

    private final transient DragAndDropWindowPreferences dragAndDropWindowPreferences;
    private final transient ExifCache exifCache;
    private final transient FontService fontService;
    private final transient MessageHandler messageHandler;

    private transient BufferedImage backgroundImage;
    private transient BufferedImage scaledBackgroundImage;

    private transient ImageConsumer proxifiedObject;

    private final List<File> allFiles = new ArrayList<>();

    private GroupingPanel groupingPanel;
    private JCheckBox sendImmediatelyCheckbox;
    private JButton sendButton;
    private JButton clearButton;

    int upperBorderHeight;

    public DragAndDropWindowImpl(DragAndDropWindowPreferences dragAndDropWindowPreferences, ExifCache exifCache,
                                 FontService fontService, MessageHandler messageHandler) {
        super("3AM Pixel Peeper DnD");
        setMinimumSize(new Dimension(250, 350));

        this.dragAndDropWindowPreferences = dragAndDropWindowPreferences;
        this.exifCache = exifCache;
        this.fontService = fontService;
        this.messageHandler = messageHandler;

        try {
            InputStream inputStream = getClass().getResourceAsStream("/filter-eye.jpg");
            assert inputStream != null;
            backgroundImage = ImageIO.read(inputStream);
        } catch (IOException e) {
            messageHandler.handleException(e);
            backgroundImage = null;
        }

        scaleBackgroundImage();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dragAndDropWindowPreferences.setVisible(false);
                setVisible(false);
            }
        });

        setLayout(new BorderLayout());

        DecorativePanel decorativePanel = new DecorativePanel();
        JPanel commandsPanel = buildCommandsPanel();

        add(decorativePanel, BorderLayout.CENTER);
        add(commandsPanel, BorderLayout.SOUTH);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                dragAndDropWindowPreferences.setWidth(getWidth());
                dragAndDropWindowPreferences.setHeight(getHeight());
                scaleBackgroundImage();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                dragAndDropWindowPreferences.setX(getX());
                dragAndDropWindowPreferences.setY(getY());
            }
        });

        pack();
        upperBorderHeight = getHeight() - getContentPane().getHeight();
        setResizable(true);
        setLocation(dragAndDropWindowPreferences.getX(), dragAndDropWindowPreferences.getY());
        setSize(dragAndDropWindowPreferences.getWidth(), dragAndDropWindowPreferences.getHeight());
        setVisible(dragAndDropWindowPreferences.isVisible());

        DragAndDropSupportHelper.addJavaFileListSupport(this, messageHandler);
    }

    private void scaleBackgroundImage() {
        if (backgroundImage == null) {
            return;
        }

        int backgroundImageWidth = backgroundImage.getWidth();
        int backgroundImageHeight = backgroundImage.getHeight();

        float panelAspectRatio = (float) dragAndDropWindowPreferences.getWidth()
                / dragAndDropWindowPreferences.getHeight();

        int cutWidth = backgroundImageWidth;
        int cutHeight = (int) (backgroundImageWidth / panelAspectRatio);
        if (cutHeight > backgroundImageHeight) {
            cutWidth = (int) (backgroundImageHeight * panelAspectRatio);
            cutHeight = backgroundImageHeight;
        }
        BufferedImage subImage = backgroundImage.getSubimage(Math.abs(backgroundImageWidth - cutWidth) / 2,
                Math.abs(backgroundImageHeight - cutHeight) / 2, cutWidth, cutHeight);

        BufferedImage scaledImage = new BufferedImage(dragAndDropWindowPreferences.getWidth(),
                dragAndDropWindowPreferences.getHeight(), backgroundImage.getType());
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.drawImage(subImage, 0, 0, dragAndDropWindowPreferences.getWidth(), dragAndDropWindowPreferences.getHeight(),
                null);
        g2d.dispose();
        scaledBackgroundImage = scaledImage;
    }

    public void setProxyFor(ImageConsumer consumer) {
        proxifiedObject = consumer;
    }

    @Override
    public void accept(List<File> moreFiles) {
        if (!moreFiles.isEmpty()) {
            if (dragAndDropWindowPreferences.isOpenImmediately()) {
                sendFiles(moreFiles);
            } else {
                moreFiles.stream().filter(File::isFile).forEach(file -> {
                    allFiles.add(file);
                    Optional<ExifMap> exifMap = exifCache.getExifMap(file);
                    if (exifMap.isPresent()) {
                        groupingPanel.mapFileToTags(file);
                    }
                });
                repaint();
            }
        }
    }

    private void sendFiles() {
        sendFiles(allFiles);
        clear();
    }

    private void clear() {
        allFiles.clear();
        groupingPanel.clearMap();
        repaint();
    }

    private void sendFiles(List<File> files) {
        if (proxifiedObject != null) {
            proxifiedObject.accept(files, groupingPanel.getExifTagToGroupBy(), groupingPanel.getTolerance(),
                    groupingPanel.getExifTagToOrderBy());
        } else {
            messageHandler.handleErrorMessage("The Drag and Drop window has no related object to transmit files to.");
        }
    }

    private void setSendImmediately(boolean sendImmediately) {
        sendImmediatelyCheckbox.setSelected(sendImmediately);
        dragAndDropWindowPreferences.setOpenImmediately(sendImmediately);
        sendButton.setEnabled(!sendImmediately);
        clearButton.setEnabled(!sendImmediately);
        if (sendImmediately) {
            sendFiles();
        }
    }

    @Override
    public Collection<Hint<String>> getHints() {
        Collection<Hint<String>> hints = new ArrayList<>();
        hints.add(new StringHint(
                "You can open the Drag and Drop window and keep it in a secondary screen to open images with ease."));
        return hints;
    }

    private JPanel buildCommandsPanel() {

        groupingPanel = new GroupingPanel(exifCache);
        groupingPanel.addGroupingByActionListener(e -> repaint());

        sendButton = new JButton("Open");
        sendButton.setEnabled(!dragAndDropWindowPreferences.isOpenImmediately());
        sendButton.addActionListener(e -> sendFiles());

        clearButton = new JButton("Clear");
        clearButton.setEnabled(!dragAndDropWindowPreferences.isOpenImmediately());
        clearButton.addActionListener(e -> clear());

        sendImmediatelyCheckbox = new JCheckBox();
        sendImmediatelyCheckbox.setSelected(dragAndDropWindowPreferences.isOpenImmediately());
        sendImmediatelyCheckbox.addActionListener(e -> setSendImmediately(sendImmediatelyCheckbox.isSelected()));

        JLabel sendImmediatelyLabel = new JLabel("Open immediately");
        sendImmediatelyLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSendImmediately(!dragAndDropWindowPreferences.isOpenImmediately());
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel sendImmediatelyPanel = new JPanel();
        sendImmediatelyPanel.setLayout(new BoxLayout(sendImmediatelyPanel, BoxLayout.LINE_AXIS));
        sendImmediatelyPanel.add(sendImmediatelyCheckbox);
        sendImmediatelyPanel.add(Box.createHorizontalStrut(5));
        sendImmediatelyPanel.add(sendImmediatelyLabel);

        JPanel openPanel = new JPanel();
        openPanel.setLayout(new BoxLayout(openPanel, BoxLayout.LINE_AXIS));
        openPanel.add(clearButton);
        openPanel.add(Box.createHorizontalStrut(5));
        openPanel.add(sendButton);

        panel.add(sendImmediatelyPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(groupingPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(openPanel);

        return panel;
    }

    private class DecorativePanel extends JPanel {

        private static final long serialVersionUID = 1L;

        @Override
        public void paintComponent(Graphics gfx) {
            super.paintComponent(gfx);
            Graphics2D g2d = (Graphics2D) gfx;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int windowWidth = dragAndDropWindowPreferences.getWidth();
            int windowHeight = dragAndDropWindowPreferences.getHeight();

            int fontHeight = windowHeight / 20;
            if (fontHeight < 12) {
                fontHeight = 12;
            }

            Font font = fontService.getFont("Arial", Font.BOLD, fontHeight);
            g2d.setFont(font);

            if (backgroundImage != null) {
                g2d.drawImage(scaledBackgroundImage, 0, 0, null);
            }

            if (allFiles.isEmpty()) {

                final int vertSpacing = fontHeight / 2;

                int startY = (this.getHeight() - (3 * fontHeight + 2 * vertSpacing)) / 2 + upperBorderHeight;

                String word;
                int wordWidth;

                word = "Drop";
                wordWidth = g2d.getFontMetrics().stringWidth(word);
                BorderedStringRenderer.drawString(g2d, word, (windowWidth - wordWidth) / 2, startY, Color.BLACK,
                        Color.LIGHT_GRAY);
                startY += fontHeight + vertSpacing;

                word = "files";
                wordWidth = g2d.getFontMetrics().stringWidth(word);
                BorderedStringRenderer.drawString(g2d, word, (windowWidth - wordWidth) / 2, startY, Color.BLACK,
                        Color.LIGHT_GRAY);
                startY += fontHeight + vertSpacing;

                word = "here";
                wordWidth = g2d.getFontMetrics().stringWidth(word);
                BorderedStringRenderer.drawString(g2d, word, (windowWidth - wordWidth) / 2, startY, Color.BLACK,
                        Color.LIGHT_GRAY);
            } else {

                final int vertSpacing = 5;
                final int horSpacing = 5;

                Map<ExifValue, Collection<File>> groupedFiles = groupingPanel.groupFiles();

                int requiredElements = allFiles.size();
                if (groupedFiles.size() > 1) {
                    requiredElements += groupedFiles.size();
                }
                int requiredHeight = fontHeight * requiredElements + vertSpacing * (requiredElements - 1);

                int startY = this.getHeight() - requiredHeight + upperBorderHeight;

                for (Entry<ExifValue, Collection<File>> entry : groupedFiles.entrySet()) {
                    if (groupedFiles.size() > 1) {
                        BorderedStringRenderer.drawString(g2d, entry.getKey().getDescription(), horSpacing, startY,
                                Color.BLACK, Color.GRAY);
                        startY += vertSpacing + fontHeight;
                    }
                    for (File file : entry.getValue()) {
                        BorderedStringRenderer.drawString(g2d, file.getName(), horSpacing, startY, Color.BLACK,
                                Color.LIGHT_GRAY);
                        startY += vertSpacing + fontHeight;
                    }
                }
            }
        }
    }

}
