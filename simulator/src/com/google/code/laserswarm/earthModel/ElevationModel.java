package com.google.code.laserswarm.earthModel;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.operation.projection.PointOutsideEnvelopeException;
import org.ujmp.core.Matrix;

import com.google.common.base.Preconditions;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class ElevationModel {

	private Matrix				elevationData;
	private GridCoverage2D		coverage;

	private Double				averageHeight;

	private static final Logger	logger	= Logger.get(ElevationModel.class);

	public ElevationModel(Matrix matrix, GridCoverage2D coverage) {
		setElevationData(matrix);
		this.setCoverage(coverage);
	}

	private List<Point3d> coll(Vector3d direction, Point3d origin, double radius) {
		Vector3d o = new Vector3d(origin);
		double A = direction.lengthSquared();
		double B = 2 * direction.dot(o);
		double C = o.lengthSquared() - radius * radius;

		ArrayList<Point3d> points = new ArrayList<Point3d>();

		double t1 = (-B + Math.sqrt(B * B - 4 * C)) / 2;
		double t2 = (-B + Math.sqrt(B * B - 4 * C)) / 2;

		if (!Double.isNaN(t1)) {
			Vector3d d = new Vector3d(direction);
			d.scale(t1);
			o = new Vector3d(origin);
			o.add(d);
			points.add(new Point3d(o));
		}
		if (!Double.isNaN(t2) && t1 != t2) {
			Vector3d d = new Vector3d(direction);
			d.scale(t2);
			o = new Vector3d(origin);
			o.add(d);
			points.add(new Point3d(o));
		}

		return points;
	}

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
	 * @return Point on the 3D surface where the intersection is;
	 * @throws PointOutsideEnvelopeException
	 *             When it does not intersect;
	 */
	public Point3d getIntersecion(Vector3d direction, Point3d origin)
			throws PointOutsideEnvelopeException {
		/* Find the intersecion with the sphere (r = r(EPSG:3785) + average height) */
		double r = 6378137 + getAverageHeight();
		List<Point3d> collPoints = coll(direction, origin, r);

		Point3d p = null;
		switch (collPoints.size()) {
			case 1:
				p = collPoints.iterator().next();
				break;
			case 2:
				double dMax = Double.NEGATIVE_INFINITY;
				for (Point3d point3d : collPoints) {
					Vector3d d = new Vector3d(origin);
					d.sub(point3d);
					if (d.lengthSquared() > dMax) {
						p = point3d;
						dMax = d.lengthSquared();
					}
				}
				break;
			default:
				throw new PointOutsideEnvelopeException(
						"The ray does not intersect the earth. Learn to point your craft man.");
		}

		double rp = new Vector3d(p).length();
		double theta = Math.acos(p.z / rp); // long
		double phi = Math.atan2(p.y, p.x); // lat

		if (coverage.getEnvelope2D().contains(new DirectPosition2D( //
				(180 / Math.PI) * theta, (180 / Math.PI) * phi)))
			return p;
		else
			throw new PointOutsideEnvelopeException("The ray does not intersect the coverage.");
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

		double g = dAngle * 6378137;
		double dx = (z5[0] - z4[0]) / (2 * g);
		double dy = (z2[0] - z7[0]) / (2 * g);

		Vector3d dF_dx = new Vector3d(1, 0, dx);
		Vector3d dF_dy = new Vector3d(0, 1, dy);
		Vector3d n = new Vector3d();
		n.cross(dF_dy, dF_dx);
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
