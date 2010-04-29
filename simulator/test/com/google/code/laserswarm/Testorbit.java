package com.google.code.laserswarm;

import jat.spacetime.Time;

import java.util.List;

import javax.vecmath.Point3d;

import com.google.code.laserswarm.Orbit.OrbitClass;
import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.earthModel.Convert;
import com.google.common.collect.Lists;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class Testorbit {

	private static final double	T0		= 0;
	private static final double	TE		= 1000000;

	private static final Logger	logger	= Logger.get(Testorbit.class);

	public static void main(String[] args) {
		new Testorbit().testOrbit();
	}

	public void testOrbit() {
		Satellite sat = new Satellite("SAT01", (0.08 * 0.08), 6700f, 0f, (float) Math.PI / 2,
				(float) (8.5 * Math.PI / 180), 0f, 0f);

		OrbitClass orb = new OrbitClass(new Time(Configuration.epoch), sat.getKeplerElements());
		orb.propogate(T0);

		List<Point3d> data = Lists.newArrayList();

		double dt = 10;
		double t = T0;
		while (t < TE) {
			Point3d p = orb.ECI_point();
			data.add(Convert.xyz(p));

			t += dt;
			orb.propogate(dt);
		}

		for (Point3d point3d : data) {
			System.out.println(String.format("t: %s\tlon: %s\t lat: %s", //
					point3d.x,//
					point3d.y * (180 / Math.PI), //
					point3d.z * (180 / Math.PI)));
		}
	}
}
