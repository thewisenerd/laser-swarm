package com.google.code.laserswarm.math;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple2d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import org.geotools.geometry.DirectPosition2D;

public abstract class Convert {

	/**
	 * new DirectPosition2D(point.y * 180 / Math.PI, point.z * 180 / Math.PI);
	 * 
	 * @param point
	 * @return
	 */
	public static DirectPosition2D toDirectPosition(Tuple3d point) {
		return new DirectPosition2D(point.y * 180 / Math.PI, point.z * 180 / Math.PI);
	}

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

	public static Point2d toPoint(Tuple2d point) {
		return new Point2d(point);
	}

	public static Point3d toPoint(Tuple3d point) {
		return new Point3d(point);
	}

	/**
	 * Extract y and z coordinate and put them in a 2d point
	 * 
	 * @return
	 */
	public static Point2d toPoint2d(Tuple3d point) {
		return new Point2d(point.y, point.z);
	}

	/**
	 * Point3d(1, point.x, point.y)
	 * 
	 * @param point
	 * @param x
	 * @return
	 */
	public static Point3d toPoint3d(Tuple2d point) {
		return toPoint3d(point, 1);
	}

	/**
	 * Point3d(x, point.x, point.y)
	 * 
	 * @param point
	 * @param x
	 * @return
	 */
	public static Point3d toPoint3d(Tuple2d point, double x) {
		return new Point3d(x, point.x, point.y);
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

	public static Vector2d toVector(Tuple2d point) {
		return new Vector2d(point);
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
