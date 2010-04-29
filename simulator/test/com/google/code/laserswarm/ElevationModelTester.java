package com.google.code.laserswarm;

import java.io.File;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import junit.framework.TestCase;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.operation.projection.PointOutsideEnvelopeException;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.earthModel.Convert;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.util.demReader.DemCreationException;
import com.google.code.laserswarm.util.demReader.DemReader;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class ElevationModelTester extends TestCase {

	private static final Logger	logger	= Logger.get(ElevationModelTester.class);

	public static void main(String[] args) throws Exception {
		new ElevationModelTester().testDemIntersection();
	}

	public void testDemIntersection() {
		EarthModel earth = new EarthModel();
		try {
			ElevationModel dem = DemReader
					.parseDem(new File(Configuration.demDir, "srtm_37_02-red.asc"));
			earth.add(dem);
		} catch (DemCreationException e) {
			logger.err(e, "");
		}
		earth.loadCoef(); // Stretch refl coef

		double lon = (Math.PI / 180) * earth.getCompleteEnvelope2D().getCenterX();
		double lat = (Math.PI / 180) * earth.getCompleteEnvelope2D().getCenterY();

		double r = 7000000;
		Point3d testPoint = Convert.toXYZ(new Point3d(r, lon, lat));

		Vector3d dir = new Vector3d(testPoint);
		dir.normalize();
		logger.inf("Testing pnt long: %s lat: %s", lon, lat);
		logger.inf("Testing with pnt %s", testPoint);
		logger.inf("Testing with direction %s", dir);
		logger.inf("Testing pnt surface h: %s", Configuration.R0 + earth.getElevation(//
				new DirectPosition2D((180 / Math.PI) * lon, (180 / Math.PI) * lat)));

		Point3d onSurfPoint = null;
		try {
			onSurfPoint = earth.getIntersecion(dir, testPoint);
		} catch (PointOutsideEnvelopeException e) {
			logger.err(e, "");
		}

		double rp = onSurfPoint.x;
		double theta = onSurfPoint.y;
		double phi = onSurfPoint.z;

		logger.inf("The found point was %s", onSurfPoint);
		logger.inf("The found point oint long: %s lat: %s", theta, phi);

		logger.inf("The surface normal is %s", earth.getSurfaceNormal(new DirectPosition2D(theta
				* (180 / Math.PI), phi * (180 / Math.PI))));

		assertEquals(phi, lat, 1E-6);
		assertEquals(theta, lon, 1E-6);
	}
}
