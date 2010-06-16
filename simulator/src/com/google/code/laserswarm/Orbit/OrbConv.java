/**
 * 
 */
package com.google.code.laserswarm.Orbit;

import jat.matvec.data.Matrix;
import jat.matvec.data.VectorN;
import jat.spacetime.EarthRef;
import jat.spacetime.Time;

import javax.vecmath.Vector3d;

import com.google.code.laserswarm.conf.Configuration;

/**
 * @author Administrator
 * 
 */
public class OrbConv {

	private EarthRef	refFrame;
	private Time		curTime;

	public OrbConv() {
		curTime = new Time(Configuration.epoch);
		refFrame = new EarthRef(curTime);
	}

	public Vector3d ECEF_vec(double t_cur, Vector3d eciVec) {
		curTime.updateTo(Configuration.epoch.mjd());
		curTime.plus(t_cur);

		Matrix m = refFrame.eci2ecef(curTime).times(
				new Matrix(new VectorN(eciVec.x, eciVec.y, eciVec.z)));
		return new Vector3d(m.get(0, 0), m.get(1, 0), m.get(2, 0));
	}

}
