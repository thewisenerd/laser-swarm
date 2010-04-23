package com.google.code.laserswarm.plot;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.geotools.geometry.DirectPosition2D;

import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.common.collect.Lists;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class plotHeightDistribution {
	private static final Logger	logger	= Logger.get(plotHeightDistribution.class);
	/**
	 * Plots height versus time.
	 * @param start
	 * 		(longitude [deg], latitude [deg]) of the starting point on the earth.
	 * @param end
	 * 		(longitude [deg], latitude [deg]) of the stopping point on the earth.
	 * @param DEM
	 * 		Digital Elevation Model to be used for height distribution.
	 * @param nSteps
	 * 		Number of steps used for plotting the path.
	 * @param lineThickness
	 * 		Line thickness used in the plot.
	 * @param plotFile
	 * 		File to plot to.
	 */
	public void plot(DirectPosition2D start, DirectPosition2D end, ElevationModel DEM, double nSteps, double lineThickness, String plotFile) {
		double dLat = end.y - start.y;
		double dLon = end.x - start.x;
		double latStep = dLat/nSteps;
		double lonStep = dLon/nSteps;
		int width = 1600;
		int height = 200;
		int hOffset = 25;
		int wOffset = 50;
		BufferedImage bimg = new BufferedImage(width+wOffset, height+hOffset, BufferedImage.TYPE_INT_RGB); // New BufferedImage used for writing.
			//ImageIO.read(new File(plotFile));	//open the background of the image file to be created for writing
		bimg.createGraphics();									//create Graphics context
		Graphics2D g = (Graphics2D)bimg.getGraphics();			//map Graphics context to new instance of Graphics2D
		g.setPaintMode();										//set mode to overwrite pixels
		g.setColor(new Color(0,0,0));							//set color to black
		for (int l=0; l<lineThickness; l++)
		{
			g.drawLine(height-l, wOffset, height-l, width+wOffset);
			g.drawLine(height, wOffset+l, 0, wOffset+l);
		}
		g.drawString("h [m]", 5, 25);
		g.drawString("Ground track from ("+Math.abs(10*start.x)/10+","+Math.abs(10*start.y)/10+
				") to ("+Math.abs(10*end.x)/10+","+Math.abs(10*end.y)/10+")", height+hOffset, wOffset);
		ArrayList<Double> h = Lists.newArrayList();
		double hMax = Double.MIN_VALUE;
		double hMin = Double.MAX_VALUE;
		for (int n = 0; n < nSteps; n++) {
			DirectPosition2D currPos = new DirectPosition2D(start.x + n*lonStep, start.y + n*latStep);
			double hFound = DEM.getElevation(currPos);
			if (hFound > hMax) {hMax = hFound;}
			if (hFound < hMin) {hMin = hFound;}
			h.add(hFound);
		}
		hMax *= 1.05;
		hMin *= 0.95;
		double hDiff = hMax - hMin;
		for (int n = 0; n < nSteps-1; n++) {
			for (int l=0; l<lineThickness; l++)
			{
				int x1 = wOffset + (int)(((double)n)/((double)nSteps)*width);
				int y1 = hOffset + (int)((h.get(n)-hMin)/hDiff*height) + l;
				int x2 = wOffset + (int)(((double)n+1)/((double)nSteps)*width);
				int y2 = hOffset + (int)((h.get(n+1)-hMin)/hDiff*height) + l;
				g.drawLine(x1, y1, x2, y2);
			}
		}
		try {
			ImageIO.write(bimg, "jpg", new File(plotFile+".jpg"));
		} catch (Exception e) {
			logger.inf(e, "Plot file writing failed.");
		}
	}
}
