package com.google.code.laserswarm.math;

import java.util.TreeMap;

import javax.vecmath.Vector3d;

public class TreeMapTools {
	public static double treeMapAvg(TreeMap<Double, Vector3d> map) {
		// Average the altitude within the given TreeMap.
		double altTotal = 0;
		for (Double alt : map.keySet()) {
			altTotal += alt;
		}
		return altTotal / map.size();
	}

	/**
	 * @param map
	 *            The map to be found minimum and maximum for.
	 * @return Returns an array containing minimum, then maximum elevation entries.
	 */
	public static double[] treeMapExtremes(TreeMap<Double, Vector3d> map) {
		// Find the minimum and maximum altitudes within the given TreeMap.
		double altMin = Double.MAX_VALUE;
		double altMax = -Double.MAX_VALUE;
		for (Double alt : map.keySet()) {
			if (alt < altMin) {
				altMin = alt;
			}
			if (alt > altMax) {
				altMax = alt;
			}
		}
		double[] minMax = { altMin, altMax };
		return minMax;
	}

	public static boolean treeMapsOverlap(TreeMap<Double, Vector3d> mapFirst,
			TreeMap<Double, Vector3d> mapSecond) {
		double[] minMaxFirst = treeMapExtremes(mapFirst);
		double[] minMaxSecond = treeMapExtremes(mapSecond);
		// Check if two maps overlap. They do:
		// If min1 is between min2 and max2
		// or if max1 is between min2 and max2
		// or if min2 is between min1 and max1
		// or if max2 is between min1 and max1
		if ((minMaxFirst[0] > minMaxSecond[0] & minMaxFirst[0] < minMaxSecond[1])
				|| (minMaxFirst[1] < minMaxSecond[1] & minMaxFirst[1] > minMaxSecond[0])
				|| (minMaxSecond[0] > minMaxFirst[0] & minMaxSecond[0] < minMaxFirst[1])
				|| (minMaxSecond[1] < minMaxFirst[1] & minMaxSecond[1] > minMaxFirst[0])) {
			return true;
		} else {
			return false;
		}
	}

}
