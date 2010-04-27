package com.google.code.laserswarm.simulation;

import java.util.HashMap;

import javax.vecmath.Point3d;

import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.earthModel.ScatteringCharacteristics;

public class SimVars {

	public double						t0;			// added
	public double						tR;			// added
	public HashMap<Satellite, Double>	tE;			// added

	public Point3d						p0;			// added
	public Point3d						pR;			// added
	public HashMap<Satellite, Point3d>	pE;			// added

	public double						power0;		// added
	/**
	 * Power reflected
	 */
	public double						powerR;		// added
	/**
	 * Power reflected after scatter
	 */
	public HashMap<Satellite, Double>	powerR_SC;
	public HashMap<Satellite, Double>	powerE;		// added

	public HashMap<Satellite, Double>	photonDensity;	// added
	public HashMap<Satellite, Integer>	photonsE;		// added

	public ScatteringCharacteristics	scatter;

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("Time:\n");
		s.append(String.format("T0:%s\n", t0));
		s.append(String.format("TR:%s\n", tR));
		for (Satellite sat : tE.keySet())
			s.append(String.format("\tTE(%s):%s\n", sat, tR));

		s.append("Points:\n");
		s.append(String.format("p0:%s\n", p0));
		s.append(String.format("pR:%s\n", pR));
		for (Satellite sat : pE.keySet())
			s.append(String.format("\tpE(%s):%s\n", sat, pE));

		s.append("Power:\n");
		s.append(String.format("power0:%s\n", power0));
		s.append(String.format("powerR:%s\n", powerR));
		for (Satellite sat : pE.keySet()) {
			s.append(String.format("\tpowerR_SC(%s):%s\n", sat, powerR_SC));
			s.append(String.format("\tpowerE(%s):%s\n", sat, powerE));
		}

		for (Satellite sat : pE.keySet()) {
			s.append(String.format("\tphotonDensity(%s):%s\n", sat, photonDensity));
			s.append(String.format("\tphotonsE(%s):%s\n", sat, photonsE));
		}

		return s.toString();
	}
}
