package com.google.code.laserswarm;

import java.io.File;

import org.geotools.geometry.DirectPosition2D;

import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.plot.plotHeightDistribution;
import com.google.code.laserswarm.util.demReader.DemCreationException;
import com.google.code.laserswarm.util.demReader.DemReader;

public class TestHeightDistributionPlot {
	public static void main(String[] args)  throws DemCreationException {
		plotHeightDistribution plot = new plotHeightDistribution();
		ElevationModel DEM = DemReader.parseDem(new File("DEM/srtm_37_02-red.asc"));
		DirectPosition2D st = new DirectPosition2D(3.55, 50.5);
		DirectPosition2D sp = new DirectPosition2D(3.6, 51);
		plot.plot(st, sp, DEM, 100, 3, "/media/DATA/coding/java/Laser SWARM/report/simulator/img/testHeightPlot");
	}
}
