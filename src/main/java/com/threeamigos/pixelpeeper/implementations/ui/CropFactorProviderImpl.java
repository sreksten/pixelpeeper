package com.threeamigos.pixelpeeper.implementations.ui;

import com.threeamigos.pixelpeeper.interfaces.datamodel.CropFactorRepository;
import com.threeamigos.pixelpeeper.interfaces.ui.CropFactorProvider;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class CropFactorProviderImpl implements CropFactorProvider {

    private final CropFactorRepository cropFactorRepository;

    boolean busy;

    public CropFactorProviderImpl(CropFactorRepository cropFactorRepository) {
        this.cropFactorRepository = cropFactorRepository;
    }

    @Override
    public float getCropFactor(String cameraManufacturer, String cameraModel, Component component) {

        boolean lockAcquired = false;

        while (!lockAcquired) {
            synchronized (CropFactorProvider.class) {
                if (!busy) {
                    lockAcquired = true;
                    busy = true;
                }
            }
            if (!lockAcquired) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
            }
        }

        Optional<Float> cropFactorOpt = cropFactorRepository.loadCropFactor(cameraManufacturer, cameraModel);
        if (cropFactorOpt.isPresent()) {
            busy = false;
            return cropFactorOpt.get();
        }

        Float cropFactor = requestCropFactor(cameraManufacturer, cameraModel, component);
        if (cropFactor != null) {
            cropFactorRepository.storeCropFactor(cameraManufacturer, cameraModel, cropFactor);
        } else {
            cropFactor = CROP_FACTOR_DEFAULT;
            cropFactorRepository.storeTemporaryCropFactor(cameraManufacturer, cameraModel, cropFactor);
        }
        busy = false;
        return cropFactor;
    }

    private Float requestCropFactor(String cameraManufacturer, String cameraModel, Component component) {

        JTextField cropFactorTextField = new JTextField();

        JPanel mainPanel = buildJPanel(cropFactorTextField, cameraManufacturer, cameraModel);

        JOptionPane optionPane = new JOptionPane();
        optionPane.setMessage(mainPanel);

        JDialog dialog = optionPane.createDialog(component, "Please provide crop factor");
        dialog.pack();
        dialog.setVisible(true);
        // At this point flow is suspended until the user selects ok or cancel
        dialog.dispose();

        String cropString = cropFactorTextField.getText().trim();
        if (!cropString.isEmpty()) {
            try {
                return Float.parseFloat(cropString);
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }

    private JPanel buildJPanel(JTextField cropFactorTextField, String cameraManufacturer, String cameraModel) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.LINE_AXIS));
        String description;
        if (cameraModel.toLowerCase().startsWith(cameraManufacturer.toLowerCase())) {
            description = cameraModel;
        } else {
            description = cameraManufacturer + " " + cameraModel;
        }
        messagePanel.add(new JLabel("Could not determine crop factor for " + description));
        messagePanel.add(Box.createHorizontalGlue());
        mainPanel.add(messagePanel);

        mainPanel.add(Box.createVerticalStrut(5));

        cropFactorTextField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                try {
                    Float.parseFloat(cropFactorTextField.getText());
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });

        JPanel cropFactorPanel = new JPanel();
        cropFactorPanel.setLayout(new BoxLayout(cropFactorPanel, BoxLayout.LINE_AXIS));
        cropFactorPanel.add(new JLabel("Crop factor:"));
        cropFactorPanel.add(Box.createHorizontalStrut(5));
        cropFactorPanel.add(cropFactorTextField);
        mainPanel.add(cropFactorPanel);

        return mainPanel;
    }

}
