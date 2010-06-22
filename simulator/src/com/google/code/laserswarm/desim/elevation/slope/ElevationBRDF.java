package com.google.code.laserswarm.desim.elevation.slope;

import com.google.code.laserswarm.desim.brdf.BRDFinput;

public class ElevationBRDF {
	private double		elevation;
	private BRDFinput	BRDFData;

	public BRDFinput getBRDFData() {
		return BRDFData;
	}

	public double getElevation() {
		return elevation;
	}

	public ElevationBRDF(double elevation, BRDFinput BRDFData) {
		this.elevation = elevation;
		this.BRDFData = BRDFData;
	}
}
