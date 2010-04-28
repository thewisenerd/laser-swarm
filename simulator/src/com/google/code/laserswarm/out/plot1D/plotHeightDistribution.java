package com.google.code.laserswarm.out.plot1D;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.geotools.geometry.DirectPosition2D;

import com.google.code.laserswarm.earthModel.IElevationModel;
import com.google.common.collect.Lists;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class plotHeightDistribution {
	private static final Logger	logger	= Logger.get(plotHeightDistribution.class);

	/**
	 * Plots height versus time.
	 * 
	 * @param start
	 *            (longitude [deg], latitude [deg]) of the starting point on the earth.
	 * @param end
	 *            (longitude [deg], latitude [deg]) of the stopping point on the earth.
	 * @param DEM
	 *            Digital Elevation Model to be used for height distribution.
	 * @param nSteps
	 *            Number of steps used for plotting the path.
	 * @param lineThickness
	 *            Line thickness used in the plot.
	 * @param plotFile
	 *            File to plot to.
	 */
	public void plot(DirectPosition2D start, DirectPosition2D end, IElevationModel DEM, double nSteps,
			double lineThickness, String plotFile) {
		double dLat = end.y - start.y;
		double dLon = end.x - start.x;
		double latStep = dLat / nSteps;
		double lonStep = dLon / nSteps;
		int width = 1600;
		int height = 200;
		int hOffset = 25;
		int wOffset = 60;
		BufferedImage bimg = new BufferedImage(width + wOffset, height + hOffset,
				BufferedImage.TYPE_INT_RGB); // New BufferedImage used for writing
		Graphics2D g = bimg.createGraphics(); // create Graphics context and map Graphics context to new
		// instance of Graphics2D
		g.setBackground(Color.white); // set the background to white
		g.clearRect(0, 0, width + wOffset, height + hOffset);
		g.setPaintMode(); // set mode to overwrite pixels
		g.setColor(new Color(0, 0, 0)); // set color to black
		for (int l = 0; l < lineThickness; l++) {
			g.drawLine(wOffset, height - l, width + wOffset, height - l);
			g.drawLine(wOffset + l, height, wOffset + l, 0);
		}
		ArrayList<Double> h = Lists.newArrayList();
		double hMax = Double.MIN_VALUE;
		double hMin = Double.MAX_VALUE;
		for (int n = 0; n < nSteps; n++) {
			DirectPosition2D currPos = new DirectPosition2D(start.x + n * lonStep, start.y + n * latStep);
			double hFound = DEM.getElevation(currPos);
			if (hFound > hMax) {
				hMax = hFound;
			}
			if (hFound < hMin) {
				hMin = hFound;
			}
			h.add(hFound);
		}
		double hDiff = hMax - hMin;
		double scaleFactor = 1;
		double plotOffset = 3;
		int fontSize = 20;
		Font font = new Font("Arial", Font.PLAIN, fontSize);
		g.setFont(font);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawString("h [m]", 5, (int) (0.13 * height * scaleFactor + fontSize));
		for (int i = 0; i < 5; i++) {
			g.drawString(Integer.toString((int) ((1 - i * 0.25) * hMax)), 5, (int) (i * 0.25 * height
					* scaleFactor + plotOffset + fontSize));
		}
		for (int i = 0; i < 8; i++) {
			g.drawString("(" + Math.floor(1000 * (start.x + i * 0.125 * dLon)) / 1000 + ","
					+ Math.floor(1000 * (start.y + i * 0.125 * dLat)) / 1000 + ")", wOffset
					+ (int) (i * 0.125 * width), height + hOffset - 5);
		}
		final float dash1[] = { 10.0f };
		for (int n = 0; n < nSteps - 1; n++) {
			for (int l = 0; l < lineThickness; l++) {
				int x1 = wOffset + (int) (((double) n) / ((double) nSteps) * width);
				int y1 = height - (int) ((h.get(n) - hMin) / hDiff * height * scaleFactor) - l;
				int x2 = wOffset + (int) (((double) n + 1) / ((double) nSteps) * width);
				int y2 = height - (int) ((h.get(n + 1) - hMin) / hDiff * height * scaleFactor) - l;
				g.drawLine(x1, y1, x2, y2);
			}
		}
		final BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
				10.0f, dash1, 0.0f);
		g.setStroke(dashed);
		for (int i = 1; i < 5; i++) {
			g.drawLine(wOffset, height - (int) (i * 0.25 * height * scaleFactor), wOffset + width,
					height - (int) (i * 0.25 * height * scaleFactor));
		}
		for (int i = 1; i < 9; i++) {
			g.drawLine(wOffset + (int) (i * 0.125 * width), 0, wOffset + (int) (i * 0.125 * width),
					height);
		}
		try {
			ImageIO.write(bimg, "png", new File(plotFile + ".png"));
		} catch (Exception e) {
			logger.inf(e, "Plot file writing failed.");
		}
	}
}
