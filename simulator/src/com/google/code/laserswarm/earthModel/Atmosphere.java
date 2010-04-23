package com.google.code.laserswarm.earthModel;

import com.google.code.laserswarm.conf.Configuration;

public class Atmosphere {

	public static double computeIntesity(double I0, double l, double optThick) {
		double I = I0 * Math.exp(-optThick * l);
		return I;
	}

	private float	optThick	= Configuration.instance.getAtmOpticalThickness();

	public Atmosphere() {
	}

	public double computeIntesity(double I0) {
		return computeIntesity(I0, 1, optThick);
	}

	/**
	 * Compute the new intensity if a ray travels through the atmosphere
	 * 
	 * @param I0
	 *            Input strength
	 * @param Angle
	 *            Angle of the input ray in ENU system
	 * @return The intensity as received on the surface
	 */
	public double computeIntesity(double I0, double angle) {
		return computeIntesity(I0, 1 / Math.cos(angle), optThick);
	}

}
