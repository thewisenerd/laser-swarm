package com.google.code.laserswarm.Desim.elevation;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.apache.commons.math.MathException;

import com.google.code.laserswarm.conf.Configuration;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class AltitudeCalculation {
	private static final Logger	logger	= Logger.get(AltitudeCalculation.class);

	/**
	 * @param emit
	 *            Point3d that represents the position of the emitter
	 * @param rec
	 *            Point3d that represents the position of the receiver
	 * @param travTime
	 *            The time in seconds it takes for the pulse to travel between emitter and receiver
	 */
	public static double calcAlt(Point3d emit, Point3d rec, double travTime) throws MathException {
		// if(trav1 < 0) throw new MathException("time difference can't be negative");
		// Assumed: Location of the satellite is known to high precision
		// Earth is a perfect sphere
		// Emitter points perp. to the earth center
		// Recievers points to the same point as the emitter

		// create an ellipse

		logger.dbg("emit: %s, %s, %s\n rec: %s, %s, %s\n travTime: %s", emit.x, emit.y, emit.z, rec.x,
				rec.y, rec.z, travTime);
		double focalDist = emit.distance(rec); // distance between the focal points formed by receiver
		// and emitter
		double dist = Math.abs(travTime) * Configuration.c;
		if (dist < focalDist)
			throw new MathException("Distance Traveled is shorter than Focal length");

		double a = dist / 2; // semimajor axis
		double a_2 = Math.pow(a, 2); // semimajor axis, squared
		double c = focalDist / 2; // half of the centerline length
		double b_2 = Math.pow(a, 2) - Math.pow(c, 2); // b^2 (semiminor axis squared)
		double ecc_2 = (a_2 - b_2) / a_2; // eccentricity^2
		double ecc = Math.sqrt(ecc_2); // eccentricity
		Vector3d em = new Vector3d(emit);
		Vector3d re = new Vector3d(rec);
		Vector3d dif = new Vector3d();
		dif.sub(em, re);
		double theta = dif.angle(em); // angle between focal length vector and the emitter vector

		if (dif.length() == 0)
			theta = Math.PI / 2; // if the receiver and emitter are the same
		double distGrndEmit = a * (1 - ecc_2) / (1 - ecc * Math.cos(theta)); // distance to the ground
		// from the emitter
		return em.length() - distGrndEmit; // altitude above the earth sphere in
		// meters
	}
}
