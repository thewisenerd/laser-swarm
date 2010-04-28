package com.google.code.laserswarm;

import java.io.File;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import junit.framework.TestCase;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.operation.projection.PointOutsideEnvelopeException;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.earthModel.IElevationModel;
import com.google.code.laserswarm.util.demReader.DemCreationException;
import com.google.code.laserswarm.util.demReader.DemReader;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class ElevationModelTester extends TestCase {

	private static final Logger	logger	= Logger.get(ElevationModelTester.class);

	public static void main(String[] args) throws Exception {
		new ElevationModelTester().testGeoTiffRead();
	}

	public void testDemIntersection() {
		ElevationModel dem = null;
		try {
			dem = DemReader.parseDem(new File("DEM/ASTGTM_N48E000_dem.asc"));
		} catch (DemCreationException e1) {
			fail("Cannot load the DEM");
		}

		double lon = (Math.PI / 180) * dem.getCoverage().getEnvelope2D().getCenterX();
		double lat = (Math.PI / 180) * dem.getCoverage().getEnvelope2D().getCenterY();

		double r = 7000000;
		Point3d testPoint = new Point3d(r * Math.sin(lon) * Math.cos(lat),//
				r * Math.sin(lon) * Math.sin(lat),//
				r * Math.cos(lon));
		Vector3d dir = new Vector3d(testPoint);
		dir.normalize();
		logger.inf("Testing pnt long: %s lat: %s", lon, lat);
		logger.inf("Testing with pnt %s", testPoint);
		logger.inf("Testing with direction %s", dir);
		logger.inf("Testing pnt surface h: %s", Configuration.R0 + dem.getElevation(//
				new DirectPosition2D((180 / Math.PI) * lon, (180 / Math.PI) * lat)));

		Point3d onSurfPoint = null;
		try {
			onSurfPoint = dem.getIntersecion(dir, testPoint);
		} catch (PointOutsideEnvelopeException e) {
			logger.err(e, "");
		}

		// double rp = new Vector3d(onSurfPoint).length();
		// double theta = Math.acos(onSurfPoint.z / rp); // long
		// double phi = Math.atan2(onSurfPoint.y, onSurfPoint.x); // lat

		double rp = onSurfPoint.x;
		double phi = onSurfPoint.y;
		double theta = onSurfPoint.z;

		logger.inf("The found point was %s", onSurfPoint);
		logger.inf("The found point oint long: %s lat: %s", theta, phi);

		logger.inf("The surface normal is %s", dem.getSurfaceNormal(new DirectPosition2D(theta
				* (180 / Math.PI), phi * (180 / Math.PI))));

		assertEquals(phi, lat, 1E-6);
		assertEquals(theta, lon, 1E-6);
	}

	public void testGeoTiffRead() throws DemCreationException {
		IElevationModel dem = DemReader.parseDem(new File(
				"C:/Documents and Settings/simon/Desktop/ASTER GDEM BE/ASTGTM_N48E000_dem.tif"));
	}
}
