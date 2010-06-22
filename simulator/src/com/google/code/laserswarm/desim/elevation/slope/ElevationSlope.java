package com.google.code.laserswarm.desim.elevation.slope;

import java.util.LinkedList;

import javax.vecmath.Point3d;

import com.google.code.laserswarm.desim.brdf.BRDFinput;
import com.google.common.collect.Lists;

public class ElevationSlope {
	private LinkedList<Point3d>		altitudes	= Lists.newLinkedList();
	private LinkedList<BRDFinput>	BRDFIn		= Lists.newLinkedList();

	public ElevationSlope(LinkedList<Point3d> altitudes, LinkedList<BRDFinput> BRDFIn) {
		this.altitudes = altitudes;
		this.BRDFIn = BRDFIn;
	}

	public ElevationSlope() {
	}

	public LinkedList<Point3d> getAltitudes() {
		return altitudes;
	}

	public void setAltitudes(LinkedList<Point3d> altitudes) {
		this.altitudes = altitudes;
	}

	public LinkedList<BRDFinput> getBRDFIn() {
		return BRDFIn;
	}

	public void setBRDFIn(LinkedList<BRDFinput> BRDFIn) {
		this.BRDFIn = BRDFIn;
	}

}
