package com.threeamigos.imageviewer.implementations.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;

import com.threeamigos.common.util.interfaces.MessageHandler;
import com.threeamigos.common.util.ui.draganddrop.BorderedStringRenderer;
import com.threeamigos.common.util.ui.draganddrop.DragAndDropSupportHelper;
import com.threeamigos.imageviewer.data.ExifMap;
import com.threeamigos.imageviewer.data.ExifValue;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifCache;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifReader;
import com.threeamigos.imageviewer.interfaces.datamodel.ExifReaderFactory;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.DragAndDropWindowPreferences;
import com.threeamigos.imageviewer.interfaces.ui.DragAndDropWindow;
import com.threeamigos.imageviewer.interfaces.ui.FontService;
import com.threeamigos.imageviewer.interfaces.ui.ImageConsumer;

public class DragAndDropWindowImpl extends JFrame implements DragAndDropWindow {

	private static final long serialVersionUID = 1L;

	private final DragAndDropWindowPreferences dragAndDropWindowPreferences;
	private final ExifReaderFactory exifReaderFactory;
	private final ExifCache exifCache;
	private final FontService fontService;
	private final MessageHandler messageHandler;

	private ImageConsumer proxifiedObject;

	private List<File> allFiles = new ArrayList<>();

	private GroupingPanel groupingPanel;
	private JCheckBox sendImmediatelyCheckbox;
	private JButton sendButton;

	public DragAndDropWindowImpl(DragAndDropWindowPreferences dragAndDropWindowPreferences,
			ExifReaderFactory exifReaderFactory, ExifCache exifCache, FontService fontService,
			MessageHandler messageHandler) {
		super("3AM Image Viewer DnD");
		this.dragAndDropWindowPreferences = dragAndDropWindowPreferences;
		this.exifReaderFactory = exifReaderFactory;
		this.exifCache = exifCache;
		this.fontService = fontService;
		this.messageHandler = messageHandler;

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dragAndDropWindowPreferences.setVisible(false);
				setVisible(false);
			}
		});

		setLayout(new BorderLayout());

		add(new DecorativePanel(), BorderLayout.CENTER);
		add(buildCommandsPanel(), BorderLayout.SOUTH);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				dragAndDropWindowPreferences.setWidth(getWidth());
				dragAndDropWindowPreferences.setHeight(getHeight());
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				dragAndDropWindowPreferences.setX(getX());
				dragAndDropWindowPreferences.setY(getY());
			}
		});

		pack();
		setResizable(true);
		setLocation(dragAndDropWindowPreferences.getX(), dragAndDropWindowPreferences.getY());
		setSize(dragAndDropWindowPreferences.getWidth(), dragAndDropWindowPreferences.getHeight());
		setVisible(dragAndDropWindowPreferences.isVisible());

		DragAndDropSupportHelper.addJavaFileListSupport(this, messageHandler);
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
				ExifReader exifReader = exifReaderFactory.getExifReader();
				for (File file : moreFiles) {
					if (file.isFile()) {
						allFiles.add(file);
						Optional<ExifMap> exifMap = exifReader.readMetadata(file);
						if (exifMap.isPresent()) {
							groupingPanel.mapFileToTags(file);
						}
					}
				}
				repaint();
			}
		}
	}

	private void sendFiles() {
		sendFiles(allFiles);
		allFiles.clear();
		groupingPanel.clearMap();
		repaint();
	}

	private void sendFiles(List<File> files) {
		if (proxifiedObject != null) {
			proxifiedObject.accept(files, groupingPanel.getSelection(), groupingPanel.getTolerance());
		} else {
			messageHandler.handleErrorMessage("The Drag and Drop window has no related object to transmit files to.");
		}
	}

	@Override
	public Collection<String> getHints() {
		Collection<String> hints = new ArrayList<>();
		hints.add("You can open the Drag and Drop window and keep it in a secondary screen to open images with ease.");
		return hints;
	}

	private JPanel buildCommandsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		groupingPanel = new GroupingPanel(exifCache);
		groupingPanel.addGroupingByActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});

		sendButton = new JButton("Open");
		sendButton.setEnabled(!dragAndDropWindowPreferences.isOpenImmediately());
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendFiles();
			}
		});

		sendImmediatelyCheckbox = new JCheckBox();
		sendImmediatelyCheckbox.setSelected(dragAndDropWindowPreferences.isOpenImmediately());
		sendImmediatelyCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean sendImmediately = sendImmediatelyCheckbox.isSelected();
				dragAndDropWindowPreferences.setOpenImmediately(sendImmediately);
				sendButton.setEnabled(!sendImmediately);
				if (sendImmediately) {
					sendFiles();
				}
			}
		});

		JLabel label = new JLabel("Open immediately");
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				boolean sendImmediately = !dragAndDropWindowPreferences.isOpenImmediately();
				dragAndDropWindowPreferences.setOpenImmediately(sendImmediately);
				sendImmediatelyCheckbox.setSelected(sendImmediately);
				sendButton.setEnabled(!sendImmediately);
				if (sendImmediately) {
					sendFiles();
				}
			}
		});

		JSeparator separator = new JSeparator(JSeparator.VERTICAL);
		separator.setMaximumSize(new Dimension(5, 20));

		panel.add(Box.createHorizontalGlue());
		panel.add(sendImmediatelyCheckbox);
		panel.add(Box.createHorizontalStrut(5));
		panel.add(label);
		panel.add(Box.createHorizontalStrut(5));
		panel.add(separator);
		panel.add(Box.createHorizontalStrut(5));
		panel.add(groupingPanel);
		panel.add(Box.createHorizontalStrut(5));
		panel.add(sendButton);

		return panel;
	}

	private class DecorativePanel extends JPanel {

		private static final long serialVersionUID = 1L;

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

			final int vertSpacing = fontHeight / 2;

			if (allFiles.isEmpty()) {
				int startY = (windowHeight - (3 * fontHeight + 2 * vertSpacing)) / 2;

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
				Map<ExifValue, Collection<File>> groupedFiles = groupingPanel.groupFiles();

				int requiredElements = allFiles.size();
				if (groupedFiles.size() > 1) {
					requiredElements += groupedFiles.size();
				}
				int requiredHeight = (fontHeight + 5 + 2) * requiredElements;

				int startY = (this.getHeight() - requiredHeight / 2) / 2;

				for (Entry<ExifValue, Collection<File>> entry : groupedFiles.entrySet()) {
					if (groupedFiles.size() > 1) {
						BorderedStringRenderer.drawString(g2d, entry.getKey().getDescription(), 5, startY, Color.BLACK,
								Color.DARK_GRAY);
						startY += 5 + fontHeight;
					}
					for (File file : entry.getValue()) {
						BorderedStringRenderer.drawString(g2d, file.getName(), 5, startY, Color.BLACK,
								Color.LIGHT_GRAY);
						startY += 5 + fontHeight;
					}
				}
			}
		}
	}

}
