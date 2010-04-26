package com.google.code.laserswarm.earthModel;

public class ScatteringParam {

	/**
	 * The index of reflection of the local terrain
	 */
	public double	indexOfRefraction;
	/**
	 * The local Minnaert constant (kappa)
	 */
	public double	kappaMinnaert;
	/**
	 * The local Henyey-Greenstein constant (theta)
	 */
	public double	thetaHenyeyGreenstein;

	public ScatteringParam(double indexOfRefraction, double kappaMinnaert, double thetaHenyeyGreenstein) {
		this.indexOfRefraction = indexOfRefraction;
		this.kappaMinnaert = kappaMinnaert;
		this.thetaHenyeyGreenstein = thetaHenyeyGreenstein;
	}

	public double getIndexOfRefraction() {
		return indexOfRefraction;
	}

	public double getKappaMinnaert() {
		return kappaMinnaert;
	}

	public double getThetaHenyeyGreenstein() {
		return thetaHenyeyGreenstein;
	}

}
