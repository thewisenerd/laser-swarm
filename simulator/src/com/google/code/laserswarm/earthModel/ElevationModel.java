package com.google.code.laserswarm.earthModel;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.operation.projection.PointOutsideEnvelopeException;
import org.opengis.geometry.DirectPosition;
import org.ujmp.core.Matrix;

import com.google.common.base.Preconditions;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class ElevationModel {

	private Matrix				elevationData;
	private GridCoverage2D		coverage;

	private Double				averageHeight;
	public static final double	R0		= 6378137;

	private static final Logger	logger	= Logger.get(ElevationModel.class);

	public ElevationModel(Matrix matrix, GridCoverage2D coverage) {
		setElevationData(matrix);
		this.setCoverage(coverage);
	}

	/**
	 * Compute the intersections of a ray with a sphere with a given radius
	 * 
	 * @param direction
	 *            Direction of the ray
	 * @param origin
	 *            Origin of the ray
	 * @param radius
	 *            Radius of the sphere (center in <0,0,0>)
	 * @return A list of intersection points
	 */
	private List<Point3d> collision(Vector3d direction, Point3d origin, double radius) {
		Vector3d o = new Vector3d(origin);

		direction.normalize();

		double B = 2 * direction.dot(o);
		double C = o.lengthSquared() - (radius * radius);

		/* List of intersection points */
		ArrayList<Point3d> points = new ArrayList<Point3d>();

		/*
		 * Solve char eq {@see
		 * http://www.siggraph.org/education/materials/HyperGraph/raytrace/rtinter1.htm}
		 */
		double t1 = (-B + Math.sqrt(B * B - 4 * C)) / (2);
		double t2 = (-B - Math.sqrt(B * B - 4 * C)) / (2);

		/**
		 * if we have a collision then compute its 3D position on the sphere <br />
		 * !NOT THE DEM!
		 */
		if (!Double.isNaN(t1)) {
			Vector3d d = new Vector3d(direction);
			d.scale(t1);
			o = new Vector3d(origin);
			o.add(d);
			points.add(new Point3d(o));
		}
		/* do the same as above if the 2nd point exists and is unique */
		if (!Double.isNaN(t2) && t1 != t2) {
			Vector3d d = new Vector3d(direction);
			d.scale(t2);
			o = new Vector3d(origin);
			o.add(d);
			points.add(new Point3d(o));
		}

		return points;
	}

	/**
	 * Get the average height of the DEM
	 * 
	 * @return average height above the ellisoid
	 */
	private Double getAverageHeight() {
		if (averageHeight == null) {
			/* Compute the avg height of the coverage */
			averageHeight = elevationData.getAbsoluteValueMean();
		}
		return averageHeight;
	}

	public GridCoverage2D getCoverage() {
		return coverage;
	}

	/**
	 * Get the elevation of a point on the dem wrt R0 (EPSG:3785)
	 * 
	 * @param point
	 *            Point in (lon, lat)
	 * @return
	 */
	public double getElevation(DirectPosition2D point) {
		double[] dest = new double[1];
		dest = getCoverage().evaluate((DirectPosition) point, dest);
		return dest[0];
	}

	public Matrix getElevationData() {
		return elevationData;
	}

	/**
	 * Find the intersection of a ray with the given DEM
	 * 
	 * @param direction
	 *            Direction of the XYZ ray (ECEF)
	 * @param origin
	 *            Origin of the ray (ECEF)
	 * @return Point on the 3D surface where the intersection is; (r, phi, theta)
	 * @throws PointOutsideEnvelopeException
	 *             When it does not intersect;
	 */
	public Point3d getIntersecion(Vector3d direction, Point3d origin)
			throws PointOutsideEnvelopeException {
		/* Find the intersection with the sphere (r = r(EPSG:3785) + average height) */
		double r = R0 + getAverageHeight();
		List<Point3d> collPoints = collision(direction, origin, r);

		/* Based on the number of collisions, find the closes intersection */
		Point3d p = null;
		switch (collPoints.size()) {
			case 1:
				p = collPoints.iterator().next();
				break;
			case 2:
				double dMin = Double.POSITIVE_INFINITY;
				for (Point3d point3d : collPoints) {
					Vector3d d = new Vector3d(origin);
					d.sub(point3d);
					if (d.lengthSquared() < dMin) {
						p = point3d;
						dMin = d.lengthSquared();
					}
				}
				break;
			default:
				throw new PointOutsideEnvelopeException(
						"The ray does not intersect the earth. Learn to point your craft man.");
		}

		/* Find is spherical coordinates */
		double rp = new Vector3d(p).length();
		double theta = Math.acos(p.z / rp); // long
		double phi = Math.atan2(p.y, p.x); // lat

		/* The (lon, lat) coordinate */
		DirectPosition2D dp = new DirectPosition2D((180 / Math.PI) * theta, (180 / Math.PI) * phi);
		/* If it is in the envelope return the 3D surface point */
		// logger.dbg("phi: %s | theta: %s", phi * 180 / Math.PI, theta * 180 / Math.PI);
		if (coverage.getEnvelope2D().contains(dp)) {
			double h = getElevation(dp);
			return new Point3d(R0 + h, phi, theta);
		} else
			throw new PointOutsideEnvelopeException(String.format(
					"The ray does not intersect the coverage. (lat:%s;long:%s)", phi, theta));
	}

	/**
	 * Get the surface normal in the ENU reference system (East, North, Up)
	 * 
	 * @param pos
	 *            Position of the point in (lat, long)
	 * @return The local surface normal in the ENU system
	 */
	public Vector3d getSurfaceNormal(DirectPosition2D pos) {
		// 1 2 3
		// 4 0 5
		// 6 7 8
		// p = (z1 - z6) + (z2 - z7) + (z3 - z8)/6g
		// q = (z3 - z1) + (z5 - z4) + (z8 - z6)/6g
		double dAngle = 0.001;

		float[] z2 = (float[]) coverage.evaluate(new DirectPosition2D(pos.x, pos.y + dAngle));
		float[] z7 = (float[]) coverage.evaluate(new DirectPosition2D(pos.x, pos.y - dAngle));
		float[] z5 = (float[]) coverage.evaluate(new DirectPosition2D(pos.x + dAngle, pos.y));
		float[] z4 = (float[]) coverage.evaluate(new DirectPosition2D(pos.x - dAngle, pos.y));

		double g = dAngle * R0;
		double dx = (z5[0] - z4[0]) / (2 * g); // dz/d(lat)
		double dy = (z2[0] - z7[0]) / (2 * g); // dz/d(lon)

		Vector3d dF_dx = new Vector3d(1, 0, dx);
		Vector3d dF_dy = new Vector3d(0, 1, dy);

		Vector3d n = new Vector3d();
		n.cross(dF_dx, dF_dy);
		// in ENU system (local, lon;lat;up)

		return n;
	}

	public void setCoverage(GridCoverage2D coverage) {
		this.coverage = Preconditions.checkNotNull(coverage);
	}

	public void setElevationData(Matrix elevationData) {
		this.elevationData = Preconditions.checkNotNull(elevationData);
	}

}
