package com.google.code.laserswarm.earthModel;

public class ScatteringParam {

	public static ScatteringParam random() {
		double scatter = Math.random() * 2 + 1;
		double kappaMinnaert = Math.random() * 2 - 1;
		double thetaHenyeyGreenstein = Math.random() * 2 * Math.PI - Math.PI;
		return new ScatteringParam(scatter, kappaMinnaert, thetaHenyeyGreenstein);
	}

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

	/**
	 * Make an average distribution of two given distributions
	 * 
	 * @param param
	 * @param param2
	 */
	public ScatteringParam(ScatteringParam param, ScatteringParam param2) {
		this((param.getIndexOfRefraction() + param2.getIndexOfRefraction()) / 2, //
				(param.getKappaMinnaert() + param2.getKappaMinnaert()) / 2, //
				(param.getThetaHenyeyGreenstein() + param2.getThetaHenyeyGreenstein()) / 2);
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

	@Override
	public String toString() {
		return String.format("indexOfRefraction=%f\tkappaMinnaert=%f\tthetaHenyeyGreenstein=%f",
				indexOfRefraction, kappaMinnaert, thetaHenyeyGreenstein);
	}

}
