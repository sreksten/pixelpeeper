package com.threeamigos.pixelpeeper.implementations.edgedetect.ui;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetector;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;

abstract class AbstractEdgesDetectorPreferencesSelectorDataModel extends BasicPropertyChangeAware
        implements EdgesDetectorPreferences, ChangeListener {

    protected final DataModel dataModel;
    protected final EdgesDetectorPreferences edgesDetectorPreferences;
    protected final Component component;

    private BufferedImage sourceImage;
    private BufferedImage edgesImage;

    private final int transparencyBackup;

    final JLabel transparencyText;

    final JSlider transparencySlider;

    AbstractEdgesDetectorPreferencesSelectorDataModel(DataModel dataModel,
                                                      EdgesDetectorPreferences edgesDetectorPreferences, Component component) {
        this.dataModel = dataModel;
        this.edgesDetectorPreferences = edgesDetectorPreferences;
        this.component = component;

        transparencyBackup = edgesDetectorPreferences.getEdgesTransparency();
        transparencyText = new JLabel(String.valueOf(transparencyBackup));
        transparencySlider = createSlider(EdgesDetectorPreferences.NO_EDGES_TRANSPARENCY,
                EdgesDetectorPreferences.TOTAL_EDGES_TRANSPARENCY, transparencyBackup);
    }

    final void abstractCancelSelection() {
        edgesDetectorPreferences.setEdgesTransparency(transparencyBackup);
        cancelSelection();
    }

    abstract void cancelSelection();

    final void abstractAcceptSelection() {
        edgesDetectorPreferences.setEdgesTransparency(transparencySlider.getValue());
        acceptSelection();
    }

    abstract void acceptSelection();

    abstract boolean isAnyCalculationParameterModified();

    final void abstractReset() {
        edgesDetectorPreferences.setEdgesTransparency(transparencyBackup);

        reset();
        transparencySlider.setValue(transparencyBackup);

        component.repaint();
    }

    abstract void reset();

    final void abstractResetToDefault() {
        edgesDetectorPreferences.setEdgesTransparency(EdgesDetectorPreferences.EDGES_TRANSPARENCY_DEFAULT);
        transparencySlider.setValue(EdgesDetectorPreferences.EDGES_TRANSPARENCY_DEFAULT);
        resetToDefault();
        component.repaint();
    }

    abstract void resetToDefault();

    protected final JSlider createSlider(int minValue, int maxValue, int currentValue) {
        JSlider slider = new JSlider(SwingConstants.HORIZONTAL, minValue, maxValue, currentValue);
        slider.addChangeListener(this);
        return slider;
    }

    protected final JCheckBox createCheckbox(boolean currentValue) {
        JCheckBox checkbox = new JCheckBox();
        checkbox.setSelected(currentValue);
        checkbox.addChangeListener(this);
        return checkbox;
    }

    public final void stateChanged(ChangeEvent e) {
        Object object = e.getSource();

        if (object == transparencySlider) {
            transparencyText.setText(String.valueOf(transparencySlider.getValue()));
            edgesDetectorPreferences.setEdgesTransparency(transparencySlider.getValue());
        } else {
            handleStateChanged(e);
        }

        startEdgesCalculation();
        component.repaint();
    }

    protected abstract void handleStateChanged(ChangeEvent e);

    public final void startEdgesCalculation() {
        if (sourceImage != null) {
            EdgesDetector edgesDetector = getEdgesDetectorImplementation();
            edgesDetector.setSourceImage(sourceImage);
            edgesDetector.process();
            edgesImage = edgesDetector.getEdgesImage();
        }
    }

    protected abstract EdgesDetector getEdgesDetectorImplementation();

    final void setSourceImage(BufferedImage sourceImage) {
        this.sourceImage = sourceImage;
    }

    final BufferedImage getSourceImage() {
        return sourceImage;
    }

    final BufferedImage getEdgesImage() {
        return edgesImage;
    }

    // EdgesDetectorPreferences

    @Override
    public final void setShowEdges(boolean showEdges) {
    }

    @Override
    public final boolean isShowEdges() {
        return true;
    }

    public final void setEdgesTransparency(int transparency) {
        transparencySlider.setValue(transparency);
    }

    public final int getEdgesTransparency() {
        return transparencySlider.getValue();
    }

    @Override
    public final void setEdgesDetectorFlavour(EdgesDetectorFlavour flavour) {
    }

    @Override
    public void loadDefaultValues() {
        resetToDefault();
    }

}
