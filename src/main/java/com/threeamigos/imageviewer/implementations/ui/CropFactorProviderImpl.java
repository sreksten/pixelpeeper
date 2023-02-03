package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;
import java.util.Optional;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.threeamigos.imageviewer.interfaces.datamodel.CropFactorRepository;
import com.threeamigos.imageviewer.interfaces.ui.CropFactorProvider;

public class CropFactorProviderImpl implements CropFactorProvider {

	private final CropFactorRepository cropFactorRepository;
	private JTextField cropFactorTextField;

	public CropFactorProviderImpl(CropFactorRepository cropFactorRepository) {
		this.cropFactorRepository = cropFactorRepository;
	}

	@Override
	public float getCropFactor(String cameraManufacturer, String cameraModel, Component component) {
		Optional<Float> cropFactorOpt = cropFactorRepository.loadCropFactor(cameraManufacturer, cameraModel);
		if (cropFactorOpt.isPresent()) {
			return cropFactorOpt.get();
		}
		Float cropFactor = requestCropFactor(cameraManufacturer, cameraModel, component);
		if (cropFactor != null) {
			cropFactorRepository.storeCropFactor(cameraManufacturer, cameraModel, cropFactor);
			return cropFactor;
		} else {
			return CROP_FACTOR_DEFAULT;
		}
	}

	private Float requestCropFactor(String cameraManufacturer, String cameraModel, Component component) {

		JPanel mainPanel = buildJPanel(cameraManufacturer, cameraModel);

		JOptionPane optionPane = new JOptionPane();
		optionPane.setMessage(mainPanel);

		JDialog dialog = optionPane.createDialog(component, "Please provide crop factor");
		dialog.pack();
		dialog.setVisible(true);
		// At this point flow is suspended until the user selects ok or cancel
		dialog.dispose();

		String cropString = cropFactorTextField.getText().trim();
		if (cropString.length() > 0)
			try {
				return Float.parseFloat(cropString);
			} catch (NumberFormatException e) {
			}
		return null;
	}

	private JPanel buildJPanel(String cameraManufacturer, String cameraModel) {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.LINE_AXIS));
		messagePanel.add(new JLabel("Could not determine crop factor for " + cameraManufacturer + " " + cameraModel));
		messagePanel.add(Box.createHorizontalGlue());
		mainPanel.add(messagePanel);

		cropFactorTextField = new JTextField();
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
