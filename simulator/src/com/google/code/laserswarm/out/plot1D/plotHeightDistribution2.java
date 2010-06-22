package com.google.code.laserswarm.out.plot1D;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.vecmath.Point3d;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.math.Convert;
import com.google.code.laserswarm.simulation.SimVars;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class plotHeightDistribution2 {
	private static final Logger	logger	= Logger.get(plotHeightDistribution2.class);

	/**
	 * Plots height versus position.
	 * 
	 * @param alts
	 *            The list of altitudes used to plot.
	 * @param lineThickness
	 *            Line thickness used in the plot.
	 * @param plotFile
	 *            File to plot to.
	 * @throws IOException
	 */
	public void plot(LinkedList<Point3d> alts, double lineThickness, String plotFile) throws IOException {
		logger.inf("Started plotting from LinkedList<Point3d>. File name will be: " + plotFile + ".png");
		int width = 1600;
		int height = 200;
		int hOffset = 50;
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
		ArrayList<Double> theta = Lists.newArrayList();
		ArrayList<Double> phi = Lists.newArrayList();
		ArrayList<Double> dist = Lists.newArrayList();
		dist.add(0.0);
		double hMax = Double.MIN_VALUE;
		double hMin = Double.MAX_VALUE;
		Iterator<Point3d> altsIt = alts.iterator();
		int count = 0;
		File out = new File("plotPoints" + plotFile + ".csv");
		out.delete();
		while (altsIt.hasNext()) {
			count++;
			Point3d sphere = altsIt.next();
			logger.dbg("Plot iteration: %s, with: %s, %s, %s", count, sphere.x, sphere.y, sphere.z);
			Files.append(sphere.x + ";" + sphere.y + ";" + sphere.z + ";\n", out, Charset
					.defaultCharset());
			double rp = sphere.x;
			double hFound = rp - Configuration.R0;
			theta.add(sphere.y); // longitude
			phi.add(sphere.z); // latitude
			h.add(hFound);
			if (count < 2) {
				hMax = hFound;
			}
			if (hFound > hMax) {
				hMax = hFound;
			}
			if (hFound < hMin) {
				hMin = hFound;
			}
			if (theta.size() > 1) {
				double lat1 = theta.get(theta.size() - 2);
				double lat2 = theta.get(theta.size() - 1);
				double lon1 = phi.get(phi.size() - 2);
				double lon2 = phi.get(phi.size() - 1);
				double dLat = lat2 - lat1;
				double dLon = lon2 - lon1;
				double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1) * Math.cos(lat2)
						* Math.sin(dLon / 2) * Math.sin(dLon / 2);
				double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
				dist.add(dist.get(dist.size() - 1) + Configuration.R0 * c);
			}
		}
		double hDiff = hMax - hMin;
		logger.inf("hDiff, hMax, hMin: %s, %s, %s", hDiff, hMax, hMin);
		int size = h.size();
		double scaleFactor = 1;
		double plotOffset = 3;
		int fontSize = 20;
		Font font = new Font("Arial", Font.PLAIN, fontSize);
		g.setFont(font);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		for (int n = 0; n < size - 1; n++) {
			for (int l = 0; l < lineThickness; l++) {
				int x1 = wOffset + (int) (((double) n) / ((double) size) * width);
				int y1 = height - (int) ((h.get(n) - hMin) / hDiff * height * scaleFactor) - l;
				int x2 = wOffset + (int) (((double) n + 1) / (size) * width);
				int y2 = height - (int) ((h.get(n + 1) - hMin) / hDiff * height * scaleFactor) - l;
				g.drawLine(x1, y1, x2, y2);
			}
		}
		final float dash1[] = { 10.0f };
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
		g.drawString("h [m]", 5, (int) (0.13 * height * scaleFactor + fontSize));
		for (int i = 0; i < 5; i++) {
			g.drawString(Integer.toString((int) ((1 - i * 0.25) * hDiff + hMin)), 5, (int) (i * 0.25
					* height
					* scaleFactor + plotOffset + fontSize));
		}
		for (int i = 0; i < 8; i++) {
			g.drawString("(" + Math.floor(1000 * 180 / Math.PI * theta.get((int) (i * 0.125 * size)))
					/ 1000 + "," + Math.floor(1000 * 180 / Math.PI * phi.get((int) (i * 0.125 * size)))
					/ 1000 + ")", wOffset + (int) (i * 0.125 * width), height + hOffset - 5);
		}
		for (int i = 0; i < 8; i++) {
			g.drawString("" + Math.floor(dist.get((int) (i * 0.125 * dist.size()))) / 1000, wOffset
					+ (int) (i * 0.125 * width), height + (int) (0.5 * hOffset - 5));
		}
		try {
			ImageIO.write(bimg, "png", new File(plotFile + ".png"));
		} catch (Exception e) {
			logger.err(e, "Plot file writing failed.");
		}
	}

	/**
	 * Plots height versus position.
	 * 
	 * @param sims
	 *            The list of SimVars used to plot.
	 * @param lineThickness
	 *            Line thickness used in the plot.t) (i * 0.125 * size))) / 1000 + ")", wOffset + (int)
	 *            (i * 0.125 * width), height +
	 * @param plotFile
	 *            File to plot to.
	 */
	public void plot(List<SimVars> sims, double lineThickness, String plotFile) {
		int width = 1600;
		int height = 200;
		int hOffset = 50;
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
		ArrayList<Double> theta = Lists.newArrayList();
		ArrayList<Double> phi = Lists.newArrayList();
		ArrayList<Double> dist = Lists.newArrayList();
		dist.add(0.0);
		double hMax = Double.MIN_VALUE;
		double hMin = Double.MAX_VALUE;
		for (SimVars aSim : sims) {
			Point3d sphere = Convert.toSphere(aSim.pR);
			double rp = sphere.x;
			double hFound = rp - Configuration.R0;
			theta.add(sphere.y); // longitude
			phi.add(sphere.z); // latitude
			h.add(hFound);
			if (hFound > hMax) {
				hMax = hFound;
			}
			if (hFound < hMin) {
				hMin = hFound;
			}
			if (theta.size() > 1) {
				double lat1 = theta.get(theta.size() - 2);
				double lat2 = theta.get(theta.size() - 1);
				double lon1 = phi.get(phi.size() - 2);
				double lon2 = phi.get(phi.size() - 1);
				double dLat = lat2 - lat1;
				double dLon = lon2 - lon1;
				double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1) * Math.cos(lat2)
						* Math.sin(dLon / 2) * Math.sin(dLon / 2);
				double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
				dist.add(dist.get(dist.size() - 1) + Configuration.R0 * c);
			}
		}
		double hDiff = hMax - hMin;
		int size = h.size();
		double scaleFactor = 1;
		double plotOffset = 3;
		int fontSize = 20;
		Font font = new Font("Arial", Font.PLAIN, fontSize);
		g.setFont(font);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		for (int n = 0; n < size - 1; n++) {
			for (int l = 0; l < lineThickness; l++) {
				int x1 = wOffset + (int) (((double) n) / ((double) size) * width);
				int y1 = height - (int) ((h.get(n) - hMin) / hDiff * height * scaleFactor) - l;
				int x2 = wOffset + (int) (((double) n + 1) / (size) * width);
				int y2 = height - (int) ((h.get(n + 1) - hMin) / hDiff * height * scaleFactor) - l;
				g.drawLine(x1, y1, x2, y2);
			}
		}
		final float dash1[] = { 10.0f };
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
		g.drawString("h [m]", 5, (int) (0.13 * height * scaleFactor + fontSize));
		for (int i = 0; i < 5; i++) {
			g.drawString(Integer.toString((int) ((1 - i * 0.25) * hDiff + hMin)), 5, (int) (i * 0.25
					* height
					* scaleFactor + plotOffset + fontSize));
		}
		for (int i = 0; i < 8; i++) {
			g.drawString("(" + Math.floor(1000 * 180 / Math.PI * theta.get((int) (i * 0.125 * size)))
					/ 1000 + "," + Math.floor(1000 * 180 / Math.PI * phi.get((int) (i * 0.125 * size)))
					/ 1000 + ")", wOffset + (int) (i * 0.125 * width), height + hOffset - 5);
		}
		for (int i = 0; i < 8; i++) {
			g.drawString("" + Math.floor(dist.get((int) (i * 0.125 * dist.size()))) / 1000, wOffset
					+ (int) (i * 0.125 * width), height + (int) (0.5 * hOffset - 5));
		}
		try {
			ImageIO.write(bimg, "png", new File(plotFile + ".png"));
		} catch (Exception e) {
			logger.err(e, "Plot file writing failed.");
		}
	}
}
