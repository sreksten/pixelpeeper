package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.common.util.interfaces.messagehandler.MessageHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.FileRenamer;
import com.threeamigos.pixelpeeper.interfaces.datamodel.NamePattern;
import com.threeamigos.pixelpeeper.interfaces.ui.FileSelector;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

public class FileRenamerImpl implements FileRenamer, PropertyChangeListener {

    private final FileSelector fileSelector;
    private final NamePattern namePattern;
    private final MessageHandler messageHandler;

    private ProgressMonitor progressMonitor;
    private Task task;
    private List<File> files;
    private int renamedFiles;

    public FileRenamerImpl(FileSelector fileSelector, NamePattern namePattern, MessageHandler messageHandler) {
        this.fileSelector = fileSelector;
        this.namePattern = namePattern;
        this.messageHandler = messageHandler;
    }

    @Override
    public void selectAndRename(Component component) {
        rename(fileSelector.getSelectedFiles(component, "Rename"), component);
    }

    private void rename(List<File> files, Component component) {
        if (files.isEmpty()) {
            return;
        }
        this.files = files;
        progressMonitor = new ProgressMonitor(component, "Renaming files", "", 0, files.size());
        task = new Task();
        task.addPropertyChangeListener(this);
        task.execute();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            int progress = (Integer) evt.getNewValue();
            progressMonitor.setProgress(progress);
            if ((progressMonitor.isCanceled() || task.isDone()) && progressMonitor.isCanceled()) {
                task.cancel(true);
                progressMonitor.close();
            }
        }
    }

    private class Task extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            int processedFiles = 0;
            renamedFiles = 0;
            for (File file : files) {
                setProgress(processedFiles);
                if (isCancelled()) {
                    break;
                }
                processedFiles++;
                if (namePattern.rename(file)) {
                    renamedFiles++;
                }
            }
            return null;
        }

        @Override
        protected void done() {
            progressMonitor.close();
            messageHandler.handleWarnMessage(String.format("%d files renamed", renamedFiles));
        }
    }
}
