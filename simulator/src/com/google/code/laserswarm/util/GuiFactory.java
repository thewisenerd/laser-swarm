package com.google.code.laserswarm.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

public abstract class GuiFactory {

	public static JFrame dualImageGui(final Image im1, final Image im2) {
		JFrame fr = getDefaultJFrame("Image comparison");
		fr.setExtendedState(JFrame.MAXIMIZED_BOTH);

		JPanel left = new JPanel() {
			private static final long	serialVersionUID	= 1L;

			@Override
			public void paintComponent(java.awt.Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = Graphics2D.class.cast(g);
				g2.drawImage(im1, 0, 0, getWidth(), getHeight(), this);
			};
		};
		JPanel right = new JPanel() {
			private static final long	serialVersionUID	= 1L;

			@Override
			public void paintComponent(java.awt.Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = Graphics2D.class.cast(g);
				g2.drawImage(im2, 0, 0, getWidth(), getHeight(), this);
			};
		};

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2);

		fr.add(splitPane);
		fr.setVisible(true);
		return fr;
	}

	public static JFrame getDefaultJFrame(String name) {
		return getDefaultJFrame(name, 800, 600);
	}

	public static JFrame getDefaultJFrame(String name, int w, int h) {
		JFrame fr = new JFrame(name);
		fr.setSize(w, h);
		fr.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return fr;
	}

	public static JFrame getImageFrame(String name, Image image) {
		JFrame fr = getDefaultJFrame(name);
		fr.add(new JLabel(new ImageIcon(image)));
		fr.setVisible(true);
		return fr;
	}

}
