package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;

abstract class FilterPreferencesSelectorDataModel extends BasicPropertyChangeAware
        implements FilterPreferences, ChangeListener {

    protected final DataModel dataModel;
    protected final FilterPreferences filterPreferences;
    protected final Component component;

    private BufferedImage sourceImage;
    private BufferedImage filteredImage;

    private final int transparencyBackup;

    final JLabel transparencyText;

    final JSlider transparencySlider;

    FilterPreferencesSelectorDataModel(DataModel dataModel,
                                       FilterPreferences filterPreferences, Component component) {
        this.dataModel = dataModel;
        this.filterPreferences = filterPreferences;
        this.component = component;

        transparencyBackup = filterPreferences.getTransparency();
        transparencyText = new JLabel(String.valueOf(transparencyBackup));
        transparencySlider = createSlider(FilterPreferences.NO_TRANSPARENCY,
                FilterPreferences.TOTAL_TRANSPARENCY, transparencyBackup);
    }

    final void abstractCancelSelection() {
        filterPreferences.setTransparency(transparencyBackup);
        cancelSelection();
    }

    abstract void cancelSelection();

    final void abstractAcceptSelection() {
        filterPreferences.setTransparency(transparencySlider.getValue());
        acceptSelection();
    }

    abstract void acceptSelection();

    abstract boolean isAnyCalculationParameterModified();

    final void abstractReset() {
        filterPreferences.setTransparency(transparencyBackup);

        reset();
        transparencySlider.setValue(transparencyBackup);

        component.repaint();
    }

    abstract void reset();

    final void abstractResetToDefault() {
        filterPreferences.setTransparency(FilterPreferences.TRANSPARENCY_DEFAULT);
        transparencySlider.setValue(FilterPreferences.TRANSPARENCY_DEFAULT);
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
            filterPreferences.setTransparency(transparencySlider.getValue());
        } else {
            handleStateChanged(e);
        }

        startFilterCalculation();
        component.repaint();
    }

    protected abstract void handleStateChanged(ChangeEvent e);

    public final void startFilterCalculation() {
        if (sourceImage != null) {
            Filter filter = getFilterImplementation();
            filter.setSourceImage(sourceImage);
            filter.process();
            filteredImage = filter.getResultingImage();
        }
    }

    protected abstract Filter getFilterImplementation();

    final void setSourceImage(BufferedImage sourceImage) {
        this.sourceImage = sourceImage;
    }

    final BufferedImage getSourceImage() {
        return sourceImage;
    }

    final BufferedImage getFilteredImage() {
        return filteredImage;
    }

    // EdgesDetectorPreferences

    @Override
    public final void setShowResults(boolean showResults) {
    }

    @Override
    public final boolean isShowResults() {
        return true;
    }

    public final void setTransparency(int transparency) {
        transparencySlider.setValue(transparency);
    }

    public final int getTransparency() {
        return transparencySlider.getValue();
    }

    @Override
    public final void setFilterFlavor(FilterFlavor flavor) {
    }

    @Override
    public void loadDefaultValues() {
        resetToDefault();
    }

}
