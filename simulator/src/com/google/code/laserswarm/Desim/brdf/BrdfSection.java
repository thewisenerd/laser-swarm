package com.google.code.laserswarm.Desim.brdf;

import static com.google.code.laserswarm.math.Convert.toPoint2d;

import java.util.Set;

import javax.vecmath.Point3d;
import javax.vecmath.Point4d;
import javax.vecmath.Vector2d;

import com.google.common.collect.Sets;

public class BrdfSection {

	private static Point4d calcRange(Point3d... vectors) {
		double lonMin = Double.POSITIVE_INFINITY;
		double lonMax = Double.NEGATIVE_INFINITY;
		double latMin = Double.POSITIVE_INFINITY;
		double latMax = Double.NEGATIVE_INFINITY;

		for (Point3d vector : vectors) {
			if (vector.y < lonMin)
				lonMin = vector.y;
			if (vector.y > lonMax)
				lonMax = vector.y;
			if (vector.z < latMin)
				latMin = vector.z;
			if (vector.z > latMax)
				latMax = vector.z;
		}

		return new Point4d(lonMin, lonMax, latMin, latMax);
	}

	private Point4d			range;
	private Set<Point3d>	vectors	= Sets.newHashSet();

	public BrdfSection(Point3d... vectors) {
		for (Point3d point3d : vectors)
			this.vectors.add(point3d);
	}

	public void add(Point3d vector) {
		vectors.add(vector);
		range = null;
	}

	private void calcRange() {
		range = calcRange(vectors.toArray(new Point3d[] {}));
	}

	public Vector2d getCenter() {
		Vector2d cg = new Vector2d();
		for (Point3d vec : vectors)
			cg.add(toPoint2d(vec));

		cg.scale(1 / vectors.size());
		return cg;
	}

	public int getPhotonCount() {
		int count = 0;
		for (Point3d vec : vectors) {
			count += vec.x;
		}
		return count;
	}

	/**
	 * Test if the vector is in the bounds (inclusive)
	 * 
	 * @param lon
	 * @param lat
	 * @return
	 */
	public boolean inRange(double lon, double lat) {
		if (range == null)
			calcRange();
		return ((range.x <= lon && lon <= range.y) && (range.z <= lat && lat <= range.w));
	}

}
