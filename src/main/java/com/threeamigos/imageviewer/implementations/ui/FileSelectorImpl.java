package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;
import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.swing.JFileChooser;

import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.ui.FileSelector;

public class FileSelectorImpl implements FileSelector {

	private final DataModel dataModel;

	public FileSelectorImpl(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	@Override
	public List<File> getSelectedFiles(Component component) {

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select images");
		fileChooser.setApproveButtonText("Load");
		fileChooser.setCurrentDirectory(new File(dataModel.getLastPath()));
		fileChooser.setApproveButtonMnemonic('L');
		fileChooser.setMultiSelectionEnabled(true);

		int returnVal = fileChooser.showOpenDialog(component);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			List<File> files = List.of(fileChooser.getSelectedFiles());
			if (!files.isEmpty()) {
				File firstFile = files.get(0);
				dataModel.setLastPath(firstFile.getParentFile().getAbsolutePath());
			}
			return files;
		}

		return Collections.emptyList();
	}

}
