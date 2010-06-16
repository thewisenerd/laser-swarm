package com.google.code.laserswarm.Desim.brdf;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import javax.vecmath.Point3d;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class DiscreteBRDF {

	/**
	 * List of the coordinates of the brdf in <photons, lon, lat>
	 */
	private Set<Point3d>	knownPoints	= Sets.newHashSet();

	/**
	 * Add a coordinate of the brdf in <photons, lon, lat>
	 * 
	 * @param point
	 */
	public void addPoint(Point3d point) {
		checkNotNull(point);
		knownPoints.add(point);
	}

	/**
	 * Get the coordinates of the points in Array( {lon, lat} )
	 * 
	 * @return
	 */
	public double[][] asArrayPoints() {
		double[][] points = new double[knownPoints.size()][2];
		int i = 0;
		for (Point3d coordinate : knownPoints) {
			points[i][0] = coordinate.y;
			points[i][1] = coordinate.z;
			i++;
		}
		return points;
	}

	/**
	 * Get the brdf value off all the coordinates in asArrayPoints (correct order)
	 * 
	 * @return
	 */
	public double[] asArrayValues() {
		double[] vals = new double[knownPoints.size()];
		int i = 0;
		for (Point3d coordinate : knownPoints) {
			vals[i] = coordinate.x;
			i++;
		}
		return vals;
	}

	public ImmutableSet<Point3d> getPoints() {
		return ImmutableSet.copyOf(knownPoints);
	}

}
