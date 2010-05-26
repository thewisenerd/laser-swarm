package com.google.code.laserswarm.math;

import java.util.TreeMap;

import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

public class LookupTable extends TreeMap<Double, Tuple3d> {

	public Tuple3d find(double t) {
		Double tf = floorKey(t);
		Tuple3d vf = get(tf);
		Double tc = ceilingKey(t);
		Tuple3d vc = get(tc);

		double dt = tc - tf;
		if(dt==0) return  vf;
		Vector3d dv = VectorMath.relative(vf, vc);
		dv.scale(1 / dt);

		dv.scale(t - tf);
		dv.add(vf);
		return dv;
	}

}
