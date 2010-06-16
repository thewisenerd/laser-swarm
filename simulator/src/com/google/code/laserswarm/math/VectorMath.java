package com.google.code.laserswarm.math;

import jat.matvec.data.RotationMatrix;
import jat.matvec.data.VectorN;

import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;

public abstract class VectorMath {

	/**
	 * Returns the average of two vectors
	 * <p>
	 * result = (v1+v2)*0.5
	 * </p>
	 * 
	 * @param v1
	 *            First vector
	 * @param v2
	 *            Second vector
	 * @return The average of vector v1 & v2
	 */
	public static Vector3d avgVector(Tuple3d v1, Tuple3d v2) {
		Vector3d dR = new Vector3d(v1);
		dR.add(v2);
		dR.scale(0.5); // Average position
		return dR;
	}

	/**
	 * Convert a point or vector in the ECEF system to the ENU system
	 * 
	 * @param ecef
	 * @param lambda
	 *            Longitude
	 * @param phi
	 *            Latitude
	 * @return
	 */
	public static Vector3d ecefToEnu(Tuple3d ecef, double lambda, double phi) {
		RealMatrix rotation = new Array2DRowRealMatrix(new double[][] {
				{ -Math.sin(lambda), Math.cos(lambda), 0 },
				{ -Math.sin(phi) * Math.cos(lambda), -Math.sin(phi) * Math.sin(lambda), Math.cos(phi) },
				{ Math.cos(phi) * Math.cos(lambda), Math.cos(phi) * Math.sin(lambda), Math.sin(phi) } },
				true);

		RealMatrix ecefVec = new Array2DRowRealMatrix(new double[][] {
				{ ecef.x }, { ecef.y }, { ecef.z } });
		RealMatrix enuVec = rotation.multiply(ecefVec);

		return new Vector3d(enuVec.getEntry(0, 0), enuVec.getEntry(1, 0), enuVec.getEntry(2, 0));
	}

	/**
	 * Convert a point or vector in the ENU system to the ECEF system
	 * 
	 * @param enu
	 * @param lambda
	 *            Longitude
	 * @param phi
	 *            Latitude
	 * @return
	 */
	public static Vector3d enuToEcef(Tuple3d enu, double lambda, double phi) {
		RealMatrix rotation = new Array2DRowRealMatrix(new double[][] {
				{ -Math.sin(lambda), Math.cos(lambda), 0 },
				{ -Math.sin(phi) * Math.cos(lambda), -Math.sin(phi) * Math.sin(lambda), Math.cos(phi) },
				{ Math.cos(phi) * Math.cos(lambda), Math.cos(phi) * Math.sin(lambda), Math.sin(phi) } },
				true);
		rotation.transpose();

		RealMatrix enuVec = new Array2DRowRealMatrix(new double[][] {
				{ enu.x }, { enu.y }, { enu.z } });
		RealMatrix ecefVec = rotation.multiply(enuVec);

		return new Vector3d(ecefVec.getEntry(0, 0), ecefVec.getEntry(1, 0), ecefVec.getEntry(2, 0));
	}

	/**
	 * 
	 * Transform a vector in the ENU CRS (East, north, up) to the Local CRS (wrt the terrain normal)
	 * 
	 * @param enu
	 *            Original vector in the ENU CRS
	 * @param normal
	 *            Terrain normal
	 * @return The new vector in the local CRS
	 */
	public static Vector3d enuToLocal(Tuple3d enu, Tuple3d normal) {
		double eastRot = Math.atan2(normal.y, normal.z);
		double northRot = Math.atan2(normal.x, normal.z);

		RotationMatrix rot = new RotationMatrix(1, eastRot, 2, northRot);
		VectorN local = rot.transform(new VectorN(enu.x, enu.y, enu.z));
		return new Vector3d(local.getArray());
	}

	/**
	 * Transform a vector in the Local CRS (wrt the terrain normal) to the ENU CRS (East, north, up)
	 * 
	 * @param local
	 *            Vector in the local CRS
	 * @param normal
	 *            Terrain normal
	 * @return The new transformed vector
	 */
	public static Vector3d localToEnu(Tuple3d local, Tuple3d normal) {
		double eastRot = Math.atan2(normal.y, normal.z);
		double northRot = Math.atan2(normal.x, normal.z);

		RotationMatrix rot = new RotationMatrix(1, eastRot, 2, northRot);
		rot.transpose();

		VectorN enu = rot.transform(new VectorN(local.x, local.y, local.z));
		return new Vector3d(enu.getArray());
	}

	/**
	 * Find the vector pointing from one point to another
	 * <p>
	 * result = end - origin
	 * </p>
	 * 
	 * @param origin
	 *            Starting point of the vector
	 * @param end
	 *            End point of the vector
	 * @return Vector pointing from origin to end
	 */
	public static Vector3d relative(Tuple3d origin, Tuple3d end) {
		Vector3d dR = new Vector3d(end);
		dR.sub(origin);
		return dR;
	}

	public static Vector3d rotate(Tuple3d vector, int axis, double rotation) {
		RotationMatrix rot = new RotationMatrix(axis, rotation);
		VectorN rotated = rot.transform(new VectorN(vector.x, vector.y, vector.z));
		return new Vector3d(rotated.getArray());
	}
}
