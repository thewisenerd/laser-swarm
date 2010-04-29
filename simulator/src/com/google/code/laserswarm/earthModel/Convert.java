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
	public static Point3d toSphere(Point3d xyz) {
		double r = new Vector3d(xyz).length();
		float theta = (float) Math.atan2(xyz.y, xyz.x); // long
		// float phi = (float) Math.atan2(Math.sqrt(xyz.x * xyz.x + xyz.y * xyz.y), xyz.z);// lat
		float phi = (float) Math.asin((xyz.z) / (r));// lat

		return new Point3d(r, theta, phi);
	}

	/**
	 * Convert (R; theta; phi) or equivalently (R; long; lat) to XYZ
	 * 
	 * @param sphere
	 * @return
	 */
	public static Point3d toXYZ(Point3d sphere) {
		double r = sphere.x;
		double theta = sphere.y;
		double phi = sphere.z;
		double x = r * Math.cos(phi) * Math.cos(theta);
		double y = r * Math.cos(phi) * Math.sin(theta);
		double z = r * Math.sin(phi);
		return new Point3d(x, y, z);
	}

}
