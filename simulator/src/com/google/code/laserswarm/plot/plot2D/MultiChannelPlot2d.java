package com.google.code.laserswarm.plot.plot2D;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class MultiChannelPlot2d {

	public static Plot2D make(BufferedImage imgR, BufferedImage imgG, BufferedImage imgB) {
		int w = Math.max(Math.max(imgR.getWidth(), imgG.getWidth()), imgB.getWidth());
		int h = Math.max(Math.max(imgR.getHeight(), imgG.getHeight()), imgB.getHeight());

		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = Graphics2D.class.cast(img.getGraphics());

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				g2.setColor(new Color( //
						new Color(imgR.getRGB(i, j)).getRed(), // 
						new Color(imgG.getRGB(i, j)).getGreen(), //
						new Color(imgB.getRGB(i, j)).getBlue()));
				g2.drawRect(i, j, 1, 1);
			}
		}

		return new Plot2D(img);
	}
}
