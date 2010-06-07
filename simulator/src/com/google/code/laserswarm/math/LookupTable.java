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

		try {
			vf = get(tf);
		} catch (NullPointerException e) {
			logger.wrn("could not retrieve floor value in the Lookup table (border value " + t + ")");
		}

		try {
			vc = get(tc);
		} catch (NullPointerException e) {
			logger.wrn("could not retrieve ceil value in the Lookup table (border value" + t + ")");
			if (vf != null)
				vc = (Tuple3d) vf.clone();
			else
				throw new RuntimeException("Cannot find the correct value for the ceil value");
		}

		if (vf == null)
			vf = (Tuple3d) vc.clone();
		else
			throw new RuntimeException("Cannot find the correct value for the floor value");

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
