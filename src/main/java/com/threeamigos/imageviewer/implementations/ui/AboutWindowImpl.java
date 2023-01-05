package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Component;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.threeamigos.imageviewer.interfaces.ui.AboutWindow;

public class AboutWindowImpl implements AboutWindow {

	@Override
	public void about(Component component) {

		Box panel = Box.createVerticalBox();

		java.net.URL imgUrl = getClass().getResource("/3AM_logo.png");
		JLabel logo = new JLabel(new ImageIcon(imgUrl));
		logo.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(logo);

		panel.add(Box.createVerticalStrut(10));

		JLabel imageViewerLabel = new JLabel("3AM Image Viewer");
		Font font = new Font("Serif", Font.BOLD, 16);
		imageViewerLabel.setFont(font);
		imageViewerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(imageViewerLabel);

		panel.add(Box.createVerticalStrut(5));

		JLabel author = new JLabel("by Stefano Reksten - stefano.reksten@gmail.com");
		author.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(author);

		panel.add(Box.createVerticalStrut(5));

		JLabel license = new JLabel("Released under the GNU General Public License");
		license.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(license);

		JOptionPane.showOptionDialog(component, panel, "3AM Image Viewer", JOptionPane.DEFAULT_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, null, null);
	}
}
