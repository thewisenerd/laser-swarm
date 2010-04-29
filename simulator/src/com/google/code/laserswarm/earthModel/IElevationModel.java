package com.google.code.laserswarm.earthModel;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.operation.projection.PointOutsideEnvelopeException;

public interface IElevationModel {

	/**
	 * Get the elevation of a point on the dem wrt R0 (EPSG:3785)
	 * 
	 * @param point
	 *            Point in (lon, lat)
	 * @return
	 */
	public abstract double getElevation(DirectPosition2D point);

	/**
	 * Find the intersection of a ray with the given DEM
	 * 
	 * @param direction
	 *            Direction of the XYZ ray (ECEF)
	 * @param origin
	 *            Origin of the ray (ECEF)
	 * @return Point on the 3D surface where the intersection is; (r, theta, phi)
	 * @throws PointOutsideEnvelopeException
	 *             When it does not intersect;
	 */
	public abstract Point3d getIntersecion(Vector3d direction, Point3d origin)
			throws PointOutsideEnvelopeException;

	/**
	 * Get the surface normal in the ENU reference system (East, North, Up)
	 * 
	 * @param pos
	 *            Position of the point in (long, lat)
	 * @return The local surface normal in the ENU system
	 */
	public abstract Vector3d getSurfaceNormal(DirectPosition2D pos);

}