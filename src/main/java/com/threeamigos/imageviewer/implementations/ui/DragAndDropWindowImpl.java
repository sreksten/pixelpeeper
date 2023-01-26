package com.threeamigos.imageviewer.implementations.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.threeamigos.common.util.interfaces.MessageHandler;
import com.threeamigos.common.util.ui.draganddrop.BorderedStringRenderer;
import com.threeamigos.common.util.ui.draganddrop.DragAndDropSupportHelper;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.DragAndDropWindowPreferences;
import com.threeamigos.imageviewer.interfaces.ui.DragAndDropWindow;
import com.threeamigos.imageviewer.interfaces.ui.FontService;

public class DragAndDropWindowImpl extends JFrame implements DragAndDropWindow {

	private static final long serialVersionUID = 1L;

	private final DragAndDropWindowPreferences dragAndDropWindowPreferences;
	private final FontService fontService;
	private final MessageHandler messageHandler;
	private Consumer<List<File>> proxifiedObject;

	public DragAndDropWindowImpl(DragAndDropWindowPreferences dragAndDropWindowPreferences, FontService fontService,
			MessageHandler messageHandler) {
		super("3AM Image Viewer DnD");
		this.dragAndDropWindowPreferences = dragAndDropWindowPreferences;
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

		JPanel decorativePanel = new DecorativePanel();
		add(decorativePanel, BorderLayout.CENTER);

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

	public void setProxyFor(Consumer<List<File>> consumer) {
		proxifiedObject = consumer;
	}

	@Override
	public void accept(List<File> selectedFiles) {
		if (!selectedFiles.isEmpty()) {
			if (proxifiedObject != null) {
				proxifiedObject.accept(selectedFiles);
			} else {
				messageHandler
						.handleErrorMessage("The Drag and Drop window has no related object to transmit files to.");
			}
		}
	}

	@Override
	public Collection<String> getHints() {
		Collection<String> hints = new ArrayList<>();
		hints.add("You can open the Drag and Drop window and keep it in a secondary screen to open images with ease.");
		return hints;
	}

	private class DecorativePanel extends JPanel {

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
		}

	}

}
