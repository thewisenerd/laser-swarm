package com.google.code.laserswarm.math;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

public abstract class Convert {

	/**
	 * Convert from ECEF to ENU
	 * 
	 * @param coord
	 *            in ECEF
	 * @return coordinates in ENU (x east, y north, up z)
	 */

	public static Vector3d toENU(Tuple3d coord) {
		double a = coord.x;
		double b = coord.y;
		double c = coord.z;
		Vector3d vec = new Vector3d(coord);
		Vector3d proj = new Vector3d(a, b, 0.0); // projection ( no z axis)

		double lat = vec.angle(proj);
		double lon = proj.angle(new Vector3d(1, 0, 0)); // xaxis

		double slon = Math.sin(lon);
		double slat = Math.sin(lat);
		double clon = Math.cos(lon);
		double clat = Math.cos(lat);

		double x = -1 * slon * a + clon * b;
		double y = -1 * slat * clon * a - slat * slon * b + clat * c;
		double z = clat * clon * a + clat * slon * b + slat * c;

		return new Vector3d(x, y, z);
	}

	public static Point3d toPoint(Tuple3d point) {
		return new Point3d(point);
	}

	/**
	 * Convert XYZ to (R; theta; phi) or equivalently (R; long; lat)
	 * 
	 * @param xyz
	 * @return
	 */
	public static Point3d toSphere(Tuple3d xyz) {
		double r = new Vector3d(xyz).length();
		float theta = (float) Math.atan2(xyz.y, xyz.x); // long
		// float phi = (float) Math.atan2(Math.sqrt(xyz.x * xyz.x + xyz.y * xyz.y), xyz.z);// lat
		float phi = (float) Math.asin((xyz.z) / (r));// lat

		return new Point3d(r, theta, phi);
	}

	public static Vector3d toVector(Tuple3d point) {
		return new Vector3d(point);
	}

	/**
	 * Convert (R; theta; phi) or equivalently (R; long; lat) to XYZ
	 * 
	 * @param sphere
	 * @return
	 */
	public static Point3d toXYZ(Tuple3d sphere) {
		double r = sphere.x;
		double theta = sphere.y;
		double phi = sphere.z;
		double x = r * Math.cos(phi) * Math.cos(theta);
		double y = r * Math.cos(phi) * Math.sin(theta);
		double z = r * Math.sin(phi);
		return new Point3d(x, y, z);
	}

}
