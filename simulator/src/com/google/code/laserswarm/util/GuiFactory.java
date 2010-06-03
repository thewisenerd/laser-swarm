package com.google.code.laserswarm.util;

import javax.swing.JFrame;

public abstract class GuiFactory {

	public static JFrame getDefaultJFrame(String name) {
		JFrame fr = new JFrame(name);
		fr.setSize(800, 600);
		fr.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return fr;
	}

}
