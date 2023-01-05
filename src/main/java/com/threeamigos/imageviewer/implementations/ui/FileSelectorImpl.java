package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;
import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.swing.JFileChooser;

import com.threeamigos.imageviewer.interfaces.ui.FileSelector;
import com.threeamigos.imageviewer.interfaces.ui.PathPreferences;

public class FileSelectorImpl implements FileSelector {

	private final PathPreferences fileSelectorPreferences;

	public FileSelectorImpl(PathPreferences fileSelectorPreferences) {
		this.fileSelectorPreferences = fileSelectorPreferences;
	}

	@Override
	public List<File> getSelectedFiles(Component component) {

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select images");
		fileChooser.setApproveButtonText("Load");
		fileChooser.setCurrentDirectory(new File(fileSelectorPreferences.getPath()));
		fileChooser.setApproveButtonMnemonic('L');
		fileChooser.setMultiSelectionEnabled(true);

		int returnVal = fileChooser.showOpenDialog(component);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			List<File> files = List.of(fileChooser.getSelectedFiles());
			if (!files.isEmpty()) {
				File firstFile = files.get(0);
				fileSelectorPreferences.setPath(firstFile.getParentFile().getAbsolutePath());
			}
			return files;
		}

		return Collections.emptyList();
	}

}
