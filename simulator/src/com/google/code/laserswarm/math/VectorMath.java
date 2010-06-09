package com.google.code.laserswarm.math;

import jat.matvec.data.RotationMatrix;
import jat.matvec.data.VectorN;

import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;

public abstract class VectorMath {

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

	public static Vector3d enuToLocal(Tuple3d enu, Tuple3d normal) {
		double eastRot = Math.atan2(normal.y, normal.z);
		double northRot = Math.atan2(normal.x, normal.z);

		RotationMatrix rot = new RotationMatrix(1, eastRot, 2, northRot);
		VectorN local = rot.transform(new VectorN(enu.x, enu.y, enu.z));
		return new Vector3d(local.getArray());
	}

	public static Vector3d relative(Tuple3d origin, Tuple3d end) {
		Vector3d dR = new Vector3d(end);
		dR.sub(origin);
		return dR;
	}
}
