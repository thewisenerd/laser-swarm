package com.google.code.laserswarm.Desim.elevation.slope;

import java.util.LinkedList;

import javax.vecmath.Point3d;

import com.google.common.collect.Lists;

public class ElevationSlope {
	private LinkedList<Point3d>	altitudes	= Lists.newLinkedList();
	private LinkedList<Double>	slopes		= Lists.newLinkedList();

	public ElevationSlope(LinkedList<Point3d> altitudes, LinkedList<Double> slopes) {
		this.altitudes = altitudes;
		this.slopes = slopes;
	}

	public ElevationSlope() {
	}

	public LinkedList<Point3d> getAltitudes() {
		return altitudes;
	}

	public void setAltitudes(LinkedList<Point3d> altitudes) {
		this.altitudes = altitudes;
	}

	public LinkedList<Double> getSlopes() {
		return slopes;
	}

	public void setSlopes(LinkedList<Double> slopes) {
		this.slopes = slopes;
	}

}
