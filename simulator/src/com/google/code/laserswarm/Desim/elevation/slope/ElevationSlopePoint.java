package com.google.code.laserswarm.Desim.elevation.slope;

public class ElevationSlopePoint {
	private double	elevation;
	private double	slope;

	public double getElevation() {
		return elevation;
	}

	public double getSlope() {
		return slope;
	}

	public ElevationSlopePoint(double elevation, double slope) {
		this.elevation = elevation;
		this.slope = slope;
	}
}
