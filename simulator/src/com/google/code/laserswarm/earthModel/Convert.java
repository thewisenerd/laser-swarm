package com.google.code.laserswarm.earthModel;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public abstract class Convert {

	/**
	 * Convert XYZ to (R; theta; phi) or equivalently (R; long; lat)
	 * 
	 * @param xyz
	 * @return
	 */
	public static Point3d sphere(Point3d xyz) {
		double r = new Vector3d(xyz).length();
		float theta = (float) Math.atan2(xyz.y, xyz.x); // long
		float phi = (float) Math.asin((xyz.z) / (r));// lat

		return new Point3d(r, theta, phi);
	}

	/**
	 * Convert (R; theta; phi) or equivalently (R; long; lat) to XYZ
	 * 
	 * @param sphere
	 * @return
	 */
	public static Point3d xyz(Point3d sphere) {
		double r = sphere.x;
		double theta = sphere.y;
		double phi = sphere.z;
		double x = r * Math.sin(theta) * Math.cos(phi);
		double y = r * Math.sin(theta) * Math.sin(phi);
		double z = r * Math.cos(theta);
		return new Point3d(x, y, z);
	}

}
