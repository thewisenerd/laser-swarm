package com.google.code.laserswarm.desim.elevation.slope;

import java.util.ArrayList;
import java.util.TreeMap;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class ElevationRelatedEntriesPoint {
	private double									elevation;
	private double									tEmit;
	private Point3d									posEmit;
	private ArrayList<TreeMap<Double, Vector3d>>	relatedEntries;
	private TreeMap<Double, Vector3d>				bestMap;

	public double getElevation() {
		return elevation;
	}

	public TreeMap<Double, Vector3d> getBestMap() {
		return bestMap;
	}

	public ArrayList<TreeMap<Double, Vector3d>> getRelatedEntries() {
		return relatedEntries;
	}

	/**
	 * @return Returns the emitter position in ECEF.
	 */
	public Point3d getPosEmit() {
		return posEmit;
	}

	public double getTEmit() {
		return tEmit;
	}

	/**
	 * @param elevation
	 *            Elevation in meters.
	 * @param tEmit
	 *            Time in seconds.
	 * @param posEmit
	 *            Emitter position in ECEF.
	 * @param entriesClose
	 * @param bestMap
	 */
	public ElevationRelatedEntriesPoint(double elevation, double tEmit, Point3d posEmit,
			ArrayList<TreeMap<Double, Vector3d>> entriesClose, TreeMap<Double, Vector3d> bestMap) {
		this.elevation = elevation;
		this.tEmit = tEmit;
		this.posEmit = posEmit;
		this.relatedEntries = entriesClose;
		this.bestMap = bestMap;
	}
}
