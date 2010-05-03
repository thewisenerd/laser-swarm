package com.google.code.laserswarm.math;

import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

public abstract class VectorMath {

	public static Vector3d avgVector(Tuple3d v1, Tuple3d v2) {
		Vector3d dR = new Vector3d(v1);
		dR.add(v2);
		dR.scale(0.5); // Average position
		return dR;
	}

	public static Vector3d relative(Tuple3d origin, Tuple3d end) {
		Vector3d dR = new Vector3d(end);
		dR.sub(origin);
		return dR;
	}

}
