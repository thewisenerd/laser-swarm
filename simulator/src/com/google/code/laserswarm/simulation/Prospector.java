package com.google.code.laserswarm.simulation;

import java.util.HashMap;

import javax.vecmath.Point3d;

import org.geotools.geometry.DirectPosition2D;

import com.google.code.laserswarm.Orbit.OrbitClass;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.earthModel.Convert;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.earthModel.ElevationModel;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class Prospector {

	public static double		roughTimeStep	= 5;
	private static final Logger	logger			= Logger.get(Prospector.class);

	public static long prospect(long i, long samples, double dt, OrbitClass emittorOrbit,
			HashMap<Satellite, OrbitClass> receiverOrbits, EarthModel earth) {

		logger.dbg("In pospector");

		long s = (long) (roughTimeStep / dt);
		boolean overGround = false;
		long moved = 1;

		while (!overGround && 1 + moved < samples) {
			if (moved - 1 % 100000 == 0)
				logger.dbg("Skipped %s samples already", moved);
			/* Find the current position */
			Point3d point = emittorOrbit.ECEF_point();
			Point3d sphere = Convert.toSphere(point);
			double lon = sphere.y * (180. / Math.PI);
			double lat = sphere.z * (180. / Math.PI);

			/* Quick test over land */
			DirectPosition2D p2d = new DirectPosition2D(lon, lat);
			ElevationModel dem = earth.findCoverage(p2d);
			if (dem != null)
				overGround = true;

			/* propagate */
			emittorOrbit.propogate(s * dt);
			for (OrbitClass orbit : receiverOrbits.values())
				orbit.propogate(s * dt);
			moved += s;
		}

		logger.dbg("Prospector found a timestep of roughly %s s or %s samples", //
				moved * dt, moved);

		return moved;
	}
}
