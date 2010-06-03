package com.google.code.laserswarm.util;

import javax.swing.JFrame;

public abstract class GuiFactory {

	public JFrame getDefaultJFrame() {
		JFrame fr = new JFrame("test");
		fr.setSize(800, 600);
		fr.setVisible(true);
		fr.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return fr;
	}

}
