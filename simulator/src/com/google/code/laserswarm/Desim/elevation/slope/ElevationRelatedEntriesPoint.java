package com.google.code.laserswarm.Desim.elevation.slope;

import java.util.ArrayList;
import java.util.TreeMap;

import javax.vecmath.Point3d;

public class ElevationRelatedEntriesPoint {
	private double								elevation;
	private double								tEmit;
	private Point3d								posEmit;
	private ArrayList<TreeMap<Double, Boolean>>	relatedEntries;
	private TreeMap<Double, Boolean>			bestMap;

	public double getElevation() {
		return elevation;
	}

	public TreeMap<Double, Boolean> getBestMap() {
		return bestMap;
	}

	public ArrayList<TreeMap<Double, Boolean>> getRelatedEntries() {
		return relatedEntries;
	}

	public Point3d getPosEmit() {
		return posEmit;
	}

	public double getTEmit() {
		return tEmit;
	}

	public ElevationRelatedEntriesPoint(double elevation, double tEmit, Point3d posEmit,
			ArrayList<TreeMap<Double, Boolean>> entriesClose, TreeMap<Double, Boolean> bestMap) {
		this.elevation = elevation;
		this.tEmit = tEmit;
		this.posEmit = posEmit;
		this.relatedEntries = entriesClose;
		this.bestMap = bestMap;
	}
}
