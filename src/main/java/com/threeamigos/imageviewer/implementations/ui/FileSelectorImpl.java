package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JFileChooser;

import com.threeamigos.imageviewer.interfaces.preferences.PathPreferences;
import com.threeamigos.imageviewer.interfaces.ui.FileSelector;

public class FileSelectorImpl implements FileSelector {

	private final PathPreferences pathPreferences;

	public FileSelectorImpl(PathPreferences pathPreferences) {
		this.pathPreferences = pathPreferences;
	}

	@Override
	public List<File> getSelectedFiles(Component component) {

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select images");
		fileChooser.setApproveButtonText("Load");
		fileChooser.setCurrentDirectory(new File(pathPreferences.getLastPath()));
		fileChooser.setApproveButtonMnemonic('L');
		fileChooser.setMultiSelectionEnabled(true);

		int returnVal = fileChooser.showOpenDialog(component);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			List<File> files = Arrays.asList(fileChooser.getSelectedFiles());
			if (!files.isEmpty()) {
				File firstFile = files.get(0);
				pathPreferences.setLastPath(firstFile.getParentFile().getAbsolutePath());
			}
			return files;
		}

		return Collections.emptyList();
	}

	@Override
	public File getSelectedDirectory(Component component) {

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select directory");
		fileChooser.setApproveButtonText("Load");
		fileChooser.setCurrentDirectory(new File(pathPreferences.getLastPath()).getParentFile());
		fileChooser.setApproveButtonMnemonic('L');
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int returnVal = fileChooser.showOpenDialog(component);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		}

		return null;
	}

}
