package com.google.code.laserswarm.Desim.elevation.slope;

import com.google.code.laserswarm.Desim.BRDFcalc.BRDFinput;

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
