package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.threeamigos.common.util.interfaces.MessageHandler;
import com.threeamigos.common.util.ui.draganddrop.DragAndDropSupportHelper;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.WindowPreferences;
import com.threeamigos.imageviewer.interfaces.ui.DragAndDropWindow;

public class DragAndDropWindowImpl extends JFrame implements DragAndDropWindow {

	private static final long serialVersionUID = 1L;

	private final MessageHandler messageHandler;
	private Consumer<List<File>> proxifiedObject;

	public DragAndDropWindowImpl(WindowPreferences windowPreferences, MessageHandler messageHandler) {
		super("3AM Image Viewer DnD");

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				windowPreferences.setDragAndDropWindowVisible(false);
				setVisible(false);
			}
		});

		setLayout(null);
		Container container = getContentPane();
		container.setPreferredSize(new Dimension(300, 300));

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				windowPreferences.setDragAndDropWindowWidth(getWidth());
				windowPreferences.setDragAndDropWindowHeight(getHeight());
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				windowPreferences.setDragAndDropWindowX(getX());
				windowPreferences.setDragAndDropWindowY(getY());
			}
		});

		pack();
		setResizable(true);
		setLocation(windowPreferences.getDragAndDropWindowX(), windowPreferences.getDragAndDropWindowY());
		setSize(windowPreferences.getDragAndDropWindowWidth(), windowPreferences.getDragAndDropWindowHeight());
		setVisible(windowPreferences.isDragAndDropWindowVisible());

		this.messageHandler = messageHandler;
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

}
