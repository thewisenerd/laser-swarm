package com.google.code.laserswarm.simulation;

import java.util.HashMap;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.earthModel.ScatteringCharacteristics;

public class SimVars {

	/**
	 * Time of origin.
	 */
	public double						t0;			// added
	/**
	 * Time from origin to reflection.
	 */
	public double						tR;			// added
	/**
	 * Time from reflection to endpoint.
	 */
	public HashMap<Satellite, Double>	tE;			// added

	/**
	 * Point of origin.
	 */
	public Point3d						p0;			// added
	/**
	 * Point of reflection.
	 */
	public Point3d						pR;			// added
	/**
	 * Endpoints.
	 */
	public HashMap<Satellite, Point3d>	pE;			// added

	/**
	 * Original power.
	 */
	public double						power0;		// added
	/**
	 * Power reflected.
	 */
	public double						powerR;		// added
	/**
	 * Power reflected after scatter.
	 */
	public HashMap<Satellite, Double>	powerR_SC;
	/**
	 * Power reaching endpoints.
	 */
	public HashMap<Satellite, Double>	powerE;		// added

	/**
	 * Photons per square meter.
	 */
	public HashMap<Satellite, Double>	photonDensity;	// added
	/**
	 * Photons received at the endpoint.
	 */
	public HashMap<Satellite, Integer>	photonsE;		// added
	/**
	 * Scattering characteristics for this footprint.
	 */
	public ScatteringCharacteristics	scatter;

	/**
	 * Is the footprint illuminated by the sun ?
	 */
	public boolean						illuminated;
	/**
	 * If illuminated by the sun, this is the incidence angle of the sun
	 */
	public Vector3d						sunVector;

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
