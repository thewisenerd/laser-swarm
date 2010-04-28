package com.google.code.laserswarm;

import java.awt.image.BufferedImage;
import java.io.File;

import junit.framework.TestCase;

import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.plot.plot2D.MultiChannelPlot2d;
import com.google.code.laserswarm.plot.plot2D.Plot2D;
import com.google.code.laserswarm.util.demReader.DemCreationException;
import com.google.code.laserswarm.util.demReader.DemReader;

public class MultiChannelPlot2DTest extends TestCase {

	public static void main(String[] args) {
		new MultiChannelPlot2DTest().testMultiChannel();
	}

	public void testMultiChannel() {
		ElevationModel dem = null;
		try {
			dem = DemReader.parseDem(new File("DEM/srtm_37_02-red.asc"));
		} catch (DemCreationException e1) {
			fail("Cannot load the DEM");
		}
		EarthModel earth = new EarthModel(dem);

		BufferedImage r = Plot2D.mkImage(earth.getKappaMinnaertMap());
		BufferedImage g = Plot2D.mkImage(earth.getSurfaceRefractionMap());
		BufferedImage b = Plot2D.mkImage(earth.getThetaHenyeyGreensteinMap());

		MultiChannelPlot2d.make(r, g, b);
	}
}
