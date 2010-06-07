package com.google.code.laserswarm.math;

import java.util.TreeMap;

import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

import com.lyndir.lhunath.lib.system.logging.Logger;

public class LookupTable extends TreeMap<Double, Tuple3d> {

	private static final Logger	logger	= Logger.get(LookupTable.class);

	public Tuple3d find(double t) {
		Double tf = floorKey(t);
		Double tc = ceilingKey(t);
		Tuple3d vf = null;
		Tuple3d vc = null;

		/* Scan for out of bounds case */
		/* Then assume constant */
		if (tf != null)
			vf = get(tf);

		if (tc != null)
			vc = get(tc);
		else {
			vc = get(tf);
			tc = tf;
		}

		if (tf == null) {
			vf = vc;
			tf = tc;
		}

		double dt = tc - tf;
		if (dt == 0)
			return vf;
		Vector3d dv = VectorMath.relative(vf, vc);
		dv.scale(1 / dt);

		dv.scale(t - tf);
		dv.add(vf);
		return dv;
	}
}
